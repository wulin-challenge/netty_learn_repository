package cn.wulin.netty.multi.port;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ScreenWindow {
	
	private static ScreenWindow screenWindow;
	
	/**
	 * 窗体
	 */
	private final JFrame jFrame;
	
	private ScreenCanvasPanel screenCanvasPanel;
	
	
	private ScreenWindow() {
		super();
		this.jFrame = new JFrame();
		screenCanvasPanel = new ScreenCanvasPanel();
		jFrame.add(screenCanvasPanel);
		setting();
	}
	
	public void refresh(byte[] bytes) {
		screenCanvasPanel.refresh(bytes);
	}
	
	public void launch() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> {
			jFrame.setVisible(true);
		});
	}
	
	

	/**
	 * 窗体属性设置
	 */
	private void setting() {
		jFrame.setLocation(250, 250);
		jFrame.setSize(1000, 800);
		jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println(".....windowClosing");
			}
		});
	}
	
	public static ScreenWindow getScreenWindow() {
		if(screenWindow == null) {
			synchronized (ScreenWindow.class) {
				if(screenWindow == null) {
					screenWindow = new ScreenWindow();
				}
				
			}
		}
		return screenWindow;
	}

}
