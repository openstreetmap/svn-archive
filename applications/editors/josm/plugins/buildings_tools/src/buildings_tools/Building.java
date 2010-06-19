package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import static buildings_tools.BuildingsToolsPlugin.eastNorth2latlon;
import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.*;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;

class Building {
	private static final double eqlen = 40075004; // length of equator in metres
	private final EastNorth[] en = new EastNorth[4];

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

		for (int i = 0; i < 4; i++)
			en[i] = null;
	}

	public EastNorth getPoint(int num) {
		return en[num];
	}

	private void updMetrics() {
		meter = 2 * Math.PI / (Math.cos(Math.toRadians(eastNorth2latlon(en[0]).lat())) * eqlen);
		reset();
	}

	public void setBase(EastNorth base) {
		en[0] = base;
		updMetrics();
	}

	public void setBase(Node base) {
		en[0] = latlon2eastNorth(base.getCoor());
		updMetrics();
	}

	/**
	 * @returns Projection of the point to the heading vector in metres
	 */
	private double projection1(EastNorth p) {
		final EastNorth vec = en[0].sub(p);
		return (Math.sin(heading) * vec.east() + Math.cos(heading) * vec.north()) / meter;
	}

	/**
	 * @returns Projection of the point to the perpendicular of the heading
	 *          vector in metres
	 */
	private double projection2(EastNorth p) {
		final EastNorth vec = en[0].sub(p);
		return (Math.cos(heading) * vec.east() - Math.sin(heading) * vec.north()) / meter;
	}

	private void updatePos() {
		if (len == 0)
			return;
		final EastNorth p1 = en[0];
		en[1] = new EastNorth(p1.east() + Math.sin(heading) * len * meter, p1.north() + Math.cos(heading) * len * meter);
		en[2] = new EastNorth(p1.east() + Math.sin(heading) * len * meter + Math.cos(heading) * width * meter, p1
				.north()
				+ Math.cos(heading) * len * meter - Math.sin(heading) * width * meter);
		en[3] = new EastNorth(p1.east() + Math.cos(heading) * width * meter, p1.north() - Math.sin(heading) * width
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
		if (en[0] == null)
			en[0] = p2;
		this.heading = en[0].heading(p2);
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
		EastNorth en3 = this.en[2];
		heading = en[0].heading(point);
		setLengthWidth(projection1(en3), projection2(en3));
		this.en[2] = en3;
	}

	public void paint(Graphics2D g, MapView mv) {
		if (len == 0)
			return;
		GeneralPath b = new GeneralPath();
		Point pp1 = mv.getPoint(eastNorth2latlon(en[0]));
		Point pp2 = mv.getPoint(eastNorth2latlon(en[1]));
		Point pp3 = mv.getPoint(eastNorth2latlon(en[2]));
		Point pp4 = mv.getPoint(eastNorth2latlon(en[3]));

		b.moveTo(pp1.x, pp1.y);
		b.lineTo(pp2.x, pp2.y);
		b.lineTo(pp3.x, pp3.y);
		b.lineTo(pp4.x, pp4.y);
		b.lineTo(pp1.x, pp1.y);
		g.draw(b);
	}

	private Node findNode(EastNorth en) {
		DataSet ds = Main.main.getCurrentDataSet();
		LatLon l = eastNorth2latlon(en);
		List<Node> nodes = ds.searchNodes(new BBox(l.lon() - 0.00001, l.lat() - 0.00001,
				l.lon() + 0.00001, l.lat() + 0.00001));
		for (Node n : nodes) {
			if (OsmPrimitive.isUsablePredicate.evaluate(n))
				return n;
		}
		return null;
	}

	public Way create() {
		if (len == 0)
			return null;
		final boolean[] created = new boolean[4];
		final Node[] nodes = new Node[4];
		for (int i = 0; i < 4; i++) {
			Node n = findNode(en[i]);
			if (n == null) {
				nodes[i] = new Node(eastNorth2latlon(en[i]));
				created[i] = true;
			} else {
				nodes[i] = n;
				created[i] = false;
			}
			if (nodes[i].getCoor().isOutSideWorld()) {
				JOptionPane.showMessageDialog(Main.parent,
						tr("Cannot place building outside of the world."));
				return null;
			}
		}
		Way w = new Way();
		w.addNode(nodes[0]);
		if (projection1(latlon2eastNorth(nodes[2].getCoor())) > 0) {
			w.addNode(nodes[1]);
			w.addNode(nodes[2]);
			w.addNode(nodes[3]);
		} else {
			w.addNode(nodes[3]);
			w.addNode(nodes[2]);
			w.addNode(nodes[1]);
		}
		w.addNode(nodes[0]);
		w.put("building", ToolSettings.getTag());
		Collection<Command> cmds = new LinkedList<Command>();
		for (int i = 0; i < 4; i++) {
			if (created[i])
				cmds.add(new AddCommand(nodes[i]));
		}
		cmds.add(new AddCommand(w));
		Command c = new SequenceCommand(tr("Create building"), cmds);
		Main.main.undoRedo.add(c);
		return w;
	}
}
