// License: GPL. For details, see LICENSE file.
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;
import org.openstreetmap.josm.plugins.elevation.IElevationModel;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationWayPointKind;

/**
 * Provides the panel showing the elevation profile.
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public class ElevationProfilePanel extends JPanel implements ComponentListener, MouseMotionListener {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -7343429725259575319L;
    private static final int BOTTOM_TEXT_Y_OFFSET = 7;

    private IElevationModel model;
    private Rectangle plotArea;
    private final IElevationProfileRenderer renderer = new DefaultElevationProfileRenderer();
    private int selectedIndex = -1;
    private final List<IElevationProfileSelectionListener> selectionChangedListeners = new ArrayList<IElevationProfileSelectionListener>();
    private boolean isPainting;
    private int step = 0;

    /**
     * Constructs a new ElevationProfilePanel with the given elevation profile.
     * @param profile The elevation profile to show in the panel.
     */
    public ElevationProfilePanel(IElevationModel profile) {
        super();
        this.model = profile;
        setDoubleBuffered(true);
        setBackground(Color.WHITE);
        createOrUpdatePlotArea();
        addComponentListener(this);
        addMouseMotionListener(this);

        Font lFont = getFont().deriveFont(9.0f);
        setFont(lFont);
    }

    /**
     * Gets the elevation profile instance.
     * @return
     */
    public IElevationModel getProfile() {
        return model;
    }

    /**
     * Sets the new elevation profile instance.
     * @param model
     */
    public void setElevationModel(IElevationModel model) {
        if (this.model != model) {
            this.model = model;
            invalidate();
        }
    }

    /**
     * Gets the plot area coordinates.
     * @return
     */
    public Rectangle getPlotArea() {
        return plotArea;
    }

    /**
     * Sets the plot area coordinates.
     * @param plotArea
     */
    public void setPlotArea(Rectangle plotArea) {
        this.plotArea = plotArea;
    }

    /**
     * Gets the selected index of the bar.
     * @return
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selected index of the bar.
     * @param selectedIndex
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;

        if (model != null) {
            model.setCurrentProfile(selectedIndex);
        }
    }

    /**
     * Gets the selected (highlighted) way point.
     * @return The selected way point or null, if no way point is selected.
     */
    public WayPoint getSelectedWayPoint() {
        if (model == null) return null;

        IElevationProfile profile = model.getCurrentProfile();

        int selWp = this.selectedIndex * step;
        if (profile != null && profile.getWayPoints() != null && selWp > 0 && profile.getWayPoints().size() > selWp) {
            return profile.getWayPoints().get(selWp);
        } else {
            return null;
        }
    }

    /**
     * Adds a selection listener.
     * @param listener The listener instance to add.
     */
    public void addSelectionListener(IElevationProfileSelectionListener listener) {
        if (listener == null) return;

        selectionChangedListeners.add(listener);
    }

    /**
     * Removes a selection listener from the list.
     * @param listener The listener instance to remove.
     */
    public void removeSelectionListener(IElevationProfileSelectionListener listener) {
        if (listener == null) return;

        selectionChangedListeners.remove(listener);
    }

    /**
     * Removes all selection listeners.
     */
    public void removeAllSelectionListeners() {
        selectionChangedListeners.clear();
    }

    protected void fireSelectionChanged(WayPoint selWayPoint) {
        for (IElevationProfileSelectionListener listener : selectionChangedListeners) {
            listener.selectedWayPointChanged(selWayPoint);
        }
    }

    @Override
    public void paint(Graphics g) {
        isPainting = true;

        try {
            super.paint(g);
            createOrUpdatePlotArea();
            int y1 = getPlotBottom();

            g.setColor(Color.DARK_GRAY);
            g.drawLine(plotArea.x, plotArea.y, plotArea.x, plotArea.y
                    + plotArea.height);
            g.drawLine(plotArea.x, plotArea.y + plotArea.height, plotArea.x
                    + plotArea.width, plotArea.y + plotArea.height);


            if (model != null) {
                IElevationProfile profile = model.getCurrentProfile();
                if (profile != null && profile.hasElevationData()) {
                    // Draw start and end date
                    drawAlignedString(formatDate(profile.getStart()), 5, y1 + BOTTOM_TEXT_Y_OFFSET,
                            TextAlignment.Left, g);
                    drawAlignedString(formatDate(profile.getEnd()),
                            getPlotRight(), y1 + BOTTOM_TEXT_Y_OFFSET, TextAlignment.Right, g);

                    // Show SRTM indicator
                    if (ElevationHelper.hasSrtmData(profile.getBounds())) {
                        String txt = "SRTM";
                        drawAlignedString(txt, getPlotHCenter(), y1 + BOTTOM_TEXT_Y_OFFSET, TextAlignment.Centered, g);
                    }
                    drawProfile(g);
                    drawElevationLines(g);
                } else {
                    // No profile or profile supports no elevation data
                    drawAlignedString(tr("(No elevation data)"), getPlotHCenter(),
                            getPlotVCenter(), TextAlignment.Centered, g);
                }
            }
        } finally {
            isPainting = false;
        }
    }

    /**
     * Draw a string with a specified alignment.
     * @param s The text to display.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param align The text alignment.
     * @param g The graphics context.
     * @return The resulting rectangle of the drawn string.
     */
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

    /**
     * Draw a string which is horizontally centered around (x,y).
     * @param s The text to display.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param g The graphics context.
     * @return The resulting rectangle of the drawn string.

    private void drawHCenteredString(String s, int x, int y, Graphics g) {
        drawAlignedString(s, x, y, TextAlignment.Centered, g);
    }*/

    /**
     * Formats the date in a predefined manner: "21. Oct 2010, 12:10".
     * @param date
     * @return
     */
    private String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("d MMM yy, HH:mm");

        return formatter.format(date);
    }

    /**
     * Helper function to draw elevation axes.
     * @param g
     */
    private void drawElevationLines(Graphics g) {
        IElevationProfile profile = model.getCurrentProfile();

        double diff = profile.getHeightDifference();

        if (diff == 0.0) {
            return;
        }

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
                String txt = ElevationHelper.getElevationText(i);

                Rectangle r = drawAlignedString(txt, getPlotHCenter(), yLine - 2,
                        TextAlignment.Right, g);
                r.grow(2, 2);

                // Draw left and right line segment
                g.drawLine(getPlotLeftAxis(), yLine, r.x,
                        yLine);
                g.drawLine(r.x + r.width, yLine, getPlotRight(),
                        yLine);
                // Draw label with shadow
                g.setColor(Color.WHITE);
                drawAlignedString(txt, getPlotHCenter() + 1, yLine - 1,
                        TextAlignment.Right, g);
                g.setColor(Color.BLACK);
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

        IElevationProfile profile = model.getCurrentProfile();

        if (!profile.hasElevationData()) {
            return y1;
        }

        double diff = profile.getHeightDifference();

        return y1 - (int) Math.round(((elevation - profile.getMinHeight()) / diff * plotArea.height));
    }

    /**
     * Draws the elevation profile
     * 
     * @param g
     */
    private void drawProfile(Graphics g) {
        IElevationProfile profile = model.getCurrentProfile();

        int nwp = profile.getNumberOfWayPoints();
        int n = Math.min(plotArea.width, nwp);

        if (n == 0) return; // nothing to draw
        // compute step size in panel (add 1 to make sure that
        // the complete range fits into panel
        step = (nwp / n) + 1;

        int yBottom = getPlotBottom();
        Color oldC = g.getColor();

        for (int i = 0, ip = 0; i < n && ip < nwp; i++, ip += step) {
            WayPoint wpt = profile.getWayPoints().get(ip);
            int eleVal = (int) ElevationHelper.getElevation(wpt);
            Color c = renderer.getColorForWaypoint(profile, wpt,
                    ElevationWayPointKind.Plain);

            // draw cursor
            if (i == this.selectedIndex) {
                g.setColor(Color.BLACK);
                drawAlignedString(ElevationHelper.getElevationText(eleVal),
                        (getPlotRight() + getPlotLeft()) / 2,
                        getPlotBottom() + 6,
                        TextAlignment.Centered,
                        g);

                c = renderer.getColorForWaypoint(profile, wpt, ElevationWayPointKind.Highlighted);
            }

            int yEle = getYForEelevation(eleVal);
            int x = getPlotLeft() + i;

            g.setColor(c);
            g.drawLine(x, yBottom, x, yEle);
            g.setColor(ElevationColors.EPLightBlue);
        }

        g.setColor(oldC);
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);

        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        this.setBorder(loweredbevel);
    }

    /**
     * Determines the size of the plot area depending on the panel size.
     */
    private void createOrUpdatePlotArea() {
        Dimension caSize = getSize();

        if (plotArea == null) {
            plotArea = new Rectangle(0, 0, caSize.width, caSize.height);
        } else {
            plotArea.width = caSize.width;
            plotArea.height = caSize.height;
        }

        plotArea.setLocation(0, 0);
        plotArea.grow(-10, -15);
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        createOrUpdatePlotArea();
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        if (isPainting || arg0.isControlDown() || arg0.isAltDown() || arg0.isShiftDown()) arg0.consume();

        int x = arg0.getX();
        int l = this.getX();
        int pl = this.getPlotLeft();
        int newIdx = x - l - pl;

        if (newIdx != this.selectedIndex && newIdx >= 0) {
            this.selectedIndex = newIdx;
            this.repaint();
            fireSelectionChanged(getSelectedWayPoint());
        }
    }

    @Override
    public String getToolTipText() {
        WayPoint wpt = getSelectedWayPoint();
        if (wpt != null) {
            return  String.format("%s: %s", ElevationHelper.getTimeText(wpt), ElevationHelper.getElevationText(wpt));
        }

        return super.getToolTipText();
    }
}
