package cn.wulin.netty.file.demo.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 此类适用于rpc传输对同步请求的控制
 * @author wulin
 *
 */
public class FileDemoFuture implements Future{
	private Sync sync = new Sync();

	@Override
	public boolean isDone() {
		return sync.isDone();
	}

	/**
	 * 等待
	 */
	@Override
	public Object get() throws InterruptedException, ExecutionException {
		sync.acquire(1);
		return null;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 通知处于等待状态的线程不要等待了
	 */
	public void done() {
		sync.release(0);
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}
	
	//使用值 1 表示未锁定状态，使用 0 表示锁定状态
	static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
        	//丢弃参数,内部state状态值默认初始值为 0 
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
        	//丢弃参数,内部state状态值默认初始值为 0 
            if (getState() == pending) {
            	//进行cas原子更新
                if (compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}
