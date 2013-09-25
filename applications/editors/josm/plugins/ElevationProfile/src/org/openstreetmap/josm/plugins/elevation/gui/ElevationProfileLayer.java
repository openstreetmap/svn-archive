/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;
import org.openstreetmap.josm.plugins.elevation.IElevationProfile;
import org.openstreetmap.josm.plugins.elevation.gpx.ElevationWayPointKind;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Layer class to show additional information on the elevation map, e. g. show
 * min/max elevation markers.
 * 
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */
public class ElevationProfileLayer extends Layer implements IElevationProfileSelectionListener {
    
    private static final double Level_Factor = 100.0;
    private IElevationProfile profile;
    private IElevationProfileRenderer renderer = new DefaultElevationProfileRenderer();
    private WayPoint selWayPoint = null;	

    /**
     * Creates a new elevation profile layer
     * 
     * @param name
     *            The name of the layer.
     */
    public ElevationProfileLayer(String name) {
	super(name);
    }

    /**
     * Gets the current elevation profile shown in this layer.
     * 
     * @return
     */
    public IElevationProfile getProfile() {
	return profile;
    }

    /**
     * Sets the current elevation profile shown in this layer.
     * 
     * @param profile
     *            The profile to show in the layer
     */
    public void setProfile(IElevationProfile profile) {
	if (this.profile != profile) {
        	this.profile = profile;
        	Main.map.repaint();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getIcon()
     */
    @Override
    public Icon getIcon() {
	return ImageProvider.get("layer", "elevation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getInfoComponent()
     */
    @Override
    public Object getInfoComponent() {
	return getToolTipText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getMenuEntries()
     */
    @Override
    public Action[] getMenuEntries() {
	// TODO: More entries???
	return new Action[] { new LayerListPopup.InfoAction(this) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getToolTipText()
     */
    @Override
    public String getToolTipText() {
	if (profile != null) {
	    return tr("Elevation profile for track ''{0}''.", profile.getName());
	} else {
	    return tr("Elevation profile");
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openstreetmap.josm.gui.layer.Layer#isMergable(org.openstreetmap.josm
     * .gui.layer.Layer)
     */
    @Override
    public boolean isMergable(Layer other) {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openstreetmap.josm.gui.layer.Layer#mergeFrom(org.openstreetmap.josm
     * .gui.layer.Layer)
     */
    @Override
    public void mergeFrom(Layer from) {
	// nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#paint(java.awt.Graphics2D,
     * org.openstreetmap.josm.gui.MapView, org.openstreetmap.josm.data.Bounds)
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
	WayPoint lastWpt = null;

	renderer.beginRendering();
	
	if (profile != null) {
	    // choose smaller font
	    Font oldFont = g.getFont();
	    Font lFont = g.getFont().deriveFont(9.0f);
	    g.setFont(lFont);

	    try {
		// paint way points one by one
		for (WayPoint wpt : profile.getWayPoints()) {
		    if (lastWpt != null) {
			// determine way point
			ElevationWayPointKind kind = classifyWayPoint(lastWpt, wpt);
			// render way point as line
			renderer.renderLine(g, profile, mv, lastWpt, wpt, kind);
			// render single way point
			renderer.renderWayPoint(g, profile, mv, wpt, kind);
		    } // else first way point -> is paint later

		    // remember last wpt for next iteration
		    lastWpt = wpt;
		}

		// now we paint special way points in emphasized style 

		// paint start/end
		renderer.renderWayPoint(g, profile, mv, profile.getStartWayPoint(),
			ElevationWayPointKind.StartPoint);
		renderer.renderWayPoint(g, profile, mv, profile.getEndWayPoint(),
			ElevationWayPointKind.EndPoint);
		// paint min/max
		renderer.renderWayPoint(g, profile, mv, profile.getMaxWayPoint(),
			ElevationWayPointKind.MaxElevation);
		renderer.renderWayPoint(g, profile, mv, profile.getMinWayPoint(),
			ElevationWayPointKind.MinElevation);


		// paint selected way point, if available
		if (selWayPoint != null) {
		    renderer.renderWayPoint(g, profile, mv, selWayPoint,
			    ElevationWayPointKind.Highlighted);
		}
	    } finally {
		g.setFont(oldFont);
	    } 
	}
	renderer.finishRendering();
    }

    /**
     * Checks if the given way point requires special decoration (e. g. elevation gain/loss or level crossing).
     * 
     * Parameters <tt>ele1</tt> and <tt>ele2</tt> point are used for detecting "level crossings",
     * e. g. 1 to 2 indicate that we crossed the 200m elevation in upward direction
     *
     * @param lastWpt the last way point
     * @param actWpt the actual way point 
     * @return the elevation way point kind
     */
    private ElevationWayPointKind classifyWayPoint(WayPoint lastWpt, WayPoint actWpt) {
	// get elevation values
	int actEle = (int) ElevationHelper.getElevation(actWpt);
	int lastEle = (int) ElevationHelper.getElevation(lastWpt);
	
	// normalize elevation to levels
	int actLevel = (int)(actEle / Level_Factor);
	int lastLevel = (int)(lastEle / Level_Factor);
	double slope = Math.abs(ElevationHelper.computeSlope(lastWpt.getCoor(), actWpt.getCoor()));

	// plain way point by default
	ElevationWayPointKind kind = ElevationWayPointKind.Plain;
	
	// check, if we passed an elevation level
	// We assume, that we cannot pass more than one levels between two way points ;-)
	if (actLevel != lastLevel && Math.abs(actLevel - lastLevel) == 1) { 
	    if (actLevel > lastLevel) { // we went down?		
		kind =ElevationWayPointKind.ElevationLevelGain; 
	    } else {
		kind =ElevationWayPointKind.ElevationLevelLoss;
	    }
	} else { // check for elevation gain or loss
	    if (actEle > lastEle) { // we went uphill?
		// TODO: Provide parameters for high/low thresholds
		if (slope > 2) kind =ElevationWayPointKind.ElevationGainLow;
		if (slope > 15) kind =ElevationWayPointKind.ElevationGainHigh;
	    } else {
		if (slope > 2) kind =ElevationWayPointKind.ElevationLossLow;
		if (slope > 15) kind =ElevationWayPointKind.ElevationLossHigh;
	    }
	}
	return kind;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openstreetmap.josm.gui.layer.Layer#visitBoundingBox(org.openstreetmap
     * .josm.data.osm.visitor.BoundingXYVisitor)
     */
    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
	// What to do here?	
    }

    @Override
    public void selectedWayPointChanged(WayPoint newWayPoint) {
	if (selWayPoint != newWayPoint) {
	    selWayPoint = newWayPoint;
	    Main.map.repaint();
	}
    }
}
