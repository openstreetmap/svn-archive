package org.openstreetmap.josm.plugins.contourmerge;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.contourmerge.util.Assert;

/**
 * <strong>ContourMergeModel</strong> keeps the current edit state for a specific edit layer,
 * if the <tt>contourmerge</tt> map mode is enabled.</p>
 */
public class ContourMergeModel implements DataSetListener{
	static private final Logger logger = Logger.getLogger(ContourMergeModel.class.getName());
	
	private OsmDataLayer layer;	
	private Node feedbackNode;
	private WaySegment dragStartFeedbackSegment;
	private WaySegment dropFeedbackSegment;
	private final ArrayList<Node> selectedNodes = new ArrayList<Node>();
	private Point dragOffset = null;
	
	/**
	 * <p>Creates a new contour merge model for the layer {@code layer}.</p>
	 * 
	 * @param layer the data layer. Must not be null. 
	 * @throws IllegalArgumentException thrown if {@code layer} is null
	 */
	public ContourMergeModel(OsmDataLayer layer) throws IllegalArgumentException {
		Assert.checkArgNotNull(layer, "layer");
		this.layer = layer;
	}
	
	/**
	 * <p>Replies the data layer this model operates on.</p>
	 * 
	 * @return the data layer
	 */
	public OsmDataLayer getLayer() {
		return layer;
	}
	
	/**
	 * <p>Replies the node the mouse is currently hovering over.</p>
	 * 
	 * @return the node 
	 */
	public Node getFeedbackNode(){
		return feedbackNode;
	}
	
	/**
	 * <p>Sets the node the mouse is currently hovering over.</p>
	 * 
	 * @param node the node 
	 */
	public void setFeedbackNode(Node node){
		this.feedbackNode = node;
	}
		
	public void reset() {
		setFeedbackNode(null);
	}	
	
	/* ---------------------------------------------------------------------------------------- */
	/* selecting nodes and way segments                                                         */
	/* ---------------------------------------------------------------------------------------- */	
	/**
	 * <p>Replies true, if {@code node} is currently selected in the contour merge mode.</p>
	 * 
	 * @param node the node. Must not be null. Must be owned by this models layer.
	 * @return true, if {@code node} is currently selected in the contour merge mode.</p>
	 */
	public boolean isSelected(Node node) throws IllegalArgumentException{
		Assert.checkArgNotNull(node, "node");
		Assert.checkArg(node.getDataSet() == layer.data, "Node must be owned by this contour merge models layer"); // don't translate
		return selectedNodes.contains(node);
	}
	
	/**
	 * <p>Selects the node {@code node}.</p>
	 * 
	 * @param node the node. Must not be null. Must be owned by this models layer.
	 * @throws IllegalArgumentException
	 */
	public void selectNode(Node node) throws IllegalArgumentException{
		Assert.checkArgNotNull(node, "node");
		Assert.checkArg(node.getDataSet() == layer.data, "Node must be owned by this contour merge models layer"); // don't translate
		if (!isSelected(node)) selectedNodes.add(node);
	}
	
	/**
	 * <p>Deselects the node {@code node}.</p>
	 * 
	 * @param node the node. Must not be null. Must be owned by this models layer.
	 * @throws IllegalArgumentException
	 */
	public void deselectNode(Node node) throws IllegalArgumentException{
		Assert.checkArgNotNull(node, "node");
		Assert.checkArg(node.getDataSet() == layer.data, "Node must be owned by this contour merge models layer"); // don't translate
		selectedNodes.remove(node);
	}
	
	/**
	 * <p>Toggles whether the node {@code node} is selected or not.</p>
	 * 
	 * @param node the node. Must not be null. Must be owned by this models layer.
	 * @throws IllegalArgumentException
	 */
	public void toggleSelected(Node node) throws IllegalArgumentException {
		Assert.checkArgNotNull(node, "node");
		Assert.checkArg(node.getDataSet() == layer.data, "Node must be owned by this contour merge models layer"); // don't translate
		if (isSelected(node)) {
			deselectNode(node);
		} else {
			selectNode(node);
		}		
	}
	
