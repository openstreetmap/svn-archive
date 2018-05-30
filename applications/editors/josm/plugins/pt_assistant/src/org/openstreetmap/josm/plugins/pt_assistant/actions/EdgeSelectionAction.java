// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayer;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The action allows to select a set of consecutive ways at once in order to
 * speed up the mapper. The selected ways are going to be coherent to the
 * current route the mapper is working on.
 *
 * @author giacomo
 */
public class EdgeSelectionAction extends MapMode {

	private static final String MAP_MODE_NAME = "Edge Selection";
	private static final long serialVersionUID = 2414977774504904238L;

	private transient Set<Way> highlighted;

	private Cursor selectionCursor;
	private Cursor waySelectCursor;
	private List<Way> edgeList;
	private String modeOfTravel = null;

	public EdgeSelectionAction() {
		super(tr(MAP_MODE_NAME), "edgeSelection", tr(MAP_MODE_NAME), Shortcut.registerShortcut("mapmode:edge_selection",
				tr("Mode: {0}", tr(MAP_MODE_NAME)), KeyEvent.VK_K, Shortcut.CTRL),
				ImageProvider.getCursor("normal", "selection"));
		highlighted = new HashSet<>();
		edgeList = new ArrayList<>();

		selectionCursor = ImageProvider.getCursor("normal", "selection");
		waySelectCursor = ImageProvider.getCursor("normal", "select_way");
	}

	/*
	 * given a way, it looks at both directions for good candidates to be added to
	 * the edge
	 */
	private List<Way> getEdgeFromWay(Way initial, String modeOfTravel) {
		List<Way> edge = new ArrayList<>();
		if (!isWaySuitableForMode(initial, modeOfTravel))
			return edge;

		Way curr = initial;
		while (true) {
			List<Way> options = curr.firstNode(true).getParentWays();
			if (curr.firstNode().hasTag("public_transport", "stop_position")) {
				break;
			}
			options.remove(curr);
			curr = chooseBestWay(options, modeOfTravel);
			if (curr == null || edge.contains(curr))
				break;
			edge.add(curr);
		}

		curr = initial;
		while (true) {
			List<Way> options = curr.lastNode(true).getParentWays();
			if (curr.lastNode().hasTag("public_transport", "stop_position")) {
				break;
			}
			options.remove(curr);
			curr = chooseBestWay(options, modeOfTravel);
			if (curr == null || edge.contains(curr))
				break;
			edge.add(curr);
		}

		edge.add(initial);
		edge = sortEdgeWays(edge);
		return edge;
	}

	private List<Way> sortEdgeWays(List<Way> edge) {
		List<RelationMember> members = edge.stream().map(w -> new RelationMember("", w)).collect(Collectors.toList());
		List<RelationMember> sorted = new RelationSorter().sortMembers(members);
		return sorted.stream().map(RelationMember::getWay).collect(Collectors.toList());
	}

	private Boolean isWaySuitableForMode(Way way, String modeOfTravel) {
		if ("bus".equals(modeOfTravel))
			return RouteUtils.isWaySuitableForBuses(way);

		if ("bicycle".equals(modeOfTravel))
			return RouteUtils.isWaySuitableForBicycle(way);

		if ("foot".equals(modeOfTravel)) {
			return way.hasTag("highway", "footway") || !(way.hasKey("highway", "motorway") || way.hasKey("foot", "no")
					|| way.hasKey("foot", "use_sidepath"));
		}

		// if ("hiking".equals(modeOfTravel))
		// return RouteUtils.isWaySuitableForBuses(toCheck);
		//
		if ("horse".equals(modeOfTravel))
			return true;

		if ("light_rail".equals(modeOfTravel))
			return way.hasTag("railway", "light_rail");

		if ("railway".equals(modeOfTravel))
			return way.hasKey("railway");

		if ("subway".equals(modeOfTravel))
			return way.hasTag("railway", "subway");

		if ("train".equals(modeOfTravel))
			return way.hasTag("railway", "rail");

		if ("tram".equals(modeOfTravel))
			return way.hasTag("railway", "tram");

		if ("trolleybus".equals(modeOfTravel)) {
			return way.hasTag("trolley_wire", "yes");
		}

		return RouteUtils.isWaySuitableForPublicTransport(way);
	}

