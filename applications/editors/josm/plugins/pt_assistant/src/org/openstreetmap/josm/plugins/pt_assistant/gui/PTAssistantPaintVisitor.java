// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.gui.layer.validation.PaintVisitor;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

/**
 * Visits the primitives to be visualized in the pt_assistant layer
 *
 * @author darya
 *
 */
public class PTAssistantPaintVisitor extends PaintVisitor {

	private static final String Node = null;
	/** The graphics */
	private final Graphics g;
	/** The MapView */
	private final MapView mv;

	/**
	 * Constructor
	 *
	 * @param g
	 *            graphics
	 * @param mv
	 *            map view
	 */
	public PTAssistantPaintVisitor(Graphics g, MapView mv) {
		super((Graphics2D) g, mv);
		this.g = g;
		this.mv = mv;
	}

	@Override
	public void visit(Relation r) {
		if (RouteUtils.isBicycleRoute(r) || RouteUtils.isFootRoute(r) || RouteUtils.isHorseRoute(r)) {
			drawCycleRoute(r);
			return;
		}

		List<RelationMember> rmList = new ArrayList<>();
		List<RelationMember> revisitedWayList = new ArrayList<>();
		// first, draw primitives:
		for (RelationMember rm : r.getMembers()) {

			if (PTStop.isPTStopPosition(rm)) {
				drawStop(rm.getMember(), true);
			} else if (PTStop.isPTPlatform(rm)) {
				drawStop(rm.getMember(), false);
			} else if (RouteUtils.isPTWay(rm)) {
				if (rm.isWay()) {
					if (rmList.contains(rm)) {
						if (!revisitedWayList.contains(rm)) {
							visit(rm.getWay(), true);
							revisitedWayList.add(rm);
						}
					} else {
						visit(rm.getWay(), false);
					}
				} else if (rm.isRelation()) {
					visit(rm.getRelation());
				}
				rmList.add(rm);
			}
		}

		// in the end, draw labels:
		HashMap<Long, String> stopOrderMap = new HashMap<>();
		int stopCount = 1;

		for (RelationMember rm : r.getMembers()) {
			if (PTStop.isPTStop(rm) || (rm.getMember().isIncomplete() && (rm.isNode() || rm.hasRole("stop")
					|| rm.hasRole("stop_entry_only") || rm.hasRole("stop_exit_only") || rm.hasRole("platform")
					|| rm.hasRole("platform_entry_only") || rm.hasRole("platform_exit_only")))) {

				StringBuilder sb = new StringBuilder();

				if (stopOrderMap.containsKey(rm.getUniqueId())) {
					sb.append(stopOrderMap.get(rm.getUniqueId())).append(";").append(stopCount);
				} else {
					if (r.hasKey("ref")) {
						sb.append(r.get("ref"));
					} else if (r.hasKey("name")) {
						sb.append(r.get("name"));
					} else {
						sb.append("NA");
					}
					sb.append(" - ").append(stopCount);
				}

				stopOrderMap.put(rm.getUniqueId(), sb.toString());
				try {
					if (PTStop.isPTStopPosition(rm))
						drawStopLabel(rm.getMember(), sb.toString(), false);
					else if (PTStop.isPTPlatform(rm))
						drawStopLabel(rm.getMember(), sb.toString(), true);
				} catch (NullPointerException ex) {
					// do nothing
					Logging.trace(ex);
				}
				stopCount++;
			}
		}

	}

	private void drawCycleRoute(Relation r) {

		List<RelationMember> members = new ArrayList<>(r.getMembers());
		members.removeIf(m -> !m.isWay());
		WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
		List<WayConnectionType> links = connectionTypeCalculator.updateLinks(members);

		for (int i = 0; i < links.size(); i++) {
			WayConnectionType link = links.get(i);
			Way way = members.get(i).getWay();
			if (!link.isOnewayLoopForwardPart && !link.isOnewayLoopBackwardPart) {
				if (way.isSelected()) {
					drawWay(way, new Color(0, 255, 0, 100));
				} else {
					drawWay(way, new Color(0, 255, 255, 100));
				}
			} else if (link.isOnewayLoopForwardPart) {
				if (way.isSelected()) {
					drawWay(way, new Color(255, 255, 0, 100));
				} else {
					drawWay(way, new Color(255, 0, 0, 100));
				}
			} else {
				if (way.isSelected()) {
					drawWay(way, new Color(128, 0, 128, 100));
				} else {
					drawWay(way, new Color(0, 0, 255, 100));
				}
			}
		}
	}

