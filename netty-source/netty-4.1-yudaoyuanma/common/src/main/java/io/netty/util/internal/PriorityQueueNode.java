/*
 * Copyright 2015 The Netty Project
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
package io.netty.util.internal;

/**
 * Provides methods for {@link DefaultPriorityQueue} to maintain internal state. These methods should generally not be
 * used outside the scope of {@link DefaultPriorityQueue}.
 * 
 * <p> 为DefaultPriorityQueue提供方法以维护内部状态。 这些方法通常不应在DefaultPriorityQueue范围之外使用。
 */
public interface PriorityQueueNode {

    /**
     * This should be used to initialize the storage returned by {@link #priorityQueueIndex(DefaultPriorityQueue)}.
     * 
     * <p> 这应该用于初始化由priorityQueueIndex（DefaultPriorityQueue）返回的存储。
     */
    int INDEX_NOT_IN_QUEUE = -1;

    /**
     * Get the last value set by {@link #priorityQueueIndex(DefaultPriorityQueue, int)} for the value corresponding to
     * {@code queue}.
     * 
     * <p> 获取由priorityQueueIndex（DefaultPriorityQueue，int）设置的最后一个值，该值对应于队列。
     * 
     * <p>
     * Throwing exceptions from this method will result in undefined behavior.
     * 
     * <p> 从此方法引发异常将导致未定义的行为。
     */
    int priorityQueueIndex(DefaultPriorityQueue<?> queue);

    /**
     * Used by {@link DefaultPriorityQueue} to maintain state for an element in the queue.
     * 
     * <p> 由DefaultPriorityQueue用来维护队列中元素的状态。
     * 
     * <p>
     * Throwing exceptions from this method will result in undefined behavior.
     * 
     * <p> 从此方法引发异常将导致未定义的行为。
     * 
     * @param queue The queue for which the index is being set.
     * 
     * <p> 正在为其设置索引的队列。
     * 
     * @param i The index as used by {@link DefaultPriorityQueue}.
     * 
     * <p> DefaultPriorityQueue使用的索引。
     */
    void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i);

}