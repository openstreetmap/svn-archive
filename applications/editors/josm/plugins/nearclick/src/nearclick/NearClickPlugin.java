package nearclick;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import org.openstreetmap.josm.Main;

public class NearClickPlugin implements AWTEventListener {

    private int mouseDownX = -1;
    private int mouseDownY = -1;
    private long mouseDownTime = -1;
    private int radiussquared = 49;
    private int delay = 250;

    public NearClickPlugin() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    try {
        int radius = Integer.parseInt(Main.pref.get("nearclick.radius", "7"));
        radiussquared = radius * radius;
        delay = Integer.parseInt(Main.pref.get("nearclick.delay", "250"));
    } catch (NumberFormatException ex) {
        delay = 250;
        radiussquared = 7 * 7;
    }
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent e = (MouseEvent)event;
            if ( e.getButton() != MouseEvent.BUTTON1 )
                return;
        int xdiff = e.getPoint().x - mouseDownX;
        int ydiff = e.getPoint().y - mouseDownY;
        
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                if ( e.getWhen() - mouseDownTime < delay &&
             ( e.getPoint().x != mouseDownX ||
               e.getPoint().y != mouseDownY) &&
             xdiff * xdiff + ydiff * ydiff < radiussquared
           ) {
                    try {
                        Robot r = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
                        r.mousePress(MouseEvent.BUTTON1_MASK);
                        r.mouseRelease(MouseEvent.BUTTON1_MASK);
                    } catch (AWTException e1) {
                    }
                }                
            }
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                mouseDownX = e.getPoint().x;
                mouseDownY = e.getPoint().y;
                mouseDownTime = e.getWhen();            
            }
        }
    }
}