	private void drawWay(Way way, Color color) {
		List<Node> nodes = way.getNodes();
		for (int i = 0; i < nodes.size() - 1; i++) {
			drawSegment(nodes.get(i), nodes.get(i + 1), color, 1, false);
		}
	}

	public void visit(Way w, boolean revisit) {
		if (w == null) {
			return;
		}

		/*-
		 * oneway values:
		 * 0 two-way street
		 * 1 oneway street in the way's direction
		 * 2 oneway street in ways's direction but public transport allowed
		 * -1 oneway street in reverse direction
		 * -2 oneway street in reverse direction but public transport allowed
		 */
		int oneway = 0;

		if (w.hasTag("junction", "roundabout") || w.hasTag("highway", "motorway")) {
			oneway = 1;
		} else if (w.hasTag("oneway", "1") || w.hasTag("oneway", "yes") || w.hasTag("oneway", "true")) {
			if (w.hasTag("busway", "lane") || w.hasTag("busway:left", "lane") || w.hasTag("busway:right", "lane")
					|| w.hasTag("oneway:bus", "no") || w.hasTag("busway", "opposite_lane")
					|| w.hasTag("oneway:psv", "no") || w.hasTag("trolley_wire", "backward")) {
				oneway = 2;
			} else {
				oneway = 1;
			}
		} else if (w.hasTag("oneway", "-1") || w.hasTag("oneway", "reverse")) {
			if (w.hasTag("busway", "lane") || w.hasTag("busway:left", "lane") || w.hasTag("busway:right", "lane")
					|| w.hasTag("oneway:bus", "no") || w.hasTag("busway", "opposite_lane")
					|| w.hasTag("oneway:psv", "no") || w.hasTag("trolley_wire", "backward")) {
				oneway = -2;
			} else {
				oneway = -1;
			}
		}

		visit(w.getNodes(), oneway, revisit);

	}

	/**
	 * Variation of the visit method that allows a special visualization of oneway
	 * roads
	 *
	 * @param nodes
	 *            nodes
	 * @param oneway
	 *            oneway
	 */
	public void visit(List<Node> nodes, int oneway, boolean revisit) {
		Node lastN = null;
		for (Node n : nodes) {
			if (lastN == null) {
				lastN = n;
				continue;
			}
			if (!revisit)
				drawSegment(lastN, n, new Color(128, 0, 128, 100), oneway, revisit);
			else
				drawSegment(lastN, n, new Color(0, 0, 0, 180), oneway, revisit);
			lastN = n;
		}
	}

	/**
	 * Draw a small rectangle. White if selected (as always) or red otherwise.
	 *
	 * @param n
	 *            The node to draw.
	 */
	@Override
	public void visit(Node n) {
		if (n.isDrawable() && isNodeVisible(n)) {
			drawNode(n, Color.BLUE);
		}
	}

	/**
	 * Draws a line around the segment
	 *
	 * @param n1
	 *            The first node of segment
	 * @param n2
	 *            The second node of segment
	 * @param color
	 *            The color
	 */
	protected void drawSegment(Node n1, Node n2, Color color, int oneway, boolean revisit) {
		if (n1.isDrawable() && n2.isDrawable() && isSegmentVisible(n1, n2)) {
			try {
				drawSegment(mv.getPoint(n1), mv.getPoint(n2), color, oneway, revisit);
			} catch (NullPointerException ex) {
				// do nothing
				Logging.trace(ex);
			}

		}
	}