	/**
	 * <p>Deselects all nodes.</p>
	 */
	public void deselectAllNodes(){
		selectedNodes.clear();
	}
	
	/**
	 * <p>Replies an <strong>unmodifiable</strong> list of the currently selected nodes.</p>
	 * 
	 * @return an <strong>unmodifiable</strong> list of the currently selected nodes.</p>
	 */
	public List<Node> getSelectedNodes() {
		return Collections.unmodifiableList(selectedNodes);
	}

	/**
	 * <p>Sets the way segment which would be affected by the next drag/drop
	 * operation.</p>
	 * 
	 * @param segment the way segment. null, if there is no feedback way segment 
	 */
	public void setDragStartFeedbackWaySegment(WaySegment segment){
		this.dragStartFeedbackSegment = segment;
	}
	
	/**
	 * <p>Replies the current feedback way segment or null, if there is currently
	 * no such segment
	 * 
	 * @return the feedback way segment 
	 */
	public WaySegment getDragStartFeedbackWaySegement(){
		return dragStartFeedbackSegment;
	}
	
	public void setDropFeedbackSegment(WaySegment segment){
		this.dropFeedbackSegment = segment;
	}
	
	public WaySegment getDropFeedbackSegment(){
		return dropFeedbackSegment;
	}
	
	/**
	 * <p>Replies the set of selected ways, i.e. the set of all parent ways of the
	 * selected nodes.</p>
	 * 
	 * @return the set of selected ways 
	 */
	protected Set<Way> computeSelectedWays(){
		Set<Way> ways = new HashSet<Way>();
		for (Node n: selectedNodes){
			ways.addAll(OsmPrimitive.getFilteredList(n.getReferrers(), Way.class));
		}
		return ways;		
	}
	
	/**
	 * <p>Replies the set of selected nodes on the way {@code way}.</p>
	 * 
	 * @param way the way
	 * @return the set of selected nodes
	 */
	protected Set<Node> computeSelectedNodesOnWay(Way way){
		Set<Node> nodes = new HashSet<Node>();
		if (way == null) return nodes;
		for (Node n : selectedNodes){
			if (!OsmPrimitive.getFilteredSet(n.getReferrers(), Way.class).contains(way)) continue;
			nodes.add(n);			
		}
		return nodes;
	}
	
	/**
	 * <p>Replies true, if we can start a drag/drop operation on way slice which is
	 * given by the currently selected nodes and the way segment {@code ws}.</p>
	 * 
	 *  @return true, if we can start a drag/drop operation. false, otherwise 
	 */
	public boolean isWaySegmentDragable(WaySegment ws){
		WaySlice slice = getWaySliceFromSelectedNodes(ws);
		return slice != null;
	}
	
	/**
	 * <p>Replies true, if {@code ws} is part of a potential drop target.</p>
	 *  
	 * @param ws the way segment. If null, replies false. 
	 * @return  true, if {@code ws} is part of a potential drop target
	 */
	public boolean isPotentialDropTarget(WaySegment ws){
		if (ws == null) return false;
		WaySlice dropTarget = getWaySliceFromSelectedNodes(ws);
		if (dropTarget == null) return false;
		
		// make sure we don't try to drop on the drag source, not even
		// on a different way slice on the way we drag from
		WaySlice dragSource = getDragSource();
		if (dragSource == null) return true;
		return ! dragSource.getWay().equals(dropTarget.getWay());
	}
	
	protected List<Integer> computeSelectedNodeIndicesOnWay(Way way){
		Set<Node> nodes = computeSelectedNodesOnWay(way);
		List<Integer> ret = new ArrayList<Integer>();
		if (nodes.isEmpty()) return ret;
		for (Node n: nodes){
			ret.add(way.getNodes().indexOf(n));
		}
		Collections.sort(ret);
		return ret;
	}
	
