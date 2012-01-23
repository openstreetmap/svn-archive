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
        // without preserving the angles, i.e. preserving the length.

        Map<Node,EastNorth> calcNodesKeepLength = calculatedNodes;

        // Now we'll proceed with the hypothetical recalculation of the endpoint coordinates
        // following the rule of preserving angles instead. The new nodes will be stored in nodeArr[].
        
        Node[] nodeArr = algnSeg.getSegmentEndPoints().toArray(new Node[2]);

        EastNorth enCalc1 = calcNodesKeepLength.get(nodeArr[0]);
        EastNorth enCalc2 = calcNodesKeepLength.get(nodeArr[1]);
        AlignWaysGeomLine lineKeepLength = new AlignWaysGeomLine(enCalc1.getX(), enCalc1.getY(), enCalc2.getX(), enCalc2.getY());

        recalculateNodesAndValidate(lineKeepLength, nodeArr[0]);
        recalculateNodesAndValidate(lineKeepLength, nodeArr[1]);

    }


    void recalculateNodesAndValidate(AlignWaysGeomLine alignedLineKeepLength, Node endpoint) {

        if (endpoint.getEastNorth().equals(pivot)) {
        	// endpoint is pivot: the coordinates won't change
        	return;
        }
        
        ArrayList<WaySegment> alws = algnSeg.getAdjacentWaySegments(endpoint);
        if (alws.size() <= 2) {
            // We need the intersection point of
            //  - the alignee following the keep length rule
            //  - the adjacent way segment
        	
        	Node adjOther1 = getNonEqualNode(alws.get(0), endpoint);
        	EastNorth enAdjOther1 = adjOther1.getEastNorth();
        	Node adjOther2 = null;
        	EastNorth enAdjOther2 = null;

            if (alws.size() == 2) {
            	adjOther2 = getNonEqualNode(alws.get(1), endpoint);
            	enAdjOther2 = adjOther2.getEastNorth();
            	
            	// In order have a chance to align, (enAdjOther1, enAdjOther2 and endpoint) must be collinear
            	ArrayList<EastNorth> enAdjPts = new ArrayList<EastNorth>(3);
            	enAdjPts.add(enAdjOther1);
            	enAdjPts.add(endpoint.getEastNorth());
            	enAdjPts.add(enAdjOther2);
            	if (!isEnSetCollinear(enAdjPts)) {
            		// Not collinear, no point to proceed
                    alignableStatKeepAngles = AlignableStatus.ALGN_INV_ANGLE_PRESERVING_CONFLICT;
                    return;
            	}
            	
            }

            // Update the calculated node for angle preserving alignment
            AlignWaysGeomPoint isectPnt = alignedLineKeepLength.getIntersection(new AlignWaysGeomLine(enAdjOther1.getX(), enAdjOther1.getY(), 
            																						  endpoint.getEastNorth().getX(), endpoint.getEastNorth().getY()));
            EastNorth enIsectPt = null;
            // If the intersection is null, the adjacent and the alignee are parallel already:
            // there's no need to update this node
            if (isectPnt != null) {
            	enIsectPt = new EastNorth(isectPnt.getX(), isectPnt.getY());       	
                calculatedNodes.put(endpoint, enIsectPt);
            } else if (alignedLineKeepLength.getIntersectionStatus() == IntersectionStatus.LINES_PARALLEL) {
                alignableStatKeepAngles = AlignableStatus.ALGN_INV_ANGLE_PRESERVING_CONFLICT;
            }
            
            // TODO - THIS APPROACH IS FAULTY!
            // 
            // For the case of two adjacent segments with collinear points, the new endpoint may  
            // not fall between enAdjOther1 and enAdjOther2; in this case one of them is redundant 
            // and should be deleted from OSM
            if (alws.size() == 2 && enIsectPt != null) {
            	int middlePtIdx = AlignWaysGeomPoint.getMiddleOf3(
            			new AlignWaysGeomPoint(enIsectPt), 
            			new AlignWaysGeomPoint(enAdjOther1), 
            			new AlignWaysGeomPoint(enAdjOther2));
            	if (middlePtIdx != 0) {
            		EastNorth middlePt = null;
            		switch(middlePtIdx) {
            			case 1:
            				middlePt = enIsectPt;
            				break;
            			case 2:
            				middlePt = enAdjOther1;
            				break;
            			case 3:
            				middlePt = enAdjOther2;
            				break;
            		}
	            	if (middlePt != null) {
	            		double eps = 1E-6;
	                	if (!middlePt.equalsEpsilon(enIsectPt, eps)) {
	                		// Intersection point didn't fall between the two adjacent points; something must go 
	                		if (middlePt.equalsEpsilon(enAdjOther1, eps)) {
	                			// Delete adjOther1
	                			// adjOther1.setDeleted(true);
	                		} else
	                			if (true);
	                			// Delete adjOther2
	                			// adjOther2.setDeleted(true);
	                	}
	            	}
            	}
            }
            
        } else {
        	// angle preserving alignment not possible
            alignableStatKeepAngles = AlignableStatus.ALGN_INV_TOOMANY_CONNECTED_WS;
        }

    }


    private boolean isEnSetCollinear(ArrayList<EastNorth> enAdjPts) {
    	ArrayList<AlignWaysGeomPoint> awAdjPts = new ArrayList<AlignWaysGeomPoint>();
    	
    	for (EastNorth en : enAdjPts) {
    		AlignWaysGeomPoint pt = new AlignWaysGeomPoint(en.getX(), en.getY());
    		awAdjPts.add(pt);
    	}
    	
		return AlignWaysGeomPoint.isSetCollinear(awAdjPts);
	}


	private Node getNonEqualNode(WaySegment waySegment, Node endpoint) {
    	if (waySegment.getFirstNode().equals(endpoint)) {
    		return waySegment.getSecondNode();
    	} else if (waySegment.getSecondNode().equals(endpoint)) {
    		return waySegment.getFirstNode();
    	} else
    		return null;
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
            statMsg = tr("Aligning would result nodes ''outside the world''.\n"
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

        // Check the remainder of the potential problems: N/A

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
