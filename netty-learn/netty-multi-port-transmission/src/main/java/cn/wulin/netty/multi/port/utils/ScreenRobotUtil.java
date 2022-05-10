package cn.wulin.netty.multi.port.utils;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * 屏幕机器人工具类
 * @author wulin
 *
 */
public class ScreenRobotUtil {
	
	private static Robot robot;
    private static Toolkit toolkit;
    
    static {
    	 try {
             robot=new Robot();
             toolkit=Toolkit.getDefaultToolkit();
         } catch (AWTException e) {
             throw new RuntimeException(e);
         }
    }
    
    public static byte[] getScreenSnapshot(){
        //获取屏幕分辨率
        Dimension d = toolkit.getScreenSize();
        //以屏幕的尺寸创建个矩形
        Rectangle screenRect = new Rectangle(d);
        //截图（截取整个屏幕图片）
        BufferedImage bufferedImage =  robot.createScreenCapture(screenRect);
        return ImageUtils.compressedImageAndGetByteArray(bufferedImage, PuppetDynamicSetting.quality/100.0f);
    }

}
