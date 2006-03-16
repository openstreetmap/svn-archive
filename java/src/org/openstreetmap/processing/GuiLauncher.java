package org.openstreetmap.processing;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import thinlet.Thinlet;

/**
 * Contains a helper function to launch a dialog within the applet.
 * 
 * @author Imi
 */
public class GuiLauncher {

	/**
	 * Create a modal dialog from the xmlfile. The dialog will have exactly one
	 * component which is a Thinlet. To get it, use {@link Container#getComponent(int)}
	 * on the returned dialog. You can also use the other convinient members of 
	 * GuiLauncher.
	 * 
	 * @param parent The parent for this dialog.
	 * @param title The title of the dialog.
	 * @param xmlFile The xml file in resource notation
	 * 
	 * @return A dialog created from the xml file
	 * @throws RuntimeException In case of problems loading the xml file, a runtime
	 * 		exception (indicating a coding error) is thrown.
	 * @see Class#getResource(String)
	 */
	public static Dialog createDialog(Frame parent, String title, String xmlFile) {
		final Dialog dlg = new Dialog(parent, title, true);
		Thinlet thinlet = new Thinlet();
		try {
			thinlet.add(thinlet.parse(xmlFile));
		} catch (IOException e) {
			throw new IllegalArgumentException("IO error while reading "+xmlFile+".", e);
		}
		dlg.setLayout(new GridBagLayout()); // GBL does respect preferred size.
		dlg.add(thinlet);
		dlg.pack();
		
		// center on parent (or screen, if parent == null)
		int x,y;
		if (parent == null) {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			x = (screen.width-dlg.getPreferredSize().width)/2;
			y = (screen.height-dlg.getPreferredSize().height)/2;
		} else {
			System.out.println(parent.getX());
			System.out.println(parent.getY());
			System.out.println(dlg.getPreferredSize());
			System.out.println(parent.getSize());
			x = parent.getX()+(parent.getWidth()-dlg.getPreferredSize().width)/2;
			y = parent.getY()+(parent.getHeight()-dlg.getPreferredSize().height)/2;
		}
		dlg.setLocation(x,y);

		// setVisible(false) for modal dialogs to close.
		dlg.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				dlg.setVisible(false);
			}
		});
		return dlg;
	}
}