	protected WaySlice getWaySliceFromSelectedNodes(WaySegment referenceSegment){
		if (referenceSegment == null) return null;
		Way way = referenceSegment.way;
		if (way.isClosed()){			
			/*
			 * This is a closed way. We need at least two selected nodes to come up
			 * with a way slice. 
			 */
			List<Integer> selIndices = computeSelectedNodeIndicesOnWay(way);
			if (selIndices.size() <2) return null;
			
			int nn= way.getNodesCount();
			int li = referenceSegment.lowerIndex;
			int lower = -1; int upper = nn;
			/*
			 * Find the first selected node to the "left" of the way segment, wrapping
			 * around at the join-node, if necessary.
			 */
			for (int i=li; i>=0;i--){
				if (selIndices.contains(i)) {lower = i; break;}
			}
			if (lower == -1){ // not found yet - wrap around and continue search
				for (int i=nn-1; i>li; i--){
					if (selIndices.contains(i)) {lower = i; break;}
				}				
			}
			/*
			 * Find the first selected node to the "right" of the way segment, wrapping
			 * around at the join-node, if necessary.
			 */
			for (int i=li+1; i< nn-1 ; i++){
				if (selIndices.contains(i)) {upper = i; break;}
			}
			if (upper == nn){ // not found yet - wrap around and continue search
				for (int i=0; i<li; i++){
					if (selIndices.contains(i)) {upper = i; break;}
				}
				/*
				 * not really a wrap around? => adjust the index
				 */
				if (upper == 0) upper = nn-1; 
			}
			if (lower < upper){
				if (upper == nn -1) {
					return new WaySlice(way, 0, lower, false /* reverse direction */);
				} else {
					return new WaySlice(way, lower,upper);
				}
			} else if (lower == upper ){
				return new WaySlice(way, 0, upper, false /* reverse direction */);
			} else {
				return new WaySlice(way, upper, lower, false /* reverse direction */);
			}
		} else {
			/*
			 * This is an open way. We can always reply a way slice. If no nodes are selected, 
			 * we drag the entire way. If 1 node
			 * is selected, the way segment determines whether we drag the first or the second
			 * half. If more than 1 nodes are selected, we drag the way slice between two selected, or
			 * the first or the last node respectively.
			 */
			List<Integer> selIndices = computeSelectedNodeIndicesOnWay(referenceSegment.way);		
			int nn= way.getNodesCount();
			int li = referenceSegment.lowerIndex;
			int lastPos = nn -1;
			int lower = 0; int upper = lastPos;
			for (int pos=li; pos >=0; pos--){
				if (selIndices.contains(pos)) {lower = pos; break;}
			}
			for (int pos=li+1; pos <=lastPos; pos++){
				if (selIndices.contains(pos)) {upper = pos; break;}
			}
			return new WaySlice(referenceSegment.way, lower, upper);
		}
	}
	
	/**
	 * <p>Replies the way slice we are currently dragging, or null, if if we
	 * aren't in a drag operation.</p>
	 * 
	 * @return the way slice or null
	 */
	public WaySlice getDragSource(){
		if (dragStartFeedbackSegment == null) return null;
		return getWaySliceFromSelectedNodes(dragStartFeedbackSegment);
	}

	/**
	 * <p>Replies the way slice we are currently hovering over and which is suitable
	 * as drop target, or null, if no such way slice is currently known.</p>
	 * 
	 * @return the way slice or null
	 */
	public WaySlice getDropTarget(){
		if (dropFeedbackSegment == null) return null;
		return getWaySliceFromSelectedNodes(dropFeedbackSegment);
	}

	/**
	 * <p>Sets the current drag offset, relative to the point where the drag operation
	 * started. Set null to indicate, that there is currently no drag operation. </p>
	 * 
	 * @param offset the drag offset
	 */
	public void setDragOffset(Point offset){
		this.dragOffset = offset;
	}
	
	/**
	 * <p>Replies the current drag offset or null, if we aren't in a drag operation.</p>
	 * 
	 * @return the drag offset 
	 */
	public Point getDragOffset(){
		return dragOffset;
	}
	
	/**
	 * <p>Replies true, if we are currently in a drag operation.</p>
	 * 
	 * @return true, if we are currently in a drag operation
	 */
	public boolean isDragging() {
		return dragOffset != null;
	}
	
