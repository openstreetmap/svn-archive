package org.openstreetmap.josm.plugins.pt_assistant.data;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;

public class PTStop extends RelationMember {

	private Node stopPosition = null;
	private OsmPrimitive platform = null;
	private Relation stopArea = null;
	
	/* Name of this stop */
	private String name = "";

	public PTStop(RelationMember other) throws IllegalArgumentException {

		super(other);

		if (other.getRole().equals("stop_position") && other.getType().equals(OsmPrimitiveType.NODE)) {
			this.stopPosition = other.getNode();
		} else if (other.getRole().equals("platform") || other.getRole().equals("platform_entry_only")
				|| other.getRole().equals("platform_exit_only")) {
			this.platform = other.getMember();
		} else if (other.getRole().equals("stop_area") && other.getType().equals(OsmPrimitiveType.RELATION)) {
			this.stopArea = other.getRelation();
		} else {
			throw new IllegalArgumentException("The RelationMember type does not match its role");
		}
		
		this.name = other.getMember().get("name");

	}

	/**
	 * Adds the given element to the stop after a check
	 * 
	 * @param member
	 *            Element to add
	 * @return true if added successfully, false otherwise. A false value
	 *         indicates either that the OsmPrimitiveType of the given
	 *         RelationMember does not match its role or that this PTStop
	 *         already has an attribute with that role.
	 */
	public boolean addStopElement(RelationMember member) {

		// each element is only allowed once per stop

		// add stop position:
		if (member.getRole().equals("stop_position")) {
			if (member.getType().equals(OsmPrimitiveType.NODE) && stopPosition == null) {
				this.stopPosition = member.getNode();
				return true;
			}
		}

		// add platform:
		if (member.getRole().equals("platform") || member.getRole().equals("platform_entry_only")
				|| member.getRole().equals("platform_exit_only")) {
			if (platform == null) {
				platform = member.getMember();
				return true;
			}
		}

		// add stop_area:
		if (member.getRole().equals("stop_area") && member.getType().equals(OsmPrimitiveType.RELATION)) {
			if (stopArea == null) {
				stopArea = member.getRelation();
				return true;
			}
		}

		return false;

	}
	
	public Node getStopPosition() {
		return this.stopPosition;
	}
	
	/**
	 * Returns platform (including platform_entry_only and platform_exit_only)
	 * @return
	 */
	public OsmPrimitive getPlatform() {
		return this.platform;
	}
	
	public Relation getStopArea() {
		return this.stopArea;
	}
	
	public String getName() {
		return this.name;
	}

}