	/**
	 * Draws a line around the segment
	 *
	 * @param p1
	 *            The first point of segment
	 * @param p2
	 *            The second point of segment
	 * @param color
	 *            The color
	 */
	protected void drawSegment(Point p1, Point p2, Color color, int oneway, boolean revisit) {

		double t = Math.atan2((double) p2.x - p1.x, (double) p2.y - p1.y);
		double cosT = 9 * Math.cos(t);
		double sinT = 9 * Math.sin(t);

		if (revisit) {
			// draw 3 separate lines
			g.setColor(new Color(0, 0, 0, 140));
			int[] xPointsMiddle = { (int) (p1.x + 0.3 * cosT), (int) (p2.x + 0.3 * cosT), (int) (p2.x - 0.3 * cosT),
					(int) (p1.x - 0.3 * cosT) };
			int[] yPointsMiddle = { (int) (p1.y - 0.3 * sinT), (int) (p2.y - 0.3 * sinT), (int) (p2.y + 0.3 * sinT),
					(int) (p1.y + 0.3 * sinT) };
			g.fillPolygon(xPointsMiddle, yPointsMiddle, 4);

			g.setColor(color);

			int[] xPointsBottom = { (int) (p1.x - cosT + 0.2 * cosT), (int) (p2.x - cosT + 0.2 * cosT),
					(int) (p2.x - 1.3 * cosT), (int) (p1.x - 1.3 * cosT) };
			int[] yPointsBottom = { (int) (p1.y + sinT - 0.2 * sinT), (int) (p2.y + sinT - 0.2 * sinT),
					(int) (p2.y + 1.3 * sinT), (int) (p1.y + 1.3 * sinT) };
			g.fillPolygon(xPointsBottom, yPointsBottom, 4);

			int[] xPointsTop = { (int) (p1.x + 1.3 * cosT), (int) (p2.x + 1.3 * cosT), (int) (p2.x + cosT - 0.2 * cosT),
					(int) (p1.x + cosT - 0.2 * cosT) };
			int[] yPointsTop = { (int) (p1.y - 1.3 * sinT), (int) (p2.y - 1.3 * sinT), (int) (p2.y - sinT + 0.2 * sinT),
					(int) (p1.y - sinT + 0.2 * sinT) };
			g.fillPolygon(xPointsTop, yPointsTop, 4);

		} else {
			int[] xPoints = { (int) (p1.x + cosT), (int) (p2.x + cosT), (int) (p2.x - cosT), (int) (p1.x - cosT) };
			int[] yPoints = { (int) (p1.y - sinT), (int) (p2.y - sinT), (int) (p2.y + sinT), (int) (p1.y + sinT) };
			g.setColor(color);
			g.fillPolygon(xPoints, yPoints, 4);
			g.fillOval(p1.x - 9, p1.y - 9, 18, 18);
			g.fillOval(p2.x - 9, p2.y - 9, 18, 18);
		}

		if (oneway != 0) {
			double middleX = (p1.x + p2.x) / 2.0;
			double middleY = (p1.y + p2.y) / 2.0;
			double cosTriangle = 6 * Math.cos(t);
			double sinTriangle = 6 * Math.sin(t);
			g.setColor(Color.WHITE);

			if (oneway > 0) {
				int[] xFillTriangle = { (int) (middleX + cosTriangle), (int) (middleX - cosTriangle),
						(int) (middleX + 2 * sinTriangle) };
				int[] yFillTriangle = { (int) (middleY - sinTriangle), (int) (middleY + sinTriangle),
						(int) (middleY + 2 * cosTriangle) };
				g.fillPolygon(xFillTriangle, yFillTriangle, 3);

				if (oneway == 2) {
					int[] xDrawTriangle = { (int) (middleX + cosTriangle), (int) (middleX - cosTriangle),
							(int) (middleX - 2 * sinTriangle) };
					int[] yDrawTriangle = { (int) (middleY - sinTriangle), (int) (middleY + sinTriangle),
							(int) (middleY - 2 * cosTriangle) };
					g.drawPolygon(xDrawTriangle, yDrawTriangle, 3);
				}
			}

			if (oneway < 0) {
				int[] xFillTriangle = { (int) (middleX + cosTriangle), (int) (middleX - cosTriangle),
						(int) (middleX - 2 * sinTriangle) };
				int[] yFillTriangle = { (int) (middleY - sinTriangle), (int) (middleY + sinTriangle),
						(int) (middleY - 2 * cosTriangle) };
				g.fillPolygon(xFillTriangle, yFillTriangle, 3);

				if (oneway == -2) {
					int[] xDrawTriangle = { (int) (middleX + cosTriangle), (int) (middleX - cosTriangle),
							(int) (middleX + 2 * sinTriangle) };
					int[] yDrawTriangle = { (int) (middleY - sinTriangle), (int) (middleY + sinTriangle),
							(int) (middleY + 2 * cosTriangle) };
					g.drawPolygon(xDrawTriangle, yDrawTriangle, 3);
				}
			}

		}

	}

