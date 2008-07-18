// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSelection;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * JComponent that displays the slippy map tiles 
 * @author Tim Haussmann
 *
 */
public class SlippyMapChooser extends JComponent implements DownloadSelection{
	
	private static final int MAX_ZOOMLEVEL = 20;
	private static final int MIN_ZOOMLEVEL = 1;

	private TileDB iTileDB;
	
	private DownloadDialog iGui;
	
	//the upper left and lower right corners of the selection rectangle
	Point iSelectionRectStart;
	Point iSelectionRectEnd;
	
	//Offsets for x and y (i.e. to center the first tile)
	int iOffsetX = 0;
	int iOffsetY = 0;
	
	//the zoom level of the currently shown tiles
	static int iZoomlevel = 3;
	
	private boolean iFirstPaint = true;
	private LatLon  iFirstPaintCenter = new LatLon(51,7);
	
	private SizeButton iSizeButton = new SizeButton();
	
	//standard dimension 
	private Dimension iDownloadDialogDimension;
	//screen size
	private Dimension iScreenSize;
	
	private LatLon iScreenCenterBeforeResize;
	private LatLon iSelectionStartBeforeResize;
	private LatLon iSelectionEndBeforeResize;
	private boolean isJustResized = false;
	
	private int iVisibleTilesX = 2;
	private int iVisibleTilesY = 3;
	
	/**
	 * Create the chooser component.
	 */
	public SlippyMapChooser() {	
				
		//create the tile db
		iTileDB = new TileDB(this);
		
		
	
		setMinimumSize(new Dimension(350, 350/2));
	}

	public void addGui(final DownloadDialog gui) {
		iGui = gui;
		JPanel temp = new JPanel();
		temp.setLayout(new BorderLayout());
		temp.add(this, BorderLayout.CENTER);
		temp.add(new JLabel((tr("Zoom: Mousewheel or double click.   Move map: Hold right mousebutton and move mouse.   Select: Click."))), BorderLayout.SOUTH);
		iGui.tabpane.add(temp, tr("Slippy map"));
		
		new OsmMapControl(this,temp, iSizeButton);
		repaint();
    }
	
	/**
	 * Performs resizing of the DownloadDialog in order to enlarge or shrink the map. 
	 */
	public void resizeSlippyMap(){
		if(iScreenSize == null){
			Component c = iGui.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent();
			//remember the initial set screen dimensions
			iDownloadDialogDimension = c.getSize();
			//retrive the size of the display
			iScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		}
		
		//remember the screen center (we want to have the same center after resizing)
		iScreenCenterBeforeResize = getLatLonOfScreenPoint(new Point(getWidth()/2, getHeight()/2));
		
		//remember lat/lon of the selection rectangle 
		if(iSelectionRectEnd != null && iSelectionRectStart != null){
			iSelectionStartBeforeResize = getLatLonOfScreenPoint(iSelectionRectStart);
			iSelectionEndBeforeResize   = getLatLonOfScreenPoint(iSelectionRectEnd);
		}
		
		//resize
		Component co = iGui.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent();
		Dimension currentDimension = co.getSize();
		
		//enlarge
		if(currentDimension.equals(iDownloadDialogDimension)){
			//make the each dimension 90% of the absolute display size and center the DownloadDialog
			int w = iScreenSize.width*90/100;
			int h = iScreenSize.height*90/100;
			co.setBounds((iScreenSize.width-w)/2, (iScreenSize.height-h)/2, w, h);
		}
		//shrink
		else{
			//set the size back to the initial dimensions and center the DownloadDialog
			int w = iDownloadDialogDimension.width;
			int h = iDownloadDialogDimension.height;
			co.setBounds((iScreenSize.width-w)/2, (iScreenSize.height-h)/2, w, h);
		}
		
		//the new dimension are 'available' after (or while) the next repaint
		isJustResized = true;
		
		repaint();
	}

