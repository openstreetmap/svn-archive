package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JoinNodeWayAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/*
 * The AddStopPositionAction is a mapmode that allows users to add
 * new stop_positions or to convert already existing nodes.
 */
@SuppressWarnings("serial")
public class AddStopPositionAction extends MapMode {

	private transient Set<OsmPrimitive> newHighlights = new HashSet<>();
	private transient Set<OsmPrimitive> oldHighlights = new HashSet<>();

    private final Cursor cursorJoinNode;
    private final Cursor cursorJoinWay;

	public AddStopPositionAction() {
		super(tr("Add stop position"),
				"bus",
				tr("Add stop position"),
				Shortcut.registerShortcut("mapmode:stop_position",
                        tr("Mode: {0}", tr("Add stop position")),
                        KeyEvent.VK_K, Shortcut.CTRL_SHIFT),
				getCursor());

		cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        cursorJoinWay = ImageProvider.getCursor("crosshair", "joinway");
	}

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", "bus");
        } catch (Exception e) {
            Main.error(e);
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
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

    @Override
    public void mouseMoved (MouseEvent e) {

    	//while the mouse is moving, surroundings are checked
    	//if anything is found, it will be highlighted.
    	//priority is given to nodes
    	Cursor newCurs = getCursor();

    	Node n = Main.map.mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
    	if(n != null) {
    		newHighlights.add(n);
    		newCurs = cursorJoinNode;
    	} else {
    		List<WaySegment> wss =
    				Main.map.mapView.getNearestWaySegments(e.getPoint(), OsmPrimitive::isSelectable);

    		if(wss.size() > 0) {
	    		for(WaySegment ws : wss) {
	    			newHighlights.add(ws.way);
	    		}
	    		newCurs = cursorJoinWay;
    		}
		}

    	Main.map.mapView.setCursor(newCurs);
    	updateHighlights();
    }

    @Override
    public void mouseClicked (MouseEvent e) {

    	Boolean newNode = false;
    	Node newStopPos;

    	//check if the user as selected an existing node, or a new one
    	Node n = Main.map.mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
        if (n == null) {
        	newNode = true;
        	newStopPos = new Node(Main.map.mapView.getLatLon(e.getX(), e.getY()));
        } else {
        	newStopPos = new Node(n);
            clearNodeTags(newStopPos);
        }

        //add the tags of the stop position
    	newStopPos.put("bus", "yes");
    	newStopPos.put("public_transport", "stop_position");

    	if(newNode) {
    		Main.main.undoRedo.add(new AddCommand(newStopPos));
    	} else {
    		Main.main.undoRedo.add(new ChangeCommand(n, newStopPos));
    	}

    	DataSet ds = Main.getLayerManager().getEditLayer().data;
    	ds.setSelected(newStopPos);

    	//join the node to the way only if the node is new
    	if(newNode) {
	        JoinNodeWayAction joinNodeWayAction = JoinNodeWayAction.createJoinNodeToWayAction();
	        joinNodeWayAction.actionPerformed(null);
    	}

        // split the way in any case
        SplitWayAction splitWayAction = new SplitWayAction();
        splitWayAction.actionPerformed(null);
    }

    private void clearNodeTags(Node newStopPos) {
		for(String key : newStopPos.keySet()) {
			newStopPos.put(key, null);
		}

	}

	//turn off what has been highlighted on last mouse move and highlight what has to be highlighted now
    private void updateHighlights()
    {
    	if(oldHighlights.size() > 0 || newHighlights.size() > 0) {

    		for(OsmPrimitive osm : oldHighlights) {
        		osm.setHighlighted(false);
        	}

        	for(OsmPrimitive osm : newHighlights) {
        		osm.setHighlighted(true);
        	}

    		Main.getLayerManager().getEditLayer().invalidate();

        	oldHighlights.clear();
        	oldHighlights.addAll(newHighlights);
        	newHighlights.clear();
    	}
    }

}
