package org.openstreetmap.fma.jtiledownloader.datatypes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;

/**
 * This class compares two tiles in three possible ways:
 * <ol>
 * <li>Simply by order, z -&gt; x -&gt; y.
 * <li>By quad-tiles, z -&gt; (quad x) -&gt; (quad y) -&gt; x -&gt; y.
 * <li>By quad-tiles recursively (not supported yet &mdash; I have no idea how).
 * </ol>
 * 
 * @author zverik
 */
public class TileComparatorFactory {
    public static final int COMPARE_DONT = 0;
    public static final int COMPARE_SIMPLE = 1;
    public static final int COMPARE_QUAD = 2;
    public static final int COMPARE_RECURSIVE = 3;
    public static final int COMPARE_COUNT = 3;
    
    private static Comparator<Tile>[] comparators = new Comparator[] {
        new SimpleComparator(),
        new SimpleComparator(),
        new QuadComparator(),
        new RecursiveComparator()
    };
    
    public static Comparator<Tile> getComparator( int type ) {
        return comparators[type];
    }
    
    public static void sortTileList( List<Tile> tileList ) {
        int tileSortingPolicy = AppConfiguration.getInstance().getTileSortingPolicy();
        if (tileSortingPolicy > 0) {
            Collections.sort(tileList, TileComparatorFactory.getComparator(tileSortingPolicy));
        }
    }
    
    private static class SimpleComparator implements Comparator<Tile> {
        public int compare(Tile t1, Tile t2) {
            int r = Integer.compare(t1.getZ(), t2.getZ());
            if( r == 0 ) {
                r = Integer.compare(t1.getX(), t2.getX());
                if( r == 0 )
                    r = Integer.compare(t1.getY(), t2.getY());
            }
            return r;
        }
    }
    
    private static class QuadComparator implements Comparator<Tile> {
        public int compare(Tile t1, Tile t2) {
            int r = Integer.compare(t1.getZ(), t2.getZ());
            if( r == 0 ) {
                r = Integer.compare(t1.getX() >> 3, t2.getX() >> 3);
                if( r == 0 ) {
                    r = Integer.compare(t1.getY() >> 3, t2.getY() >> 3);
                    if( r == 0 ) {
                        r = Integer.compare(t1.getX(), t2.getX());
                        if (r == 0) {
                            r = Integer.compare(t1.getY(), t2.getY());
                        }
                    }
                }
            }
            return r;
        }
    }
    
    private static class RecursiveComparator implements Comparator<Tile> {
        public int compare(Tile t1, Tile t2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
