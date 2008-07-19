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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openstreetmap.josmng.view.BBox;

/**
 * A Quadrant tree with unbounded number of node entries and limited depth.
 * The qtree can store both points and 2d-objects, but currently 2d objects
 * can end up too high in the tree (in case they cross high-order bit
 * coordinate axe).
 * In future, the tree can recognize objects stuck much higher than what
 * would correlate with their size and split them along the problematic axis.
 * 
 * @author nenik
 */
public class QTree {
    private final int maxDepth = 15;
    private final int countTres = 100;
    
    private int size;

    public QTree() {}

    // A root quadrant node = one split at (0,0)
    private LeafNode root = new TreeNode(0, 0);

    private class LeafNode extends ArrayList<View> {
        public LeafNode() {
            super(201);
        }
        
        void add(View object, BBox bbox, int depth) {
            add(object);
        }

        void remove(Object object, BBox r) {
            if (remove(object)) size--;
        }
        
        void gather(Set<View> target, BBox r) {
            target.addAll(this);
        }

        void trim() {
            trimToSize();
        }
    }
    
    private class TreeNode extends LeafNode {
        private int midX, midY;
        private LeafNode sw, nw, se, ne; // four subquadrants

        public TreeNode(int midX, int midY) {
            this.midX = midX;
            this.midY = midY;
        }        
        
        // 1:sw, 2:nw, 4:se, 8:ne
        private int getCoincidenceVector(BBox r) {
            return r.getCoincidenceVector(midX, midY);
        }

        @Override void add(View object, BBox bbox, int depth) {
            if (sw == null && nw == null && se == null && ne == null) {
                add(object);
                if (size() > countTres) {
                    // redistribute
                    List<View> keep = new ArrayList();
                    for (View t : this) {
                        if (!doAdd(t, t.getBounds(), depth+1)) keep.add(t);
                    }
                    if (sw == null && nw == null && se == null && ne == null) {
                        // all was local, mark as redist, just in case...
                        int shift = (0x80000000 >>> (depth+1));
                        sw = create(depth+1, midX - shift, midY - shift);
                    }
                    clear();
                    addAll(keep);
                }
            } else {
                if (!doAdd(object, bbox, depth+1)) add(object);
            }
        }
        
        private LeafNode create(int depth, int midX, int midY) {
            if (depth >= maxDepth) return new LeafNode();
            return new TreeNode(midX, midY);
        }
        
        boolean doAdd(View object, BBox r, int subDepth) {
            int vec = getCoincidenceVector(r);
            int shift = (0x80000000 >>> subDepth);

            LeafNode q = null;
            switch (vec) {
                case 1:
                    if (sw == null) sw = create(subDepth, midX - shift, midY - shift);
                    q = sw;
                    break;
                case 2:
                    if (nw == null) nw = create(subDepth, midX - shift, midY + shift);
                    q = nw;
                    break;
                case 4:
                    if (se == null) se = create(subDepth, midX + shift, midY - shift);
                    q = se;
                    break;
                case 8:
                    if (ne == null) ne = create(subDepth, midX + shift, midY + shift);
                    q = ne;
                    break;
                default:
                    return false;
            }
            q.add(object, r, subDepth);
            return true;
        }

        @Override void remove(Object object, BBox r) {
            if (remove(object)) {
                size--;
                return;
            }

            int vec = getCoincidenceVector(r);
            if (sw != null && (vec & 1) != 0) sw.remove(object, r);
            if (nw != null && (vec & 2) != 0) nw.remove(object, r);
            if (se != null && (vec & 4) != 0) se.remove(object, r);
            if (ne != null && (vec & 8) != 0) ne.remove(object, r);
        }
        
        @Override void gather(Set<View> target, BBox r) {
            target.addAll(this);
            
            int vec = getCoincidenceVector(r);
            if (sw != null && (vec & 1) != 0) sw.gather(target, r);
            if (nw != null && (vec & 2) != 0) nw.gather(target, r);
            if (se != null && (vec & 4) != 0) se.gather(target, r);
            if (ne != null && (vec & 8) != 0) ne.gather(target, r);
        }

        @Override void trim() {
            super.trim();
            if (sw != null) sw.trim();
            if (nw != null) nw.trim();
            if (se != null) se.trim();
            if (ne != null) ne.trim();
        }
    }
    
    public void add(View object) {
        root.add(object, object.getBounds(), 0);
        size++;
    }
    
    public void remove(View object) {
        root.remove(object, object.getBounds());       
    }
    
    public void gather(Set<View> target, BBox r) {
        root.gather(target, r);
    }
    
    public void trim() {
        root.trim();
    }
    
    public int getSize() {
        return size;
    }
}
