package org.openstreetmap.josm.plugins.JunctionChecker.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.JunctionChecker.JunctionCheckerPlugin;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;

/**
 * Hilfsklasse zum Umwandeln eines CHannels-Hashsets (welches eine Kreuzung repräsentiert) in eine gültige Relation auf den DigraphLayer Relation wird nur dann erzeugt, wenn sie noch nicht existiert
 * @author  joerg
 */
public class RelationProducer {

	private JunctionCheckerPlugin plugin;
	private HashSet<HashSet<Channel>> storedRelations;

	public RelationProducer(JunctionCheckerPlugin plugin) {
		this.plugin = plugin;
		storedRelations = new HashSet<HashSet<Channel>>();
	}

	public void produceRelation(HashSet<Channel> subset, int n) {
		if (isProduced(subset)) {
			return;
		}
		LinkedList<OsmPrimitive> ways = new LinkedList<OsmPrimitive>();
		Iterator<Channel> cit = subset.iterator();
		while (cit.hasNext()) {
			Channel c = cit.next();
			// System.out.println(c.getWay().getId());
			if (!(ways.contains(plugin.getOsmlayer().data.getPrimitiveById(c
					.getWay().getId(), OsmPrimitiveType.WAY)))) {
				ways.add(plugin.getOsmlayer().data.getPrimitiveById(c.getWay()
						.getId(), OsmPrimitiveType.WAY));
			}
		}
		Main.map.mapView.setActiveLayer(plugin.getOsmlayer());
		plugin.getOsmlayer().data.setSelected(ways);

		Relation jrelation = new Relation();
		jrelation.put("type", "junction");
		jrelation.put("n", Integer.toString(n));
		for (int i = 0; i < ways.size(); i++) {
			jrelation.addMember(new RelationMember("part of", ways.get(i)));
		}

		plugin.getOsmlayer().data.addPrimitive(jrelation);
	}

	private boolean isProduced(HashSet<Channel> subset) {
		if (storedRelations.contains(subset)) {
			return true;
		} else {
			storedRelations.add(subset);
		}
		return false;
	}
}
