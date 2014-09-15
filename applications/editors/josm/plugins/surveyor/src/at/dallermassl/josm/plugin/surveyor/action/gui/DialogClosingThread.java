/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;

/**
 * @author cdaller
 *
 */
public class DialogClosingThread extends Thread implements KeyListener, DocumentListener {
    public static final long DEFAULT_TIMEOUT = 5000;
    private JDialog dialog;
    private long timeout;
    private long loopCount;

    /**
     * Using the given dialog and the default timeout.
     * @param dialog
     */
    public DialogClosingThread(JDialog dialog) {
        this(dialog, DEFAULT_TIMEOUT);
    }

    /**
     * @param dialog
     * @param timeout
     */
    public DialogClosingThread(JDialog dialog, long timeout) {
        super();
        this.dialog = dialog;
        this.timeout = timeout;
        this.loopCount = timeout / 1000;
    }

    @Override
    public void run() {
        String title = dialog.getTitle();
        while(loopCount > 0) {
            dialog.setTitle(title + " (" + loopCount + "sec)");
            --loopCount;
            try {
                sleep(1000);
            } catch(InterruptedException ignore) {}
        }

        dialog.setVisible(false);
        dialog.dispose();
    }

    public void reset() {
        this.loopCount = timeout / 1000;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        reset();
        Main.debug("keypressed: " + e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        reset();
        Main.debug("keyreleased: " + e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        reset();
        Main.debug("keytyped: " + e.getKeyCode());
    }

    /**
     * @param optionPane
     */
    public void observe(Container container) {
        for(Component component : container.getComponents()) {
            if(component instanceof JTextField) {
                observe((JTextField)component);
            } else {
                observe(component);
            }
        }
    }

    public void observe(Component component) {
        component.addKeyListener(this);
    }

    public void observe(JTextField textfield) {
        textfield.getDocument().addDocumentListener(this);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        reset();
        Main.debug("changedUpdate: " + e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        reset();
        Main.debug("insertUpdate: " + e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        reset();
        Main.debug("removeUpdate: " + e);
    }
}
