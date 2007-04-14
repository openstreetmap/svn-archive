package org.openstreetmap.gui;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.Arrays;

import thinlet.Thinlet;

public class MsgBox {

	public static class BoxThinlet extends Thinlet {
		private int index = 0;
		private String[] buttons = null;
		public Dialog dlg;
		/**
		 * Callback of msgbox.xml
		 */
		public void handleButton(String text) {
			index = Arrays.asList(buttons).indexOf(text);
			dlg.setVisible(false);
		}
	}
	
	/**
	 * Display a message box with all the texts as buttons and return the users choice
	 * (index of the clicked button).
	 * 
	 * The first button will be the default button (mapped to enter). The last will be 
	 * the cancel button (mapped to escape).
	 * 
	 * This function can be called from any thread. If it is called from a non-gui thread,
	 * it will block the thread until the gui-thread emptied its message loop and then have
	 * the gui thread display the message box.
	 * 
	 * @see EventQueue#invokeAndWait(java.lang.Runnable)
	 * 
	 * @param msg The text to display
	 */
	public static int show(String msg, String[] buttons) {
		try {
			BoxThinlet t = new BoxThinlet();
			t.buttons = buttons;
			t.add(t.parse(GuiHandler.class.getResource("/data/msgbox.xml").openStream()));
			t.setString(t.find("msg"), "text", msg);
			
			Object buttonPanel = t.find("buttons");
			for (int i = 0; i < buttons.length; ++i) {
				Object b = Thinlet.create("button");
				t.setString(b, "text", buttons[i]);
				t.setMethod(b, "action", "handleButton(this.text)", t.find("root"), t);
				t.add(buttonPanel, b);
				if (i == 0)
					t.setChoice(b, "type", "default");
				else if (i+1 == buttons.length)
					t.setChoice(b, "type", "cancel");
			}
			
			final GuiLauncher dlg = new GuiLauncher("", t);
			t.dlg = dlg;
			dlg.setModal(true);
			dlg.setTitle("Information");
			
			// show the message in the main thread, if we are not running already there
			if (!EventQueue.isDispatchThread())
				EventQueue.invokeAndWait(new Runnable(){
					public void run() {
						dlg.setVisible(true);
					}
				});
			else
				dlg.setVisible(true);
			return t.index;
		} catch (Exception e) {
			e.printStackTrace();
			return buttons.length-1; // default cancel button
		}
	}

	/**
	 * Shows just a message box with ok button.
	 */
	public static void msg(String msg) {
		show(msg, new String[]{"OK"});
	}
}
