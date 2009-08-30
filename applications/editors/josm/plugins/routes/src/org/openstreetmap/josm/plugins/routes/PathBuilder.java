package org.openstreetmap.josm.plugins.routes;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.routes.ConvertedWay.WayEnd;

public class PathBuilder {

	private Map<Way, BitSet> wayRoutes = new HashMap<Way, BitSet>(); 

	public void addWay(Way way, RouteDefinition route) {

		if (way.getNodesCount() >= 2) {
			BitSet routes = wayRoutes.get(way);
			if (routes == null) {
				routes = new BitSet();
				wayRoutes.put(way, routes);
			}
			routes.set(route.getIndex());
		}

	}

	public Collection<ConvertedWay> getConvertedWays() {
		Map<WayEnd, ConvertedWay> ways = new HashMap<WayEnd, ConvertedWay>();

		for (Entry<Way, BitSet> wayEntry:wayRoutes.entrySet()) {
			ConvertedWay way = new ConvertedWay(wayEntry.getValue(), wayEntry.getKey());

			ConvertedWay wayBefore = ways.get(way.getStart());
			ConvertedWay wayAfter = ways.get(way.getStop());

			if (wayBefore != null) {
				removeWay(ways, wayBefore);
				way.connect(wayBefore);
			} 

			if (wayAfter != null) {
				removeWay(ways, wayAfter);
				way.connect(wayAfter);
			}

			ways.put(way.getStart(), way);
			ways.put(way.getStop(), way);
		}

		Set<ConvertedWay> uniqueWays = new HashSet<ConvertedWay>();
		uniqueWays.addAll(ways.values());
		return uniqueWays;
	}

	private void removeWay(Map<WayEnd, ConvertedWay> map, ConvertedWay wayInMap) {
		map.remove(wayInMap.getStart());
		map.remove(wayInMap.getStop());
	}

	public void clear() {
		wayRoutes.clear();
	}

}