	/**
	 * Draws a circle around the node
	 *
	 * @param n
	 *            The node
	 * @param color
	 *            The circle color
	 */
	@Override
	protected void drawNode(Node n, Color color) {
		if (mv == null || g == null) {
			return;
		}
		Point p = mv.getPoint(n);
		if (p == null) {
			return;
		}
		g.setColor(color);
		g.drawOval(p.x - 5, p.y - 5, 10, 10);

	}

	/**
	 * Draws s stop_position as a blue circle; draws a platform as a blue square
	 *
	 * @param primitive
	 *            primitive
	 */
	protected void drawStop(OsmPrimitive primitive, Boolean stopPosition) {

		// find the point to which the stop visualization will be linked:
		Node n = new Node(primitive.getBBox().getCenter());

		Point p = mv.getPoint(n);

		g.setColor(Color.BLUE);

		if (stopPosition) {
			g.fillOval(p.x - 8, p.y - 8, 16, 16);
		} else {
			g.fillRect(p.x - 8, p.y - 8, 16, 16);
		}

	}

	/**
	 * Draws the labels for the stops, which include the ordered position of the
	 * stop in the route and the ref numbers of other routes that use this stop
	 *
	 * @param primitive
	 *            primitive
	 * @param label
	 *            label
	 */
	protected void drawStopLabel(OsmPrimitive primitive, String label, Boolean platform) {

		// find the point to which the stop visualization will be linked:
		Node n = new Node(primitive.getBBox().getCenter());

		Point p = mv.getPoint(n);

		if (label != null && !label.equals("")) {
			Font stringFont = new Font("SansSerif", Font.PLAIN, 24);
			if (platform) {
				g.setColor(new Color(255, 255, 102));
				g.setFont(stringFont);
				g.drawString(label, p.x + 20, p.y - 40);
			} else {
				g.setColor(Color.WHITE);
				g.setFont(stringFont);
				g.drawString(label, p.x + 20, p.y - 20);
			}
		}

		// draw the ref values of all parent routes:
		List<String> parentsLabelList = new ArrayList<>();
		for (OsmPrimitive parent : primitive.getReferrers()) {
			if (parent.getType().equals(OsmPrimitiveType.RELATION)) {
				Relation relation = (Relation) parent;
				if (RouteUtils.isVersionTwoPTRoute(relation) && relation.get("ref") != null
						&& !relation.get("ref").equals("")) {

					boolean stringFound = false;
					for (String s : parentsLabelList) {
						if (s.equals(relation.get("ref"))) {
							stringFound = true;
						}
					}
					if (!stringFound) {
						parentsLabelList.add(relation.get("ref"));
					}

				}
			}
		}

		Collections.sort(parentsLabelList, new RefTagComparator());

		StringBuilder sb = new StringBuilder();
		for (String s : parentsLabelList) {
			sb.append(s).append(";");
		}

		if (sb.length() > 0) {
			// remove the last semicolon:
			String parentsLabel = sb.substring(0, sb.length() - 1);

			g.setColor(new Color(255, 20, 147));
			Font parentLabelFont = new Font("SansSerif", Font.ITALIC, 20);
			g.setFont(parentLabelFont);
			g.drawString(parentsLabel, p.x + 20, p.y + 20);
		}

	}

