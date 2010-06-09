package reverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.ChangesetDataSet;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.ChangesetDataSet.ChangesetDataSetEntry;
import org.openstreetmap.josm.data.osm.ChangesetDataSet.ChangesetModificationType;
import org.openstreetmap.josm.data.osm.history.HistoryOsmPrimitive;
import org.openstreetmap.josm.data.osm.history.HistoryRelation;
import org.openstreetmap.josm.data.osm.history.HistoryWay;
import org.openstreetmap.josm.data.osm.history.RelationMember;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.MultiFetchServerObjectReader;
import org.openstreetmap.josm.io.OsmServerChangesetReader;
import org.openstreetmap.josm.io.OsmTransferException;

public class ChangesetReverter {
    public final int changesetId;
    public final Changeset changeset;

    private final DataSet ds;
    private final ChangesetDataSet osmchange;

    private final HashSet<PrimitiveId> missing = new HashSet<PrimitiveId>();
    private LinkedList<Command> cmds;

    static class MyCsDataset {
        public HashMap<PrimitiveId,Integer> created = new HashMap<PrimitiveId,Integer>();
        public HashMap<PrimitiveId,Integer> updated = new HashMap<PrimitiveId,Integer>();
        public HashMap<PrimitiveId,Integer> deleted = new HashMap<PrimitiveId,Integer>();

        private static void put(HashMap<PrimitiveId,Integer> map,PrimitiveId id,int version) {
            if (map.containsKey(id)) {
                if (version < map.get(id))
                    map.put(id, version);
            } else {
                map.put(id, version);
            }
        }
        
        private void addEntry(ChangesetDataSetEntry entry) {
            HistoryOsmPrimitive t = entry.getPrimitive();
            if (entry.getModificationType() == ChangesetModificationType.CREATED) {
                put(created, new SimplePrimitiveId(t.getId(),t.getType()), (int)t.getVersion());
            } else if (entry.getModificationType() == ChangesetModificationType.UPDATED) {
                put(updated, new SimplePrimitiveId(t.getId(),t.getType()), (int)t.getVersion());
            } else if (entry.getModificationType() == ChangesetModificationType.DELETED) {
                put(deleted, new SimplePrimitiveId(t.getId(),t.getType()), (int)t.getVersion());
            } else throw new AssertionError();
        }
        
        public MyCsDataset(ChangesetDataSet ds) {
            Iterator<ChangesetDataSetEntry> iterator = ds.iterator();
            while (iterator.hasNext()) {
                addEntry(iterator.next());
            }
        }
    }
    
    private void AddIfMissing(PrimitiveId id) {
        if (ds.getPrimitiveById(id) == null) {
            missing.add(id);
        }
    }
    private void AddMissingIds(Set<HistoryOsmPrimitive> primitives) {
        for (HistoryOsmPrimitive p : primitives) {
            AddIfMissing(new SimplePrimitiveId(p.getId(),p.getType()));
            if (p.getType() == OsmPrimitiveType.WAY) {
                for (long nd : ((HistoryWay)p).getNodes()) {
                    AddIfMissing(new SimplePrimitiveId(nd,OsmPrimitiveType.NODE));
                }
            }
            if (p.getType() == OsmPrimitiveType.RELATION) {
                for (RelationMember member : ((HistoryRelation)p).getMembers()) {
                    AddIfMissing(new SimplePrimitiveId(member.getPrimitiveId(),
                            member.getPrimitiveType()));
                }
            }
        }
    }
    private void CheckMissing() {
        /* Create list of objects that created or modified my the changeset but
         * doesn't present in the current dataset. 
         */
        missing.clear();
        AddMissingIds(osmchange.
                getPrimitivesByModificationType(ChangesetModificationType.CREATED));
        AddMissingIds(osmchange.
                getPrimitivesByModificationType(ChangesetModificationType.UPDATED));
    }
    
    public ChangesetReverter(int changesetId) throws OsmTransferException {
        this.changesetId = changesetId;
        this.ds = Main.main.getCurrentDataSet();

        OsmServerChangesetReader csr = new OsmServerChangesetReader();
        changeset = csr.readChangeset(changesetId, NullProgressMonitor.INSTANCE);
        osmchange = csr.downloadChangeset(changesetId, NullProgressMonitor.INSTANCE);
        CheckMissing();
    }

