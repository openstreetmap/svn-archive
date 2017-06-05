package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class EdgeSelectionAction extends MapMode implements MouseListener, MouseMotionListener  {

	private Set<Way> highlighted;
	private Cursor selectionCursor;
	private Cursor waySelectCursor;

	private static final long serialVersionUID = 2414977774504904238L;

	public EdgeSelectionAction() {
		super(tr("Edge Selection"),
				"edgeSelection",
				tr("Edge Selection"),
				Shortcut.registerShortcut("mapmode:edge_selection",
                        tr("Mode: {0}", tr("Edge Selection")),
                        KeyEvent.VK_K, Shortcut.CTRL),
				ImageProvider.getCursor("normal", "selection"));
		highlighted = new HashSet<>();

		selectionCursor = ImageProvider.getCursor("normal", "selection");
		waySelectCursor = ImageProvider.getCursor("normal", "select_way");
	}

    /*
     * given a way, it looks at both directions until it finds a
     * crossway (parents.size > 2) or until the end of the
     * edge (parens.size = 1)
     */
    private List<Way> getEdgeFromWay(Way initial)
    {
    	List<Way> edge = new ArrayList<>();

    	Way curr = initial;
    	while(true) {
    		List<Way> parents = curr.firstNode(true).getParentWays();
    		if(parents.size() != 2)
    			break;
    		curr = parents.get(0) == curr ? parents.get(1) : parents.get(0);
    		edge.add(curr);
    	}

    	curr = initial;
    	while(true) {
    		List<Way> parents = curr.lastNode(true).getParentWays();
    		if(parents.size() != 2)
    			break;
    		curr = parents.get(0) == curr ? parents.get(1) : parents.get(0);
    		edge.add(curr);
    	}

    	edge.add(initial);
    	return edge;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

		DataSet ds = Main.getLayerManager().getEditLayer().data;
    	Way initial = Main.map.mapView.getNearestWay(e.getPoint(), OsmPrimitive::isUsable);
    	if(initial != null)
    		ds.setSelected(getEdgeFromWay(initial));
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
    		highlighted.addAll(getEdgeFromWay(initial));
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
