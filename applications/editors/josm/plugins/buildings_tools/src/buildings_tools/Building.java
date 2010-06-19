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
	private double width;
	private double heading;
	private boolean angConstrained;
	private double angConstraint = 0;

	public void disableAngConstraint() {
		angConstrained = false;
	}

	public void setAngConstraint(double angle) {
		angConstrained = true;
		angConstraint = angle;
	}

	public double getLength() {
		return len;
	}

	public double getWidth() {
		return width;
	}

	public boolean isRectDrawing() {
		return angConstrained && ToolSettings.getWidth() == 0 && ToolSettings.getLenStep() == 0;
	}

	public void reset() {
		len = 0;
		en1 = null;
		en2 = null;
		en3 = null;
		en4 = null;
	}

	public EastNorth point1() {
		return en1;
	}

	public EastNorth point2() {
		return en2;
	}

	public EastNorth point3() {
		return en3;
	}

	public EastNorth point4() {
		return en4;
	}

	private void updMetrics() {
		meter = 2 * Math.PI / (Math.cos(Math.toRadians(eastNorth2latlon(p1).lat())) * eqlen);
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

	/**
	 * @returns Projection of the point to the heading vector in metres
	 */
	private double projection1(EastNorth p) {
		final EastNorth vec = p1.sub(p);
		return (Math.sin(heading) * vec.east() + Math.cos(heading) * vec.north()) / meter;
	}

	/**
	 * @returns Projection of the point to the perpendicular of the heading
	 *          vector in metres
	 */
	private double projection2(EastNorth p) {
		final EastNorth vec = p1.sub(p);
		return (Math.cos(heading) * vec.east() - Math.sin(heading) * vec.north()) / meter;
	}

	private void updatePos() {
		if (len == 0)
			return;
		en1 = p1;
		en2 = new EastNorth(p1.east() + Math.sin(heading) * len * meter, p1.north() + Math.cos(heading) * len * meter);
		en3 = new EastNorth(p1.east() + Math.sin(heading) * len * meter + Math.cos(heading) * width * meter, p1.north()
				+ Math.cos(heading) * len * meter - Math.sin(heading) * width * meter);
		en4 = new EastNorth(p1.east() + Math.cos(heading) * width * meter, p1.north() - Math.sin(heading) * width
				* meter);
	}

	public void setLengthWidth(double length, double width) {
		this.len = length;
		this.width = width;
		updatePos();
	}

	public void setWidth(EastNorth p3) {
		this.width = projection2(p3);
		updatePos();
	}

	public void setPlace(EastNorth p2, double width, double lenstep, boolean ignoreConstraints) {
		this.heading = p1.heading(p2);
		double hdang = 0;
		if (angConstrained && !ignoreConstraints) {
			hdang = Math.round((heading - angConstraint) / Math.PI * 4);
			hdang = hdang % 8;
			if (hdang < 0)
				hdang += 8;
			heading = (hdang * Math.PI / 4 + angConstraint) % (2 * Math.PI);
		}

		this.width = width;
		this.len = projection1(p2);
		if (lenstep > 0 && !ignoreConstraints)
			this.len = Math.round(this.len / lenstep) * lenstep;

		updatePos();

		Main.map.statusLine.setHeading(Math.toDegrees(heading));
		if (angConstrained && !ignoreConstraints) {
			Main.map.statusLine.setAngle(hdang * 45);
		}
	}

	public void setPlaceRect(EastNorth p2) {
		if (!isRectDrawing())
			throw new IllegalStateException("Invalid drawing mode");
		heading = angConstraint;
		setLengthWidth(projection1(p2), projection2(p2));
		Main.map.statusLine.setHeading(Math.toDegrees(heading));
	}

	public void angFix(EastNorth point) {
		EastNorth en3 = this.en3;
		heading = p1.heading(point);
		setLengthWidth(projection1(en3), projection2(en3));
		this.en3 = en3;
	}

	public void paint(Graphics2D g, MapView mv) {
		if (len == 0)
			return;
		GeneralPath b = new GeneralPath();
		Point pp1 = mv.getPoint(eastNorth2latlon(en1));
		Point pp2 = mv.getPoint(eastNorth2latlon(en2));
		Point pp3 = mv.getPoint(eastNorth2latlon(en4));
		Point pp4 = mv.getPoint(eastNorth2latlon(en3));

		b.moveTo(pp1.x, pp1.y);
		b.lineTo(pp3.x, pp3.y);
		b.lineTo(pp4.x, pp4.y);
		b.lineTo(pp2.x, pp2.y);
		b.lineTo(pp1.x, pp1.y);
		g.draw(b);
	}

	public Way create() {
		if (len == 0)
			return null;
		Node n1;
		if (node == null)
			n1 = new Node(eastNorth2latlon(en1));
		else
			n1 = node;
		Node n2 = new Node(eastNorth2latlon(en2));
		Node n3 = new Node(eastNorth2latlon(en3));
		Node n4 = new Node(eastNorth2latlon(en4));
		if (n1.getCoor().isOutSideWorld() || n2.getCoor().isOutSideWorld() ||
				n3.getCoor().isOutSideWorld() || n4.getCoor().isOutSideWorld()) {
			JOptionPane.showMessageDialog(Main.parent,
					tr("Cannot place building outside of the world."));
			return null;
		}
		Way w = new Way();
		w.addNode(n1);
		if (projection1(en3) > 0) {
			w.addNode(n2);
			w.addNode(n3);
			w.addNode(n4);
		} else {
			w.addNode(n4);
			w.addNode(n3);
			w.addNode(n2);
		}
		w.addNode(n1);
		w.put("building", ToolSettings.getTag());
		Collection<Command> cmds = new LinkedList<Command>();
		if (node == null)
			cmds.add(new AddCommand(n1));
		cmds.add(new AddCommand(n2));
		cmds.add(new AddCommand(n3));
		cmds.add(new AddCommand(n4));
		cmds.add(new AddCommand(w));
		Command c = new SequenceCommand(tr("Create building"), cmds);
		Main.main.undoRedo.add(c);
		return w;
	}
}
