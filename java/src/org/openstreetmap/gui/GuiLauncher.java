package org.openstreetmap.gui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;



/**
 * Contains a helper function to launch a dialog within the applet.
 * 
 * @author Imi
 */
public class GuiLauncher extends Dialog implements WindowListener {

	public GuiHandler handler;

	/**
	 * Create a modal dialog from the xmlfile. The dialog will have exactly one
	 * component which is a Thinlet. To get it, use {@link Container#getComponent(int)}
	 * on the returned dialog. You can also use the other convinient members of 
	 * GuiLauncher.
	 * 
	 * @param title The title of the dialog.
	 */
	public GuiLauncher(String title, GuiHandler handler) {
		super(new Frame(), title, false);
		this.handler = handler;

		setResizable(false);
	
		setLayout(new GridBagLayout()); // GBL does respect preferred size.
		add(handler);
		pack();

		// center on screen, if parent == null
		int x,y;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		x = (screen.width-getPreferredSize().width)/2;
		y = (screen.height-getPreferredSize().height)/2;
		setLocation(x,y);

		handler.dlg = this;

		// setAlwaysOnTop is only for 1.5. Call it if it is available.
		try {
			Method alwaysOnTop = getClass().getMethod("setAlwaysOnTop", new Class[] { Boolean.TYPE });
			alwaysOnTop.invoke(this, new Object[] { Boolean.TRUE });
		} catch (Exception e) {
			System.out.println("no setAlwaysOnTop available");
			
		}
		
		addWindowListener(this);
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {setVisible(false);}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}
