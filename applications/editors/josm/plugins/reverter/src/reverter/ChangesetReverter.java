package reverter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.ChangesetDataSet;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.ChangesetDataSet.ChangesetDataSetEntry;
import org.openstreetmap.josm.data.osm.ChangesetDataSet.ChangesetModificationType;
import org.openstreetmap.josm.data.osm.history.HistoryOsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerChangesetReader;
import org.openstreetmap.josm.io.OsmTransferException;

public class ChangesetReverter {
    private int changesetId;
    private Changeset changeset;
    private ChangesetDataSet osmchange;
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
    
    public ChangesetReverter(int changesetId) {
        this.changesetId = changesetId;
        try {
            OsmServerChangesetReader csr = new OsmServerChangesetReader();
            changeset = csr.readChangeset(changesetId, NullProgressMonitor.INSTANCE);
            osmchange = csr.downloadChangeset(changesetId, NullProgressMonitor.INSTANCE);
        } catch (OsmTransferException e) {
            e.printStackTrace();
        }
    }
    LinkedList<Command> cmds;

    public void RevertChangeset(ProgressMonitor progressMonitor) throws OsmTransferException {
        final OsmDataLayer layer = Main.main.getEditLayer();
        final DataSet ds = layer.data;
        final MyCsDataset cds = new MyCsDataset(osmchange);
        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();
        
        progressMonitor.beginTask("",cds.updated.size()+cds.deleted.size()+2);
        progressMonitor.worked(1);
        try {
            // Fetch objects that was updated or deleted by changeset
            for (Map.Entry<PrimitiveId,Integer> entry : cds.updated.entrySet()) {
                rdr.ReadObject(entry.getKey().getUniqueId(), entry.getValue()-1, entry.getKey().getType(),
                        progressMonitor.createSubTaskMonitor(1, true));
            }
            for (Map.Entry<PrimitiveId,Integer> entry : cds.deleted.entrySet()) {
                rdr.ReadObject(entry.getKey().getUniqueId(), entry.getValue()-1, entry.getKey().getType(),
                        progressMonitor.createSubTaskMonitor(1, true));
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
    public List<Command> getCommands() {
        return cmds;
    }
}
