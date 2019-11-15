package cn.wulin.netty.file.demo.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;
import io.netty.handler.stream.ChunkedInput;

/**
 * A {@link ChunkedInput} that fetches data from a file chunk by chunk.
 * <p>
 * If your operating system supports
 * <a href="http://en.wikipedia.org/wiki/Zero-copy">zero-copy file transfer</a>
 * such as {@code sendfile()}, you might want to use {@link FileRegion} instead.
 */
public class FileDemoChunkedFile implements ChunkedInput<ByteBuf> {

    private final RandomAccessFile file;
    private final long startOffset;
    private final long endOffset;
    private final int chunkSize;
    //传输排序号
    private final AtomicLong transferSortNumber = new AtomicLong(0);
    private long offset;
    
    private int requestId;
    
    static final int DEFAULT_CHUNK_SIZE = 8192;

    /**
     * Creates a new instance that fetches data from the specified file.
     */
    public FileDemoChunkedFile(File file,int requestId) throws IOException {
        this(file, DEFAULT_CHUNK_SIZE,requestId);
    }

    /**
     * Creates a new instance that fetches data from the specified file.
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public FileDemoChunkedFile(File file, int chunkSize,int requestId) throws IOException {
        this(new RandomAccessFile(file, "r"), chunkSize);
        this.requestId = requestId;
    }

    /**
     * Creates a new instance that fetches data from the specified file.
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    private FileDemoChunkedFile(RandomAccessFile file, int chunkSize) throws IOException {
        this(file, 0, file.length(), chunkSize);
    }

    /**
     * Creates a new instance that fetches data from the specified file.
     *
     * @param offset the offset of the file where the transfer begins
     * @param length the number of bytes to transfer
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    private FileDemoChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (offset < 0) {
            throw new IllegalArgumentException(
                    "offset: " + offset + " (expected: 0 or greater)");
        }
        if (length < 0) {
            throw new IllegalArgumentException(
                    "length: " + length + " (expected: 0 or greater)");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize: " + chunkSize +
                    " (expected: a positive integer)");
        }

        this.file = file;
        this.offset = startOffset = offset;
        endOffset = offset + length;
        this.chunkSize = chunkSize;

        file.seek(offset);
    }

    /**
     * Returns the offset in the file where the transfer began.
     */
    public long startOffset() {
        return startOffset;
    }

    /**
     * Returns the offset in the file where the transfer will end.
     */
    public long endOffset() {
        return endOffset;
    }

    /**
     * Returns the offset in the file where the transfer is happening currently.
     */
    public long currentOffset() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return !(offset < endOffset && file.getChannel().isOpen());
    }

    @Override
    public void close() throws Exception {
        file.close();
    }

    @Deprecated
    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }
    
    AtomicInteger count = new AtomicInteger();

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
    	
    	
        long offset = this.offset;
        if (offset >= endOffset) {
            return null;
        }
        String name = Thread.currentThread().getName();
        System.out.println("server readChunk : "+count.incrementAndGet()+" , sort : "+transferSortNumber.get()+" , thread name : "+name);
        
        int chunkSize = (int) Math.min(this.chunkSize, endOffset - offset);
        this.offset = offset + chunkSize;
        // Check if the buffer is backed by an byte array. If so we can optimize it a bit an safe a copy
        int bufferSize = chunkSize + 4 + 4 + 8 + 4;

        ByteBuf buf = allocator.heapBuffer(bufferSize+8);
        
        buf.writeInt(requestId);
        buf.writeInt(isEndOfInput()?-1:0); //第一次传输 1标记 就不要了
        buf.writeLong(length());
        buf.writeInt(chunkSize+8);
        buf.writeLong(transferSortNumber.getAndIncrement()); //数据块的传输顺序序号
        boolean release = true;
        try {
        	//注意: 这种方式读取会有数据不全的问题,一定不要使用这个方式进行数据的读取,特别是传输大文件是这个问题非常突出,小文件没有什么问题
//            file.readFully(buf.array(), buf.writerIndex(), chunkSize);
//            buf.writerIndex(bufferSize+8);
        	
        	//这个方式彻底解决上述这个问题
        	byte[] fileData = new byte[chunkSize];
        	file.readFully(fileData, 0, chunkSize);
        	buf.writeBytes(fileData,0,chunkSize);
            
            release = false;
            return buf;
        } finally {
            if (release) {
                buf.release();
            }
        }
    }
    
    @Override
    public long length() {
        return endOffset - startOffset;
    }

    @Override
    public long progress() {
        return offset - startOffset;
    }
}
