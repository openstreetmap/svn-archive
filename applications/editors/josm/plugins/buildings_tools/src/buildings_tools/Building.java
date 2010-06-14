package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import static buildings_tools.BuildingsToolsPlugin.eastNorth2latlon;
import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;

class Building {
	private static final double eqlen = 40075004; // length of equator in metres
	private EastNorth en1;
	private EastNorth en2;
	private EastNorth en3;
	private EastNorth en4;
	
	private EastNorth p1;
	private Node node;
	double meter = 0;
	
	private double len = 0;
	private double lwidth;
	private double heading;
	private boolean angconstrainted;
	private double angconstraint = 0;
	
	public void disableAngConstraint() {
		angconstrainted = false;
	}
	public void setAngConstraint(double angle) {
		angconstrainted = true;
		angconstraint = angle;
		while (angconstraint>(Math.PI/4)) angconstraint-=Math.PI/4;
	}
	public double getLength() {
		return len;
	}
	
	public void reset() {
		len = 0;
		en1=null;
		en2=null;
		en3=null;
		en4=null;
	}
	public EastNorth Point1() { return en1; }
	public EastNorth Point2() { return en2; }
	public EastNorth Point3() { return en3; }
	public EastNorth Point4() { return en4; }
	private void updMetrics() {
		meter = 2*Math.PI/(Math.cos(Math.toRadians(eastNorth2latlon(p1).lat())) * eqlen);
		reset();
	}
	public void setBase(EastNorth base) {
		node = null;
		p1 = base;
		updMetrics();
	}
	public void setBase(Node base) {
		node = base;
		p1 = latlon2eastNorth(base.getCoor());
		updMetrics();
	}
	private void updatePos() {
		en1 = p1;
		en2 = new EastNorth(p1.east()+Math.sin(heading)*len*meter,p1.north()+Math.cos(heading)*len*meter);
		en3 = new EastNorth(p1.east()+Math.sin(heading)*len*meter+Math.cos(heading)*lwidth*meter,p1.north()+Math.cos(heading)*len*meter-Math.sin(heading)*lwidth*meter);
		en4 = new EastNorth(p1.east()+Math.cos(heading)*lwidth*meter,p1.north()-Math.sin(heading)*lwidth*meter);
	}
	public void setPlace(EastNorth p2,double width,double lenstep,boolean ignoreConstraint) {
		heading = p1.heading(p2);
		double hdang = 0;
		if (angconstrainted && !ignoreConstraint) {
			hdang = Math.round((heading-angconstraint)/Math.PI*4);
			if (hdang>=8)hdang-=8;
			if (hdang<0)hdang+=8;
			heading = hdang*Math.PI/4+angconstraint;
		}
		double distance = eastNorth2latlon(p1).greatCircleDistance(eastNorth2latlon(p2));
		if (lenstep <= 0) len=distance; else len = Math.round(distance/lenstep)*lenstep;
		if (len == 0) return;
		lwidth = width;
		updatePos();
		Main.map.statusLine.setHeading(Math.toDegrees(heading));
		if (angconstrainted && !ignoreConstraint) {
			Main.map.statusLine.setAngle(hdang*45);
		}
	}
	public void setWidth(double width) {
		lwidth = width;
		updatePos();
	}
	public void paint(Graphics2D g, MapView mv) {
		if (len == 0) return;
		GeneralPath b = new GeneralPath();
		Point pp1 = mv.getPoint(eastNorth2latlon(en1));
		Point pp2 = mv.getPoint(eastNorth2latlon(en2));
		Point pp3 = mv.getPoint(eastNorth2latlon(en4));
		Point pp4 = mv.getPoint(eastNorth2latlon(en3));

		b.moveTo(pp1.x, pp1.y); b.lineTo(pp3.x, pp3.y);
		b.lineTo(pp4.x, pp4.y); b.lineTo(pp2.x, pp2.y);
		b.lineTo(pp1.x, pp1.y);
		g.draw(b);
	}
	public Way create() {
		if (len == 0) return null;
		Node n1;
		if (node==null) 
			n1 = new Node(eastNorth2latlon(en1));
		else
			n1 = node;
		Node n2 = new Node(eastNorth2latlon(en2));
		Node n3 = new Node(eastNorth2latlon(en3));
		Node n4 = new Node(eastNorth2latlon(en4));
		if (n1.getCoor().isOutSideWorld()||n2.getCoor().isOutSideWorld()||
				n3.getCoor().isOutSideWorld()||n4.getCoor().isOutSideWorld()) {
			JOptionPane.showMessageDialog(Main.parent,
				tr("Cannot place building outside of the world."));
			return null;
		}
		Way w = new Way();
		w.addNode(n1);
		if (lwidth>=0) {
			w.addNode(n2);
			w.addNode(n3);
			w.addNode(n4);
		} else {
			w.addNode(n4);
			w.addNode(n3);
			w.addNode(n2);
		}
		w.addNode(n1);
		w.put("building", "yes");
		Collection<Command> cmds = new LinkedList<Command>();
		if (node==null) cmds.add(new AddCommand(n1));
		cmds.add(new AddCommand(n2));
		cmds.add(new AddCommand(n3));
		cmds.add(new AddCommand(n4));
		cmds.add(new AddCommand(w));
		Command c = new SequenceCommand(tr("Create building"), cmds);
		Main.main.undoRedo.add(c);
		return w;
	}
}
