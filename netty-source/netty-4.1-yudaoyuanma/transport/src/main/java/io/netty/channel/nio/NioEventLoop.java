/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopException;
import io.netty.channel.SelectStrategy;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.IntSupplier;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nio EventLoop 实现类，实现对注册到其中的 Channel 的就绪的 IO 事件，和对用户提交的任务进行处理
 *
 * {@link SingleThreadEventLoop} implementation which register the {@link Channel}'s to a
 * {@link Selector} and so does the multi-plexing of these in the event loop.
 */
public final class NioEventLoop extends SingleThreadEventLoop {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioEventLoop.class);

    /**
     * TODO 1007 NioEventLoop cancel
     * <p> // XXX Hard-coded value, but won't need customization.
     * <p> //     硬编码的值，但不需要自定义。
     */
    private static final int CLEANUP_INTERVAL = 256; 

    /**
     * 是否禁用 SelectionKey 的优化，默认开启
     */
    private static final boolean DISABLE_KEYSET_OPTIMIZATION = SystemPropertyUtil.getBoolean("io.netty.noKeySetOptimization", false);

    /**
     * 少于该 N 值，不开启空轮询重建新的 Selector 对象的功能
     */
    private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
    /**
     * NIO Selector 空轮询该 N 次后，重建新的 Selector 对象
     */
    private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;

    private final IntSupplier selectNowSupplier = new IntSupplier() {
        @Override
        public int get() throws Exception {
            return selectNow();
        }
    };

    private final Callable<Integer> pendingTasksCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return NioEventLoop.super.pendingTasks();
        }
    };

    // Workaround for JDK NIO bug.
    //
    // See:
    // - http://bugs.sun.com/view_bug.do?bug_id=6427854
    // - https://github.com/netty/netty/issues/203
    static {
        // 解决 Selector#open() 方法
        final String key = "sun.nio.ch.bugLevel";
        final String buglevel = SystemPropertyUtil.get(key);
        if (buglevel == null) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        System.setProperty(key, "");
                        return null;
                    }
                });
            } catch (final SecurityException e) {
                logger.debug("Unable to get/set System Property: " + key, e);
            }
        }

        // 初始化
        int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("io.netty.selectorAutoRebuildThreshold", 512);
        if (selectorAutoRebuildThreshold < MIN_PREMATURE_SELECTOR_RETURNS) {
            selectorAutoRebuildThreshold = 0;
        }
        SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.noKeySetOptimization: {}", DISABLE_KEYSET_OPTIMIZATION);
            logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", SELECTOR_AUTO_REBUILD_THRESHOLD);
        }
    }

    /**
     * The NIO {@link Selector}.
     *
     * 包装的 Selector 对象，经过优化
     *
     * {@link #openSelector()}
     */
    private Selector selector;
    /**
     * 未包装的 Selector 对象
     */
    private Selector unwrappedSelector;
    /**
     * 注册的 SelectionKey 集合。Netty 自己实现，经过优化。
     */
    private SelectedSelectionKeySet selectedKeys;
    /**
     * SelectorProvider 对象，用于创建 Selector 对象
     */
    private final SelectorProvider provider;

    /**
     * Boolean that controls determines if a blocked Selector.select should
     * break out of its selection process. In our case we use a timeout for
     * the select method and the select method will block for that time unless
     * waken up.
     * 
     * <p> 控制该值的布尔值，确定被阻塞的Selector.select是否应退出其选择过程。 
     * 在我们的例子中，我们对select方法使用了超时，除非唤醒，否则select方法将在该时间段内阻塞。
     *
     * <p> 唤醒标记。因为唤醒方法 {@link Selector#wakeup()} 开销比较大，通过该标识，减少调用。
     *
     * @see #wakeup(boolean)
     * @see #run()
     */
    private final AtomicBoolean wakenUp = new AtomicBoolean();
    /**
     * Select 策略
     *
     * @see #select(boolean)
     */
    private final SelectStrategy selectStrategy;
    /**
     * 处理 Channel 的就绪的 IO 事件，占处理任务的总时间的比例
     */
    private volatile int ioRatio = 50;
    /**
     * 取消 SelectionKey 的数量
     *
     * TODO 1007 NioEventLoop cancel
     */
    private int cancelledKeys;
    /**
     * 是否需要再次 select Selector 对象
     *
     * TODO 1007 NioEventLoop cancel
     */
    private boolean needsToSelectAgain;

    NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
                 SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
        super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
        if (selectorProvider == null) {
            throw new NullPointerException("selectorProvider");
        }
        if (strategy == null) {
            throw new NullPointerException("selectStrategy");
        }
        provider = selectorProvider;
        // 创建 Selector 对象
        final SelectorTuple selectorTuple = openSelector();
        selector = selectorTuple.selector;
        unwrappedSelector = selectorTuple.unwrappedSelector;
        selectStrategy = strategy;
    }

    /**
     * Selector 元组
     */
    private static final class SelectorTuple {

        /**
         * 未包装的 Selector 对象
         */
        final Selector unwrappedSelector;
        /**
         * 未包装的 Selector 对象
         */
        final Selector selector;

        SelectorTuple(Selector unwrappedSelector) {
            this.unwrappedSelector = unwrappedSelector;
            this.selector = unwrappedSelector;
        }

        SelectorTuple(Selector unwrappedSelector, Selector selector) {
            this.unwrappedSelector = unwrappedSelector;
            this.selector = selector;
        }

    }

    private SelectorTuple openSelector() {
        // 创建 Selector 对象，作为 unwrappedSelector
        final Selector unwrappedSelector;
        try {
            unwrappedSelector = provider.openSelector();
        } catch (IOException e) {
            throw new ChannelException("failed to open a new selector", e);
        }

        // 禁用 SelectionKey 的优化，则直接返回 SelectorTuple 对象。即，selector 也使用 unwrappedSelector 。
        if (DISABLE_KEYSET_OPTIMIZATION) {
            return new SelectorTuple(unwrappedSelector);
        }

        // 获得 SelectorImpl 类
        Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    return Class.forName(
                            "sun.nio.ch.SelectorImpl",
                            false,
                            PlatformDependent.getSystemClassLoader()); // 成功，则返回该类
                } catch (Throwable cause) {
                    return cause; // 失败，则返回该异常
                }
            }
        });

        // 获得 SelectorImpl 类失败，则直接返回 SelectorTuple 对象。即，selector 也使用 unwrappedSelector 。
        if (!(maybeSelectorImplClass instanceof Class) ||
                // ensure the current selector implementation is what we can instrument.
        		// 确保当前的选择器实现是我们可以检测的。
                !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
            if (maybeSelectorImplClass instanceof Throwable) {
                Throwable t = (Throwable) maybeSelectorImplClass;
                logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
            }
            return new SelectorTuple(unwrappedSelector);
        }

        final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;

        // 创建 SelectedSelectionKeySet 对象
        final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

        // 设置 SelectedSelectionKeySet 对象到 unwrappedSelector 中
        Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    // 获得 "selectedKeys" "publicSelectedKeys" 的 Field
                    Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                    Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

                    // 设置 Field 可访问
                    Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
                    if (cause != null) {
                        return cause;
                    }
                    cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
                    if (cause != null) {
                        return cause;
                    }

                    // 设置 SelectedSelectionKeySet 对象到 unwrappedSelector 的 Field 中
                    selectedKeysField.set(unwrappedSelector, selectedKeySet);
                    publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
                    return null;
                } catch (NoSuchFieldException e) {
                    return e; // 失败，则返回该异常
                } catch (IllegalAccessException e) {
                    return e; // 失败，则返回该异常
                }
            }
        });

        // 设置 SelectedSelectionKeySet 对象到 unwrappedSelector 中失败，则直接返回 SelectorTuple 对象。即，selector 也使用 unwrappedSelector 。
        if (maybeException instanceof Exception) {
            selectedKeys = null;
            Exception e = (Exception) maybeException;
            logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
            return new SelectorTuple(unwrappedSelector);
        }

        // 设置 SelectedSelectionKeySet 对象到 selectedKeys 中
        selectedKeys = selectedKeySet;
        logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);

        // 创建 SelectedSelectionKeySetSelector 对象
        // 创建 SelectorTuple 对象。即，selector 也使用 SelectedSelectionKeySetSelector 对象。
        return new SelectorTuple(unwrappedSelector, new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
    }

    /**
     * Returns the {@link SelectorProvider} used by this {@link NioEventLoop} to obtain the {@link Selector}.
     */
    public SelectorProvider selectorProvider() {
        return provider;
    }

    @Override
    protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        // This event loop never calls takeTask()
        return maxPendingTasks == Integer.MAX_VALUE ? PlatformDependent.<Runnable>newMpscQueue()
                                                    : PlatformDependent.<Runnable>newMpscQueue(maxPendingTasks);
    }

    @Override
    public int pendingTasks() {
        // As we use a MpscQueue we need to ensure pendingTasks() is only executed from within the EventLoop as
        // otherwise we may see unexpected behavior (as size() is only allowed to be called by a single consumer).
    	// 当我们使用MpscQueue时，我们需要确保仅从EventLoop内部执行未完成的Tasks（），否则我们可能会看到意外的行为（因为size（）仅允许单个使用者调用）。
        // See https://github.com/netty/netty/issues/5297
        if (inEventLoop()) {
            return super.pendingTasks();
        } else {
            return submit(pendingTasksCallable).syncUninterruptibly().getNow();
        }
    }

    /**
     * Registers an arbitrary {@link SelectableChannel}, not necessarily created by Netty, to the {@link Selector}
     * of this event loop.  Once the specified {@link SelectableChannel} is registered, the specified {@code task} will
     * be executed by this event loop when the {@link SelectableChannel} is ready.
     * 
     * <p> 向该事件循环的选择器注册一个任意的SelectableChannel，不一定由Netty创建。 一旦注册了指定的SelectableChannel，
     * 当SelectableChannel准备就绪时，此事件循环将执行指定的任务。
     */
    public void register(final SelectableChannel ch, final int interestOps, final NioTask<?> task) {
        if (ch == null) {
            throw new NullPointerException("ch");
        }
        if (interestOps == 0) {
            throw new IllegalArgumentException("interestOps must be non-zero.");
        }
        if ((interestOps & ~ch.validOps()) != 0) {
            throw new IllegalArgumentException(
                    "invalid interestOps: " + interestOps + "(validOps: " + ch.validOps() + ')');
        }
        if (task == null) {
            throw new NullPointerException("task");
        }

        if (isShutdown()) {
            throw new IllegalStateException("event loop shut down");
        }

        try {
            ch.register(selector, interestOps, task);
        } catch (Exception e) {
            throw new EventLoopException("failed to register a channel", e);
        }
    }

    /**
     * Returns the percentage of the desired amount of time spent for I/O in the event loop.
     * 
     * <p> 返回事件循环中I / O花费的期望时间量的百分比。
     */
    public int getIoRatio() {
        return ioRatio;
    }

    /**
     * Sets the percentage of the desired amount of time spent for I/O in the event loop.  The default value is
     * {@code 50}, which means the event loop will try to spend the same amount of time for I/O as for non-I/O tasks.
     * 
     * <p> 设置事件循环中I / O花费的所需时间量的百分比。 默认值为50，这意味着事件循环将尝试花费与非I / O任务相同的时间来进行I / O。
     */
    public void setIoRatio(int ioRatio) {
        if (ioRatio <= 0 || ioRatio > 100) {
            throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
        }
        this.ioRatio = ioRatio;
    }

    /**
     * Replaces the current {@link Selector} of this event loop with newly created {@link Selector}s to work
     * around the infamous epoll 100% CPU bug.
     * 
     * <p> 用新创建的选择器替换此事件循环的当前选择器，以解决臭名昭著的epoll 100％CPU错误。
     */
    public void rebuildSelector() {
        // 只允许在 EventLoop 的线程中执行
        if (!inEventLoop()) {
            execute(new Runnable() {
                @Override
                public void run() {
                    rebuildSelector0();
                }
            });
            return;
        }
        rebuildSelector0();
    }

    private void rebuildSelector0() {
        final Selector oldSelector = selector;
        if (oldSelector == null) {
            return;
        }

        // 创建新的 Selector 对象
        final SelectorTuple newSelectorTuple;
        try {
            newSelectorTuple = openSelector();
        } catch (Exception e) {
            logger.warn("Failed to create a new Selector.", e);
            return;
        }

        // Register all channels to the new Selector.
        // 将注册在 NioEventLoop 上的所有 Channel ，注册到新创建 Selector 对象上
        int nChannels = 0; // 计算重新注册成功的 Channel 数量
        for (SelectionKey key: oldSelector.keys()) {
            Object a = key.attachment();
            try {
                if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null) {
                    continue;
                }

                int interestOps = key.interestOps();
                // 取消老的 SelectionKey
                key.cancel();
                // 将 Channel 注册到新的 Selector 对象上
                SelectionKey newKey = key.channel().register(newSelectorTuple.unwrappedSelector, interestOps, a);
                // 修改 Channel 的 selectionKey 指向新的 SelectionKey 上
                if (a instanceof AbstractNioChannel) {
                    // Update SelectionKey
                    ((AbstractNioChannel) a).selectionKey = newKey;
                }

                // 计数 ++
                nChannels ++;
            } catch (Exception e) {
                logger.warn("Failed to re-register a Channel to the new Selector.", e);
                // 关闭发生异常的 Channel
                if (a instanceof AbstractNioChannel) {
                    AbstractNioChannel ch = (AbstractNioChannel) a;
                    ch.unsafe().close(ch.unsafe().voidPromise());
                // 调用 NioTask 的取消注册事件
                } else {
                    @SuppressWarnings("unchecked")
                    NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                    invokeChannelUnregistered(task, key, e);
                }
            }
        }

        // 修改 selector 和 unwrappedSelector 指向新的 Selector 对象
        selector = newSelectorTuple.selector;
        unwrappedSelector = newSelectorTuple.unwrappedSelector;

        // 关闭老的 Selector 对象
        try {
            // time to close the old selector as everything else is registered to the new one
            oldSelector.close();
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close the old Selector.", t);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
        }
    }

    /**
     * NioEventLoop开启的单线程将会在此方法中一直做死循环,执行添加到队列的每一个任务
     */
    @Override
    protected void run() {
        for (;;) {
            try {
                switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
                    case SelectStrategy.CONTINUE: // 默认实现下，不存在这个情况。
                        continue;
                    case SelectStrategy.SELECT:
                        // 重置 wakenUp 标记为 false
                        // 选择( 查询 )任务
                        select(wakenUp.getAndSet(false));

                        // 'wakenUp.compareAndSet(false, true)' is always evaluated
                        // before calling 'selector.wakeup()' to reduce the wake-up
                        // overhead. (Selector.wakeup() is an expensive operation.)
                        //
                        // 始终在调用“ selector.wakeup（）”之前评估“ wakenUp.compareAndSet（false，true）”，
                        // 以减少唤醒开销。 （Selector.wakeup（）是一个昂贵的操作。）
                        //
                        // However, there is a race condition in this approach.
                        // The race condition is triggered when 'wakenUp' is set to
                        // true too early.
                        //
                        // 但是，这种方法存在竞争条件。 当'wakenUp'设置为true太早时，会触发竞争条件。
                        //
                        // 'wakenUp' is set to true too early if:
                        // 1) Selector is waken up between 'wakenUp.set(false)' and
                        //    'selector.select(...)'. (BAD)
                        // 2) Selector is waken up between 'selector.select(...)' and
                        //    'if (wakenUp.get()) { ... }'. (OK)
                        // 
                        // 如果出现以下情况，将'wake Up'设置为true为时过早：
                        //
                        // 1）在“ wakenUp.set（false）”和“ selector.select（...）”之间唤醒选择器。 （坏）
                        // 2）选择器在“ selector.select（...）”和“ if（wakenUp.get（））{...}”之间唤醒。 （好）
                        //
                        // In the first case, 'wakenUp' is set to true and the
                        // following 'selector.select(...)' will wake up immediately.
                        // Until 'wakenUp' is set to false again in the next round,
                        // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                        // any attempt to wake up the Selector will fail, too, causing
                        // the following 'selector.select(...)' call to block
                        // unnecessarily.
                        //
                        // 在第一种情况下，“ wakenUp”设置为true，随后的“ selector.select（...）”将立即唤醒。 
                        // 在下一轮“ wakenUp”再次设置为false之前，“ wakenUp.compareAndSet（false，true）”将失败，
                        // 因此，任何唤醒选择器的尝试也将失败，从而导致以下“ selector.select（。 ..）'呼叫以进行不必要的封锁。
                        //
                        // To fix this problem, we wake up the selector again if wakenUp
                        // is true immediately after selector.select(...).
                        // It is inefficient in that it wakes up the selector for both
                        // the first case (BAD - wake-up required) and the second case
                        // (OK - no wake-up required).
                        // 
                        // 要解决此问题，如果在selector.select（...）之后wakenUp为true，则再次唤醒选择器。 
                        // 它的效率低下，因为它会唤醒第一种情况（BAD-需要唤醒）和第二种情况（OK-无需唤醒）的选择器。
                        

                        // 唤醒。原因，见上面中文注释
                        if (wakenUp.get()) {
                            selector.wakeup();
                        }
                        // fall through
                    default:
                }

                // TODO 1007 NioEventLoop cancel 方法
                cancelledKeys = 0;
                needsToSelectAgain = false;

                final int ioRatio = this.ioRatio;
                if (ioRatio == 100) {
                    try {
                        // 处理 Channel 感兴趣的就绪 IO 事件
                        processSelectedKeys();
                    } finally {
                        // 运行所有普通任务和定时任务，不限制时间
                        // Ensure we always run tasks.
                        runAllTasks();
                    }
                } else {
                    final long ioStartTime = System.nanoTime();
                    try {
                        // 处理 Channel 感兴趣的就绪 IO 事件
                        processSelectedKeys();
                    } finally {
                        // 运行所有普通任务和定时任务，限制时间
                        // Ensure we always run tasks.
                        final long ioTime = System.nanoTime() - ioStartTime;
                        runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                }
            } catch (Throwable t) {
                handleLoopException(t);
            }
            // TODO 1006 EventLoop 优雅关闭
            // Always handle shutdown even if the loop processing threw an exception.
            try {
                if (isShuttingDown()) {
                    closeAll();
                    if (confirmShutdown()) {
                        return;
                    }
                }
            } catch (Throwable t) {
                handleLoopException(t);
            }
        }
    }

    private static void handleLoopException(Throwable t) {
        logger.warn("Unexpected exception in the selector loop.", t);

        // Prevent possible consecutive immediate failures that lead to
        // excessive CPU consumption.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    private void processSelectedKeys() {
        if (selectedKeys != null) {
            processSelectedKeysOptimized();
        } else {
            processSelectedKeysPlain(selector.selectedKeys());
        }
    }

    @Override
    protected void cleanup() {
        try {
            selector.close();
        } catch (IOException e) {
            logger.warn("Failed to close a selector.", e);
        }
    }

    void cancel(SelectionKey key) {
        key.cancel();
        cancelledKeys ++;
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            needsToSelectAgain = true;
        }
    }

    @Override
    protected Runnable pollTask() {
        Runnable task = super.pollTask();
        if (needsToSelectAgain) {
            selectAgain();
        }
        return task;
    }

    private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys) {
        // check if the set is empty and if so just return to not create garbage by
        // creating a new Iterator every time even if there is nothing to process.
        // See https://github.com/netty/netty/issues/597
        if (selectedKeys.isEmpty()) {
            return;
        }

        // 遍历 SelectionKey 迭代器
        Iterator<SelectionKey> i = selectedKeys.iterator();
        for (;;) {
            // 获得 SelectionKey 对象
            final SelectionKey k = i.next();
            // 从迭代器中移除
            i.remove();

            final Object a = k.attachment();
            // 处理一个 Channel 就绪的 IO 事件
            if (a instanceof AbstractNioChannel) {
                processSelectedKey(k, (AbstractNioChannel) a);
            // 使用 NioTask 处理一个 Channel 就绪的 IO 事件
            } else {
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                processSelectedKey(k, task);
            }

            // 无下一个节点，结束
            if (!i.hasNext()) {
                break;
            }

            // TODO 1007 NioEventLoop cancel 方法
            if (needsToSelectAgain) {
                selectAgain();
                selectedKeys = selector.selectedKeys();

                // Create the iterator again to avoid ConcurrentModificationException
                if (selectedKeys.isEmpty()) {
                    break;
                } else {
                    i = selectedKeys.iterator();
                }
            }
        }
    }

    private void processSelectedKeysOptimized() {
        // 遍历数组
        for (int i = 0; i < selectedKeys.size; ++i) {
            final SelectionKey k = selectedKeys.keys[i];
            // null out entry in the array to allow to have it GC'ed once the Channel close
            // 数组中的空输出条目允许通道关闭后对其进行GC处理
            
            // See https://github.com/netty/netty/issues/2363
            selectedKeys.keys[i] = null;

            final Object a = k.attachment();

            // 处理一个 Channel 就绪的 IO 事件
            if (a instanceof AbstractNioChannel) {
                processSelectedKey(k, (AbstractNioChannel) a);
            // 使用 NioTask 处理一个 Channel 就绪的 IO 事件
            } else {
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                processSelectedKey(k, task);
            }

            // TODO 1007 NioEventLoop cancel 方法
            if (needsToSelectAgain) {
                // null out entries in the array to allow to have it GC'ed once the Channel close
                // See https://github.com/netty/netty/issues/2363
                selectedKeys.reset(i + 1);

                selectAgain();
                i = -1;
            }
        }
    }

    private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
        // 如果 SelectionKey 是不合法的，则关闭 Channel
        final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
        if (!k.isValid()) {
            final EventLoop eventLoop;
            try {
                eventLoop = ch.eventLoop();
            } catch (Throwable ignored) {
                // If the channel implementation throws an exception because there is no event loop, we ignore this
                // because we are only trying to determine if ch is registered to this event loop and thus has authority
                // to close ch.
            	
            	// 如果通道实现由于没有事件循环而引发异常，则我们将其忽略，因为我们仅试图确定ch是否已注册到该事件循环，因此有权关闭ch。
                return;
            }
            // Only close ch if ch is still registered to this EventLoop. ch could have deregistered from the event loop
            // and thus the SelectionKey could be cancelled as part of the deregistration process, but the channel is
            // still healthy and should not be closed.
            
            // 如果ch仍注册到此EventLoop，则仅关闭ch。 ch可能已经从事件循环中注销，因此可以作为注销过程的一部分取消SelectionKey，
            // 但该通道仍处于正常状态，因此不应关闭。
            
            // See https://github.com/netty/netty/issues/5125
            if (eventLoop != this) {
                return;
            }
            // close the channel if the key is not valid anymore
            // 如果密钥不再有效，则关闭通道
            unsafe.close(unsafe.voidPromise());
            return;
        }

        try {
            // 获得就绪的 IO 事件的 ops
            int readyOps = k.readyOps();

            // OP_CONNECT 事件就绪
            // We first need to call finishConnect() before try to trigger a read(...) or write(...) as otherwise
            // the NIO JDK channel implementation may throw a NotYetConnectedException.
            
            // 在尝试触发read（...）或write（...）之前，我们首先需要调用finishConnect（），
            // 否则NIO JDK通道实现可能会引发NotYetConnectedException。
            if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                // 移除对 OP_CONNECT 感兴趣
                // remove OP_CONNECT as otherwise Selector.select(..) will always return without blocking
            	// 删除OP_CONNECT，否则Selector.select（..）将始终返回而不会阻塞
            	
                // See https://github.com/netty/netty/issues/924
                int ops = k.interestOps();
                ops &= ~SelectionKey.OP_CONNECT;
                k.interestOps(ops);
                // 完成连接
                unsafe.finishConnect();
            }

            // OP_WRITE 事件就绪
            // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
            if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                // Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
                // 向 Channel 写入数据
                ch.unsafe().forceFlush();
            }

            // SelectionKey.OP_READ 或 SelectionKey.OP_ACCEPT 就绪
            // readyOps == 0 是对 JDK Bug 的处理，防止空的死循环
            // Also check for readOps of 0 to workaround possible JDK bug which may otherwise lead
            // to a spin loop
            
            //新连接的已准备接入或者已存在的连接有数据可读
            if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
                unsafe.read();
            }
        } catch (CancelledKeyException ignored) {
            // 发生异常，关闭 Channel
            unsafe.close(unsafe.voidPromise());
        }
    }

    private static void processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task) {
        int state = 0; // 未执行
        try {
            // 调用 NioTask 的 Channel 就绪事件
            task.channelReady(k.channel(), k);
            state = 1; // 执行成功
        } catch (Exception e) {
            // SelectionKey 取消
            k.cancel();
            // 执行 Channel 取消注册
            invokeChannelUnregistered(task, k, e);
            state = 2; // 执行异常
        } finally {
            switch (state) {
            case 0:
                // SelectionKey 取消
                k.cancel();
                // 执行 Channel 取消注册
                invokeChannelUnregistered(task, k, null);
                break;
            case 1:
                // SelectionKey 不合法，则执行 Channel 取消注册
                if (!k.isValid()) { // Cancelled by channelReady()
                    invokeChannelUnregistered(task, k, null);
                }
                break;
            }
        }
    }

    // TODO 芋艿 TODO 1006 EventLoop 优雅关闭
    private void closeAll() {
        selectAgain();
        Set<SelectionKey> keys = selector.keys();
        Collection<AbstractNioChannel> channels = new ArrayList<AbstractNioChannel>(keys.size());
        for (SelectionKey k: keys) {
            Object a = k.attachment();
            if (a instanceof AbstractNioChannel) {
                channels.add((AbstractNioChannel) a);
            } else {
                k.cancel();
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                invokeChannelUnregistered(task, k, null);
            }
        }

        for (AbstractNioChannel ch: channels) {
            ch.unsafe().close(ch.unsafe().voidPromise());
        }
    }

    private static void invokeChannelUnregistered(NioTask<SelectableChannel> task, SelectionKey k, Throwable cause) {
        try {
            task.channelUnregistered(k.channel(), cause);
        } catch (Exception e) {
            logger.warn("Unexpected exception while running NioTask.channelUnregistered()", e);
        }
    }

    @Override
    protected void wakeup(boolean inEventLoop) {
        if (!inEventLoop && wakenUp.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    Selector unwrappedSelector() {
        return unwrappedSelector;
    }

    int selectNow() throws IOException {
        try {
            return selector.selectNow();
        } finally {
            // restore wakeup state if needed
        	// 根据需要恢复唤醒状态
            if (wakenUp.get()) {
                selector.wakeup();
            }
        }
    }

    private void select(boolean oldWakenUp) throws IOException {
        // 记录下 Selector 对象
        Selector selector = this.selector;
        try {
            // select 计数器
            int selectCnt = 0; // cnt 为 count 的缩写
            // 记录当前时间，单位：纳秒
            long currentTimeNanos = System.nanoTime();
            // 计算 select 截止时间，单位：纳秒。
            long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);

            for (;;) {
                // 计算本次 select 的超时时长，单位：毫秒。
                // + 500000L 是为了四舍五入
                // / 1000000L 是为了纳秒转为毫秒
                long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
                // 如果超时时长，则结束 select
                if (timeoutMillis <= 0) {
                    if (selectCnt == 0) { // 如果是首次 select ，selectNow 一次，非阻塞
                        selector.selectNow();// 主要是为了 清除所有以前调用 wakeup 方法所得的结果。
                        selectCnt = 1;
                    }
                    break;
                }

                // If a task was submitted when wakenUp value was true, the task didn't get a chance to call
                // Selector#wakeup. So we need to check task queue again before executing select operation.
                // If we don't, the task might be pended until select operation was timed out.
                // It might be pended until idle timeout if IdleStateHandler existed in pipeline.
                
                /*
                 * 如果在wakenUp值为true时提交了任务，则该任务没有机会调用Selector＃wakeup。 因此，我们需要在执行选择操作之前
                 * 再次检查任务队列。 如果我们不这样做，则可能要等待该任务，直到选择操作超时。 如果IdleStateHandler存在于管道中，
                 * 则可能要等到空闲超时为止。
                 */
                // 若有新的任务加入
                if (hasTasks() && wakenUp.compareAndSet(false, true)) {
                    // selectNow 一次，非阻塞
                    selector.selectNow();// 主要是为了 清除所有以前调用 wakeup 方法所得的结果。
                    // 重置 select 计数器
                    selectCnt = 1;
                    break;
                }

                // 阻塞 select ，查询 Channel 是否有就绪的 IO 事件
                int selectedKeys = selector.select(timeoutMillis);
                // select 计数器 ++
                selectCnt ++;

                // 结束 select ，如果满足下面任一一个条件
                if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()) {
                    // - Selected something,
                    // - waken up by user, or
                    // - the task queue has a pending task.
                    // - a scheduled task is ready for processing
                    break;
                }
                // 线程被打断。一般情况下不会出现，出现基本是 bug ，或者错误使用。
                if (Thread.interrupted()) {
                    // Thread was interrupted so reset selected keys and break so we not run into a busy loop.
                    // As this is most likely a bug in the handler of the user or it's client library we will
                    // also log it.
                    //
                    // See https://github.com/netty/netty/issues/2426
                    if (logger.isDebugEnabled()) {
                        logger.debug("Selector.select() returned prematurely because " +
                                "Thread.currentThread().interrupt() was called. Use " +
                                "NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
                    }
                    selectCnt = 1;
                    break;
                }

                // 记录当前时间
                long time = System.nanoTime();
                // 符合 select 超时条件，重置 selectCnt 为 1
                if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
                    // timeoutMillis elapsed without anything selected.
                    selectCnt = 1;
                // 不符合 select 超时的提交，若 select 次数到达重建 Selector 对象的上限，进行重建
                } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
                        selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                    // The selector returned prematurely many times in a row.
                    // Rebuild the selector to work around the problem.
                	
                	// 选择器连续过早返回多次。 重建选择器以解决该问题。(该问题指的是 空轮训导致cpu 100%的问题)
                    logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.", selectCnt, selector);

                    // 重建 Selector 对象
                    rebuildSelector();
                    // 修改下 Selector 对象
                    selector = this.selector;

                    // Select again to populate selectedKeys.
                    // 立即 selectNow 一次，非阻塞
                    selector.selectNow(); // 主要是为了 清除所有以前调用 wakeup 方法所得的结果。
                    // 重置 selectCnt 为 1
                    selectCnt = 1;
                    // 结束 select
                    break;
                }

                currentTimeNanos = time;
            }

            if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.", selectCnt - 1, selector);
                }
            }
        } catch (CancelledKeyException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?", selector, e);
            }
            // Harmless exception - log anyway
        }
    }

    private void selectAgain() {
        needsToSelectAgain = false;
        try {
            selector.selectNow();
        } catch (Throwable t) {
            logger.warn("Failed to update SelectionKeys.", t);
        }
    }

}
