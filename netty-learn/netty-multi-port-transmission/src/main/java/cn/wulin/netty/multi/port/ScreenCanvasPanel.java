package cn.wulin.netty.multi.port;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cn.wulin.netty.multi.port.utils.ImageUtils;

public class ScreenCanvasPanel extends JPanel {

	private static final long serialVersionUID = -270134315129857869L;

	private BlockingQueue<BufferedImage> task = new LinkedBlockingQueue<BufferedImage>(5000);

//	@Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//
//    }

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		BufferedImage image;
		try {
			image = task.poll(10, TimeUnit.MILLISECONDS);
			if (image != null) {
				g.drawImage(image, 0, 0, null);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 刷新图片
	 * 
	 * @param bytes
	 */
	public void refresh(byte[] bytes) {

		BufferedImage image = ImageUtils.getImageFromByteArray(bytes);
		try {
			task.put(image);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> {
			ScreenCanvasPanel.this.repaint();
		});
	}
}
