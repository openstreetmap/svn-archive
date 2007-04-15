package nearclick;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

public class NearClickPlugin implements AWTEventListener {

    private int mouseDownX = -1;
    private int mouseDownY = -1;
    private long mouseDownTime = -1;

    public NearClickPlugin() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent e = (MouseEvent)event;
            if ( e.getButton() != MouseEvent.BUTTON1 )
                return;
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                if ( e.getWhen() - mouseDownTime < 250 &&
                        e.getPoint().x - mouseDownX < 7 &&
                        e.getPoint().y - mouseDownY < 7 &&
                        e.getPoint().x != mouseDownX &&
                        e.getPoint().y != mouseDownY ) {
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
