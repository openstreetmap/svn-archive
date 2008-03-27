// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * This class controls the user input by listening to mouse and key events. Currently implemented is:
 * - zooming in and out with scrollwheel
 * - zooming in and centering by double clicking
 * - selecting an area by clicking and dragging the mouse
 * 
 * @author Tim Haussmann
 */
public class OsmMapControl extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

	
	//positions while moving the map
	private int iLastX = 0;
	private int iLastY = 0;
	
	//start and end point of selection rectangle
	private Point iStartSelectionPoint;
	private Point iEndSelectionPoint;

	//the SlippyMapChooserComponent
	private final SlippyMapChooser iSlippyMapChooser;
	
	//The old cursor when we changed it to movement cursor.
	private Cursor iOldCursor;

	private boolean isMovementInPlace = false;
	
	private SizeButton iSizeButton = null;

	
	private final class ZoomerAction extends AbstractAction {
		private final String action;
		public ZoomerAction(String action) {
			this.action = action;
        }
	    public void actionPerformed(ActionEvent e) {
	    	if (action.equals(".") || action.equals(",")) {
	    		Point mouse = iSlippyMapChooser.getMousePosition();
	    		if (mouse == null)
	    			mouse = new Point((int)iSlippyMapChooser.getBounds().getCenterX(), (int)iSlippyMapChooser.getBounds().getCenterY());
	    		MouseWheelEvent we = new MouseWheelEvent(iSlippyMapChooser, e.getID(), e.getWhen(), e.getModifiers(), mouse.x, mouse.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, action.equals(",") ? -1 : 1);
	    		mouseWheelMoved(we);
	    	} else {
	    		if (action.equals(tr("left")))
	    			iSlippyMapChooser.moveMap(-30, 0);
	    		else if (action.equals("right"))
	    			iSlippyMapChooser.moveMap(30, 0);
	    		else if (action.equals("up"))
	    			iSlippyMapChooser.moveMap(0, -30);
	    		else if (action.equals("down"))
	    			iSlippyMapChooser.moveMap(0, 30);
	    	}
	    }
    }
	
	
	
	/**
	 * Create a new OsmMapControl
	 */
	public OsmMapControl(SlippyMapChooser navComp, JPanel contentPane, SizeButton sizeButton) {
		this.iSlippyMapChooser = navComp;
		iSlippyMapChooser.addMouseListener(this);
		iSlippyMapChooser.addMouseMotionListener(this);
		iSlippyMapChooser.addMouseWheelListener(this);
		
		String[] n = {",",".","up","right","down","left"};
		int[] k = {KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT};

		if (contentPane != null) {
			for (int i = 0; i < n.length; ++i) {
				contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(k[i], KeyEvent.CTRL_DOWN_MASK), "MapMover.Zoomer."+n[i]);
				contentPane.getActionMap().put("MapMover.Zoomer."+n[i], new ZoomerAction(n[i]));
			}
		}
		
		iSizeButton = sizeButton;
	}

	/**
	 * If the right mouse button is pressed and moved while holding, move the map.
	 * If the left mouse button is pressed start the selection rectangle and stop when left mouse button is released
	 */
	public void mouseDragged(MouseEvent e) {			
		int offMask = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
		//Moving the map
		if ((e.getModifiersEx() & (MouseEvent.BUTTON3_DOWN_MASK | offMask)) == MouseEvent.BUTTON3_DOWN_MASK) {
			
			startMovement(e);
			
			//calc the moved distance since the last drag event
			int distX = e.getX()-iLastX;
			int distY = e.getY()-iLastY;
			
			//change the offset for the origin of the map
			iSlippyMapChooser.moveMap(-distX, -distY);
			
			//update the last position of the mouse for the next dragging event
			iLastX = e.getX();
			iLastY = e.getY();
		}
		//selecting
		else if((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK){
			iEndSelectionPoint = e.getPoint();
			iSlippyMapChooser.setSelection(iStartSelectionPoint, e.getPoint());
		}		
		//end moving of the map 
		else
			endMovement();
	}

	/**
	 * Start moving the map, if it was the 3rd button (right button).
	 * Start drawing the selection rectangle if it was the 1st button (left button)
	 */
	@Override public void mousePressed(MouseEvent e) {
		int offMask = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
		if (e.getButton() == MouseEvent.BUTTON3 && (e.getModifiersEx() & offMask) == 0)
			startMovement(e);
		else if (e.getButton() == MouseEvent.BUTTON1){
			if(!iSizeButton.hit(e.getPoint())){
				iStartSelectionPoint = e.getPoint();
				iSlippyMapChooser.setSelection(iStartSelectionPoint, iEndSelectionPoint);
			}
		}
	}
	
	
	/**
	 * When dragging the map change the cursor back to it's pre-move cursor.
	 * If a double-click occurs center and zoom the map on the clicked location.
	 */
	@Override public void mouseReleased(MouseEvent e) {		
		if (e.getButton() == MouseEvent.BUTTON3)
			endMovement();
		else if (e.getButton() == MouseEvent.BUTTON1){
			if(iSizeButton.hit(e.getPoint())){
				iSizeButton.toggle();
				iSlippyMapChooser.resizeSlippyMap();
			}else{
				if(e.getClickCount() == 1){
					iSlippyMapChooser.setSelection(iStartSelectionPoint, e.getPoint());
					
					//reset the selections start and end
					iEndSelectionPoint 	 = null;
					iStartSelectionPoint = null;
				}
				else if(e.getClickCount() == 2){
					iSlippyMapChooser.centerOnScreenPoint(e.getPoint());
					iSlippyMapChooser.zoomIn();
				}		
			}
		}			
	}

	/**
	 * Start movement by setting a new cursor and remember the current mouse
	 * position.
	 */
	private void startMovement(MouseEvent e) {
		if (isMovementInPlace)
			return;
		isMovementInPlace = true;
		iLastX = e.getX();
		iLastY = e.getY();
		iOldCursor = iSlippyMapChooser.getCursor();
		iSlippyMapChooser.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}

	/**
	 * End the movement. Setting back the cursor and clear the movement variables
	 */
	private void endMovement() {
		if (!isMovementInPlace)
			return;
		isMovementInPlace = false;
		if (iOldCursor != null)
			iSlippyMapChooser.setCursor(iOldCursor);
		else
			iSlippyMapChooser.setCursor(Cursor.getDefaultCursor());
		iOldCursor = null;
	}

	/**
	 * Zoom the map in and out.
	 * @param e The wheel event.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		//scroll wheel rotated away from user
		if(rot < 0){
			iSlippyMapChooser.zoomIn();
		}
		//scroll wheel rotated towards the user
		else if(rot > 0){
			iSlippyMapChooser.zoomOut();
		}
	}

	/**
	 * Does nothing. Only to satisfy MouseMotionListener
	 */
	public void mouseMoved(MouseEvent e) {}
}
