package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * This is the enumeration of turn restriction types, see 
 * <a href="http://wiki.openstreetmap.org/wiki/Turn_restriction">OSM Wiki</a>
 * 
 */
public enum TurnRestrictionType {
    NO_RIGHT_TURN("no_right_turn", tr("No Right Turn")),
    NO_LEFT_TURN("no_left_turn", tr("No Left Turn")),
    NO_U_TURN("no_u_turn", tr("No U-Turn")),
    NO_STRAIGHT_ON("no_straight_on", tr("No Straight On")), 
    ONLY_RIGHT_TURN("only_right_turn", tr("Only Right Turn")),
    ONLY_LEFT_TURN("only_left_turn", tr("Only Left Turn")),
    ONLY_STRAIGHT_ON("only_straight_on", tr("Only Straight On"));
    
    private String tagValue;
    private String displayName;
    
    TurnRestrictionType(String tagValue, String displayName) {
        this.tagValue = tagValue;
        this.displayName = displayName;
    }
    
    /**
     * Replies the tag value for a specific turn restriction type
     * 
     * @return the tag value for a specific turn restriction type
     */
    public String getTagValue() {
        return tagValue;
    }
    
    /**
     * Replies the localized display name for a turn restriction type
     */
    public String getDisplayName() {
        return displayName;
    }   
    
    /**
     * Replies the enumeration value for a given tag value. null,
     * if {@code tagValue} is null or if there isnt an enumeration value
     * for this {@code tagValue}
     *  
     * @param tagValue the tag value, i.e. <tt>no_left_turn</tt>
     * @return the enumeration value
     */
    static public TurnRestrictionType fromTagValue(String tagValue) {
        if (tagValue == null) return null;
        for(TurnRestrictionType type: values()) {
            if(type.getTagValue().equals(tagValue)) return type;
        }
        return null;
    }

    /**
     * Replies true if {@code tagValue} is a standard restriction type. 
     * 
     * @param tagValue the tag value 
     * @return true if {@code tagValue} is a standard restriction type
     */
    static public boolean isStandardTagValue(String tagValue){
        return fromTagValue(tagValue) != null;
    }
}