	/*
	 *
	 */
	private Way chooseBestWay(List<Way> ways, String modeOfTravel) {
		ways.removeIf(w -> !isWaySuitableForMode(w, modeOfTravel));
		if (ways.isEmpty())
			return null;
		if (ways.size() == 1)
			return ways.get(0);

		Way theChoosenOne = null;

		// if ("bus".equals(modeOfTravel)) {
		//
		// }
		// if ("tram".equals(modeOfTravel)) {
		//
		// }

		return theChoosenOne;
	}

	private String getModeOfTravel(Way initial) {
		// find a way to get the currently opened relation editor and get the
		// from there the current type of route
		List<Layer> layers = MainApplication.getLayerManager().getLayers();
		for (Layer layer : layers) {
			if (layer.getName().equals("pt_assistant layer")) {
				PTAssistantLayer PTL = (PTAssistantLayer) layer;
				if (PTL.getModeOfTravel() != null)
					return PTL.getModeOfTravel();
			}
		}
		return "bus";
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		DataSet ds = MainApplication.getLayerManager().getEditDataSet();
		Way initial = MainApplication.getMap().mapView.getNearestWay(e.getPoint(), OsmPrimitive::isUsable);

		updateKeyModifiers(e);

		if (!shift && !ctrl) {
			/*
			 * remove all previous selection and just add the latest selection
			 */
			edgeList.clear();
			ds.clearSelection();
			if (initial != null) {
				modeOfTravel = getModeOfTravel(initial);
				List<Way> edge = getEdgeFromWay(initial, modeOfTravel);
				for (Way way : edge) {
					if (!edgeList.contains(way))
						edgeList.add(way);
				}
				edgeList.addAll(edge);
				ds.setSelected(edgeList);
				AutoScaleAction.zoomTo(edge.stream().map(w -> (OsmPrimitive) w).collect(Collectors.toList()));
			}

		} else if (!shift && ctrl && initial != null) {
			/*
			 * toggle mode where we can individually select and deselect the edges
			 */
			if (edgeList.size() == 0 || modeOfTravel == null) {
				modeOfTravel = getModeOfTravel(initial);
			}

			List<Way> edge = getEdgeFromWay(initial, modeOfTravel);
			List<Way> newEdges = new ArrayList<>();
			if (edgeList.containsAll(edge)) {
				for (Way way : edge) {
					if (edgeList.contains(way))
						edgeList.remove(way);
				}
			} else {
				for (Way way : edge) {
					if (!edgeList.contains(way)) {
						edgeList.add(way);
						newEdges.addAll(findNewEdges(way, edge, edgeList));
					}
				}
				if (newEdges != null) {
					System.out.println("new"+newEdges.size());
					List<Way> waysToBeRemoved = waysToBeRemoved(newEdges);
					if (waysToBeRemoved != null) {
						newEdges.removeAll(waysToBeRemoved);
					}
					edgeList.addAll(newEdges);
				}
			}
			ds.clearSelection();
			ds.setSelected(edgeList);
			AutoScaleAction.zoomTo(edge.stream().map(w -> (OsmPrimitive) w).collect(Collectors.toList()));
		} else if (shift && !ctrl && initial != null) {
			/*
			 * add new selection to existing edges
			 */
			if (edgeList.size() == 0 || modeOfTravel == null) {
				modeOfTravel = getModeOfTravel(initial);
			}
			if (initial != null) {
				List<Way> edge = getEdgeFromWay(initial, modeOfTravel);
				List<Way> newEdges = new ArrayList<>();

				for (Way way : edge) {
					if (!edgeList.contains(way)) {
						edgeList.add(way);
						newEdges.addAll(findNewEdges(way, edge, edgeList));
					}
				}

				if (newEdges != null) {
					System.out.println("new"+newEdges.size());
					List<Way> waysToBeRemoved = waysToBeRemoved(newEdges);
					if (waysToBeRemoved != null) {
						newEdges.removeAll(waysToBeRemoved);
					}
					edgeList.addAll(newEdges);
				}

				ds.setSelected(edgeList);
				AutoScaleAction.zoomTo(edge.stream().map(w -> (OsmPrimitive) w).collect(Collectors.toList()));
			}
		}

	}