	/**
	 * Draw the map.
	 */
	@Override public void paint(Graphics g) {
		
		if(iFirstPaint){
			//calculate numbers of visible tiles 			
			calcVisibleTiles();	
			
			//save selection
			LatLon selStart = null;
			LatLon selEnd   = null;
			if(iSelectionRectEnd != null && iSelectionRectStart != null){
				selStart = getLatLonOfScreenPoint(iSelectionRectStart);
				selEnd   = getLatLonOfScreenPoint(iSelectionRectEnd);
			}			
			centerOnLatLon(iFirstPaintCenter);	
			//restore selection 
			if(selStart != null && selEnd != null){
				iSelectionRectStart = getScreenPointForLatLon(selStart);
				iSelectionRectEnd   = getScreenPointForLatLon(selEnd);
			}
			
			loadVisibleTiles();
			iFirstPaint = false;
			repaint();
		}
		
		if(isJustResized){
			centerOnLatLon(iScreenCenterBeforeResize);
			//restore selection 
			if(iSelectionEndBeforeResize != null && iSelectionStartBeforeResize != null){
				iSelectionRectStart = getScreenPointForLatLon(iSelectionStartBeforeResize);
				iSelectionRectEnd   = getScreenPointForLatLon(iSelectionEndBeforeResize);
			}
			
			//calculate numbers of visible tiles 			
			calcVisibleTiles();			
			
			loadVisibleTiles();
			isJustResized = false;
		}
			
		//translate origin to draw map tiles in 'map space'
		g.translate(iOffsetX, iOffsetY);
		
		//draw tiles of the current zoomlevel
		for(int y=0; y<iVisibleTilesY; y++){
			for(int x=0; x<iVisibleTilesX; x++){					
				OsmTile t = iTileDB.get(OsmTile.key(iZoomlevel,  (-iOffsetX + x*OsmTile.WIDTH)/OsmTile.WIDTH, ((-iOffsetY+ y*OsmTile.HEIGHT)/OsmTile.HEIGHT)));
				if(t != null){
					t.paint(g,iTileDB);						
				}
			} 
		}		
		
		//translate origin back
		g.translate(-iOffsetX, -iOffsetY);
		
		//draw selection rectangle
		if(iSelectionRectStart != null && iSelectionRectEnd != null){
			
			g.setColor(Color.black);
			g.drawRect(iSelectionRectStart.x, iSelectionRectStart.y, iSelectionRectEnd.x -iSelectionRectStart.x, iSelectionRectEnd.y -iSelectionRectStart.y);
		
			g.setColor(new Color(0.9f,0.7f,0.7f,0.6f));
			g.fillRect(iSelectionRectStart.x+1, iSelectionRectStart.y+1, iSelectionRectEnd.x -iSelectionRectStart.x-1, iSelectionRectEnd.y -iSelectionRectStart.y-1);
		}
		
		iSizeButton.paint(g);
		
		
		if(SlippyMapChooserPlugin.DEBUG_MODE){
			g.setColor(Color.black);
			g.drawString("Free Memory: " + Runtime.getRuntime().freeMemory()/1024 + "/" + Runtime.getRuntime().totalMemory()/1024 + "kB" , 5, 50);
			g.drawString("Tiles in DB: " + iTileDB.getCachedTilesSize(), 5, 65);
			g.drawString("Loading Queue Size: " + iTileDB.getLoadingQueueSize(), 5, 80);
			
		}
	}

	
	public void boundingBoxChanged(DownloadDialog gui) {
		
		//calc the screen coordinates for the new selection rectangle
		int x1 = OsmMercator.LonToX(gui.minlon, iZoomlevel);
		int x2 = OsmMercator.LonToX(gui.maxlon, iZoomlevel);
		int y1 = OsmMercator.LatToY(gui.minlat, iZoomlevel);
		int y2 = OsmMercator.LatToY(gui.maxlat, iZoomlevel);
		
		iSelectionRectStart = new Point(Math.min(x1, x2)+iOffsetX, Math.min(y1, y2)+iOffsetY);
		iSelectionRectEnd   = new Point(Math.max(x1, x2)+iOffsetX, Math.max(y1, y2)+iOffsetY);
		
		//calc zoom level 	
		double dLat = Math.abs(gui.maxlat - gui.minlat);
		double dLon = Math.abs(gui.maxlon - gui.minlon);		
		int zoomLat = (int) (Math.log(90 / dLat)/Math.log(2));
		int zoomLon = (int) (Math.log(90 / dLon)/Math.log(2));		
		iZoomlevel = Math.max(zoomLat, zoomLon);
		
		//center on the rectangle
		if(gui.minlat != 0 && gui.maxlat != 0 && gui.minlon != 0 && gui.maxlat != 0){
			iFirstPaintCenter = new LatLon((gui.minlat + gui.maxlat)/2, (gui.minlon + gui.maxlon)/2);
			iFirstPaint = true;
		}
		repaint();
	}
	
	/**
	 * Loads all tiles that are visible
	 */
	void loadVisibleTiles(){	
		for(int y=iVisibleTilesY-1; y>=0; y--){
			for(int x=0; x<iVisibleTilesX; x++){					
				if(y > 0 && y < iVisibleTilesX-2){
					iTileDB.loadTile(iZoomlevel, (-iOffsetX + x*OsmTile.WIDTH)/OsmTile.WIDTH,((-iOffsetY+ y*OsmTile.HEIGHT)/OsmTile.HEIGHT), TileDB.PRIO_HIGH );
				}else{
					iTileDB.loadTile(iZoomlevel, (-iOffsetX + x*OsmTile.WIDTH)/OsmTile.WIDTH,((-iOffsetY+ y*OsmTile.HEIGHT)/OsmTile.HEIGHT), TileDB.PRIO_LOW );
				}
			} 
		}		
	}

