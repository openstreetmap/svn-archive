// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann
package org.openstreetmap.fma.jtiledownloader.views.main.slippymap;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * This class controls the user input by listening to mouse and key events.
 * Currently implemented is: - zooming in and out with scrollwheel - zooming in
 * and centering by double clicking - selecting an area by clicking and dragging
 * the mouse
 * 
 * @author Tim Haussmann
 */
public class OsmMapControl extends MouseAdapter implements MouseMotionListener, MouseListener {

    /** A Timer for smoothly moving the map area */
    private static final Timer timer = new Timer(true);

    /** Does the moving */
    private MoveTask moveTask = new MoveTask();

    /** How often to do the moving (milliseconds) */
    private static long timerInterval = 20;

    /** The maximum speed (pixels per timer interval) */
    private static final double MAX_SPEED = 20;

    /** The speed increase per timer interval when a cursor button is clicked */
    private static final double ACCELERATION = 0.10;

    // start and end point of selection rectangle
    private Point iStartSelectionPoint;
    private Point iEndSelectionPoint;

    // the SlippyMapChooserComponent
    private final SlippyMapChooser iSlippyMapChooser;

    private SourceButton iSourceButton = null;

    /**
     * Create a new OsmMapControl
     */
    public OsmMapControl(SlippyMapChooser navComp, JPanel contentPane, SourceButton sourceButton) {
        this.iSlippyMapChooser = navComp;
        iSlippyMapChooser.addMouseListener(this);
        iSlippyMapChooser.addMouseMotionListener(this);

        String[] n = { ",", ".", "up", "right", "down", "left" };
        int[] k = { KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT };

        if (contentPane != null) {
            for (int i = 0; i < n.length; ++i) {
                contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        KeyStroke.getKeyStroke(k[i], KeyEvent.CTRL_DOWN_MASK), "MapMover.Zoomer." + n[i]);
            }
        }
        iSourceButton = sourceButton;

        InputMap inputMap = navComp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = navComp.getActionMap();

        // map moving
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "MOVE_RIGHT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "MOVE_LEFT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "MOVE_UP");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "MOVE_DOWN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "STOP_MOVE_HORIZONTALLY");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "STOP_MOVE_HORIZONTALLY");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "STOP_MOVE_VERTICALLY");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "STOP_MOVE_VERTICALLY");

        // zooming. To avoid confusion about which modifier key to use,
        // we just add all keys left of the space bar
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK, false), "ZOOM_IN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_DOWN_MASK, false), "ZOOM_IN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK, false), "ZOOM_IN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false), "ZOOM_OUT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_DOWN_MASK, false), "ZOOM_OUT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK, false), "ZOOM_OUT");

        // action mapping
        actionMap.put("MOVE_RIGHT", new MoveXAction(1));
        actionMap.put("MOVE_LEFT", new MoveXAction(-1));
        actionMap.put("MOVE_UP", new MoveYAction(-1));
        actionMap.put("MOVE_DOWN", new MoveYAction(1));
        actionMap.put("STOP_MOVE_HORIZONTALLY", new MoveXAction(0));
        actionMap.put("STOP_MOVE_VERTICALLY", new MoveYAction(0));
        actionMap.put("ZOOM_IN", new ZoomInAction());
        actionMap.put("ZOOM_OUT", new ZoomOutAction());
    }

    /**
     * Start drawing the selection rectangle if it was the 1st button (left
     * button)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            iStartSelectionPoint = e.getPoint();
            iEndSelectionPoint = e.getPoint();
        }

    }

    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
            if (iStartSelectionPoint != null) {
                iEndSelectionPoint = e.getPoint();
                iSlippyMapChooser.setSelection(iStartSelectionPoint, iEndSelectionPoint);
            }
        }
    }

    /**
     * When dragging the map change the cursor back to it's pre-move cursor. If
     * a double-click occurs center and zoom the map on the clicked location.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {

            int sourceButton = iSourceButton.hit(e.getPoint());

            if (sourceButton == SourceButton.HIDE_OR_SHOW) {
                iSourceButton.toggle();
                iSlippyMapChooser.repaint();

            } else if (sourceButton == SourceButton.MAPNIK || sourceButton == SourceButton.OSMARENDER
                    || sourceButton == SourceButton.CYCLEMAP) {
                iSlippyMapChooser.toggleMapSource(sourceButton);
            } else {
                if (e.getClickCount() == 1) {
                    iSlippyMapChooser.setSelection(iStartSelectionPoint, e.getPoint());

                    // reset the selections start and end
                    iEndSelectionPoint = null;
                    iStartSelectionPoint = null;
                }
            }

        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    private class MoveXAction extends AbstractAction {

        int direction;

        public MoveXAction(int direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            moveTask.setDirectionX(direction);
        }
    }

    private class MoveYAction extends AbstractAction {

        int direction;

        public MoveYAction(int direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            moveTask.setDirectionY(direction);
        }
    }

    /** Moves the map depending on which cursor keys are pressed (or not) */
    private class MoveTask extends TimerTask {
        /** The current x speed (pixels per timer interval) */
        private double speedX = 1;

        /** The current y speed (pixels per timer interval) */
        private double speedY = 1;

        /** The horizontal direction of movement, -1:left, 0:stop, 1:right */
        private int directionX = 0;

        /** The vertical direction of movement, -1:up, 0:stop, 1:down */
        private int directionY = 0;

        /**
         * Indicated if <code>moveTask</code> is currently enabled (periodically
         * executed via timer) or disabled
         */
        protected boolean scheduled = false;

        protected void setDirectionX(int directionX) {
            this.directionX = directionX;
            updateScheduleStatus();
        }

        protected void setDirectionY(int directionY) {
            this.directionY = directionY;
            updateScheduleStatus();
        }

        private void updateScheduleStatus() {
            boolean newMoveTaskState = !(directionX == 0 && directionY == 0);

            if (newMoveTaskState != scheduled) {
                scheduled = newMoveTaskState;
                if (newMoveTaskState)
                    timer.schedule(this, 0, timerInterval);
                else {
                    // We have to create a new instance because rescheduling a
                    // once canceled TimerTask is not possible
                    moveTask = new MoveTask();
                    cancel(); // Stop this TimerTask
                }
            }
        }

        @Override
        public void run() {
            // update the x speed
            switch (directionX) {
            case -1:
                if (speedX > -1)
                    speedX = -1;
                if (speedX > -1 * MAX_SPEED)
                    speedX -= ACCELERATION;
                break;
            case 0:
                speedX = 0;
                break;
            case 1:
                if (speedX < 1)
                    speedX = 1;
                if (speedX < MAX_SPEED)
                    speedX += ACCELERATION;
                break;
            }

            // update the y speed
            switch (directionY) {
            case -1:
                if (speedY > -1)
                    speedY = -1;
                if (speedY > -1 * MAX_SPEED)
                    speedY -= ACCELERATION;
                break;
            case 0:
                speedY = 0;
                break;
            case 1:
                if (speedY < 1)
                    speedY = 1;
                if (speedY < MAX_SPEED)
                    speedY += ACCELERATION;
                break;
            }

            // move the map
            int moveX = (int) Math.floor(speedX);
            int moveY = (int) Math.floor(speedY);
            if (moveX != 0 || moveY != 0)
                iSlippyMapChooser.moveMap(moveX, moveY);
        }
    }

    private class ZoomInAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            iSlippyMapChooser.zoomIn();
        }
    }

    private class ZoomOutAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            iSlippyMapChooser.zoomOut();
        }
    }

}
