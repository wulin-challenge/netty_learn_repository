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
package io.netty.buffer;

/**
 * Implementations are responsible to allocate buffers. Implementations of this interface are expected to be
 * thread-safe.
 * 
 * <p> 实现负责分配缓冲区。 该接口的实现应该是线程安全的。
 */
public interface ByteBufAllocator {

    ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;

    /**
     * Allocate a {@link ByteBuf}. If it is a direct or heap buffer
     * depends on the actual implementation.
     * 
     * <p> 分配一个ByteBuf。 是直接缓冲区还是堆缓冲区取决于实际实现。
     */
    ByteBuf buffer();

    /**
     * Allocate a {@link ByteBuf} with the given initial capacity.
     * If it is a direct or heap buffer depends on the actual implementation.
     * 
     * <p> 用给定的初始容量分配一个ByteBuf。 是直接缓冲区还是堆缓冲区取决于实际实现。
     */
    ByteBuf buffer(int initialCapacity);

    /**
     * Allocate a {@link ByteBuf} with the given initial capacity and the given
     * maximal capacity. If it is a direct or heap buffer depends on the actual
     * implementation.
     * 
     * <p> 用给定的初始容量和给定的最大容量分配一个ByteBuf。 是直接缓冲区还是堆缓冲区取决于实际实现。
     */
    ByteBuf buffer(int initialCapacity, int maxCapacity);

    /**
     * Allocate a {@link ByteBuf}, preferably a direct buffer which is suitable for I/O.
     * 
     * <p> 分配ByteBuf，最好是适合I / O的直接缓冲区。
     */
    ByteBuf ioBuffer();

    /**
     * Allocate a {@link ByteBuf}, preferably a direct buffer which is suitable for I/O.
     * 
     * <p> 分配ByteBuf，最好是适合I / O的直接缓冲区。
     */
    ByteBuf ioBuffer(int initialCapacity);

    /**
     * Allocate a {@link ByteBuf}, preferably a direct buffer which is suitable for I/O.
     * 
     * <p> 分配ByteBuf，最好是适合I / O的直接缓冲区。
     */
    ByteBuf ioBuffer(int initialCapacity, int maxCapacity);

    /**
     * Allocate a heap {@link ByteBuf}.
     * 
     * <p> 分配堆ByteBuf。
     */
    ByteBuf heapBuffer();

    /**
     * Allocate a heap {@link ByteBuf} with the given initial capacity.
     * 
     * <p> 用给定的初始容量分配堆ByteBuf。
     */
    ByteBuf heapBuffer(int initialCapacity);

    /**
     * Allocate a heap {@link ByteBuf} with the given initial capacity and the given
     * maximal capacity.
     * 
     * <p> 用给定的初始容量和给定的最大容量分配一个堆ByteBuf。
     */
    ByteBuf heapBuffer(int initialCapacity, int maxCapacity);

    /**
     * Allocate a direct {@link ByteBuf}.
     * 
     * <p> 分配直接的ByteBuf。
     */
    ByteBuf directBuffer();

    /**
     * Allocate a direct {@link ByteBuf} with the given initial capacity.
     * 
     * <p> 用给定的初始容量和给定的最大容量分配一个直接的ByteBuf。
     */
    ByteBuf directBuffer(int initialCapacity);

    /**
     * Allocate a direct {@link ByteBuf} with the given initial capacity and the given
     * maximal capacity.
     * 
     * <p> 用给定的初始容量和给定的最大容量分配一个直接的ByteBuf。
     */
    ByteBuf directBuffer(int initialCapacity, int maxCapacity);

    /**
     * Allocate a {@link CompositeByteBuf}.
     * If it is a direct or heap buffer depends on the actual implementation.
     * 
     * <p> 分配一个CompositeByteBuf。 是直接缓冲区还是堆缓冲区取决于实际实现。
     */
    CompositeByteBuf compositeBuffer();

    /**
     * Allocate a {@link CompositeByteBuf} with the given maximum number of components that can be stored in it.
     * If it is a direct or heap buffer depends on the actual implementation.
     * 
     * <p> 用给定的可存储组件的最大数量分配一个CompositeByteBuf。 是直接缓冲区还是堆缓冲区取决于实际实现。
     */
    CompositeByteBuf compositeBuffer(int maxNumComponents);

    /**
     * Allocate a heap {@link CompositeByteBuf}.
     * 
     * <p> 分配堆CompositeByteBuf。
     */
    CompositeByteBuf compositeHeapBuffer();

    /**
     * Allocate a heap {@link CompositeByteBuf} with the given maximum number of components that can be stored in it.
     * 
     * <p> 用给定的可以存储在其中的最大组件数量分配一个CompositeBy组件。
     */
    CompositeByteBuf compositeHeapBuffer(int maxNumComponents);

    /**
     * Allocate a direct {@link CompositeByteBuf}.
     * 
     * <p> 分配直接的CompositeByteBuf。
     */
    CompositeByteBuf compositeDirectBuffer();

    /**
     * Allocate a direct {@link CompositeByteBuf} with the given maximum number of components that can be stored in it.
     * 
     * <p> 使用给定的最大可存储组件数分配直接CompositeByteBuf。
     */
    CompositeByteBuf compositeDirectBuffer(int maxNumComponents);

    /**
     * Returns {@code true} if direct {@link ByteBuf}'s are pooled
     * 
     * <p> 如果直接的ByteBuf被池化，则返回true
     */
    boolean isDirectBufferPooled();

    /**
     * Calculate the new capacity of a {@link ByteBuf} that is used when a {@link ByteBuf} needs to expand by the
     * {@code minNewCapacity} with {@code maxCapacity} as upper-bound.
     * 
     * <p> 计算一个ByteBuf的新容量，该容量在ByteBuf需要扩展minNewCapacity且maxCapacity为上限时使用。
     */
    int calculateNewCapacity(int minNewCapacity, int maxCapacity);

}
