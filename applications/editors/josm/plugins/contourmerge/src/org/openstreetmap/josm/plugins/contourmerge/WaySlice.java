package org.openstreetmap.josm.plugins.contourmerge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.contourmerge.util.Assert;

/**
 * <p>A <strong>WaySlice</strong> is a sub sequence of a ways sequence of nodes.</p>
 * 
 */
public class WaySlice {
	//static private final Logger logger = Logger.getLogger(WaySlice.class.getName());

	private Way w;
	private int start;
	private int end;
	private boolean inDirection = true;
			
	/**
	 * <p>Creates a new way slice for the way {@code w}. It consists of the nodes at the positions
	 * <code>[start, start+1, ..., end]</code>.</p>
	 * 
	 * @param w the way. Must not be null.
	 * @param start the index of the start node. 0 <= start < w.getNodeCount(). start < end
	 * @param end the index of the end node. 0 <= end < w.getNodeCount(). start < end
	 * @throws IllegalArgumentException thrown if one of the arguments isn't valid
	 */
	public WaySlice(Way w, int start, int end) throws IllegalArgumentException {
		Assert.checkArgNotNull(w, "w");
		Assert.checkArg(start >= 0 && start < w.getNodesCount(), "start out of range, got {0}", start);
		Assert.checkArg(end >= 0 && end < w.getNodesCount(), "end out of range, got {0}", start);
		Assert.checkArg(start < end, "expected start < end, got start={0}, end={1}", start, end);
		this.w = w;
		this.start = start;		
		this.end = end;
	}
	
	/**
	 * <p>Creates a new way slice for the way {@code w}.</p>
	 * 
	 * <p>If {@code inDirection==true}, it consists of the nodes at the positions
	 * <code>[start, start+1, ..., end]</code>.</p>
	 * 
	 * <p>If {@code inDirection==false} <strong>and w is {@link Way#isClosed() closed}</strong>, 
	 * it consists of the nodes at the positions <code>[end, end+1,...,0,1,...,start]</code>.</p>  
	 * @param w the way. Must not be null. 
	 * @param start the index of the start node. 0 <= start < w.getNodeCount(). start < end
	 * @param end the index of the end node. 0 <= end < w.getNodeCount(). start < end
	 * @param inDirection true, this way slice is given by the nodes <code>[start, ..., end]</code>; false, if
	 * is  given by the nodes <code>[end,..,0,..,start]</code> (provided the way is closed)
	 * @throws IllegalArgumentException thrown if a precondition is violated 
	 */
	public WaySlice(Way w, int start, int end, boolean inDirection) throws IllegalArgumentException{
		this(w,start,end);
		if (!inDirection){
			Assert.checkArg(w.isClosed(), "inDirection=false only supported provided w is closed");
		}
		if (w.isClosed() && start == 0 && end == w.getNodesCount() -1){
			Assert.checkArg(false, "for a closed way, start and end must not both refer to the shared 'join'-node");
		}
		this.inDirection = inDirection;
	}
	
	/**
	 * Replies the way this is a slice of.
	 * 
	 * @return the way this is a slice of.
	 */
	public Way getWay() {
		return w;
	}
	
	/**
	 * Replies the index of the first node of this way slice.
	 * 
	 * @return the index of the first node of this way slice
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * Replies the index of the last node of this way slice.
	 * 
	 * @return the index of the first node of this way slice
	 */
	public int getEnd() {
		return end;
	}
	
	/**
	 * Replies true, if this way slice has the same direction and the
	 * parent way. Replies false, if it has the opposite direction.
	 * 
	 * @return
	 */
	public boolean isInDirection() {
		return inDirection;
	}
	
	public Node getStartNode(){
		return w.getNode(start);
	}
	
	public Node getEndNode() {
		return w.getNode(end);
	}
	
