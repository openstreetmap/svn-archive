package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
			tr("Delete unnecessary nodes from a way."), KeyEvent.VK_Y, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, true);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> selection = Main.ds.getSelected();
		
		for (OsmPrimitive prim : selection) {
			if (prim instanceof Way) {
				simplifyWay((Way) prim);
			}
		}
	}

	public void simplifyWay(Way w) {
		double threshold = Double.parseDouble(
			Main.pref.get("simplify-way.max-error", "3"));

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
			if (!used) used = wnew.nodes.get(i).tagged;

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
	 * Takes an interval [from,to] and adds nodes from (from,to) to ns.
	 * (from and to are indices of wnew.nodes.)
	 */
	public void simplifyWayRange(Way wnew, int from, int to, ArrayList<Node> ns, double thr) {
		Node fromN = wnew.nodes.get(from), toN = wnew.nodes.get(to);

		int imax = -1;
		double xtemax = 0;
		for (int i = from+1; i < to; i++) {
			Node n = wnew.nodes.get(i);
			double xte = Math.abs(EARTH_RAD * xtd(
				fromN.coor.lat() * Math.PI/180, fromN.coor.lon() * Math.PI/180,
				toN.coor.lat() * Math.PI/180, toN.coor.lon() * Math.PI/180,
				n.coor.lat() * Math.PI/180, n.coor.lon() * Math.PI/180));
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

	public static double EARTH_RAD = 6378137.0;

	/* From Aviaton Formulary v1.3
	 * http://williams.best.vwh.net/avform.htm
	 */
	public static double dist(double lat1, double lon1, double lat2, double lon2) {
		return 2*Math.asin(Math.sqrt(Math.pow(Math.sin((lat1-lat2)/2), 2) + 
			Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin((lon1-lon2)/2), 2)));
	}

	public static double course(double lat1, double lon1, double lat2, double lon2) {
		return Math.atan2(Math.sin(lon1-lon2)*Math.cos(lat2),
		    Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon1-lon2)) % (2*Math.PI);
	}

	public static double xtd(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
		double dist_AD = dist(lat1, lon1, lat3, lon3);
		double crs_AD = course(lat1, lon1, lat3, lon3);
		double crs_AB = course(lat1, lon1, lat2, lon2);
		return Math.asin(Math.sin(dist_AD)*Math.sin(crs_AD-crs_AB));
	}
}