	/**
	 * Compares route ref numbers
	 *
	 * @author darya
	 *
	 */
	private static class RefTagComparator implements Comparator<String> {

		@Override
		public int compare(String s1, String s2) {

			if (s1 == null || s1.equals("") || s2 == null || s2.equals("")) {
				// if at least one of the strings is null or empty, there is no
				// point in comparing:
				return 0;
			}

			String[] splitString1 = s1.split("\\D");
			String[] splitString2 = s2.split("\\D");

			if (splitString1.length == 0 && splitString2.length != 0) {
				// if the first ref does not start a digit and the second ref
				// starts with a digit:
				return 1;
			}

			if (splitString1.length != 0 && splitString2.length == 0) {
				// if the first ref starts a digit and the second ref does not
				// start with a digit:
				return -1;
			}

			if (splitString1.length == 0 && splitString2.length == 0) {
				// if both ref values do not start with a digit:
				return s1.compareTo(s2);
			}

			String firstNumberString1 = splitString1[0];
			String firstNumberString2 = splitString2[0];

			try {
				int firstNumber1 = Integer.parseInt(firstNumberString1);
				int firstNumber2 = Integer.parseInt(firstNumberString2);
				if (firstNumber1 > firstNumber2) {
					return 1;
				} else if (firstNumber1 < firstNumber2) {
					return -1;
				} else {
					// if the first number is the same:

					return s1.compareTo(s2);

				}
			} catch (NumberFormatException ex) {
				return s1.compareTo(s2);
			}

		}

	}

	/**
	 * Visualizes the fix variants, assigns colors to them based on their order
	 *
	 * @param fixVariants
	 *            fix variants
	 */
	protected void visitFixVariants(HashMap<Character, List<PTWay>> fixVariants,
			HashMap<Way, List<Character>> wayColoring) {

		drawFixVariantsWithParallelLines(wayColoring);

		Color[] colors = { new Color(255, 0, 0, 150), new Color(0, 255, 0, 150), new Color(0, 0, 255, 150),
				new Color(255, 255, 0, 150), new Color(0, 255, 255, 150) };

		int colorIndex = 0;

		double letterX = MainApplication.getMap().mapView.getBounds().getMinX() + 20;
		double letterY = MainApplication.getMap().mapView.getBounds().getMinY() + 100;

		for (Entry<Character, List<PTWay>> entry : fixVariants.entrySet()) {
			Character c = entry.getKey();
			if (fixVariants.get(c) != null) {
				drawFixVariantLetter(c.toString(), colors[colorIndex % 5], letterX, letterY);
				colorIndex++;
				letterY = letterY + 60;
			}
		}

		// display the "Esc" label:
		if (!fixVariants.isEmpty()) {
			drawFixVariantLetter("Esc", Color.WHITE, letterX, letterY);
		}
	}

	@SuppressWarnings("unused")
	private void drawFixVariant(List<PTWay> fixVariant, Color color) {
		for (PTWay ptway : fixVariant) {
			for (Way way : ptway.getWays()) {
				for (Pair<Node, Node> nodePair : way.getNodePairs(false)) {
					drawSegment(nodePair.a, nodePair.b, color, 0, false);
				}
			}
		}
	}

	protected void drawFixVariantsWithParallelLines(Map<Way, List<Character>> wayColoring) {

		HashMap<Character, Color> colors = new HashMap<>();
		colors.put('A', new Color(255, 0, 0, 200));
		colors.put('B', new Color(0, 255, 0, 200));
		colors.put('C', new Color(0, 0, 255, 200));
		colors.put('D', new Color(255, 255, 0, 200));
		colors.put('E', new Color(0, 255, 255, 200));

		for (Entry<Way, List<Character>> entry : wayColoring.entrySet()) {
			Way way = entry.getKey();
			List<Character> letterList = wayColoring.get(way);
			List<Color> wayColors = new ArrayList<>();
			for (Character letter : letterList) {
				wayColors.add(colors.get(letter));
			}
			for (Pair<Node, Node> nodePair : way.getNodePairs(false)) {
				drawSegmentWithParallelLines(nodePair.a, nodePair.b, wayColors);
			}
		}
	}