	/**
	 * <p>Replies the lower node idx of the node from which this way slice is torn off.</p>
	 * 
	 * 
	 * <strong>Example</strong>
	 * <pre>
	 *     n0 ------------- n1 ---------- n2 --------- n3 -------------- n4
	 *                    start                       end 
	 *     ^-- this is the lower position where the way slice [n1,n2,n3] is torn off  
	 *     ==> the method replies 0             
	 * </pre>
	 * 
	 * <p>Replies -1, if there is no such index.</p>
	 *  
	 * <strong>Example</strong>
	 * <pre>
	 *     n0 ------------- n1 ---------- n2 --------- n3 -------------- n4
	 *     start                         end 
	 *     The way slice starts at the first node of an open way => there is
	 *     no node where the way slice is torn off
	 *     ==> the method replies -1             
	 * </pre>
	 * 
	 * @return
	 */
	public int getStartTearOffIdx() {
		if (! w.isClosed()) {  // an open way 
			return start > 0 ? start - 1 : -1;
		} else {               // a closed way			
			if (isInDirection()){			
				int lower = start - 1;
				if (lower < 0) lower = w.getNodesCount() - 2;
				return lower == end ? -1 : lower;
			} else {
				int lower = end - 1;
				if (lower < 0) lower = w.getNodesCount() - 2;
				return lower == start ? -1 : lower;
			}
		}
	}
	
	/**
	 * <p>Replies the lower node from which this way slice is torn off, see
	 * {@link #getStartTearOffIdx()} for more details.</p>

	 * @return 
	 */
	public Node getStartTearOffNode() {
		int i = getStartTearOffIdx();
		return i==-1 ? null : w.getNode(i);
	}
	
	/**
	 * <p>Replies the upper node idx of the node from which this way slice is torn off.</p>
	 * 
	 * <strong>Example</strong>
	 * <pre>
	 *     n0 ------------- n1 ---------- n2 --------- n3 -------------- n4
	 *                    start=====================  end 
	 *                                                                   ^
	 *                                                                   |
	 *     this is the upper position where the way slice [n1,n2,n3] is torn off  
	 *     ==> the method replies 4             
	 * </pre>
	 * 
	 * <p>Replies -1, if there is no such index.</p> 
	 * <strong>Example</strong>
	 * <pre>
	 *     n0 ------------- n1 ---------- n2 --------- n3 -------------- n4
	 *                                    start ===================     end 
	 *     The way slice ends at the last node of an open way => there is
	 *     no node where the way slice is torn off
	 *     ==> the method replies -1             
	 * </pre>
	 * 	 
	 * @return
	 */
	public int getEndTearOffIdx() {
		if (! w.isClosed()) {	  // an open way		
			return end < w.getNodesCount()-1 ? end + 1 : -1;
		} else {                  // a closed way 
			if (inDirection) {
				int upper = end + 1;
				if (upper >= w.getNodesCount()-1) upper = 0;
				return upper == start ? -1 : upper;
			} else {
				int upper = start + 1;
				if (upper >= w.getNodesCount()-1) upper = 0;
				return upper == end ? -1 : upper;
			}
		}
	}
	
	/**
	 * <p>Replies the upper node from which this way slice is torn off, see
	 * {@link #getEndTearOffIdx()} for more details.</p>

	 * @return 
	 */
	public Node getEndTearOffNode() {
		int i = getEndTearOffIdx();
		return i == -1 ? null : w.getNode(i);
	}
	
	/**
	 * <p>Replies the number of way segments in this way slice.</p>
	 * 
	 * @return the number of way segments in this way slice
	 */
	public int getNumSegments() {
		if (inDirection) return end - start;
		return start + (w.getNodesCount() - 1 - end); // for closed ways 
	}
	
	/**
	 * <p>Replies the opposite way slice, or null, if this way slice doesn't have
	 * an opposite way slice, because it is a way slice in an open way.</p>
	 * 
	 * @return the oposite way slice 
	 */
	public WaySlice getOpositeSlice(){
		if (!w.isClosed()) return null;
		return new WaySlice(w, start, end, !inDirection);
	}
	
