// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SelectByInternalPointAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Extends current selection by selecting nodes on all touched ways
 */
public class SelectBoundaryAction extends JosmAction {
    private Way lastUsedStartingWay; //used for repeated calls
    private boolean lastUsedLeft;

    public SelectBoundaryAction() {
        super(tr("Area boundary [testing]"), "selboundary", tr("Select relation or all ways that forms area boundary"),
                Shortcut.registerShortcut("tools:selboundary", tr("Tool: {0}", "Area boundary [testing]"),
                        KeyEvent.VK_SLASH, Shortcut.SHIFT), true);
        putValue("help", ht("/Action/SelectAreaBoundary"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getActiveDataSet();
        if (ds != null) {
            Collection<Way> selectedWays = ds.getSelectedWays();
            Collection<Node> selectedNodes = ds.getSelectedNodes();

            Set<Way> newWays = new HashSet<>();

            Way w = null;

            if (selectedWays.isEmpty()) {
                if (selectedNodes.size() == 1) {
                    for (OsmPrimitive p : selectedNodes.iterator().next().getReferrers()) {
                        if (p instanceof Way && p.isSelectable()) {
                            w = (Way) p;
                            break;
                        }
                    }
                } else if (MainApplication.isDisplayingMapView()) {
                    MapView mapView = MainApplication.getMap().mapView;
                    Point p = mapView.getMousePosition();
                    if (p != null) {
                        SelectByInternalPointAction.performSelection(mapView.getEastNorth(p.x, p.y), false, false);
                    }
                    return;
                }
            } else if (selectedWays.size() == 1) {
                w = selectedWays.iterator().next();
            } else if (selectedWays.contains(lastUsedStartingWay)) {
                w = lastUsedStartingWay; //repeated call for selected way
                lastUsedLeft = !lastUsedLeft;
            }

            if (w == null) return; //no starting way found
            if (!w.isSelectable()) return;
            if (w.isClosed()) return;
            if (w.getNodesCount() < 2) return;

            newWays.add(w);
            lastUsedStartingWay = w;

            // try going left at each turn
            if (!NodeWayUtils.addAreaBoundary(w, newWays, lastUsedLeft)) {
                NodeWayUtils.addAreaBoundary(w, newWays, !lastUsedLeft); // try going right at each turn
            }

            if (!newWays.isEmpty()) {
                ds.setSelected(newWays);
            } else {
                new Notification(tr("Nothing found. Please select way that is a part of some polygon formed by connected ways"))
                .setIcon(JOptionPane.WARNING_MESSAGE).show();
            }
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getActiveDataSet() != null);
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
