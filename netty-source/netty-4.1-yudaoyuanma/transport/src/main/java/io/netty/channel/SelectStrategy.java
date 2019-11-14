/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel;

import io.netty.util.IntSupplier;

/**
 * Select strategy interface.
 *
 * <p> 选择策略接口
 *
 * <p> Provides the ability to control the behavior of the select loop. For example a blocking select
 * operation can be delayed or skipped entirely if there are events to process immediately.
 * 
 * <p> 提供控制选择循环行为的功能。 例如，如果有事件要立即处理，则阻塞选择操作可以被延迟或完全跳过。
 */
public interface SelectStrategy {

    /**
     * Indicates a blocking select should follow.
     * 
     * <p> 指示应遵循阻止选择。
     *
     * <p> 表示使用阻塞 select 的策略。
     */
    int SELECT = -1;
    /**
     * Indicates the IO loop should be retried, no blocking select to follow directly.
     * 
     * <p> 表示应重试IO循环，没有阻塞选择可直接执行。
     *
     * <p> 表示需要进行重试的策略。
     */
    int CONTINUE = -2;

    /**
     * The {@link SelectStrategy} can be used to steer the outcome of a potential select
     * call.
     * 
     * <p> SelectStrategy可用于控制潜在选择调用的结果。
     *
     * @param selectSupplier The supplier with the result of a select result.
     * 
     * <p> 选择结果的供应者。
     * 
     * @param hasTasks true if tasks are waiting to be processed.
     * 
     * <p> 如果正在等待处理任务，则为true。
     * 
     * @return {@link #SELECT} if the next step should be blocking select {@link #CONTINUE} if
     *         the next step should be to not select but rather jump back to the IO loop and try
     *         again. Any value >= 0 is treated as an indicator that work needs to be done.
     *         
     * <p> 如果下一步应该阻塞，则选择SELECT
     * （如果下一步不选择，而是跳回IO循环，然后重试）。 任何大于等于0的值都视为需要完成工作的指标。
     */
    int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception;

}