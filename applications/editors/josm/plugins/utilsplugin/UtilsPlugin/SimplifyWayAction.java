package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Forgets the selected data, unless it is referenced by something.
 * 
 * "Forgetting", as opposed to "deleting", means that the data is simply removed from JOSM, and
 * not tagged as "to be deleted on server".
 *
 * - selected WAYS can always be forgotten.
 * - selected SEGMENTS can be forgotten unless they are referenced by not-forgotten ways.
 * - selected NODES can be forgotten unless they are referenced by not-forgotten segments.
 */

public class SimplifyWayAction extends JosmAction implements SelectionChangedListener {

	
	private Way selectedWay = null;

	/**
	 * Create a new SimplifyWayAction.
	 */
	public SimplifyWayAction() {
		super(tr("Simplify Way"), "simplify", tr("Delete low-information nodes from a way."), 0, 0, true);
		try { Main.ds.addSelectionChangedListener(this); }
		catch( NoSuchMethodError e )
		{
			try {
			java.lang.reflect.Field f = DataSet.class.getDeclaredField("listeners");
			((Collection<SelectionChangedListener>)f.get(Main.ds)).add(this);
//			Main.ds.listeners.add(this);
			} catch (Exception x) { System.out.println( e ); }
		}
	}

	/**
	 * Called when the action is executed.
	 */
	public void actionPerformed(ActionEvent e) {

		Collection<OsmPrimitive> selection = Main.ds.getSelected();


		Visitor selectVisitor = new Visitor(){
			public void visit(Node n) {
            }
			public void visit(Segment s) {
            }
			public void visit(Way w) {
				selectedWay = w;
            }
		};
		
		for (OsmPrimitive p : selection)
			p.visit(selectVisitor);
		
		simplifyWay(selectedWay);
	}

	private class NodeRecord {
		public boolean keep = false; // whether this node must be kept
		public Node node; // the node
		public NodeRecord previous; // the segment leading to this node
		public double xte; // the cross-track error
		public NodeRecord next;
	}
	
