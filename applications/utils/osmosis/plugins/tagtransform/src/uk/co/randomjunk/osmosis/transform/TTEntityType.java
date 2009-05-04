package uk.co.randomjunk.osmosis.transform;

import com.bretth.osmosis.core.domain.v0_6.EntityType;

public enum TTEntityType {

	NODE, WAY, RELATION;
	
	
	public EntityType getEntityType0_6() {
		switch ( this ) {
		case NODE: return EntityType.Node;
		case WAY: return EntityType.Way;
		case RELATION: return EntityType.Relation;
		default: return null;
		}
	}
	
	public com.bretth.osmosis.core.domain.v0_5.EntityType getEntityType0_5() {
		switch ( this ) {
		case NODE: return com.bretth.osmosis.core.domain.v0_5.EntityType.Node;
		case WAY: return com.bretth.osmosis.core.domain.v0_5.EntityType.Way;
		case RELATION: return com.bretth.osmosis.core.domain.v0_5.EntityType.Relation;
		default: return null;
		}
	}
	
	public static TTEntityType fromEntityType0_6(EntityType entityType) {
		switch ( entityType ) {
		case Node: return NODE;
		case Way: return WAY;
		case Relation: return RELATION;
		default: return null;
		}
	}
	
	public static TTEntityType fromEntityType0_5(com.bretth.osmosis.core.domain.v0_5.EntityType entityType) {
		switch ( entityType ) {
		case Node: return NODE;
		case Way: return WAY;
		case Relation: return RELATION;
		default: return null;
		}
	}
}
