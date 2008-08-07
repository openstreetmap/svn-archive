// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSelection;

/**
 * JComponent that displays the slippy map tiles
 * 
 * @author Tim Haussmann
 * 
 */
public class SlippyMapChooser extends JMapViewer implements DownloadSelection {

	private DownloadDialog iGui;

	// upper left and lower right corners of the selection rectangle (x/y on
	// ZOOM_MAX)
	Point iSelectionRectStart;
	Point iSelectionRectEnd;

	private SizeButton iSizeButton = new SizeButton();

	// standard dimension
	private Dimension iDownloadDialogDimension;
	// screen size
	private Dimension iScreenSize;

	/**
	 * Create the chooser component.
	 */
	public SlippyMapChooser() {
		super();
		setZoomContolsVisible(false);
		setMapMarkerVisible(false);
		setMinimumSize(new Dimension(350, 350 / 2));
	}

	public void addGui(final DownloadDialog gui) {
		iGui = gui;
		JPanel temp = new JPanel();
		temp.setLayout(new BorderLayout());
		temp.add(this, BorderLayout.CENTER);
		temp.add(new JLabel((tr("Zoom: Mousewheel or double click.   "
				+ "Move map: Hold right mousebutton and move mouse.   Select: Click."))),
				BorderLayout.SOUTH);
		iGui.tabpane.add(temp, tr("Slippy map"));

		new OsmMapControl(this, temp, iSizeButton);
		boundingBoxChanged(gui);
	}

	protected Point getTopLeftCoordinates() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	/**
	 * Draw the map.
	 */
	@Override
	public void paint(Graphics g) {
		try {
			super.paint(g);

			// draw selection rectangle
			if (iSelectionRectStart != null && iSelectionRectEnd != null) {

				int zoomDiff = MAX_ZOOM - zoom;
				Point tlc = getTopLeftCoordinates();
				int x_min = (iSelectionRectStart.x >> zoomDiff) - tlc.x;
				int y_min = (iSelectionRectStart.y >> zoomDiff) - tlc.y;
				int x_max = (iSelectionRectEnd.x >> zoomDiff) - tlc.x;
				int y_max = (iSelectionRectEnd.y >> zoomDiff) - tlc.y;

				int w = x_max - x_min;
				int h = y_max - y_min;
				g.setColor(new Color(0.9f, 0.7f, 0.7f, 0.6f));
				g.fillRect(x_min, y_min, w, h);

				g.setColor(Color.BLACK);
				g.drawRect(x_min, y_min, w, h);

			}

			iSizeButton.paint(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void boundingBoxChanged(DownloadDialog gui) {

		// test if a bounding box has been set set
		if (gui.minlat == 0.0 && gui.minlon == 0.0 && gui.maxlat == 0.0 && gui.maxlon == 0.0)
			return;

		int y1 = OsmMercator.LatToY(gui.minlat, MAX_ZOOM);
		int y2 = OsmMercator.LatToY(gui.maxlat, MAX_ZOOM);
		int x1 = OsmMercator.LonToX(gui.minlon, MAX_ZOOM);
		int x2 = OsmMercator.LonToX(gui.maxlon, MAX_ZOOM);

		iSelectionRectStart = new Point(Math.min(x1, x2), Math.min(y1, y2));
		iSelectionRectEnd = new Point(Math.max(x1, x2), Math.max(y1, y2));

		// calc the screen coordinates for the new selection rectangle
		MapMarkerDot xmin_ymin = new MapMarkerDot(gui.minlat, gui.minlon);
		MapMarkerDot xmax_ymax = new MapMarkerDot(gui.maxlat, gui.maxlon);

		Vector<MapMarker> marker = new Vector<MapMarker>(2);
		marker.add(xmin_ymin);
		marker.add(xmax_ymax);
		setMapMarkerList(marker);
		setDisplayToFitMapMarkers();
		zoomOut();
	}

	/**
	 * Callback for the OsmMapControl. (Re-)Sets the start and end point of the
	 * selection rectangle.
	 * 
	 * @param aStart
	 * @param aEnd
	 */
	public void setSelection(Point aStart, Point aEnd) {
		if (aStart == null || aEnd == null)
			return;
		Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y, aStart.y));
		Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y, aStart.y));

		Point tlc = getTopLeftCoordinates();
		int zoomDiff = MAX_ZOOM - zoom;
		Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
		Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);

		pEnd.x <<= zoomDiff;
		pEnd.y <<= zoomDiff;
		pStart.x <<= zoomDiff;
		pStart.y <<= zoomDiff;

		iSelectionRectStart = pStart;
		iSelectionRectEnd = pEnd;

		Point2D.Double l1 = getPosition(p_max);
		Point2D.Double l2 = getPosition(p_min);
		iGui.minlat = Math.min(l2.x, l1.x);
		iGui.minlon = Math.min(l1.y, l2.y);
		iGui.maxlat = Math.max(l2.x, l1.x);
		iGui.maxlon = Math.max(l1.y, l2.y);

		iGui.boundingBoxChanged(this);
		repaint();
	}

	/**
	 * Performs resizing of the DownloadDialog in order to enlarge or shrink the
	 * map.
	 */
	public void resizeSlippyMap() {
		if (iScreenSize == null) {
			Component c =
					iGui.getParent().getParent().getParent().getParent().getParent().getParent()
							.getParent().getParent().getParent();
			// remember the initial set screen dimensions
			iDownloadDialogDimension = c.getSize();
			// retrive the size of the display
			iScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		}

		// resize
		Component co =
				iGui.getParent().getParent().getParent().getParent().getParent().getParent()
						.getParent().getParent().getParent();
		Dimension currentDimension = co.getSize();

		// enlarge
		if (currentDimension.equals(iDownloadDialogDimension)) {
			// make the each dimension 90% of the absolute display size and
			// center the DownloadDialog
			int w = iScreenSize.width * 90 / 100;
			int h = iScreenSize.height * 90 / 100;
			co.setBounds((iScreenSize.width - w) / 2, (iScreenSize.height - h) / 2, w, h);
		}
		// shrink
		else {
			// set the size back to the initial dimensions and center the
			// DownloadDialog
			int w = iDownloadDialogDimension.width;
			int h = iDownloadDialogDimension.height;
			co.setBounds((iScreenSize.width - w) / 2, (iScreenSize.height - h) / 2, w, h);
		}
		repaint();
	}

}
