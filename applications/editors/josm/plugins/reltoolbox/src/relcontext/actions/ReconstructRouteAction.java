// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * Build in order stop/platforms, stop/platforms ... route
 * @author freeExec
 */
public class ReconstructRouteAction extends AbstractAction implements ChosenRelationListener {
    private final ChosenRelation rel;

    public ReconstructRouteAction(ChosenRelation rel) {
        super(tr("Reconstruct route"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "filter"));
        putValue(LONG_DESCRIPTION, "Reconstruct route relation to scheme of public_transport");
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(isSuitableRelation(rel.get()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Relation r = rel.get();
        Relation recRel = new Relation(r);
        recRel.removeMembersFor(recRel.getMemberPrimitives());

        Map<OsmPrimitive, RelationMember> stopMembers = new LinkedHashMap<>();
        Map<String, List<RelationMember>> platformMembers = new LinkedHashMap<>();

        List<RelationMember> routeMembers = new ArrayList<>();
        List<RelationMember> wtfMembers = new ArrayList<>();

        int mCount = r.getMembersCount();
        for (int i = 0; i < mCount; i++) {
            RelationMember m = r.getMember(i);
            if (PublicTransportHelper.isMemberStop(m)) {
                RelationMember rm = new RelationMember(
                        m.hasRole() ? m.getRole() : PublicTransportHelper.STOP,
                                m.getMember());
                stopMembers.put(rm.getMember(), rm);
            } else if (PublicTransportHelper.isMemberPlatform(m)) {
                RelationMember rm = new RelationMember(
                        m.hasRole() ? m.getRole() : PublicTransportHelper.PLATFORM,
                                m.getMember());
                String platformName = PublicTransportHelper.getNameViaStoparea(rm);
                if (platformName == null) {
                    platformName = "";
                }
                if (platformMembers.containsKey(platformName)) {
                    platformMembers.get(platformName).add(rm);
                } else {
                    List<RelationMember> nList = new ArrayList<>();
                    nList.add(rm);
                    platformMembers.put(platformName, nList);
                }
            } else if (PublicTransportHelper.isMemberRouteway(m)) {
                routeMembers.add(new RelationMember(m));
            } else {
                wtfMembers.add(new RelationMember(m));
            }
        }

        routeMembers = RelationSorter.sortMembersByConnectivity(routeMembers);

        Node lastNode = null;
        for (int rIndex = 0; rIndex < routeMembers.size(); rIndex++) {
            Way w = (Way) routeMembers.get(rIndex).getMember();
            boolean dirForward = false;
            if (lastNode == null) { // first segment
                if (routeMembers.size() > 2) {
                    Way nextWay = (Way) routeMembers.get(rIndex + 1).getMember();
                    if (w.lastNode().equals(nextWay.lastNode()) || w.lastNode().equals(nextWay.firstNode())) {
                        dirForward = true;
                        lastNode = w.lastNode();
                    } else {
                        lastNode = w.firstNode();
                    }
                } // else one segment - direction unknown
            } else {
                if (lastNode.equals(w.firstNode())) {
                    dirForward = true; lastNode = w.lastNode();
                } else {
                    lastNode = w.firstNode();
                }
            }
            final int wayNodeBeginIndex = (dirForward ? 0 : w.getNodesCount() - 1);
            final int wayNodeEndIndex = (dirForward ? w.getNodesCount() - 1 : 0);
            final int increment = (dirForward ? 1 : -1);
            for (int nIndex = wayNodeBeginIndex;
                    nIndex != wayNodeEndIndex;
                    nIndex += increment) {
                Node refNode = w.getNode(nIndex);
                if (PublicTransportHelper.isNodeStop(refNode)) {
                    if (stopMembers.containsKey(refNode)) {
                        recRel.addMember(stopMembers.get(refNode));
                        stopMembers.remove(refNode);
                        String stopName = PublicTransportHelper.getNameViaStoparea(refNode);
                        if (stopName == null) {
                            stopName = "";
                        }
                        boolean existsPlatform = platformMembers.containsKey(stopName);
                        if (!existsPlatform) {
                            stopName = ""; // find of the nameless
                        }
                        if (existsPlatform || platformMembers.containsKey(stopName)) {
                            List<RelationMember> lMember = platformMembers.get(stopName);
                            if (lMember.size() == 1) {
                                recRel.addMember(lMember.get(0));
                                lMember.remove(0);
                            } else {
                                // choose closest
                                RelationMember candidat = getClosestPlatform(lMember, refNode);
                                if (candidat != null) {
                                    recRel.addMember(candidat);
                                    lMember.remove(candidat);
                                }
                            }
                            if (lMember.isEmpty()) {
                                platformMembers.remove(stopName);
                            }
                        }
                    }
                }
            }
        }

        for (RelationMember stop : stopMembers.values()) {
            recRel.addMember(stop);
            String stopName = PublicTransportHelper.getNameViaStoparea(stop);
            boolean existsPlatform = platformMembers.containsKey(stopName);
            if (!existsPlatform) {
                stopName = ""; // find of the nameless
            }
            if (existsPlatform || platformMembers.containsKey(stopName)) {
                List<RelationMember> lMember = platformMembers.get(stopName);
                if (lMember.size() == 1) {
                    recRel.addMember(lMember.get(0));
                    lMember.remove(0);
                } else {
                    // choose closest
                    RelationMember candidat = getClosestPlatform(lMember, stop.getNode());
                    if (candidat != null) {
                        recRel.addMember(candidat);
                        lMember.remove(candidat);
                    }
                }
                if (lMember.isEmpty()) {
                    platformMembers.remove(stopName);
                }
            }
        }

        for (List<RelationMember> lPlatforms : platformMembers.values()) {
            for (RelationMember platform : lPlatforms) {
                recRel.addMember(platform);
            }
        }

        for (RelationMember route : routeMembers) {
            recRel.addMember(route);
        }
        for (RelationMember wtf : wtfMembers) {
            recRel.addMember(wtf);
        }
        Command command = new ChangeCommand(r, recRel);
        UndoRedoHandler.getInstance().add(command);
    }

    private static final double maxSqrDistBetweenStopAndPlatform = 2000; // ~ 26m
    private RelationMember getClosestPlatform(List<RelationMember> members, Node stop) {
        if (stop == null || members.isEmpty()) return null;
        double maxDist = maxSqrDistBetweenStopAndPlatform;
        RelationMember result = null;
        for (RelationMember member : members) {
            if (member.getType() == OsmPrimitiveType.NODE) {
                Node node = member.getNode();
                double sqrDist = stop.getEastNorth().distanceSq(node.getEastNorth());
                if (sqrDist < maxDist) {
                    maxDist = sqrDist;
                    result = member;
                }
            } else if (member.getType() == OsmPrimitiveType.WAY) {
                Way way = member.getWay();
                EastNorth closest = Geometry.closestPointToSegment(
                        way.firstNode().getEastNorth(),
                        way.lastNode().getEastNorth(),
                        stop.getEastNorth()
                        );
                double sqrDist = stop.getEastNorth().distanceSq(closest);
                if (sqrDist < maxDist) {
                    maxDist = sqrDist;
                    result = member;
                }
            }
        }
        return result;
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(isSuitableRelation(newRelation));
    }

    private boolean isSuitableRelation(Relation newRelation) {
        return !(newRelation == null || !"route".equals(newRelation.get("type")) || newRelation.getMembersCount() == 0);
    }
}
