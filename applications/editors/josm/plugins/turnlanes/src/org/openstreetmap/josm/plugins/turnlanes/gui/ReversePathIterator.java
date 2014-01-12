package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.awt.geom.PathIterator;
import java.util.Arrays;

/**
 * Path iterator which reverses the path of a given iterator.
 * 
 * <p>
 * The given (unreversed) iterator must start with a {@code PathIterator.SEG_MOVETO} and must not
 * contain any {@code PathIterator.SEG_CLOSE}. This class is intended for use with iterators
 * returned by {@code Path.getIterator} which has exactly those properties.
 * </p>
 * 
 * @author Ben Schulz
 */
class ReversePathIterator implements PathIterator {
    private static final int[] COUNTS = {
        2, // SEG_MOVETO = 0
        2, // SEG_LINETO = 1
        4, // SEG_QUADTO = 2
        6, // SEG_CUBICTO = 3
        0, // SEG_CLOSE = 4
    };
    
    public static ReversePathIterator reverse(PathIterator it) {
        return new ReversePathIterator(it);
    }
    
    private static int[] reverseTypes(int[] types, int length) {
        if (length > 0 && types[0] != SEG_MOVETO) {
            // the last segment of the reversed path is not defined
            throw new IllegalArgumentException("Can not reverse path without initial SEG_MOVETO.");
        }
        
        final int[] result = new int[length];
        
        result[0] = SEG_MOVETO;
        
        int lower = 1;
        int upper = length - 1;
        
        while (lower <= upper) {
            result[lower] = types[upper];
            result[upper] = types[lower];
            
            ++lower;
            --upper;
        }
        
        return result;
    }
    
    private static double[] reverseCoords(double[] coords, int length) {
        final double[] result = new double[length];
        
        int lower = 0;
        int upper = length - 2;
        
        while (lower <= upper) {
            result[lower] = coords[upper];
            result[lower + 1] = coords[upper + 1];
            result[upper] = coords[lower];
            result[upper + 1] = coords[lower + 1];
            
            lower += 2;
            upper -= 2;
        }
        
        return result;
    }
    
    private final int winding;
    
    private final int[] types;
    private int typesIndex = 0;
    
    private final double[] coords;
    private int coordsIndex = 0;
    
    private ReversePathIterator(PathIterator it) {
        this.winding = it.getWindingRule();
        
        double[] tmpCoords = new double[62];
        int[] tmpTypes = new int[11];
        
        int tmpCoordsI = 0;
        int tmpTypesI = 0;
        
        while (!it.isDone()) {
            if (tmpTypesI >= tmpTypes.length) {
                tmpTypes = Arrays.copyOf(tmpTypes, 2 * tmpTypes.length);
            }
            
            final double[] cs = new double[6];
            final int t = it.currentSegment(cs);
            tmpTypes[tmpTypesI++] = t;
            final int count = COUNTS[t];
            
            if (tmpCoordsI + count > tmpCoords.length) {
                tmpCoords = Arrays.copyOf(tmpCoords, 2 * tmpCoords.length);
            }
            System.arraycopy(cs, 0, tmpCoords, tmpCoordsI, count);
            tmpCoordsI += count;
            
            it.next();
        }
        
        this.types = reverseTypes(tmpTypes, tmpTypesI);
        this.coords = reverseCoords(tmpCoords, tmpCoordsI);
    }
    
    @Override
    public int getWindingRule() {
        return winding;
    }
    
    @Override
    public boolean isDone() {
        return typesIndex >= types.length;
    }
    
    @Override
    public void next() {
        coordsIndex += COUNTS[types[typesIndex]];
        ++typesIndex;
    }
    
    @Override
    public int currentSegment(float[] coords) {
        final double[] tmp = new double[6];
        final int type = currentSegment(tmp);
        
        coords[0] = (float) tmp[0];
        coords[1] = (float) tmp[1];
        coords[2] = (float) tmp[2];
        coords[3] = (float) tmp[3];
        coords[4] = (float) tmp[4];
        coords[5] = (float) tmp[5];
        
        return type;
    }
    
    @Override
    public int currentSegment(double[] coords) {
        final int type = types[typesIndex];
        System.arraycopy(this.coords, coordsIndex, coords, 0, COUNTS[type]);
        return type;
    }
}
