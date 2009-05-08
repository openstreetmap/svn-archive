package uk.co.randomjunk.osmosis.transform;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

public enum TTEntityType {

	NODE, WAY, RELATION, BOUND;
	
	
	public EntityType getEntityType0_6() {
		switch ( this ) {
		case NODE: return EntityType.Node;
		case WAY: return EntityType.Way;
		case RELATION: return EntityType.Relation;
		case BOUND: return EntityType.Bound;
		default: return null;
		}
	}
	
	public org.openstreetmap.osmosis.core.domain.v0_5.EntityType getEntityType0_5() {
		switch ( this ) {
		case NODE: return org.openstreetmap.osmosis.core.domain.v0_5.EntityType.Node;
		case WAY: return org.openstreetmap.osmosis.core.domain.v0_5.EntityType.Way;
		case RELATION: return org.openstreetmap.osmosis.core.domain.v0_5.EntityType.Relation;
		default: return null;
		}
	}
	
	public static TTEntityType fromEntityType0_6(EntityType entityType) {
		switch ( entityType ) {
		case Node: return NODE;
		case Way: return WAY;
		case Relation: return RELATION;
		case Bound: return BOUND;
		default: return null;
		}
	}
	
	public static TTEntityType fromEntityType0_5(org.openstreetmap.osmosis.core.domain.v0_5.EntityType entityType) {
		switch ( entityType ) {
		case Node: return NODE;
		case Way: return WAY;
		case Relation: return RELATION;
		default: return null;
		}
	}
}
