// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JoinNodeWayAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The AddStopPositionAction is a mapmode that allows users to add
 * new stop_positions or to convert already existing nodes.
 *
 * @author giacomo
 */
public class AddStopPositionAction extends MapMode {

    private static final String MAP_MODE_NAME = "Add stop position";

    private transient Set<OsmPrimitive> newHighlights = new HashSet<>();
    private transient Set<OsmPrimitive> oldHighlights = new HashSet<>();

    private final Cursor cursorJoinNode;
    private final Cursor cursorJoinWay;

    /**
     * Creates a new AddStopPositionAction
     */
    public AddStopPositionAction() {
        super(tr(MAP_MODE_NAME), "bus", tr(MAP_MODE_NAME),
                Shortcut.registerShortcut("mapmode:stop_position",
                        tr("Mode: {0}", tr(MAP_MODE_NAME)),
                        KeyEvent.VK_K, Shortcut.CTRL_SHIFT),
                getCursor());

        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        cursorJoinWay = ImageProvider.getCursor("crosshair", "joinway");
    }

    private static Cursor getCursor() {
        Cursor cursor = ImageProvider.getCursor("crosshair", "bus");
        if (cursor == null)
            cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        return cursor;
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
    public void mouseMoved(MouseEvent e) {

        //while the mouse is moving, surroundings are checked
        //if anything is found, it will be highlighted.
        //priority is given to nodes
        Cursor newCurs = getCursor();

        Node n = Main.map.mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
        if (n != null) {
            newHighlights.add(n);
            newCurs = cursorJoinNode;
        } else {
            List<WaySegment> wss =
                    Main.map.mapView.getNearestWaySegments(e.getPoint(), OsmPrimitive::isSelectable);

            if (!wss.isEmpty()) {
                for (WaySegment ws : wss) {
                    newHighlights.add(ws.way);
                }
                newCurs = cursorJoinWay;
            }
        }

        Main.map.mapView.setCursor(newCurs);
        updateHighlights();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

        if (newNode) {
            Main.main.undoRedo.add(new AddCommand(newStopPos));
        } else {
            Main.main.undoRedo.add(new ChangeCommand(n, newStopPos));
            newStopPos = n;
        }

        Main.getLayerManager().getEditLayer().data.setSelected(newStopPos);

        //join the node to the way only if the node is new
        if (newNode) {
            JoinNodeWayAction joinNodeWayAction = JoinNodeWayAction.createMoveNodeOntoWayAction();
            joinNodeWayAction.actionPerformed(null);
        }

        if (newStopPos.getParentWays().isEmpty())
            return;

        Way affected = newStopPos.getParentWays().get(0);
        Map<Relation, Boolean> needPostProcess = getAffectedRelation(affected);

        if (needPostProcess.isEmpty())
            return;

        SplitWayResult result = SplitWayAction.split(getLayerManager().getEditLayer(),
                affected, Collections.singletonList(newStopPos), Collections.emptyList());
        Main.main.undoRedo.add(result.getCommand());

        List<Command> cmds = new ArrayList<>();
        for (Entry<Relation, Boolean> route : needPostProcess.entrySet()) {
            Relation r = new Relation(route.getKey());
            if (route.getValue())
                deleteFirstWay(r);
            else
                deleteLastWay(r);
            cmds.add(new ChangeCommand(route.getKey(), r));
        }
        Main.main.undoRedo.add(new SequenceCommand("Update PT Relations", cmds));
    }

    private void deleteLastWay(Relation r) {
        int delete = 0;
        for (int i = r.getMembersCount() - 1; i >= 0; i--) {
            RelationMember rm = r.getMember(i);
            if (rm.getType() == OsmPrimitiveType.WAY &&
                    !PTStop.isPTPlatform(rm)) {
                delete = i;
                break;
            }
        }
        r.removeMember(delete);
    }

    private void deleteFirstWay(Relation r) {
        int delete = 0;
        for (int i = 0; i < r.getMembersCount(); i++) {
            RelationMember rm = r.getMember(i);
            if (rm.getType() == OsmPrimitiveType.WAY &&
                    !PTStop.isPTPlatform(rm)) {
                delete = i;
                break;
            }
        }
        r.removeMember(delete);
    }

    private Map<Relation, Boolean> getAffectedRelation(Way affected) {
        Map<Relation, Boolean> ret = new HashMap<>();
        for (Relation route : getPTRouteParents(affected)) {
            if (isFirstMember(affected, route)) {
                ret.put(route, true);
            } else if (isLastMember(affected, route)) {
                ret.put(route, false);
            }
        }
        return ret;
    }

    private boolean isFirstMember(Way affected, Relation route) {
        for (int i = 0; i < route.getMembersCount(); i++) {
            RelationMember rm = route.getMember(i);
            if (rm.getMember().equals(affected)) {
                return true;
            } else if (rm.getType() == OsmPrimitiveType.WAY &&
                    !PTStop.isPTPlatform(rm)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLastMember(Way affected, Relation route) {
        for (int i = route.getMembersCount() - 1; i >= 0; i--) {
            RelationMember rm = route.getMember(i);
            if (rm.getMember().equals(affected)) {
                return true;
            } else if (rm.getType() == OsmPrimitiveType.WAY &&
                    !PTStop.isPTPlatform(rm)) {
                return false;
            }
        }

        return true;
    }

    private void clearNodeTags(Node newStopPos) {
        for (String key : newStopPos.keySet()) {
            newStopPos.put(key, null);
        }

    }

    private List<Relation> getPTRouteParents(Way way) {
        List<Relation> referrers = OsmPrimitive.getFilteredList(
                way.getReferrers(), Relation.class);
        referrers.removeIf(r -> !RouteUtils.isPTRoute(r));
        return referrers;
    }

    //turn off what has been highlighted on last mouse move and highlight what has to be highlighted now
    private void updateHighlights() {
        if (oldHighlights.isEmpty() && newHighlights.isEmpty()) {
            return;
        }

        for (OsmPrimitive osm : oldHighlights) {
            osm.setHighlighted(false);
        }

        for (OsmPrimitive osm : newHighlights) {
            osm.setHighlighted(true);
        }

        Main.getLayerManager().getEditLayer().invalidate();

        oldHighlights.clear();
        oldHighlights.addAll(newHighlights);
        newHighlights.clear();
    }
}