	protected void drawSegmentWithParallelLines(Node n1, Node n2, List<Color> colors) {
		if (!n1.isDrawable() || !n2.isDrawable() || !isSegmentVisible(n1, n2)) {
			return;
		}

		Point p1 = mv.getPoint(n1);
		Point p2 = mv.getPoint(n2);
		double t = Math.atan2((double) p2.x - p1.x, (double) p2.y - p1.y);
		double cosT = 9 * Math.cos(t);
		double sinT = 9 * Math.sin(t);
		double heightCosT = 9 * Math.cos(t);
		double heightSinT = 9 * Math.sin(t);

		double prevPointX = p1.x;
		double prevPointY = p1.y;
		double nextPointX = p1.x + heightSinT;
		double nextPointY = p1.y + heightCosT;

		Color currentColor = colors.get(0);
		int i = 0;
		g.setColor(currentColor);
		g.fillOval(p1.x - 9, p1.y - 9, 18, 18);

		if (colors.size() == 1) {
			int[] xPoints = { (int) (p1.x + cosT), (int) (p2.x + cosT), (int) (p2.x - cosT), (int) (p1.x - cosT) };
			int[] yPoints = { (int) (p1.y - sinT), (int) (p2.y - sinT), (int) (p2.y + sinT), (int) (p1.y + sinT) };
			g.setColor(currentColor);
			g.fillPolygon(xPoints, yPoints, 4);
		} else {
			boolean iterate = true;
			while (iterate) {
				currentColor = colors.get(i % colors.size());

				int[] xPoints = { (int) (prevPointX + cosT), (int) (nextPointX + cosT), (int) (nextPointX - cosT),
						(int) (prevPointX - cosT) };
				int[] yPoints = { (int) (prevPointY - sinT), (int) (nextPointY - sinT), (int) (nextPointY + sinT),
						(int) (prevPointY + sinT) };
				g.setColor(currentColor);
				g.fillPolygon(xPoints, yPoints, 4);

				prevPointX = prevPointX + heightSinT;
				prevPointY = prevPointY + heightCosT;
				nextPointX = nextPointX + heightSinT;
				nextPointY = nextPointY + heightCosT;
				i++;
				if ((p1.x < p2.x && nextPointX >= p2.x) || (p1.x >= p2.x && nextPointX <= p2.x)) {
					iterate = false;
				}
			}

			int[] lastXPoints = { (int) (prevPointX + cosT), (int) (p2.x + cosT), (int) (p2.x - cosT),
					(int) (prevPointX - cosT) };
			int[] lastYPoints = { (int) (prevPointY - sinT), (int) (p2.y - sinT), (int) (p2.y + sinT),
					(int) (prevPointY + sinT) };
			g.setColor(currentColor);
			g.fillPolygon(lastXPoints, lastYPoints, 4);
		}

		g.setColor(currentColor);
		g.fillOval(p2.x - 9, p2.y - 9, 18, 18);
	}

	/**
	 * Visuallizes the letters for each fix variant
	 *
	 * @param letter
	 *            letter
	 * @param color
	 *            color
	 * @param letterX
	 *            letter X
	 * @param letterY
	 *            letter Y
	 */
	private void drawFixVariantLetter(String letter, Color color, double letterX, double letterY) {
		g.setColor(color);
		Font stringFont = new Font("SansSerif", Font.PLAIN, 50);
		g.setFont(stringFont);
		try {
			g.drawString(letter, (int) letterX, (int) letterY);
			g.drawString(letter, (int) letterX, (int) letterY);
		} catch (NullPointerException ex) {
			// do nothing
			Logging.trace(ex);
		}

	}

}