	/**
	 * <p>Replies a clone of the underlying way, where the nodes given by this way slice are
	 * replaced with the nodes in {@code newNodes}.</code>
	 * 
	 * @param newNodes the new nodes. Ignored if null.
	 * @return the cloned way with the new nodes 
	 */
	public Way replaceNodes(List<Node> newNodes) {
		Way nw = new Way(w);
		if (newNodes == null || newNodes.isEmpty()) return nw;
		
		if (!w.isClosed()) {
			List<Node> oldNodes = new ArrayList<Node>(w.getNodes());
			for (int i=start; i<= end;i++) oldNodes.remove(start);
			oldNodes.addAll(start, newNodes);
			nw.setNodes(oldNodes);
		} else {
			List<Node> oldNodes = new ArrayList<Node>(w.getNodes());
			if (inDirection) {
				if (start == 0)oldNodes.remove(oldNodes.size()-1);
				for (int i=start; i<= end;i++)oldNodes.remove(start);
				oldNodes.addAll(start, newNodes);
				if (start == 0) oldNodes.add(newNodes.get(0));
				nw.setNodes(oldNodes);
			} else {
				int upper = oldNodes.size()-1;
				for (int i=end; i<=upper; i++) oldNodes.remove(end);
				for (int i=0; i<=start;i++) oldNodes.remove(0);
				oldNodes.addAll(0, newNodes);
				oldNodes.add(newNodes.get(0));  // make sure the new way is closed again
				nw.setNodes(oldNodes);
			}
		}
		return nw;
	}
		
	/**
	 * <p>Replies the list of nodes, always starting at the start index, following the nodes
	 * in the appropriate direction to the end index.</p>
	 * 
	 * @return the list of nodes
	 */
	public List<Node> getNodes(){
		List<Node> nodes = new ArrayList<Node>();
		if (!w.isClosed()) {
			nodes.addAll(w.getNodes().subList(start, end+1));
		} else {
			if (inDirection) {
				nodes.addAll(w.getNodes().subList(start, end+1));
			} else {
				// do not add the last node which is the join node common to the node at index 0
				for (int i=end; i<=w.getNodesCount()-2;i++)nodes.add(w.getNode(i));
				for (int i=0; i <= start; i++) nodes.add(w.getNode(i));
			}
		}
		return nodes;
	}
	
	
	/**
	 * Replies true if this way slice participates in at least one sling.
	 * Here's an example of such a sling.
	 * <pre>
	 *                  5
	 *                  |
	 *                  | 
	 *    1======2======3=======4
	 *                  |       |
	 *                  |       |
	 *                  7-------6
	 * </pre>
	 * <ul>
	 *   <li>the nodes [3,4,6,7] form a sling in the way. Note that the way itself is not <em>closed</em>,
	 *   {@link Way#isClosed() isClosed()} will return false.</li>
	 *   <li>the way slice [1,2,3,4] <strong>does</strong> participate in a sling</li>
	 *   <li>the way slice [1,2] <strong>doesn't</strong> participate in a sling</li>
	 * </ul>
	 * 
	 * @return true if this way slice participates in at least one sling.
	 */
	protected boolean hasSlings() {
		Set<Node> nodeSet = new HashSet<Node>();
		if (w.isClosed()){
			if (isInDirection()) {
				for (int i=start; i<=end; i++){
					nodeSet.add(w.getNode(i));
				}
			} else {
				/* A way slice including the common start/end node of a closed way.
				 * Make sure we look only once at the common start/end node.
				 */
				for (int i=start; i > 0 /* don't add the the start node */; i--){
					nodeSet.add(w.getNode(i));
				}
				for (int i=w.getNodesCount()-1 /* add the end node */; i >= end; i--){
					nodeSet.add(w.getNode(i));
				}
			}
		} else {
			for (int i=start; i<=end; i++){
				nodeSet.add(w.getNode(i));
			}			
		}
		/*
		 * make sure each node in  way slice occurs exactly once in the way. This ensures
		 * that the way slice is not participating in any slings. 
		 */
		Set<Node> seen = new HashSet<Node>();
		for (int i=0; i< (w.isClosed() ? w.getNodesCount()-1 : w.getNodesCount()); i++) {
			Node n = w.getNode(i);
			if (seen.contains(n)) return true;
			if (nodeSet.contains(n)) seen.add(n);
		}
		return false;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<way-slice ").append("way=").append(w.getPrimitiveId())
			.append(", start=").append(start)
			.append(", end=").append(end)
			.append(", isInDirection=").append(isInDirection())
			.append(">");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + (inDirection ? 1231 : 1237);
		result = prime * result + start;
		result = prime * result + ((w == null) ? 0 : w.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WaySlice other = (WaySlice) obj;
		if (end != other.end)
			return false;
		if (inDirection != other.inDirection)
			return false;
		if (start != other.start)
			return false;
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}
}