	/**
	 * <p>Builds the command to align the two contours. Replies null, if the command
	 * can't be created, i.e. because there is no defined drag source or drop target.</p>
	 * 
	 * @return the contour align command
	 */
	public Command buildContourAlignCommand() {
		WaySlice dragSource = getDragSource();
		WaySlice dropTarget = getDropTarget();
		if (dragSource == null) return null;
		if (dropTarget == null) return null;
		List<Node> targetNodes = dropTarget.getNodes();
		if (! areDirectionAligned(dragSource, dropTarget)) {
			logger.info("not direction aligned !");
			Collections.reverse(targetNodes);
		}
		List<Command> cmds = new ArrayList<Command>();
		// the command to change the source way
		cmds.add(new ChangeCommand(dragSource.getWay(), dragSource.replaceNodes(targetNodes)));
		
		// the command to delete nodes we don't need anymore 
		for (Node n: dragSource.getNodes()) {
			List<OsmPrimitive> parents = n.getReferrers();
			parents.remove(dragSource.getWay());
			if (parents.isEmpty() && !n.isTagged()) {
				cmds.add(new DeleteCommand(n));
			}
		}
		
		SequenceCommand cmd = new SequenceCommand(tr("Merging Contour"), cmds);
		return cmd;
	}
	
	/**
	 * <p>Replies true, if the two polylines given by the node lists {@code n1} and
	 * {@code n2} are "direction aligned". Their direction is aligned, if the two lines
	 * between the two start nodes and the two end nodes of {@code n1} and code {@code n2}
	 * respectively, do not intersect.</p> 
	 * 
	 * @param n1 the first list of nodes 
	 * @param n2 the second list of nodes 
	 * @return true, if the two polylines are "direction aligned".
	 */
	protected boolean areDirectionAligned(List<Node> n1, List<Node> n2) {
		EastNorth s1 = n1.get(0).getEastNorth();
		EastNorth s2 = n1.get(n1.size()-1).getEastNorth();
		
		EastNorth t1 = n2.get(0).getEastNorth();
		EastNorth t2 = n2.get(n2.size()-1).getEastNorth();

		Line2D l1 = new Line2D.Double(s1.getX(), s1.getY(), t1.getX(),t1.getY());
		Line2D l2 = new Line2D.Double(s2.getX(), s2.getY(), t2.getX(),t2.getY());
		return ! l1.intersectsLine(l2);

	}
	
	/**
	 * <p>Replies true, if the two way slices are "direction aligned".</p>
	 * 
	 * @param dragSource the first way slice 
	 * @param dropTarget the second way slice 
	 * @return  true, if the two way slices are "direction aligned"
	 * @see #areDirectionAligned(List, List)
	 */
	protected boolean areDirectionAligned(WaySlice dragSource, WaySlice dropTarget){
		if (dragSource == null) return false;
		if (dropTarget == null) return false;
		return areDirectionAligned(dragSource.getNodes(), dropTarget.getNodes());
	}

	protected void ensureSelectedNodesConsistent() {
		Iterator<Node> it = selectedNodes.iterator();
		while(it.hasNext()) {
			Node n = it.next();
			if (OsmPrimitive.getFilteredSet(n.getReferrers(), Way.class).isEmpty()) {
				it.remove();
			} else if (n.isDeleted()) {
				it.remove();
			}			 
		}
	}
	
	/* ------------------------------------------------------------------------------- */
	/* interface DataSetListener                                                       */
	/* ------------------------------------------------------------------------------- */

	@Override
	public void primtivesRemoved(PrimitivesRemovedEvent event) {
		ensureSelectedNodesConsistent();
	}

	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {
		ensureSelectedNodesConsistent();
	}

	@Override
	public void dataChanged(DataChangedEvent event) {
		ensureSelectedNodesConsistent();
	}

	public void relationMembersChanged(RelationMembersChangedEvent event) {/* ignore */}
	public void otherDatasetChange(AbstractDatasetChangedEvent event) {/*ignore */}
	public void primtivesAdded(PrimitivesAddedEvent event) {/* ignore */}
	public void tagsChanged(TagsChangedEvent event) { /* ignore */}
	public void nodeMoved(NodeMovedEvent event) {/* ignore */}
}
