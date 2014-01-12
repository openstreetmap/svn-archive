package utils;

// Thanks to http://www.arco.in-berlin.de/keyevent.html
// (code simplified here)


import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.TreeSet;
import javax.swing.Timer;

public class TimedKeyReleaseListener implements AWTEventListener {
    private final TreeSet<Integer> set = new TreeSet<Integer>();
    private Timer timer;
    protected KeyEvent releaseEvent;
    
    public TimedKeyReleaseListener() {
        timer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                 timer.stop();
                 if (set.remove(releaseEvent.getKeyCode())) {
                  doKeyReleaseEvent(releaseEvent);
                 }
            }
        });
        
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
        }
    }
    @Override
    public void eventDispatched(AWTEvent event) {
        if (!(event instanceof KeyEvent)) return;
        KeyEvent e = (KeyEvent) event;
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (timer.isRunning()) {
                timer.stop();
            } else {
                if (set.add((e.getKeyCode()))) doKeyPressEvent((KeyEvent) event);
            }
        }
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            if (timer.isRunning()) {
                timer.stop();
                if (set.remove(e.getKeyCode())) doKeyReleaseEvent(e);
            } else {
                releaseEvent = e;
                timer.restart();
            }
        }
    }
    

    public void stop() {
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
        }
    }

    
    protected void doKeyReleaseEvent(KeyEvent evt) {
    }

    protected void doKeyPressEvent(KeyEvent evt) {
    }
}