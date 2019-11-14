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
package io.netty.channel;

import io.netty.util.ReferenceCounted;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * A region of a file that is sent via a {@link Channel} which supports
 * <a href="http://en.wikipedia.org/wiki/Zero-copy">zero-copy file transfer</a>.
 * 
 * <p> 通过支持零拷贝文件传输的通道发送的文件区域。
 *
 * <h3>Upgrade your JDK / JRE</h3>
 * 
 * <p> 升级您的JDK / JRE
 *
 * <p> {@link FileChannel#transferTo(long, long, WritableByteChannel)} has at least
 * four known bugs in the old versions of Sun JDK and perhaps its derived ones.
 * Please upgrade your JDK to 1.6.0_18 or later version if you are going to use
 * zero-copy file transfer.
 * 
 * <p> FileChannel.transferTo（long，long，WritableByteChannel）在Sun JDK的旧版本中以及它的派生错误中至少有四个已知的错误。 
 * 如果要使用零拷贝文件传输，请将JDK升级到1.6.0_18或更高版本。
 * 
 * <ul>
 * <li><a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5103988">5103988</a>
 *   - FileChannel.transferTo() should return -1 for EAGAIN instead throws IOException</li>
 * <li></li>
 * <li></li> 5103988-FileChannel.transferTo（）应该为EAGAIN返回-1，而不是抛出IOException
 * <li></li>
 * <li><a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6253145">6253145</a>
 *   - FileChannel.transferTo() on Linux fails when going beyond 2GB boundary</li>
 * <li></li>
 * <li></li> 6253145-超出2GB边界时，Linux上的FileChannel.transferTo（）失败
 * <li></li>
 * <li><a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6427312">6427312</a>
 *   - FileChannel.transferTo() throws IOException "system call interrupted"</li>
 * <li></li>
 * <li></li> 6427312-FileChannel.transferTo（）引发IOException“系统调用中断”
 * <li></li>
 * <li><a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6524172">6470086</a>
 *   - FileChannel.transferTo(2147483647, 1, channel) causes "Value too large" exception</li>
 * <li></li>
 * <li></li> 6470086-FileChannel.transferTo（2147483647，1，channel）导致“值太大”异常
 * </ul>
 *
 * <h3>Check your operating system and JDK / JRE</h3>
 * <h3>检查您的操作系统和JDK / JRE</h3>
 *
 * <p> If your operating system (or JDK / JRE) does not support zero-copy file
 * transfer, sending a file with {@link FileRegion} might fail or yield worse
 * performance.  For example, sending a large file doesn't work well in Windows.
 * 
 * <p> 如果您的操作系统（或JDK / JRE）不支持零拷贝文件传输，
 * 则使用FileRegion发送文件可能会失败或产生较差的性能。 例如，在Windows中无法发送大文件。
 *
 * <h3>Not all transports support it</h3>
 * <h3>并非所有传输工具都支持</h3>
 */
public interface FileRegion extends ReferenceCounted {

    /**
     * Returns the offset in the file where the transfer began.
     */
    long position();

    /**
     * Returns the bytes which was transfered already.
     *
     * @deprecated Use {@link #transferred()} instead.
     */
    @Deprecated
    long transfered();

    /**
     * Returns the bytes which was transfered already.
     */
    long transferred();

    /**
     * Returns the number of bytes to transfer.
     */
    long count();

    /**
     * Transfers the content of this file region to the specified channel.
     * 
     * <p> 将此文件区域的内容传输到指定的通道。
     *
     * @param target    the destination of the transfer
     * 
     * <p> 传输目的地
     * 
     * @param position  the relative offset of the file where the transfer
     *                  begins from.  For example, <tt>0</tt> will make the
     *                  transfer start from {@link #position()}th byte and
     *                  <tt>{@link #count()} - 1</tt> will make the last
     *                  byte of the region transferred.
     *                  
     * <p> 传输开始的文件的相对偏移量。 例如，0将使传输从第position（）个字节开始，
     * 而count（）-1将使传输的区域的最后一个字节。
     */
    long transferTo(WritableByteChannel target, long position) throws IOException;

    @Override
    FileRegion retain();

    @Override
    FileRegion retain(int increment);

    @Override
    FileRegion touch();

    @Override
    FileRegion touch(Object hint);
}
