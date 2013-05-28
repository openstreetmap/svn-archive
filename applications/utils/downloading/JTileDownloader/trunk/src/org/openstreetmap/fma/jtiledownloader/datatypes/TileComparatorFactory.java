/*
 * Copyright 2011, Ilya Zverev
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

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
            int r = Integer.valueOf(t1.getZ()).compareTo(Integer.valueOf(t2.getZ()));
            if( r == 0 ) {
                r = Integer.valueOf(t1.getX()).compareTo(Integer.valueOf(t2.getX()));
                if( r == 0 )
                    r = Integer.valueOf(t1.getY()).compareTo(Integer.valueOf(t2.getY()));
            }
            return r;
        }
    }
    
    private static class QuadComparator implements Comparator<Tile> {
        public int compare(Tile t1, Tile t2) {
            int r = Integer.valueOf(t1.getZ()).compareTo(Integer.valueOf(t2.getZ()));
            if( r == 0 ) {
                r = Integer.valueOf(t1.getX() >> 3).compareTo(Integer.valueOf(t2.getX() >> 3));
                if( r == 0 ) {
                    r = Integer.valueOf(t1.getY() >> 3).compareTo(Integer.valueOf(t2.getY() >> 3));
                    if( r == 0 ) {
                        r = Integer.valueOf(t1.getX()).compareTo(Integer.valueOf(t2.getX()));
                        if (r == 0) {
                            r = Integer.valueOf(t1.getY()).compareTo(Integer.valueOf(t2.getY()));
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
