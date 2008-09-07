/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.utils;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.openstreetmap.josmng.ui.Main;

/**
 * A runnable that performs on a separate thread, while optionally providing
 * progress updates. It can block the UI (by the means of a modal progress
 * dialog, not blocking the EDT or perform really independently
 * in the background. It has provision for cancelation, but real cancelability
 * depends on the actual task.
 * 
 * @author nenik
 */
public abstract class BackgroundTask {
    private static final Executor executor = Executors.newCachedThreadPool();
    private Thread worker;
    private JDialog dialog;
    private JProgressBar progress;
    private JLabel label;
    private volatile boolean cancelled;
    
    
    /**
     * The worker function that will get called on the background thread.
     */
    protected abstract void perform();

    /**
     * The ui-part function that will get called on the EDT thread once
     * the perform function is finished. It is called also when the task
     * was cancelled, so it may call isCancelled and either finish the task
     * or perform a cleanup instead.
     */
    protected void finish() {}
    
    public final void performBlocking() {
        assert EventQueue.isDispatchThread();
        
        // Set up a blocking dialog
        createWaitDialog();
        
        // start the task
        executor.execute(new Runnable() {
            public void run() {
                worker = Thread.currentThread();
                try {
                    perform();
                } finally {
                    worker = null;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                                hideWaitDialog();
                                finish();
                        }
                    });
                }
            }
        });
        
        
        // and block the ui
        dialog.show();

    }
    
    /**
     * 
     */
    public final void performInBackground() {
        assert EventQueue.isDispatchThread();
        
        throw new UnsupportedOperationException("Not implemented yet");
        
        // Set up a progress bar in status line
        // fire a background thread to perform the task
        // once done, remove the progress bar.
    }
    
    protected void setLabel(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                label.setText(text);
            }
        });
    }
    
    protected void setProgress(final int dots, final int outOf) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progress.getModel().setRangeProperties(dots, 0, 0, outOf, false);
            }
        });
    }

    protected boolean isCancelled() {
        return cancelled;
    }
    
    protected void cancel() {
        cancelled = true;
        worker.interrupt();
        hideWaitDialog();
    }
    
    private void hideWaitDialog() {
        dialog.hide();
    }
    
    private void createWaitDialog() {
        JPanel panel = new JPanel(new BorderLayout());
        label = new JLabel("...");
        progress = new JProgressBar();
        progress.setIndeterminate(true);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progress, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        
        panel.add(cancel, BorderLayout.SOUTH);
        
        dialog = new JDialog(Main.main, "Please wait...", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
    }
}