	/**
	 * Callback for the OsmMapControl. (Re-)Sets the start and end point of the selection rectangle.
	 * @param aStart  
	 * @param aEnd
	 */
	void setSelection(Point aStart, Point aEnd){
		if(aStart == null || aEnd == null)
			return;
		iSelectionRectEnd = new Point(Math.max(aEnd.x , aStart.x ) ,Math.max(aEnd.y, aStart.y ));
		iSelectionRectStart = new Point(Math.min(aEnd.x , aStart.x ) ,Math.min(aEnd.y , aStart.y ));
		
		LatLon l1 = getLatLonOfScreenPoint(aStart);	
		LatLon l2 = getLatLonOfScreenPoint(aEnd);
		
		iGui.minlat = Math.min(l1.lat(), l2.lat());
		iGui.minlon = Math.min(l1.lon(), l2.lon());
		iGui.maxlat = Math.max(l1.lat(), l2.lat());
		iGui.maxlon = Math.max(l1.lon(), l2.lon());
		
		iGui.boundingBoxChanged(this);
		
		repaint();
	}
	
	/**
	 * Callback for OsmMapControll. Moves the map and the selection rectangle.
	 * @param x number of pixels to move along the x-axis (longitude)
	 * @param y number of pixels to move along the y axis (latitude)
	 */
	void moveMap(int x, int y) {
		int moveX = x;
		int moveY = y;
		
		int tempOffsetX = iOffsetX - x;
		int tempOffsetY = iOffsetY - y;
		
		
		int maxPixels = OsmMercator.getMaxPixels(iZoomlevel);
		
		if(moveX != 0){
			//deactivate dragging if the map is smaller than the DownloadDialog
			if(maxPixels < getWidth()){
				//center map horizontally
				iOffsetX = (getWidth()-maxPixels)/2;	
				moveX = 0;
			}else{
				//don't allow the map to hide outside the JComponent drawing area
				if(tempOffsetX > 30){
					if(moveX < 0){
						moveX = 0;
						iOffsetX = 30;
					}
				}else if(-tempOffsetX > maxPixels + 30 - getWidth()){
					if(moveX > 0){
						moveX = 0;
						iOffsetX = -(maxPixels + 30 - getWidth());
					}
				}
			}
		}
		
		if(moveY != 0){
			//deactivate dragging if the map is smaller than the DownloadDialog
			if(maxPixels < getHeight()){
				//center map vertically
				iOffsetY = (getHeight()-maxPixels)/2;
				moveY = 0;
			}else{
				//don't allow the map to hide outside the JComponent drawing area
				if(tempOffsetY > 30){
					if(moveY < 0){
						moveY = 0;
						iOffsetY = 30;
					}
				}else if(-tempOffsetY > maxPixels + 30 - getHeight()){
					if(moveY > 0){
						moveY = 0;
						iOffsetY = -(maxPixels + 30 - getHeight());
					}
				}
			}
		}
		
		
		
		
		
		//execute the movement
		iOffsetX -= moveX;
		iOffsetY -= moveY;
		
		//also move the selection rect
		if(iSelectionRectEnd != null && iSelectionRectStart != null){
			iSelectionRectEnd.x   -= moveX;
			iSelectionRectEnd.y   -= moveY;
			iSelectionRectStart.x -= moveX;
			iSelectionRectStart.y -= moveY;
		}
		
		loadVisibleTiles();
		
		repaint();
	}
	
	/**
	 * Zoom in one level	
	 * Callback for OsmMapControl. Zoom out one level		 
	 */
	void zoomIn(Point curPos){	
		
		//cache center of screen and the selection rectangle
		LatLon l = getLatLonOfScreenPoint(curPos);
		LatLon selStart = null;
		LatLon selEnd   = null;
		if(iSelectionRectEnd != null && iSelectionRectStart != null){
			selStart = getLatLonOfScreenPoint(iSelectionRectStart);
			selEnd   = getLatLonOfScreenPoint(iSelectionRectEnd);
		}
		
		//increment zoom level
		iZoomlevel += 1;
		if(iZoomlevel > MAX_ZOOMLEVEL){
			iZoomlevel = MAX_ZOOMLEVEL;
			return;
		}
					
		setLatLonAtPoint(l, curPos);
		
		//restore selection 
		if(selStart != null && selEnd != null){
			iSelectionRectStart = getScreenPointForLatLon(selStart);
			iSelectionRectEnd   = getScreenPointForLatLon(selEnd);
		}
		
		loadVisibleTiles();
		
		centerMap();
		
		repaint();
	}
	
