/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.action;

import javax.swing.JDialog;

/**
 * @author cdaller
 *
 */
public class DialogClosingThread extends Thread {
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
}