	private List<Way> waysToBeRemoved(List<Way> newEdges) {

		List<Way> waysToBeRemoved = new ArrayList<>();

		for (int i = 0; i < newEdges.size(); i++) {
			Node node1 = newEdges.get(i).firstNode();
			Node node2 = newEdges.get(i).lastNode();
			for (int j = i + 1; j < newEdges.size(); j++) {
				if (newEdges.get(i).equals(newEdges.get(j)))
					continue;
				Node node3 = newEdges.get(j).firstNode();
				Node node4 = newEdges.get(j).lastNode();

				if (node1.equals(node3) && node2.equals(node4)) {
					if (!waysToBeRemoved.contains(newEdges.get(i)))
						waysToBeRemoved.add(newEdges.get(i));
					if (!waysToBeRemoved.contains(newEdges.get(j)))
						waysToBeRemoved.add(newEdges.get(j));

				} else if (node1.equals(node4) && node2.equals(node3)) {
					if (!waysToBeRemoved.contains(newEdges.get(i)))
						waysToBeRemoved.add(newEdges.get(i));
					if (!waysToBeRemoved.contains(newEdges.get(j)))
						waysToBeRemoved.add(newEdges.get(j));
				}
			}
		}
		System.out.println("remove"+waysToBeRemoved.size());
		return waysToBeRemoved;
	}

	private List<Way> findNewEdges(Way way, List<Way> edge, List<Way> edgeList) {
		List<Way> newEdges = new ArrayList<>();

		Node firstNode = way.firstNode();
		Node lastNode = way.lastNode();

		List<Way> parentWayList1 = firstNode.getParentWays();
		parentWayList1.removeAll(edgeList);
		parentWayList1.removeAll(edge);

		List<Way> parentWayList2 = lastNode.getParentWays();
		parentWayList2.removeAll(edgeList);
		parentWayList2.removeAll(edge);

		parentWayList1.addAll(parentWayList2);

		for (Way parentWay : parentWayList1) {
			if (edge.contains(parentWay) || edgeList.contains(parentWay))
				continue;

			Node node1 = parentWay.firstNode();
			Node node2 = parentWay.lastNode();
			for (Way oldWay : edgeList) {
				if (!oldWay.equals(way)) {
					if ((oldWay.containsNode(node1) && !way.containsNode(node1))
							|| (oldWay.containsNode(node2) && !way.containsNode(node2))) {
						newEdges.add(parentWay);
					}

				}
			}
		}
		return newEdges;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		for (Way way : highlighted) {
			way.setHighlighted(false);
		}
		highlighted.clear();

		Way initial = MainApplication.getMap().mapView.getNearestWay(e.getPoint(), OsmPrimitive::isUsable);
		if (initial == null) {
			MainApplication.getMap().mapView.setCursor(selectionCursor);
		} else {
			MainApplication.getMap().mapView.setCursor(waySelectCursor);
			highlighted.addAll(getEdgeFromWay(initial, modeOfTravel));
		}

		for (Way way : highlighted) {
			way.setHighlighted(true);
		}
	}

	@Override
	public void enterMode() {
		super.enterMode();
		MainApplication.getMap().mapView.addMouseListener(this);
		MainApplication.getMap().mapView.addMouseMotionListener(this);
	}

	@Override
	public void exitMode() {
		super.exitMode();
		MainApplication.getMap().mapView.removeMouseListener(this);
		MainApplication.getMap().mapView.removeMouseMotionListener(this);
	}

}
