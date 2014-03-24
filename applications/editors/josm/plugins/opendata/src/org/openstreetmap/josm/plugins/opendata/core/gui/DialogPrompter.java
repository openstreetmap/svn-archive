// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.gui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.ExtendedDialog;

public class DialogPrompter <T extends ExtendedDialog> implements Runnable {
	
	private T dialog;
	private int value = -1;

	protected T buildDialog() {return null;} // To be overriden if needed
	
	public DialogPrompter() {
		this(null);
	}
	
	public DialogPrompter(T dialog) {
		this.dialog = dialog;
	}
	
	public final T getDialog() {
		return dialog;
	}
	
	@Override
	public final void run() {
		if (dialog == null) {
			dialog = buildDialog();
		}
		if (dialog != null) {
			value = dialog.showDialog().getValue();
		}
	}
	
	public final DialogPrompter promptInEdt() {
		if (SwingUtilities.isEventDispatchThread()) {
			run();
		} else {
			try {
				SwingUtilities.invokeAndWait(this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return this;
	}

	public final int getValue() {
		return value;
	}
}
