package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.CollectBackReferencesVisitor;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.actions.JosmAction;

public class SimplifyWayAction extends JosmAction {
	public SimplifyWayAction() {
		super(tr("Simplify Way"), "simplify",
			tr("Delete unnecessary nodes from a way."), 0, 0, true);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> selection = Main.ds.getSelected();

		if (selection.size() == 1 && selection.iterator().next() instanceof Way) {
			simplifyWay((Way) selection.iterator().next());
		}
	}

	public void simplifyWay(Way w) {
		double threshold = Double.parseDouble(
			Main.pref.get("simplify-way.max-error", "50"));

		Way wnew = new Way(w);

		int toI = wnew.nodes.size() - 1;
		for (int i = wnew.nodes.size() - 1; i >= 0; i--) {
			CollectBackReferencesVisitor backRefsV =
				new CollectBackReferencesVisitor(Main.ds, false);
			backRefsV.visit(wnew.nodes.get(i));
			boolean used = false;
			if (backRefsV.data.size() == 1) {
				used = Collections.frequency(
					w.nodes, wnew.nodes.get(i)) > 1;
			} else {
				backRefsV.data.remove(w);
				used = !backRefsV.data.isEmpty();
			}

			if (used) {
				simplifyWayRange(wnew, i, toI, threshold);
				toI = i;
			}
		}
		simplifyWayRange(wnew, 0, toI, threshold);

		HashSet<Node> delNodes = new HashSet<Node>();
		delNodes.addAll(w.nodes);
		delNodes.removeAll(wnew.nodes);

		if (wnew.nodes.size() != w.nodes.size()) {
			Collection<Command> cmds = new LinkedList<Command>();
			cmds.add(new ChangeCommand(w, wnew));
			cmds.add(new DeleteCommand(delNodes));
			Main.main.undoRedo.add(
				new SequenceCommand(tr("Simplify Way (remove {0} nodes)",
						delNodes.size()),
					cmds));
			Main.map.repaint();
		}
	}

	public void simplifyWayRange(Way wnew, int from, int to, double thr) {
		if (to - from >= 2) {
			ArrayList<Node> ns = new ArrayList<Node>();
			simplifyWayRange(wnew, from, to, ns, thr);
			for (int j = to-1; j > from; j--) wnew.nodes.remove(j);
			wnew.nodes.addAll(from+1, ns);
		}
	}

	/*
	 * Takes an interval [from,to] and adds nodes from the set (from,to) to
	 * ns.
	 */
	public void simplifyWayRange(Way wnew, int from, int to, ArrayList<Node> ns, double thr) {
		Node fromN = wnew.nodes.get(from), toN = wnew.nodes.get(to);

		int imax = -1;
		double xtemax = 0;
		for (int i = from+1; i < to; i++) {
			Node n = wnew.nodes.get(i);
			double xte = radtometers(linedist(
				fromN.coor.lat(), fromN.coor.lon(),
				n.coor.lat(), n.coor.lon(),
				toN.coor.lat(), toN.coor.lon()));
			if (xte > xtemax) {
				xtemax = xte;
				imax = i;
			}
		}

		if (imax != -1 && xtemax >= thr) {
			simplifyWayRange(wnew, from, imax, ns, thr);
			ns.add(wnew.nodes.get(imax));
			simplifyWayRange(wnew, imax, to, ns, thr);
		}
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
