// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

/**
 * Enumerates the two roles a "leg" in a turn restriction can have.
 */
public enum TurnRestrictionLegRole {
    FROM("from"),
    TO("to");

    private String osmRoleName;

    TurnRestrictionLegRole(String osmRoleName) {
        this.osmRoleName = osmRoleName;
    }

    public String getOsmRole() {
        return osmRoleName;
    }
}