	/**
	 * Zoom out one level.
	 * Callback for OsmMapControl. 
	 */
	void zoomOut(Point curPos){
		//cache center of screen and the selction rect
		LatLon l = getLatLonOfScreenPoint(curPos);
		LatLon selStart = null;
		LatLon selEnd   = null;
		if(iSelectionRectEnd != null && iSelectionRectStart != null){
			selStart = getLatLonOfScreenPoint(iSelectionRectStart);
			selEnd   = getLatLonOfScreenPoint(iSelectionRectEnd);
		}
		
		//decrement zoom level
		iZoomlevel -= 1;
		if(iZoomlevel < MIN_ZOOMLEVEL){
			iZoomlevel = MIN_ZOOMLEVEL;
			return;
		}
		
		setLatLonAtPoint(l, curPos);
		
		//restore selection 
		if(selStart != null && selEnd != null){
			iSelectionRectStart = getScreenPointForLatLon(selStart);
			iSelectionRectEnd   = getScreenPointForLatLon(selEnd);
		}
		
		loadVisibleTiles();
		
		centerMap();
		
		repaint();
	}
	
	/**
	 * Calculates the latitude and longitude of a Point given in map space
	 * @param aPoint in pixels on the map
	 * @return the LatLon of the given Point
	 */
	private LatLon getLatLonOfPoint(Point aPoint){	
		return new LatLon(OsmMercator.YToLat(aPoint.y, iZoomlevel),OsmMercator.XToLon(aPoint.x,	iZoomlevel));
	}
	
	/**
	 * Returns the map coordinates for a LatLon
	 * @param aLatLon 
	 * @return
	 */
	private Point getPointForLatLon(LatLon aLatLon){
		Point p = new Point();
		p.y = OsmMercator.LatToY(aLatLon.lat(), iZoomlevel);
		p.x = OsmMercator.LonToX(aLatLon.lon(), iZoomlevel);
		return p;
	}
	
	/**
	 * Calculates the latitude and longitude of a Point given in screen space (in the JComponent)
	 * @param aPoint in Pixels on the screen (the JComponent)
	 * @return the LatLon of the given Point
	 */
	LatLon getLatLonOfScreenPoint(Point aPoint){
		Point mapCoordinates = new Point(aPoint);		
		mapCoordinates.x -= iOffsetX;
		mapCoordinates.y -= iOffsetY;			
		return getLatLonOfPoint(mapCoordinates);
	}
	
	/**
	 * Calculates the screen coordinates of a LatLon.
	 * @param aLatLon 
	 * @return the coordinates as Point in the JComponent 
	 */
	Point getScreenPointForLatLon(LatLon aLatLon){
		Point p = getPointForLatLon(aLatLon);
		p.x += iOffsetX;
		p.y += iOffsetY;
		return p;
	}
	
	
	/**
	 * Centers the map on a Point 
	 * @param aPoint in screen coordinates (JComponent)
	 */
	void centerOnScreenPoint(Point aPoint){
		moveMap(aPoint.x - getWidth()/2, aPoint.y-getHeight()/2);
	}
	
	/**
	 * Centers the map on the location given by LatLon
	 * @param aLatLon the location to center on
	 */
	private void centerOnLatLon(LatLon aLatLon){
		setLatLonAtPoint(aLatLon, new Point(getWidth()/2,getHeight()/2));
	}

	/**
	 * Moves the map that the specified latLon is shown at the point on screen
	 * given
	 * @param aLatLon a position
	 * @param p a point on the screen
	 */
	private void setLatLonAtPoint(LatLon aLatLon, Point p){
		int x = OsmMercator.LonToX(aLatLon.lon(), iZoomlevel);
		int y = OsmMercator.LatToY(aLatLon.lat(), iZoomlevel);
		iOffsetX = - x + p.x;
		iOffsetY = - y + p.y;
		repaint();
	}
	
	/**
	 * Caclulates the visible tiles for each dimension
	 */
	private void calcVisibleTiles(){
		if(iGui != null){
			iVisibleTilesX = iGui.getWidth()/256 + 2;
			iVisibleTilesY = iGui.getHeight()/256 + 2;
		}
	}
	
	/**
	 * Centers the map after zooming. I.e. when the map is smaller than the
	 * component it is shown in.
	 */
	private void centerMap(){
		
		int maxPixels = OsmMercator.getMaxPixels(iZoomlevel);		
	
		if(maxPixels < getWidth()){
			//center map horizontally
			iOffsetX = (getWidth()-maxPixels)/2;
		}
		if(maxPixels < getHeight()){
			//center map vertically
			iOffsetY = (getHeight()-maxPixels)/2;
		}
	}
}
