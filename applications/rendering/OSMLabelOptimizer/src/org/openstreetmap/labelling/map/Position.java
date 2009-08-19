/**
 * 
 */
package org.openstreetmap.labelling.map;

/**
 * @author sebi
 *
 */
public enum Position
{
    BASELINE_EAST(0),
    TOPLINE_EAST(0.175),
    BASELINE_WEST(0.4),
    TOPLINE_WEST(0.6),
    BASELINE_NORTH_EAST(0.15),
    TOPLINE_SOUTH_EAST(0.2),
    BASELINE_NORTH_WEST(0.575),
    BASELINE_HALF_NORTH(0.8),
    BASELINE_HALF_DESCENDER_NORTH(0.8 + 0.25),
    TOPLINE_HALF_SOUTH(0.9),
    BASELINE_ONE_THIRD_NORTH(0.825),
    BASELINE_ONE_THIRD_DESCENDER_NORTH(0.925 + 0.25),
    TOPLINE_ONE_THIRD_SOUTH(0.95),
    BASELINE_TWO_THIRD_NORTH(0.875),
    BASELINE_TWO_THIRD_DESCENDER_NORTH(0.875 + 0.25),
    TOPLINE_TWO_THIRD_SOUTH(1),
    X_HEIGHT_LINE_EAST(0.1),
    X_HEIGHT_LINE_WEST(0.5),
    HALF_X_HEIGHT_LINE_EAST(0.07),
    HALF_X_HEIGHT_LINE_WEST(0.47);
    
    private final double value;
    
    Position(double value)
    {
        this.value = value;
    }
    
    public double getValue()
    {
        return value;
    }
}
