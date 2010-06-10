package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.ConflictAddCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.conflict.Conflict;
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
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.MultiFetchServerObjectReader;
import org.openstreetmap.josm.io.OsmServerChangesetReader;
import org.openstreetmap.josm.io.OsmTransferException;

/**
 * Fetches and stores data for reverting of specific changeset.
 * @author Upliner
 *
 */
public class ChangesetReverter {
    public final int changesetId;
    public final Changeset changeset;

    private final OsmDataLayer layer; // data layer associated with reverter 
    private final DataSet ds; // DataSet associated with reverter 
    private final ChangesetDataSet cds; // Current changeset data 
    private DataSet nds; // Dataset that contains new objects downloaded by reverter

    private final HashSet<PrimitiveId> missing = new HashSet<PrimitiveId>();

    private final HashSet<HistoryOsmPrimitive> created = new HashSet<HistoryOsmPrimitive>();
    private final HashSet<HistoryOsmPrimitive> updated = new HashSet<HistoryOsmPrimitive>();
    private final HashSet<HistoryOsmPrimitive> deleted = new HashSet<HistoryOsmPrimitive>();

    //// Handling missing objects
    ////////////////////////////////////////
    private void addIfMissing(PrimitiveId id) {
        OsmPrimitive p = ds.getPrimitiveById(id); 
        if (p == null || p.isIncomplete()) {
            missing.add(id);
        }
    }
    private void addMissingHistoryIds(Iterable<HistoryOsmPrimitive> primitives) {
        for (HistoryOsmPrimitive p : primitives) {
            addIfMissing(p.getPrimitiveId());
            if (p.getType() == OsmPrimitiveType.WAY) {
                for (long nd : ((HistoryWay)p).getNodes()) {
                    addIfMissing(new SimplePrimitiveId(nd,OsmPrimitiveType.NODE));
                }
            }
            if (p.getType() == OsmPrimitiveType.RELATION) {
                for (RelationMember member : ((HistoryRelation)p).getMembers()) {
                    addIfMissing(new SimplePrimitiveId(member.getPrimitiveId(),
                            member.getPrimitiveType()));
                }
            }
        }
    }

    private void addMissingIds(Iterable<OsmPrimitive> primitives) {
        for (OsmPrimitive p : primitives) {
            addIfMissing(p);
            if (p.getType() == OsmPrimitiveType.WAY) {
                for (Node nd : ((Way)p).getNodes()) {
                    addIfMissing(nd);
                }
            }
            if (p.getType() == OsmPrimitiveType.RELATION) {
                for (org.openstreetmap.josm.data.osm.RelationMember member
                        : ((Relation)p).getMembers()) {
                    addIfMissing(member.getMember());
                }
            }
        }
    }
    
    /**
     * creates a reverter for specific changeset and fetches initial data
     * @param changesetId
     * @param monitor
     * @throws OsmTransferException
     */
    public ChangesetReverter(int changesetId, ProgressMonitor monitor) throws OsmTransferException {
        this.changesetId = changesetId;
        this.layer = Main.main.getEditLayer();
        this.ds = layer.data;

        OsmServerChangesetReader csr = new OsmServerChangesetReader();
        monitor.beginTask("", 2);
        try {
            changeset = csr.readChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
            cds = csr.downloadChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
        } finally {
            monitor.finishTask();
        }
        
        // Build our own lists of created/updated/modified objects for better performance
        Iterator<ChangesetDataSetEntry> iterator = cds.iterator();
        while (iterator.hasNext()) {
            ChangesetDataSetEntry entry = iterator.next();
            if (entry.getModificationType() == ChangesetModificationType.CREATED) {
                created.add(entry.getPrimitive());
            } else if (entry.getModificationType() == ChangesetModificationType.UPDATED) {
                updated.add(entry.getPrimitive());
            } else if (entry.getModificationType() == ChangesetModificationType.DELETED) {
                deleted.add(entry.getPrimitive());
            } else throw new AssertionError();
        }
        
        /* Create list of objects that created or modified my the changeset but
         * doesn't present in the current dataset. 
         */
        missing.clear();
        addMissingHistoryIds(created);
        addMissingHistoryIds(updated);
    }

