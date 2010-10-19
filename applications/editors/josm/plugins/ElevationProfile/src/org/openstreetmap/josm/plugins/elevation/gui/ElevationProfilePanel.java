package org.openstreetmap.josm.plugins.elevation.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.elevation.ElevationWayPointKind;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.WayPointHelper;

public class ElevationProfilePanel extends JPanel implements ComponentListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -7343429725259575319L;
	private IElevationProfile profile;
	private Rectangle plotArea;
	private IElevationProfileRenderer renderer = new DefaultElevationProfileRenderer();

	public ElevationProfilePanel(IElevationProfile profile) {
		super();
		this.profile = profile;
		setDoubleBuffered(true);
		setBackground(Color.WHITE);
		createOrUpdatePlotArea();
		addComponentListener(this);
	}

	public IElevationProfile getProfile() {
		return profile;
	}

	public void setElevationModel(IElevationProfile profile) {
		this.profile = profile;
	}

	public Rectangle getPlotArea() {
		return plotArea;
	}

	public void setPlotArea(Rectangle plotArea) {
		this.plotArea = plotArea;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		int y1 = getPlotBottom();

		g.setColor(Color.DARK_GRAY);
		g.drawLine(plotArea.x, plotArea.y, plotArea.x, plotArea.y
				+ plotArea.height);
		g.drawLine(plotArea.x, plotArea.y + plotArea.height, plotArea.x
				+ plotArea.width, plotArea.y + plotArea.height);

		Font oldFont = getFont();
		Font lFont = getFont().deriveFont(9.0f);
		setFont(lFont);
		try {
			if (profile != null) {
				drawAlignedString(formatDate(profile.getStart()), 5, y1 + 5,
						TextAlignment.Left, g);
				drawAlignedString(formatDate(profile.getEnd()),
						getPlotRight(), y1 + 5, TextAlignment.Right, g);
				
				
				drawProfile(g);
				drawElevationLines(g);
				drawHCenteredString(profile.getName(), getPlotHCenter(),
						getPlotTop() + 2, g);
			} else {
				drawAlignedString(tr("(No elevation data)"), getPlotHCenter(),
						getPlotVCenter(), TextAlignment.Centered, g);
			}
		} finally {
			setFont(oldFont);
		}
	}

	private Rectangle drawAlignedString(String s, int x, int y,
			TextAlignment align, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		int h = fm.getHeight();

		int xoff = w / 2;
		int yoff = h / 2;

		if (align == TextAlignment.Left) {
			xoff = 0;
		}
		if (align == TextAlignment.Right) {
			xoff = w;
		}

		g.drawString(s, x - xoff, y + yoff);

		return new Rectangle(x - xoff, y - yoff, w, h);
	}

	private void drawHCenteredString(String s, int x, int y, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int xoff = fm.stringWidth(s) / 2;
		// int yoff = (fm.getAscent() + fm.getDescent()) / 2;

		g.drawString(s, x - xoff, y);
	}

	private String formatDate(Date date) {
		Format formatter = new SimpleDateFormat("d MMM yy, HH:mm");

		return formatter.format(date);
	}

	private void drawElevationLines(Graphics g) {
		double diff = profile.getHeightDifference();

		double z10 = Math.floor(Math.log10(diff));
		double scaleUnit = Math.pow(10, z10); // scale unit, e. g. 100 for
		// values below 1000

		int upperLimit = (int) (Math.round(Math.ceil(profile.getMaxHeight()
				/ scaleUnit)) * scaleUnit);
		int lowerLimit = (int) (Math.round(Math.floor(profile.getMinHeight()
				/ scaleUnit)) * scaleUnit);
		int su = (int) scaleUnit;

		for (int i = lowerLimit; i <= upperLimit; i += su) {
			int yLine = getYForEelevation(i);

			// check bounds
			if (yLine <= getPlotBottom() && yLine >= getPlotTop()) {
				String txt = String.format("%dm", i);

				g.setColor(Color.BLACK);
				Rectangle r = drawAlignedString(txt, getPlotHCenter(), yLine - 2,
						TextAlignment.Right, g);
				r.grow(2, 2);
				
				// Draw left and right line segment
				g.drawLine(getPlotLeftAxis(), yLine, r.x,
						yLine);
				g.drawLine(r.x + r.width, yLine, getPlotRight(),
						yLine);				
				// Draw label
				drawAlignedString(txt, getPlotHCenter(), yLine - 2,
						TextAlignment.Right, g);
			}
		}
	}
	
	/**
	 * Gets the x value of the left border for axes (slightly smaller than the
	 * left x).
	 * 
	 * @return
	 */
	private int getPlotLeftAxis() {
		return plotArea.x - 3;
	}

	/**
	 * Gets the x value of the left border.
	 * 
	 * @return
	 */
	private int getPlotLeft() {
		return plotArea.x + 1;
	}

	/**
	 * Gets the horizontal center coordinate (mid between left and right x).
	 * 
	 * @return
	 */
	private int getPlotHCenter() {
		return (getPlotLeft() + getPlotRight()) / 2;
	}

	/**
	 * Gets the vertical center coordinate (mid between top and bottom y).
	 * 
	 * @return
	 */
	private int getPlotVCenter() {
		return (getPlotTop() + getPlotBottom()) / 2;
	}

	/**
	 * Gets the x value of the right border.
	 * 
	 * @return
	 */
	private int getPlotRight() {
		return plotArea.x + plotArea.width - 1;
	}

	private int getPlotBottom() {
		return plotArea.y + plotArea.height - 1;
	}

	private int getPlotTop() {
		return plotArea.y + 1;
	}

	/**
	 * Gets for an elevation value the according y coordinate in the plot area.
	 * 
	 * @param elevation
	 * @return The y coordinate in the plot area.
	 */
	private int getYForEelevation(int elevation) {
		int y1 = getPlotBottom();

		if (!profile.hasElevationData()) {
			return y1;
		}

		double diff = profile.getHeightDifference();

		return y1
				- (int) Math
						.round(((elevation - profile.getMinHeight()) / diff * plotArea.height));
	}

	/**
	 * Draws the elevation profile
	 * 
	 * @param g
	 */
	private void drawProfile(Graphics g) {
		int n = Math.min(plotArea.width, profile.getNumberOfWayPoints());

		// int y0 = plotArea.y + 1;
		int yBottom = getPlotBottom();
		Color oldC = g.getColor();

		for (int i = 0; i < n; i++) {
			WayPoint wpt = profile.getWayPoints().get(i);
			int eleVal = (int) WayPointHelper.getElevation(wpt);
			Color c = renderer.getColorForWaypoint(profile, wpt,
					ElevationWayPointKind.Plain);
			int yEle = getYForEelevation(eleVal);
			int x = getPlotLeft() + i;

			g.setColor(c);
			g.drawLine(x, yBottom, x, yEle);	
			
			int geoidVal = 0;
			switch(WayPointHelper.getGeoidKind()) {
			case Auto: geoidVal = WayPointHelper.getGeoidCorrection(wpt); break;
			case Fixed: // not impl
			}
			g.setColor(ElevationColors.EPLightBlue);
			
			int yGeoid = getYForEelevation(eleVal - geoidVal);
			g.drawLine(x, Math.min(yGeoid, yBottom), x, yEle);
		}
		g.setColor(oldC);
	}

	@Override
	protected void paintBorder(Graphics g) {
		super.paintBorder(g);

		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		this.setBorder(loweredbevel);
	}

	private void createOrUpdatePlotArea() {
		Dimension caSize = getSize();

		if (plotArea == null) {
			plotArea = new Rectangle(0, 0, caSize.width, caSize.height);
		} else {
			plotArea.width = caSize.width;
			plotArea.height = caSize.height;
		}

		plotArea.setLocation(10, 0);
		plotArea.grow(-20, -15);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentResized(ComponentEvent arg0) {
		createOrUpdatePlotArea();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

}