    public void RevertChangeset(ProgressMonitor progressMonitor) throws OsmTransferException {
        final MyCsDataset cds = new MyCsDataset(osmchange);
        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();
        
        progressMonitor.beginTask("",cds.updated.size()+cds.deleted.size()+2);
        progressMonitor.worked(1);
        try {
            // Fetch objects that was updated or deleted by changeset
            for (Map.Entry<PrimitiveId,Integer> entry : cds.updated.entrySet()) {
                rdr.ReadObject(entry.getKey().getUniqueId(), entry.getValue()-1, entry.getKey().getType(),
                        progressMonitor.createSubTaskMonitor(1, true));
                if (progressMonitor.isCancelled()) return;
            }
            for (Map.Entry<PrimitiveId,Integer> entry : cds.deleted.entrySet()) {
                rdr.ReadObject(entry.getKey().getUniqueId(), entry.getValue()-1, entry.getKey().getType(),
                        progressMonitor.createSubTaskMonitor(1, true));
                if (progressMonitor.isCancelled()) return;
            }
            final DataSet nds = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true));
            
            // Mark objects that have visible=false to be deleted
            LinkedList<OsmPrimitive> toDelete = new LinkedList<OsmPrimitive>();
            for (OsmPrimitive p : nds.allPrimitives()) {
                if (!p.isVisible()) {
                    OsmPrimitive dp = ds.getPrimitiveById(p);
                    if (dp != null) toDelete.add(dp);
                }
            }
            
            // Create commands to restore/update all affected objects 
            this.cmds = new DataSetToCmd(nds,ds).getCommandList();
            
            // Mark all created objects to be deleted
            for (PrimitiveId id : cds.created.keySet()) {
                OsmPrimitive p = ds.getPrimitiveById(id);
                if (p != null) toDelete.add(p);
            }
            // Create a Command to delete all marked objects
            List<? extends OsmPrimitive> list;
            list = OsmPrimitive.getFilteredList(toDelete, Relation.class);
            if (!list.isEmpty()) cmds.add(new DeleteCommand(list));
            list = OsmPrimitive.getFilteredList(toDelete, Way.class);
            if (!list.isEmpty()) cmds.add(new DeleteCommand(list));
            list = OsmPrimitive.getFilteredList(toDelete, Node.class);
            if (!list.isEmpty()) cmds.add(new DeleteCommand(list));
        } finally {
            progressMonitor.finishTask();
        }
    }
    
    public void DownloadMissingPrimitives(ProgressMonitor monitor) throws OsmTransferException {
        MultiFetchServerObjectReader rdr = new MultiFetchServerObjectReader();
        for (PrimitiveId id : missing) {
            switch (id.getType()) {
            case NODE:
                rdr.append(new Node(id.getUniqueId()));
                break;
            case WAY:
                rdr.append(new Way(id.getUniqueId()));
                break;
            case RELATION:
                rdr.append(new Relation(id.getUniqueId()));
                break;
            default: throw new AssertionError();
            }
        }
        DataSet from = rdr.parseOsm(monitor);
        for (Node n : from.getNodes()) {
            if (ds.getPrimitiveById(n) == null) {
                ds.addPrimitive(new Node(n));
            }
        }
        for (Way w : from.getWays()) {
            if (ds.getPrimitiveById(w) == null) {
                Way nw = new Way(w.getId());
                ds.addPrimitive(nw);
                nw.load(w.save());
            }
        }
        final LinkedList<Relation> rels = new LinkedList<Relation>();
        for (Relation r : from.getRelations()) {
            if (ds.getPrimitiveById(r) == null) {
                ds.addPrimitive(new Relation(r.getId()));
                rels.add(r);
            }
        }
        for (Relation r : rels) {
            ((Relation)ds.getPrimitiveById(r)).load(r.save());
        }
        missing.clear();
    }
    
    public List<Command> getCommands() {
        return cmds;
    }
    public Set<PrimitiveId> getMissingObjects() {
        return missing;
    }
}
