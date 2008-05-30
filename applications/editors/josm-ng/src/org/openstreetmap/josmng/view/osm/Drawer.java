/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view.osm;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class used for collecting and sequentially painting all to-be-painted
 * {@link Part}s from individual views. It keeps a number of buckets to collect
 * the Parts according to their requested z-order and draws the image only once
 * all the Parts get collected, thus honouring the intended z-order globally. 
 * 
 * Note: The name Drawer actually cover two common meanings of the word, as it
 * both collects paint requests like "into a drawer" for later rendering,
 * and it actually draws the final image of the layer.
 * 
 * @author nenik
 */
final class Drawer {
    private static final Logger LOG = Logger.getLogger(Drawer.class.getName());
    
    private List<Part>[] buckets = new List[255];

    Drawer() {
        for (int i=0; i < buckets.length; i++) {
            buckets[i] = new ArrayList<Part>();
        }
    }
    
    /**
     * Store a drawing {@link Part} into the layer z for layered rendering. 
     * @param z The layer (z-order) for the Part.
     * @param part the Part to render later.
     */
    public void put(int z, Part part) {
        assert z >= 0;
        assert z < buckets.length;
        
        buckets[z].add(part);
    }
    
    /**
     * Draw all the parts according to their z-order. The rendering is performed
     * by drawing the parts with lower z-value before the parts with higher
     * z-value, thus the parts with higher z paint over the parts with lower z.
     * 
     * @param g the graphics to paint through. The method expects graphics
     * preset as wanted (e.g. antialising enabled) and doesn't change
     * such global setup.
     * @return the number of parts painted (for statistic purposes only).
     */
    public void draw(Graphics2D g2d) {
        long zero = System.currentTimeMillis();
        
        int total = 0;
        for (int i = 0; i< buckets.length; i++) {
            long time = System.currentTimeMillis();
            List<Part> bucket = buckets[i];
            int count = 0;
            for (Part part : bucket) {
                count++;
                part.paint((Graphics2D)g2d.create());
            }
            if (count > 0 && LOG.isLoggable(Level.FINEST)) {
                time = System.currentTimeMillis() - time;
                LOG.finest(String.format("Layer %d (%d items) took %dms.", i, count, time));
            }
            total += count;
        }
        LOG.finer(String.format("Painted %d items in %dms.", total, System.currentTimeMillis()-zero));
    }
}
