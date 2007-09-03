// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.xml.sax.SAXException;

/**
 * Instanced of this thread will display a "Please Wait" message in middle of JOSM
 * to indicate a progress beeing executed.
 *
 * @author Imi
 */
public abstract class PleaseWaitRunnable implements Runnable {

	public String errorMessage;

	private boolean closeDialogCalled = false;
	private boolean cancelled = false;

	private final String title;

	/**
	 * Create the runnable object with a given message for the user.
	 */
	public PleaseWaitRunnable(String title) {
		this.title = title;
		Main.pleaseWaitDlg.cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (!cancelled) {
					cancelled = true;
					cancel();
				}
			}
		});
		Main.pleaseWaitDlg.addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e) {
				if (!closeDialogCalled) {
					if (!cancelled) {
						cancelled = true;
						cancel();
					}
					closeDialog();
				}
			}
		});
	}

	public final void run() {
		try {
			if (cancelled)
				return; // since realRun isn't executed, do not call to finish

			// reset dialog state
			Main.pleaseWaitDlg.setTitle(title);
			errorMessage = null;
			closeDialogCalled = false;

			// show the dialog
			synchronized (this) {
	            EventQueue.invokeLater(new Runnable() {
	            	public void run() {
	            		synchronized (PleaseWaitRunnable.this) {
	            			PleaseWaitRunnable.this.notifyAll();
	            		}
	            		Main.pleaseWaitDlg.setVisible(true);
	            	}
	            });
	            try {wait();} catch (InterruptedException e) {}
			}

			realRun();
		} catch (SAXException x) {
			x.printStackTrace();
			errorMessage = tr("Error while parsing")+": "+x.getMessage();
		} catch (FileNotFoundException x) {
			x.printStackTrace();
			errorMessage = tr("File not found")+": "+x.getMessage();
		} catch (IOException x) {
			x.printStackTrace();
			errorMessage = x.getMessage();
		} finally {
			closeDialog();
		}
	}

	/**
	 * User pressed cancel button.
	 */
	protected abstract void cancel();

	/**
	 * Called in the worker thread to do the actual work. When any of the
	 * exception is thrown, a message box will be displayed and closeDialog
	 * is called. finish() is called in any case.
	 */
	protected abstract void realRun() throws SAXException, IOException;

	/**
	 * Finish up the data work. Is guaranteed to be called if realRun is called.
	 * Finish is called in the gui thread just after the dialog disappeared.
	 */
	protected abstract void finish();

	/**
	 * Close the dialog. Usually called from worker thread.
	 */
	public void closeDialog() {
		if (closeDialogCalled)
			return;
		closeDialogCalled  = true;
		try {
			Runnable runnable = new Runnable(){
				public void run() {
					try {
						finish();
					} finally {
						Main.pleaseWaitDlg.setVisible(false);
						Main.pleaseWaitDlg.dispose();
					}
					if (errorMessage != null)
						JOptionPane.showMessageDialog(Main.parent, errorMessage);
				}
			};

			// make sure, this is called in the dispatcher thread ASAP
			if (EventQueue.isDispatchThread())
				runnable.run();
			else
				EventQueue.invokeAndWait(runnable);

		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
