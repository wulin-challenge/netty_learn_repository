package cn.wulin.netty.multi.port.test;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;

import cn.wulin.netty.multi.port.utils.CommonUtil;
import cn.wulin.netty.multi.port.utils.GzipUtil;
import cn.wulin.netty.multi.port.utils.ImageUtils;
import cn.wulin.netty.multi.port.utils.PuppetDynamicSetting;

public class TestScreenImage {
	
	private Robot robot;
    private Toolkit toolkit;
    
    public static void main(String[] args) throws IOException {
    	TestScreenImage ts = new TestScreenImage();
    	
    	byte[] screenSnapshot = ts.getScreenSnapshot();
    	
    	File origin = new File("F:/resources/temp/temp4/receiveData/","origin.txt");
    	File zip = new File("F:/resources/temp/temp4/receiveData/","zip.txt");
    	
    	FileUtils.writeByteArrayToFile(origin, screenSnapshot);
    	
    	byte[] jzlib = GzipUtil.compress(screenSnapshot);
    	FileUtils.writeByteArrayToFile(zip, jzlib);
    	
    	BufferedImage convertByteArrayToImage = ImageUtils.convertByteArrayToImage(GzipUtil.uncompress(jzlib));
    	
    	File image = new File("F:/resources/temp/temp4/receiveData/",System.currentTimeMillis()+".png");
    	ImageIO.write(convertByteArrayToImage, "png", image);
    	
	}

    public TestScreenImage(){
        try {
            robot=new Robot();
            toolkit=Toolkit.getDefaultToolkit();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

	public byte[] getScreenSnapshot(){
        //获取屏幕分辨率
        Dimension d = toolkit.getScreenSize();
        //以屏幕的尺寸创建个矩形
        Rectangle screenRect = new Rectangle(d);
        //截图（截取整个屏幕图片）
        BufferedImage bufferedImage =  robot.createScreenCapture(screenRect);
        return ImageUtils.compressedImageAndGetByteArray(bufferedImage, PuppetDynamicSetting.quality/100.0f);
    }
}