	/**
	 * Simplifies the given way by potentially removing nodes and segments.
	 * 
	 * @param way
	 * @return true if simplification was successful (even if way was not changed)
	 *         false if simplification was not possible (branching/unordered ways)
	 */
	public boolean simplifyWay(Way way) {
		
		// first build some structures that help us working with this way, assuming
		// it might be very long, so we want to be efficient.
		
		// a map holding one NodeRecord object for every node in the way, except 
		// the first node (which is never "simplified" anyway) 
		HashMap<Node,NodeRecord> nodeIndex = new HashMap<Node,NodeRecord>();
		
		// a hash set containing all segments in this way, for fast is-in-way checks
		HashSet<Segment> segmentIndex = new HashSet<Segment>();
		
		// in addition to all this, we also have each NodeRecord pointing
		// to the next one along the way, making a linked list.
		NodeRecord firstNr = null;
		
		// fill structures
		NodeRecord prevNr = null;
		for (Segment s : way.segments) {
			if ((prevNr != null) && (!s.from.equals(prevNr.node))) {
				// error
				System.out.println("XXX err");
				return false;
			}
			segmentIndex.add(s);
			NodeRecord nr = new NodeRecord();
			nr.node = s.to;
			if (prevNr == null) {
				nr.previous = new NodeRecord();
				nr.previous.node = s.from;
				// set "keep" on first node
				nr.previous.keep = true;
				firstNr = nr.previous;
				firstNr.next = nr;
				nodeIndex.put(s.from, nr.previous);
			} else {
				nr.previous = prevNr;
				prevNr.next = nr;
			}
			nr.xte = 0;
			nr.next = null;
			prevNr = nr;
			nodeIndex.put(s.to, nr);
		}
		
		// set "keep" on last node
		prevNr.keep = true;
		
		// check the current data set, and mark all nodes that are used by a segment
		// not exclusively owned by the current way as "untouchable".
		for (Segment s: Main.ds.segments) {
			if (s.deleted) continue;
			if (segmentIndex.contains(s)) continue; // these don't count
			NodeRecord tmp;
			tmp = nodeIndex.get(s.from); if (tmp != null) tmp.keep = true;
			tmp = nodeIndex.get(s.to); if (tmp != null) tmp.keep = true;
		}
		
		for (Way w: Main.ds.ways) {
			if (w.deleted) continue; 
			if (w.equals(way)) continue; // these don't count
			for (Segment s: w.segments)
			{
				NodeRecord tmp;
				tmp = nodeIndex.get(s.from); if (tmp != null) tmp.keep = true;
				tmp = nodeIndex.get(s.to); if (tmp != null) tmp.keep = true;
			}
		}
		
		// keep all nodes which have tags other than source and created_by
		for (NodeRecord nr : nodeIndex.values()) {
			Collection<String> keyset = nr.node.keySet();
			keyset.remove("source");
			keyset.remove("created_by");
			if (!keyset.isEmpty()) nr.keep = true;
		}
		
		// compute cross-track error for all elements. cross-track error is the
		// distance between a node and the nearest point on a line from the 
		// previous to the next node - that's the error you would introduce
		// by removing the node.
		for (NodeRecord r = firstNr; r.next != null; r = r.next) {
			computeXte(r);
		}
		
		boolean stayInLoop = true;
		double treshold = Double.parseDouble(Main.pref.get("simplify-way.max-error", "0.06"));
		while(stayInLoop) {
			NodeRecord[] sorted = new NodeRecord[nodeIndex.size()];
			nodeIndex.values().toArray(sorted);
			Arrays.sort(sorted, new Comparator<NodeRecord>() {
				public int compare(NodeRecord a, NodeRecord b) {
					return (a.xte < b.xte) ? -1 : (a.xte > b.xte) ? 1 : 0;
				}
			});

			stayInLoop = false;
			for (NodeRecord nr : sorted) {
				if (nr.keep) continue;
				if (nr.xte < treshold) {
					// delete this node
					nodeIndex.remove(nr.node);
					if (nr == firstNr) {
						firstNr = nr.next;
					} else {
						nr.previous.next = nr.next;
					}
					if (nr.next != null) {
						nr.next.previous = nr.previous;
					}
					computeXte(nr.next);
					computeXte(nr.previous);
					stayInLoop = true;
				}
				break;
			}
		}
		
		Segment currentOriginalSegment = null;
		Segment currentModifiedSegment = null;
		Way wayCopy = null;
		int delCount = 0;
		Collection<Command> cmds = new LinkedList<Command>();
		
		for (Segment s : way.segments) {
			if (currentOriginalSegment == null) {
				currentOriginalSegment = s;
				currentModifiedSegment = s;
				continue;
			}
			
			if (nodeIndex.containsKey(s.from)) {
				// the current remaining segment's "to" node is not
				// deleted, so it may stay.
				if (currentModifiedSegment != currentOriginalSegment) {
					cmds.add(new ChangeCommand(currentOriginalSegment, currentModifiedSegment));
				}
				currentOriginalSegment = s;
				currentModifiedSegment = s;
			} else {
				// the "to" node is to be deleted; delete segment and 
				// node
				cmds.add(new DeleteCommand(Arrays.asList(new OsmPrimitive[]{s, s.from})));
				delCount ++;
				if (wayCopy == null) {
					wayCopy = new Way(way);
				}
				wayCopy.segments.remove(s);
				if (currentModifiedSegment == currentOriginalSegment) {
					currentModifiedSegment = new Segment(currentOriginalSegment);
				}
				currentModifiedSegment.to = s.to;
			}
		}
		if (currentModifiedSegment != currentOriginalSegment) {
			cmds.add(new ChangeCommand(currentOriginalSegment, currentModifiedSegment));
		}
		
		if (wayCopy != null) {
			cmds.add(new ChangeCommand(way, wayCopy));
			Main.main.editLayer().add(new SequenceCommand(tr("Simplify Way (remove {0} nodes)", delCount), cmds));
		}
		
		return true;
		
	}
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		setEnabled(!newSelection.isEmpty());
	}

	private static void computeXte(NodeRecord r) {
		if ((r.previous == null) || (r.next == null)) {
			r.xte = 0;
			return;
		}
		Node prevNode = r.previous.node;
		Node nextNode = r.next.node;
		r.xte = radtomiles(linedist(prevNode.coor.lat(), prevNode.coor.lon(),
			r.node.coor.lat(), r.node.coor.lon(),
			nextNode.coor.lat(), nextNode.coor.lon()));
	}
	
	/* ---------------------------------------------------------------------- 
	 * Everything below this comment was converted from C to Java by Frederik
	 * Ramm. The original sources are the files grtcirc.c and smplrout.c from 
	 * the gpsbabel source code (www.gpsbabel.org), which is under GPL. The
	 * relevant code portions have been written by Robert Lipe.
	 * 
	 * Method names have been left unchanged where possible.
	 */
	
	public static double EARTH_RAD = 6378137.0;
	public static double radmiles = EARTH_RAD*100.0/2.54/12.0/5280.0;

	public static double[] crossproduct(double[] v1, double[] v2) {
		double[] rv = new double[3];
		rv[0] = v1[1]*v2[2]-v2[1]*v1[2];
		rv[1] = v1[2]*v2[0]-v2[2]*v1[0];
		rv[2] = v1[0]*v2[1]-v1[1]*v2[0];
		return rv;
	}

	public static double dotproduct(double[] v1, double[] v2) {
		return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
	}

	public static double radtomiles(double rads) {
		return (rads*radmiles);
	}

	public static double radtometers(double rads) {
		return (rads * EARTH_RAD);
	}
	
	public static double veclen(double[] vec) {
		return Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
	}

	public static double gcdist(double lat1, double lon1, double lat2, double lon2) 
	{
		double res;
		double sdlat, sdlon;

		sdlat = Math.sin((lat1 - lat2) / 2.0);
		sdlon = Math.sin((lon1 - lon2) / 2.0);

		res = Math.sqrt(sdlat * sdlat + Math.cos(lat1) * Math.cos(lat2) * sdlon * sdlon);

		if (res > 1.0) {
			res = 1.0;
		} else if (res < -1.0) {
			res = -1.0;
		}

		res = Math.asin(res);
		return 2.0 * res;
	}

	static double linedist(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {

		double dot;

		/* degrees to radians */
		lat1 = Math.toRadians(lat1);  lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);  lon2 = Math.toRadians(lon2);
		lat3 = Math.toRadians(lat3);  lon3 = Math.toRadians(lon3);

		/* polar to ECEF rectangular */
		double[] v1 = new double[3];
		double[] v2 = new double[3];
		double[] v3 = new double[3];
		v1[0] = Math.cos(lon1)*Math.cos(lat1); v1[1] = Math.sin(lat1); v1[2] = Math.sin(lon1)*Math.cos(lat1);
		v2[0] = Math.cos(lon2)*Math.cos(lat2); v2[1] = Math.sin(lat2); v2[2] = Math.sin(lon2)*Math.cos(lat2);
		v3[0] = Math.cos(lon3)*Math.cos(lat3); v3[1] = Math.sin(lat3); v3[2] = Math.sin(lon3)*Math.cos(lat3);

		/* 'va' is the axis; the line that passes through the center of the earth
		 * and is perpendicular to the great circle through point 1 and point 2 
		 * It is computed by taking the cross product of the '1' and '2' vectors.*/
		double[] va = crossproduct(v1, v2);
		double la = veclen(va);

		if (la != 0) {
			va[0] /= la;
			va[1] /= la;
			va[2] /= la;

			/* dot is the component of the length of '3' that is along the axis.
			 * What's left is a non-normalized vector that lies in the plane of 
			 * 1 and 2. */

			dot = dotproduct(v3, va);

			double[] vp = new double[3];
			vp[0]=v3[0]-dot*va[0];
			vp[1]=v3[1]-dot*va[1];
			vp[2]=v3[2]-dot*va[2];

			double lp = veclen(vp);

			if (lp != 0) {

				/* After this, 'p' is normalized */
				vp[0] /= lp;
				vp[1] /= lp;
				vp[2] /= lp;

				double[] cp1 = crossproduct(v1, vp);
				double dp1 = dotproduct(cp1, va);

				double[] cp2 = crossproduct(v2, vp);
				double dp2 = dotproduct(cp2, va);

				if ( dp1 >= 0 && dp2 >= 0 ) {
					/* rather than call gcdist and all its sines and cosines and
					 * worse, we can get the angle directly.  It's the arctangent
					 * of the length of the component of vector 3 along the axis 
					 * divided by the length of the component of vector 3 in the 
					 * plane.  We already have both of those numbers. 
					 * 
					 * atan2 would be overkill because lp and Math.abs are both
					 * known to be positive. */
					return Math.atan(Math.abs(dot)/lp); 
				}

				/* otherwise, get the distance from the closest endpoint */
				double c1 = dotproduct(v1, vp);
				double c2 = dotproduct(v2, vp);
				dp1 = Math.abs(dp1);
				dp2 = Math.abs(dp2);

				/* This is a hack.  d$n$ is proportional to the sine of the angle
				 * between point $n$ and point p.  That preserves orderedness up
				 * to an angle of 90 degrees.  c$n$ is proportional to the cosine
				 * of the same angle; if the angle is over 90 degrees, c$n$ is
				 * negative.  In that case, we flop the sine across the y=1 axis
				 * so that the resulting value increases as the angle increases. 
				 * 
				 * This only works because all of the points are on a unit sphere. */

				if (c1 < 0) {
					dp1 = 2 - dp1;
				}
				if (c2 < 0) {
					dp2 = 2 - dp2;
				}

				if (Math.abs(dp1) < Math.abs(dp2)) {
					return gcdist(lat1,lon1,lat3,lon3);  
				} else {
					return gcdist(lat2,lon2,lat3,lon3);
				}
			} else {
				/* lp is 0 when 3 is 90 degrees from the great circle */
				return Math.PI/2;
			}    
		} else {
			/* la is 0 when 1 and 2 are either the same point or 180 degrees apart */
			dot = dotproduct(v1, v2);
			if (dot >= 0) { 
				return gcdist(lat1,lon1,lat3,lon3);
			} else {
				return 0;
			}
		}
	}
}
