package relcontext.relationfix;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class AssociatedStreetFixer extends RelationFixer {

	public AssociatedStreetFixer() {
		super("associatedStreet");
	}

	@Override
	public boolean isRelationGood(Relation rel) {
		for (RelationMember m : rel.getMembers()) {
    		if (m.getType().equals(OsmPrimitiveType.NODE) && !"house".equals(m.getRole())) {
    		    setWarningMessage(tr("Node without 'house' role found"));
    			return false;
    		}
    		if (m.getType().equals(OsmPrimitiveType.WAY) && !("house".equals(m.getRole()) || "street".equals(m.getRole()))) {
    		    setWarningMessage(tr("Way without 'house' or 'street' role found"));
    		    return false;
    		}
    		if (m.getType().equals(OsmPrimitiveType.RELATION) && !"house".equals(m.getRole())) {
    		    setWarningMessage(tr("Relation without 'house' role found"));
    			return false;
    		}
    	}
		// relation should have name
		if (!rel.hasKey("name")) {
		    setWarningMessage(tr("Relation does not have name"));
			return false;
		}
		// check that all street members have same name as relation (???)
		String streetName = rel.get("name");
		if (streetName == null) streetName = "";
		for (RelationMember m : rel.getMembers()) {
			if ("street".equals(m.getRole()) && !streetName.equals(m.getWay().get("name"))) {
			    setWarningMessage(tr("Relation has streets with different names"));
			    return false;
			}
		}
		clearWarningMessage();
		return true;
	}

    @Override
	public Command fixRelation(Relation source) {
		// any way with highway tag -> street
		// any way/point/relation with addr:housenumber=* or building=* or type=multipolygon -> house
		// name - check which name is most used in street members and add to relation
		// copy this name to the other street members (???)
		Relation rel = new Relation(source);
		boolean fixed = false;

		for (int i = 0; i < rel.getMembersCount(); i++) {
			RelationMember m = rel.getMember(i);

			if (m.isNode()) {
				Node node = m.getNode();
				if (!"house".equals(m.getRole()) &&
						(node.hasKey("building") || node.hasKey("addr:housenumber"))) {
					fixed = true;
					rel.setMember(i, new RelationMember("house", node));
				}
			} else if (m.isWay()) {
				Way way = m.getWay();
				if (!"street".equals(m.getRole()) && way.hasKey("highway")) {
					fixed = true;
					rel.setMember(i, new RelationMember("street", way));
				} else if (!"house".equals(m.getRole()) &&
						(way.hasKey("building") || way.hasKey("addr:housenumber"))) {
					fixed = true;
					rel.setMember(i,  new RelationMember("house", way));
				}
			} else if (m.isRelation()) {
				Relation relation = m.getRelation();
				if (!"house".equals(m.getRole()) &&
						(relation.hasKey("building") || relation.hasKey("addr:housenumber") || "multipolygon".equals(relation.get("type")))) {
					fixed = true;
					rel.setMember(i, new RelationMember("house", relation));
				}
			}
		}

		// fill relation name
		Map<String, Integer> streetNames = new HashMap<String, Integer>();
		for (RelationMember m : rel.getMembers())
			if ("street".equals(m.getRole()) && m.isWay()) {
				String name = m.getWay().get("name");
				if (name == null || name.isEmpty()) continue;

				Integer count = streetNames.get(name);

				streetNames.put(name, count != null? count + 1 : 1);
			}
		String commonName = "";
		Integer commonCount = 0;
		for (Map.Entry<String, Integer> entry : streetNames.entrySet()) {
			if (entry.getValue() > commonCount) {
				commonCount = entry.getValue();
				commonName = entry.getKey();
			}
		}

		if (!rel.hasKey("name") && !commonName.isEmpty()) {
			fixed = true;
			rel.put("name", commonName);
		} else {
			commonName = ""; // set empty common name - if we already have name on relation, do not overwrite it
		}

		List<Command> commandList = new ArrayList<Command>();
		if (fixed) {
			commandList.add(new ChangeCommand(source, rel));
		}

		/*if (!commonName.isEmpty())
		// fill common name to streets
		for (RelationMember m : rel.getMembers())
			if ("street".equals(m.getRole()) && m.isWay()) {
				String name = m.getWay().get("name");
				if (commonName.equals(name)) continue;

				// TODO: ask user if he really wants to overwrite street name??

				Way oldWay = m.getWay();
				Way newWay = new Way(oldWay);
				newWay.put("name", commonName);

				commandList.add(new ChangeCommand(oldWay, newWay));
			}
		*/
		// return results
		if (commandList.size() == 0)
			return null;
		if (commandList.size() == 1)
			return commandList.get(0);
		return new SequenceCommand(tr("fix associatedStreet relation"), commandList);
	}
}
