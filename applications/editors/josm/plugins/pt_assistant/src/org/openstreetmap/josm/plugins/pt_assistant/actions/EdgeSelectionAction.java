// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The action allows to select a set of consecutive ways at once in order to
 * speed up the mapper. The selected ways are going to be coherent to the
 * current route the mapper is working on.
 *
 * @author giacomo
 */
public class EdgeSelectionAction extends MapMode {

    private static final String mapModeName = "Edge Selection";
    private static final long serialVersionUID = 2414977774504904238L;

    private transient Set<Way> highlighted;

    private Cursor selectionCursor;
    private Cursor waySelectCursor;

    public EdgeSelectionAction() {
        super(tr(mapModeName), "edgeSelection", tr(mapModeName),
                Shortcut.registerShortcut("mapmode:edge_selection",
                        tr("Mode: {0}", tr(mapModeName)),
                        KeyEvent.VK_K, Shortcut.CTRL),
                ImageProvider.getCursor("normal", "selection"));
        highlighted = new HashSet<>();

        selectionCursor = ImageProvider.getCursor("normal", "selection");
        waySelectCursor = ImageProvider.getCursor("normal", "select_way");
    }

    /*
     * given a way, it looks at both directions for good candidates to be added
     * to the edge
     */
    private List<Way> getEdgeFromWay(Way initial, String modeOfTravel) {
        List<Way> edge = new ArrayList<>();
        if(!isWaySuitableForMode(initial, modeOfTravel))
            return edge;

        Way curr = initial;
        while(true) {
            List<Way> options = curr.firstNode(true).getParentWays();
            options.remove(curr);
            curr = chooseBestWay(options, modeOfTravel);
            if(curr == null || edge.contains(curr))
                break;
            edge.add(curr);
        }

        curr = initial;
        while(true) {
            List<Way> options = curr.lastNode(true).getParentWays();
            options.remove(curr);
            curr = chooseBestWay(options, modeOfTravel);
            if(curr == null || edge.contains(curr))
                break;
            edge.add(curr);
        }

        edge.add(initial);
        return edge;
    }

    private Boolean isWaySuitableForMode(Way toCheck, String modeOfTravel) {
        if("bus".equals(modeOfTravel))
            return RouteUtils.isWaySuitableForBuses(toCheck);

        return RouteUtils.isWaySuitableForPublicTransport(toCheck);
    }

    /*
     *
     */
    private Way chooseBestWay(List<Way> ways, String modeOfTravel) {
        ways.removeIf(w -> !isWaySuitableForMode(w, modeOfTravel));
        if(ways.isEmpty())
            return null;
        if(ways.size() == 1)
            return ways.get(0);

        Way theChoosenOne = null;

        if("bus".equals(modeOfTravel))
        {

        }
        if("tram".equals(modeOfTravel))
        {

        }

        return theChoosenOne;
    }

    private String getModeOfTravel() {
        //find a way to get the currently opened relation editor and get the
        //from there the current type of route
        return "bus";
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        DataSet ds = Main.getLayerManager().getEditLayer().data;
        Way initial = Main.map.mapView.getNearestWay(e.getPoint(), OsmPrimitive::isUsable);
        if(initial != null){
            ds.setSelected(getEdgeFromWay(initial, getModeOfTravel()));
        }
        else
            ds.clearSelection();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);

        for(Way way : highlighted)
            way.setHighlighted(false);
        highlighted.clear();

        Way initial = Main.map.mapView.getNearestWay(e.getPoint(), OsmPrimitive::isUsable);
        if(initial == null) {
            Main.map.mapView.setCursor(selectionCursor);
        }
        else {
            Main.map.mapView.setCursor(waySelectCursor);
            highlighted.addAll(getEdgeFromWay(initial, getModeOfTravel()));
        }

        for(Way way : highlighted)
            way.setHighlighted(true);
    }

    @Override
    public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
    }
}
