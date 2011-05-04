// License: GPL. Copyright 2011 by Josh Doe and others
// Connects from JOSM menu action to Plugin
package org.openstreetmap.josm.plugins.conflation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.search.SearchAction.SearchMode;
import org.openstreetmap.josm.actions.search.SearchAction.SearchSetting;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Shortcut;

//@SuppressWarnings("serial")
public class ConflationAction extends JosmAction {
    ConflictCollection conflicts;

    public ConflationAction() {
        super(tr("Conflation"), "conflation", tr("Conflation tool for merging data"),
                Shortcut.registerShortcut("tool:conflation", tr("Tool: {0}", tr("Conflation")),
                KeyEvent.VK_C, Shortcut.GROUP_MENU,
                InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK), true);
        //setEnabled(false);
        //DataSet.selListeners.add(this);

        conflicts = new ConflictCollection();
    }

    public void actionPerformed(ActionEvent e) {
        // get list of OsmDataLayers
        List<OsmDataLayer> layerList = null;
        if (Main.map != null && Main.map.mapView != null) {
            layerList = Main.map.mapView.getLayersOfType(OsmDataLayer.class);
        }
        if (layerList == null || layerList.isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent, tr("There are no data layers "
                    + "present. Please open or create at least one data layer and try again."),
                    tr("Cannot perform conflation"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // show options dialog
        ConflationOptionsDialog conflationDialog = new ConflationOptionsDialog(Main.parent, layerList);
        conflationDialog.setVisible(true);

        if (conflationDialog.isCanceled()) {
            return;
        }

        // get layer and filter settings
        final OsmDataLayer refLayer = conflationDialog.getPanel().getRefLayer();
        final OsmDataLayer nonRefLayer = conflationDialog.getPanel().getNonRefLayer();
        SearchSetting refSearchSetting = conflationDialog.getPanel().getRefSearchSetting();
        SearchSetting nonRefSearchSetting = conflationDialog.getPanel().getNonRefSearchSetting();

        // apply filter criteria to each layer
        Collection<OsmPrimitive> refColl = getSelection(refSearchSetting, refLayer.data);
        Collection<OsmPrimitive> nonRefColl = getSelection(nonRefSearchSetting, nonRefLayer.data);

        if (refColl.isEmpty() || nonRefColl.isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent, tr("At least one layer has no primitives given current filter settings."),
                    tr("Cannot perform conflation"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        ArrayList<OsmPrimitive> refList = new ArrayList<OsmPrimitive>(refColl);
        ArrayList<OsmPrimitive> nonRefList = new ArrayList<OsmPrimitive>(nonRefColl);

        int n = refList.size();
        int m = nonRefList.size();
        int maxLen = Math.max(n, m);
        double cost[][] = new double[maxLen][maxLen];

        // calculate cost matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost[i][j] = calcCost(refList.get(i), nonRefList.get(j));
            }
        }

        // perform assignment using Hungarian algorithm
        int[][] assignment = new int[maxLen][2];
        assignment = HungarianAlgorithm.hgAlgorithm(cost, "min");

        // create array of primitives based on indices from assignment
        OsmPrimitive[][] pairs = new OsmPrimitive[maxLen][2];
        for (int i = 0; i < maxLen; i++) {
            if (assignment[i][0] < n)
                pairs[i][0] = refList.get(assignment[i][0]);
            else
                pairs[i][0] = null;
            if (assignment[i][1] < m)
                pairs[i][1] = nonRefList.get(assignment[i][1]);
            else
                pairs[i][1] = null;

            if (pairs[i][0] != null && pairs[i][1] != null) {
                conflicts.add(pairs[i][0], pairs[i][1]);
            }
        }

        // add conflation layer
        try {
            ConflationLayer conflationLayer = new ConflationLayer(refLayer.data, conflicts);
            Main.main.addLayer(conflationLayer);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.parent, ex.toString(),
                    "Error adding conflation layer", JOptionPane.ERROR_MESSAGE);
        }

        // print list of matched pairsalong with distance
        // upon selection of one pair, highlight them and draw arrow


    }

    /**
     * Get selection of primitives for a given layer based on filter settings.
     *
     * @param   s    the given <code>SearchSetting</code>'s.
     * @param   ds   the <code>DataSet</code> of the selected <code>Layer</code>.
     */
    public static Collection<OsmPrimitive> getSelection(SearchSetting s, DataSet ds) {
        Collection<OsmPrimitive> sel = new ArrayList<OsmPrimitive>();
        try {
            String searchText = s.text;
            SearchCompiler.Match matcher = SearchCompiler.compile(searchText, s.caseSensitive, s.regexSearch);

            Collection<OsmPrimitive> all;
            if (s.allElements)
                all = ds.allPrimitives();
            else
                all = ds.allNonDeletedCompletePrimitives();

            if (s.mode != SearchMode.replace)
                sel = ds.getSelected();

            for (OsmPrimitive osm : all) {
                if (s.mode == SearchMode.replace) {
                    if (matcher.match(osm)) {
                        sel.add(osm);
                    }
                } else if (s.mode == SearchMode.add && !ds.isSelected(osm) && matcher.match(osm)) {
                    sel.add(osm);
                } else if (s.mode == SearchMode.remove && ds.isSelected(osm) && matcher.match(osm)) {
                    sel.remove(osm);
                } else if (s.mode == SearchMode.in_selection && ds.isSelected(osm)&& !matcher.match(osm)) {
                    sel.remove(osm);
                }
            }
        } catch (SearchCompiler.ParseError e) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    e.getMessage(),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE

            );
        }
        return sel;
    }

    public static EastNorth getCenter(OsmPrimitive prim) {
            LatLon center = prim.getBBox().getTopLeft().getCenter(prim.getBBox().getBottomRight());
            return Main.map.mapView.getProjection().latlon2eastNorth(center);
    }

    /**
     * Calculate the cost of a pair of <code>OsmPrimitive</code>'s. A
     * simple cost consisting of the Euclidean distance is used
     * now, later we can also use dissimilarity between tags.
     *
     * @param   refPrim      the reference <code>OsmPrimitive</code>.
     * @param   nonRefPrim   the non-reference <code>OsmPrimitive</code>.
     */
    public double calcCost(OsmPrimitive refPrim, OsmPrimitive nonRefPrim) {
        double dist;
        try {
            dist = getCenter(refPrim).distance(getCenter(nonRefPrim));
        } catch (Exception e) {
            dist = 1000; // FIXME: what number to use?
        }

        // TODO: use other "distance" measures, i.e. matching tags
        return dist;
    }
}
