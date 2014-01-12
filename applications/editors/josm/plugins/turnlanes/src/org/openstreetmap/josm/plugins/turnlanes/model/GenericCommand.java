package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.AbstractPrimitive;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveData;

public class GenericCommand extends Command {
    private static final class BeforeAfter {
        final PrimitiveData before;
        final AbstractPrimitive after;
        
        public BeforeAfter(PrimitiveData before, AbstractPrimitive after) {
            this.before = before;
            this.after = after;
        }
        
        public OsmPrimitive afterPrimitive() {
            return (OsmPrimitive) after;
        }
        
        public PrimitiveData afterData() {
            return (PrimitiveData) after;
        }
    }
    
    private final DataSet dataSet;
    private final String description;
    private final Map<OsmPrimitive, BeforeAfter> beforeAfters = new HashMap<OsmPrimitive, BeforeAfter>();
    
    public GenericCommand(DataSet dataSet, String description) {
        this.dataSet = dataSet;
        this.description = description;
    }
    
    void add(OsmPrimitive p) {
        beforeAfters.put(p, new BeforeAfter(null, p));
    }
    
    AbstractPrimitive backup(OsmPrimitive p) {
        final BeforeAfter ba = beforeAfters.get(p);
        
        if (ba == null) {
            final BeforeAfter newBa = new BeforeAfter(p.save(), p.save());
            beforeAfters.put(p, newBa);
            return newBa.after;
        } else {
            return ba.after;
        }
    }
    
    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added) {}
    
    @Override
    public String getDescriptionText() {
        return description;
    }
    
    @Override
    public boolean executeCommand() {
        for (Entry<OsmPrimitive, BeforeAfter> e : beforeAfters.entrySet()) {
            if (e.getValue().before == null) {
                dataSet.addPrimitive(e.getValue().afterPrimitive());
            } else {
                e.getKey().load(e.getValue().afterData());
            }
        }
        return true;
    }
    
    @Override
    public void undoCommand() {
        for (Entry<OsmPrimitive, BeforeAfter> e : beforeAfters.entrySet()) {
            if (e.getValue().before == null) {
                dataSet.removePrimitive(e.getValue().afterPrimitive().getPrimitiveId());
            } else {
                e.getKey().load(e.getValue().before);
            }
        }
    }
    
    @Override
    public PrimitiveData getOrig(OsmPrimitive osm) {
        return beforeAfters.get(osm).before;
    }
    
    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        return Collections.unmodifiableSet(beforeAfters.keySet());
    }
}
