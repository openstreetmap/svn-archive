package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.regex.Pattern;

public interface Constants {
    String SEPARATOR = ";";
    String SPLIT_REGEX = "\\p{Zs}*[,:;]\\p{Zs}*";
    Pattern SPLIT_PATTERN = Pattern.compile(SPLIT_REGEX);
    
    String TYPE_LENGTHS = "turnlanes:lengths";
    
    String LENGTHS_KEY_LENGTHS_LEFT = "lengths:left";
    String LENGTHS_KEY_LENGTHS_RIGHT = "lengths:right";
    
    String TYPE_TURNS = "turnlanes:turns";
    
    String TURN_ROLE_VIA = "via";
    String TURN_ROLE_FROM = "from";
    String TURN_ROLE_TO = "to";
    
    String TURN_KEY_LANES = "lanes";
    String TURN_KEY_EXTRA_LANES = "lanes:extra";
    String LENGTHS_ROLE_END = "end";
    String LENGTHS_ROLE_WAYS = "ways";
    
}
