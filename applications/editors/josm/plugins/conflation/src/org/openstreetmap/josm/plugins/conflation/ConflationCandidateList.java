/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.conflation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 *  Holds a list of conflation candidates and provides convenience functions.
 */
public class ConflationCandidateList implements Iterable<ConflationCandidate> {
    private CopyOnWriteArrayList<ConflationListChangedListener> listeners = new CopyOnWriteArrayList<ConflationListChangedListener>();

    List<ConflationCandidate> candidates;

    public ConflationCandidateList() {
        candidates = new LinkedList<ConflationCandidate>();
    }

    public boolean hasCandidate(ConflationCandidate c) {
        return hasCandidateForSource(c.getSourcePrimitive());
    }

    public boolean hasCandidate(OsmPrimitive src, OsmPrimitive tgt) {
        return hasCandidateForSource(src) || hasCandidateForTarget(tgt);
    }

    public boolean hasCandidateForSource(OsmPrimitive src) {
        return getCandidateBySource(src) != null;
    }

    public boolean hasCandidateForTarget(OsmPrimitive tgt) {
        return getCandidateByTarget(tgt) != null;
    }

    public ConflationCandidate getCandidateBySource(OsmPrimitive src) {
        for (ConflationCandidate c : candidates) {
            if (c.getSourcePrimitive() == src) {
                return c;
            }
        }
        return null;
    }

    public ConflationCandidate getCandidateByTarget(OsmPrimitive tgt) {
        for (ConflationCandidate c : candidates) {
            if (c.getTargetPrimitive() == tgt) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Iterator<ConflationCandidate> iterator() {
        return candidates.iterator();
    }

    public void add(ConflationCandidate c) {
        candidates.add(c);
        fireSelectionChanged();
    }

    public int size() {
        return candidates.size();
    }

    public ConflationCandidate get(int index) {
        return candidates.get(index);
    }

    public ConflationCandidate remove(int index) {
        return candidates.remove(index);
    }
    
    public void clear() {
        candidates.clear();
        fireSelectionChanged();
    }

    public boolean remove(ConflationCandidate c) {
        boolean ret = candidates.remove(c);
        fireSelectionChanged();
        return ret;
    }
    
    public void addConflationListChangedListener(ConflationListChangedListener listener) {
        listeners.addIfAbsent(listener);
    }

    public void fireSelectionChanged(){
        for (ConflationListChangedListener l : listeners) {
            l.conflationListChanged(this);
        }
    }
}
