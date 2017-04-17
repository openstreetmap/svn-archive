package imageryadjust;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.util.TreeSet;
import javax.swing.Timer;

import org.openstreetmap.josm.Main;

public abstract class TimedKeyReleaseListener implements AWTEventListener {
    private final TreeSet<Integer> set = new TreeSet<>();
    private Timer timer;
    protected KeyEvent releaseEvent;
    
    public TimedKeyReleaseListener() {
        timer = new Timer(0, ae -> {
             timer.stop();
             if (set.remove(releaseEvent.getKeyCode())) {
                 doKeyReleaseEvent(releaseEvent);
             }
        });
        
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
            Main.error(ex);
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
            Main.error(ex);
        }
    }
    
    protected abstract void doKeyReleaseEvent(KeyEvent evt);

    protected abstract void doKeyPressEvent(KeyEvent evt);
}
