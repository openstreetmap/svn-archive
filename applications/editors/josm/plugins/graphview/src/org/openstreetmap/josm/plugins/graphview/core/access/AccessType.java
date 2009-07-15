package org.openstreetmap.josm.plugins.graphview.core.access;

public enum AccessType {

	YES("yes"),
	PERMISSIVE("permissive"),
	DESIGNATED("designated"),
	DESTINATION("destination"),
	AGRICULTURAL("agricultural"),
	FORESTRY("forestry"),
	DELIVERY("delivery"),
	PRIVATE("private"),
	NO("no"),
	UNDEFINED();

	private String[] valueStrings;
	private AccessType(String... valueStrings) {
		this.valueStrings = valueStrings;
	}

	/**
	 * returns the AccessType that fits for a tag's value
	 *
	 * @param valueString  a tag's value; != null
	 * @return             AccessType for the value; != null, will be UNDEFINED for unknown values
	 */
	public static AccessType getAccessType(String valueString) {
		for (AccessType accessType : AccessType.values()) {
			for (String typeValueString : accessType.valueStrings) {
				if (typeValueString.equals(valueString)) {
					return accessType;
				}
			}
		}
		return UNDEFINED;
	}

}
