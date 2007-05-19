/**
 * 
 */
package test;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author cdaller
 *
 */
public class DialogClosingThread extends Thread implements KeyListener, DocumentListener {
    private static long DEFAULT_TIMEOUT = 5000;
    private JDialog dialog;
    private long timeout;
    private String dialogTitle;
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

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
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

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        reset();
        System.out.println("keypressed: " + e.getKeyCode());
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        reset();
        System.out.println("keyreleased: " + e.getKeyCode());
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        reset();
        System.out.println("keytyped: " + e.getKeyCode());
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

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        reset();
        System.out.println("changedUpdate: " + e);        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        reset();
        System.out.println("insertUpdate: " + e);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        reset();
        System.out.println("removeUpdate: " + e);        
    }
}