    /**
     * fetch objects that was updated or deleted by changeset
     * @param progressMonitor
     * @throws OsmTransferException
     */
    public void downloadObjectsHistory(ProgressMonitor progressMonitor) throws OsmTransferException {
        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();
        
        progressMonitor.beginTask("Downloading objects history",updated.size()+deleted.size()+1);
        try {
            for (HistoryOsmPrimitive entry : updated) {
                rdr.ReadObject(entry.getPrimitiveId(), (int)entry.getVersion()-1,
                        progressMonitor.createSubTaskMonitor(1, true));
                if (progressMonitor.isCancelled()) return;
            }
            for (HistoryOsmPrimitive entry : deleted) {
                rdr.ReadObject(entry.getPrimitiveId(), (int)entry.getVersion()-1,
                        progressMonitor.createSubTaskMonitor(1, true));
                if (progressMonitor.isCancelled()) return;
            }
            nds = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true));
            addMissingIds(nds.allPrimitives());
        } finally {
            progressMonitor.finishTask();
        }
    }
    
    public void downloadMissingPrimitives(ProgressMonitor monitor) throws OsmTransferException {
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
        layer.mergeFrom(rdr.parseOsm(monitor));
        missing.clear();
    }
    
    /**
     * Builds a list of commands that will revert the changeset
     * 
     */
    public List<Command> getCommands() {
        if (this.nds == null) return null;
        LinkedList<OsmPrimitive> toDelete = new LinkedList<OsmPrimitive>();
        LinkedList<Command> cmds = new LinkedList<Command>();
        for (OsmPrimitive p : nds.allPrimitives()) {
            if (p.isIncomplete()) continue;
            OsmPrimitive dp = ds.getPrimitiveById(p);
            if (dp == null)
                throw new IllegalStateException(tr("Missing merge target for {0} with id {1}",
                        p.getType(), p.getUniqueId()));
            // Mark objects that have visible=false to be deleted
            if (!p.isVisible()) {
                toDelete.add(dp);
            }
        }
        
        // Check objects version
        Iterator<ChangesetDataSetEntry> iterator = cds.iterator();
        while (iterator.hasNext()) {
            ChangesetDataSetEntry entry = iterator.next();
            HistoryOsmPrimitive p = entry.getPrimitive();
            OsmPrimitive dp = ds.getPrimitiveById(p.getPrimitiveId());
            if (dp == null)
                throw new IllegalStateException(tr("Missing merge target for {0} with id {1}",
                        p.getType(), p.getId()));
            OsmPrimitive np = nds.getPrimitiveById(p.getPrimitiveId());
            if (np == null && entry.getModificationType() != ChangesetModificationType.CREATED)
                throw new IllegalStateException(tr("Missing new dataset object for {0} with id {1}",
                        p.getType(), p.getId()));
            if (p.getVersion() != dp.getVersion()
                    && (p.isVisible() || dp.isVisible())) {
                Conflict<? extends OsmPrimitive> c;
                switch (dp.getType()) {
                case NODE:
                    if (np == null) {
                        np = new Node((Node)dp);
                        np.setDeleted(true);
                    }
                    c = new Conflict<Node>((Node)np,new Node((Node)dp),
                            entry.getModificationType() == ChangesetModificationType.CREATED);
                    break;
                case WAY:
                    if (np == null) {
                        np = new Way((Way)dp);
                        np.setDeleted(true);
                    }
                    c = new Conflict<Way>((Way)np,new Way((Way)dp),
                            entry.getModificationType() == ChangesetModificationType.CREATED);
                    break;
                case RELATION:
                    if (np == null) {
                        np = new Relation((Relation)dp);
                        np.setDeleted(true);
                    }
                    c = new Conflict<Relation>((Relation)np,new Relation((Relation)dp),
                            entry.getModificationType() == ChangesetModificationType.CREATED);
                    break;
                default: throw new AssertionError();
                }
                cmds.add(new ConflictAddCommand(layer,c));
            }
        }
        
        // Create commands to restore/update all affected objects 
        cmds.addAll(new DataSetToCmd(nds,ds).getCommandList());
        
        // Mark all created objects to be deleted
        for (HistoryOsmPrimitive id : created) {
            OsmPrimitive p = ds.getPrimitiveById(id.getPrimitiveId());
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
        return cmds;
    }
    public boolean haveMissingObjects() {
        return !missing.isEmpty();
    }
}
