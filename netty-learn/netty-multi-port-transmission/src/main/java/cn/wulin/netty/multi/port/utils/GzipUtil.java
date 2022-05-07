package cn.wulin.netty.multi.port.utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GzipUtil {

    /**
     * 将文件压缩成gzip
     * @param sourceFile 源文件，如：archive.tar
     * @param targetFile 目标文件，如：archive.tar.gz
     */
    public static byte[] compress(byte[] object) {
    	byte[] data = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GzipCompressorOutputStream zOut = new GzipCompressorOutputStream(out);
			DataOutputStream objOut = new DataOutputStream(zOut);
			objOut.write(object);
			objOut.flush();
			zOut.close();
			data = out.toByteArray();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
    }
    
    /**
	 * 解压被压缩的数据
	 *
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] uncompress(byte[] object) {
		byte[] data = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(object);
			GzipCompressorInputStream zIn = new GzipCompressorInputStream(in);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = zIn.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			data = baos.toByteArray();
			baos.flush();
			baos.close();
			zIn.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}