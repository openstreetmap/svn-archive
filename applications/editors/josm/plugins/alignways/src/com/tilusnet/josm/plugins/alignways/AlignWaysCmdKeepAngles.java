/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.WaySegment;

import com.tilusnet.josm.plugins.alignways.geometry.AlignWaysGeomLine;
import com.tilusnet.josm.plugins.alignways.geometry.AlignWaysGeomLine.IntersectionStatus;
import com.tilusnet.josm.plugins.alignways.geometry.AlignWaysGeomPoint;


/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public class AlignWaysCmdKeepAngles extends AlignWaysCmdKeepLength {

    AlignableStatus alignableStatKeepAngles = AlignableStatus.ALGN_VALID;

    public AlignWaysCmdKeepAngles() {
        super();

        // Now the calculatedNodes reflect the coordinates that we'd have
        // without preserving the angles.

        Map<Node,EastNorth> calcNodesKeepLength = calculatedNodes;

        Node[] nodeArr = algnSeg.getSegmentEndPoints().toArray(new Node[2]);

        EastNorth enCalc1 = calcNodesKeepLength.get(nodeArr[0]);
        EastNorth enCalc2 = calcNodesKeepLength.get(nodeArr[1]);
        AlignWaysGeomLine lineKeepLength = new AlignWaysGeomLine(enCalc1.getX(), enCalc1.getY(), enCalc2.getX(), enCalc2.getY());

        recalculateNodes(lineKeepLength, nodeArr[0]);
        recalculateNodes(lineKeepLength, nodeArr[1]);

    }


    void recalculateNodes(AlignWaysGeomLine alignedLineKeepLength, Node endpoint) {

        ArrayList<WaySegment> alws = algnSeg.getAdjacentWaySegments(endpoint);
        if (alws.size() == 1) {
            // We need the intersection point of
            //  - the freely rotated alignee as calculated in calcNodesFreeAngle
            //  - the adjacent way segment

            EastNorth enAdj1 = alws.get(0).getFirstNode().getEastNorth();
            EastNorth enAdj2 = alws.get(0).getSecondNode().getEastNorth();

            // Update the calculated node for aligning while keeping angles
            AlignWaysGeomPoint isectPnt = alignedLineKeepLength.getIntersection(new AlignWaysGeomLine(enAdj1.getX(), enAdj1.getY(), enAdj2.getX(), enAdj2.getY()));
            // If the intersection is null, the adjacent and the alignee are parallel already:
            // there's no need to update this node
            if (isectPnt != null) {
                calculatedNodes.put(endpoint, new EastNorth(isectPnt.getX(), isectPnt.getY()));
            } else if (alignedLineKeepLength.getIntersectionStatus() == IntersectionStatus.LINES_PARALLEL) {
                alignableStatKeepAngles = AlignableStatus.ALGN_INV_ANGLE_PRESERVING_CONFLICT;
            }
        } else {
            if (!endpoint.getEastNorth().equals(pivot)) {
                // Report non-pivot endpoints only
                alignableStatKeepAngles = AlignableStatus.ALGN_INV_TOOMANY_CONNECTED_WS;
            }
        }

    }


    /**
     * Reports invalid alignable statuses on screen in dialog boxes.
     * 
     * @param stat The invalid status to report
     */
    @Override
    void reportInvalidCommand(AlignableStatus stat) {
        String statMsg;

        switch (stat) {
        case ALGN_INV_CONNECTED_UNSHARED_PIVOT:
            statMsg = tr("Please select two segments that don''t share any nodes.\n"
                    + "Alternatively put the pivot on their common node.\n");
            break;
        case ALGN_INV_OUTSIDE_WORLD:
            statMsg = tr("Aligning would result nodes 'outside the world'.\n"
                    + "Alignment not possible.\n");
            break;
        case ALGN_INV_TOOMANY_CONNECTED_WS:
            statMsg = tr("There is at least a non-pivot endpoint of the alignee that joins more than two way segments.\n"
                    + "Preserved angles type alignment is not possible.\n");
            break;
        case ALGN_INV_ANGLE_PRESERVING_CONFLICT:
            statMsg = tr("The alignment is not possible with maintaining the angles of the joint segments.\n"
                    + "Either choose the ''keep length'' aligning method or select other segments.\n");
            break;
        default:
            statMsg = tr("Undocumented problem occured.\n");
            break;
        }

        JOptionPane.showMessageDialog(
                Main.parent,
                tr(statMsg),
                tr("AlignWayS: Alignment not possible"),
                JOptionPane.WARNING_MESSAGE
                );

    }


    /**
     * Returns true if the two selected segments are alignable.
     * They are not if they are connected *and* the pivot is not the connection
     * node.
     * They are also not if the moving segment endpoints connect to more than
     * one other way segment.
     */
    @Override
    AlignableStatus areSegsAlignable() {
        AlignableStatus status = super.areSegsAlignable();

        // The constructor may already have reported some problems: check.
        if (alignableStatKeepAngles != AlignableStatus.ALGN_VALID)
            return alignableStatKeepAngles;

        // The superclass may have reported problems: check.
        if (status != AlignableStatus.ALGN_VALID)
            return status;

        // Check the remainder of the potential problems: -
        // Check for the number of segments connecting to the alignee endpoints
        for (Node nA : displaceableNodes) {
            if (nA.getEastNorth().equals(pivot)) {
                // Pivots set to endpoints are exempt from this check
                continue;
            } else {
                // This node should not connect to more than one other way segment
                if (isReferredByNOtherWaySegments(nA, 2))
                    return AlignableStatus.ALGN_INV_TOOMANY_CONNECTED_WS;
            }
        }

        // In all other cases alignment is possible
        return AlignableStatus.ALGN_VALID;
    }




    private boolean isReferredByNOtherWaySegments(Node node, int numWSeg) {

        Collection<WaySegment> coll_ws = algnSeg.getAdjacentWaySegments(node);
        if (coll_ws != null)
            return coll_ws.size() >= numWSeg;
            else
                return false;
    }

}
