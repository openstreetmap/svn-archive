/*
 *      Type.java
 *      
 *      Copyright 2010 Hind <foxhind@gmail.com>
 *      
 *      NODE     - Node
 *      WAY      - Way
 *      RELATION - Relation
 *      ANY      - Any osm object (node, way or relation)
 *      POINT    - Coordinates like as Lon,Lat
 *      LENGTH   - Fractional number
 *      NATURAL  - Natural number (1, 10, 9000)
 *      STRING   - Text string
 *      TRIGGER  - Yes/No
 *      RELAY    - 
 * 
 */
 
package commandline;

public enum Type { NODE, WAY, RELATION, ANY, POINT, LENGTH, NATURAL, STRING, TRIGGER, RELAY }
