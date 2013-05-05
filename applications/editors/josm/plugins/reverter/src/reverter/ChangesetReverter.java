package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.ConflictAddCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMemberData;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.history.HistoryNode;
import org.openstreetmap.josm.data.osm.history.HistoryOsmPrimitive;
import org.openstreetmap.josm.data.osm.history.HistoryRelation;
import org.openstreetmap.josm.data.osm.history.HistoryWay;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.MultiFetchServerObjectReader;
import org.openstreetmap.josm.io.OsmApiException;
import org.openstreetmap.josm.io.OsmTransferException;

import reverter.corehacks.ChangesetDataSet;
import reverter.corehacks.ChangesetDataSet.ChangesetDataSetEntry;
import reverter.corehacks.ChangesetDataSet.ChangesetModificationType;
import reverter.corehacks.OsmServerChangesetReader;

/**
 * Fetches and stores data for reverting of specific changeset.
 * @author Upliner
 *
 */
public class ChangesetReverter {

    public static enum RevertType {
        FULL,
        SELECTION,
        SELECTION_WITH_UNDELETE
    }

    public final int changesetId;
    public final Changeset changeset;
    public final RevertType revertType;
    
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
        }
    }

    /**
     * Checks if {@see ChangesetDataSetEntry} conforms to current RevertType
     * @param entry entry to be checked
     * @return <code>true</code> if {@see ChangesetDataSetEntry} conforms to current RevertType
     */
    private boolean checkOsmChangeEntry(ChangesetDataSetEntry entry) {
        if (revertType == RevertType.FULL) return true;
        if (revertType == RevertType.SELECTION_WITH_UNDELETE &&
                entry.getModificationType() == ChangesetModificationType.DELETED) {
            return true;
        }
        OsmPrimitive p = ds.getPrimitiveById(entry.getPrimitive().getPrimitiveId());
        if (p == null) return false;
        return p.isSelected();
    }

    /**
     * creates a reverter for specific changeset and fetches initial data
     * @param changesetId
     * @param monitor
     * @throws OsmTransferException
     */
    public ChangesetReverter(int changesetId, RevertType revertType, boolean newLayer, ProgressMonitor monitor)
            throws OsmTransferException {
        this.changesetId = changesetId;
        if (newLayer) {
            this.ds = new DataSet();
            this.layer = new OsmDataLayer(this.ds, tr("Reverted changeset") + tr(" [id: {0}]", changesetId), null);
        } else {
            this.layer = Main.main.getEditLayer();
            this.ds = layer.data;
        }
        this.revertType = revertType;

        OsmServerChangesetReader csr = new OsmServerChangesetReader();
        monitor.beginTask("", 2);
        try {
            changeset = csr.readChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
            cds = csr.downloadChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
        } finally {
            monitor.finishTask();
            if (newLayer) {
                GuiHelper.runInEDT(new Runnable() {
                    @Override
                    public void run() {
                        Main.main.addLayer(layer);
                    }
                });
            }
        }

        // Build our own lists of created/updated/modified objects for better performance
        for (Iterator<ChangesetDataSetEntry> it = cds.iterator();it.hasNext();) {
            ChangesetDataSetEntry entry = it.next();
            if (!checkOsmChangeEntry(entry)) continue;
            if (entry.getModificationType() == ChangesetModificationType.CREATED) {
                created.add(entry.getPrimitive());
            } else if (entry.getModificationType() == ChangesetModificationType.UPDATED) {
                updated.add(entry.getPrimitive());
            } else if (entry.getModificationType() == ChangesetModificationType.DELETED) {
                deleted.add(entry.getPrimitive());
            } else throw new AssertionError();
        }
    }
    public void checkMissingCreated() {
        addMissingHistoryIds(created);
    }
    public void checkMissingUpdated() {
        addMissingHistoryIds(updated);
    }
    public void checkMissingDeleted() {
        addMissingHistoryIds(deleted);
    }
    
    private void readObjectVersion(OsmServerMultiObjectReader rdr, PrimitiveId id, int version, ProgressMonitor progressMonitor) throws OsmTransferException {
        boolean readOK = false;
        while (!readOK && version >= 1) {
            try {
                rdr.readObject(id, version, progressMonitor.createSubTaskMonitor(1, true));
                readOK = true;
            } catch (OsmApiException e) {
                if (e.getResponseCode() != HttpURLConnection.HTTP_FORBIDDEN) {
                    throw e;
                }
                String message = "Version "+version+" of "+id+" is unauthorized";
                if (version > 1) {
                    message += ", requesting previous one";
                }
                Main.info(message);
                version--;
            }
        }
        if (!readOK) {
            Main.warn("Cannot retrieve any previous version of "+id);
        }
    }

    /**
     * fetch objects that were updated or deleted by changeset
     * @param progressMonitor
     * @throws OsmTransferException
     */
    public void downloadObjectsHistory(ProgressMonitor progressMonitor) throws OsmTransferException {
        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();

        progressMonitor.beginTask("Downloading objects history",updated.size()+deleted.size()+1);
        try {
            for (HashSet<HistoryOsmPrimitive> collection : Arrays.asList(new HashSet[]{updated, deleted})) {
                for (HistoryOsmPrimitive entry : collection) {
                    PrimitiveId id = entry.getPrimitiveId();
                    readObjectVersion(rdr, id, cds.getEarliestVersion(id)-1, progressMonitor);
                    if (progressMonitor.isCanceled()) return;
                }
            }
            nds = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true));
            for (OsmPrimitive p : nds.allPrimitives()) {
                if (!p.isIncomplete()) {
                    addMissingIds(Collections.singleton(p));
                } else {
                    if (ds.getPrimitiveById(p.getPrimitiveId()) == null) {
                        switch (p.getType()) {
                        case NODE: ds.addPrimitive(new Node(p.getUniqueId())); break;
                        case CLOSEDWAY:
                        case WAY: ds.addPrimitive(new Way(p.getUniqueId())); break;
                        case MULTIPOLYGON:
                        case RELATION: ds.addPrimitive(new Relation(p.getUniqueId())); break;
                        default: throw new AssertionError();
                        }
                    }
                }
            }
        } finally {
            progressMonitor.finishTask();
        }
    }

    public void downloadMissingPrimitives(ProgressMonitor monitor) throws OsmTransferException {
        if (!hasMissingObjects()) return;
        MultiFetchServerObjectReader rdr = new MultiFetchServerObjectReader();
        for (PrimitiveId id : missing) {
            switch (id.getType()) {
            case NODE:
                rdr.append(new Node(id.getUniqueId()));
                break;
            case CLOSEDWAY:
            case WAY:
                rdr.append(new Way(id.getUniqueId()));
                break;
            case MULTIPOLYGON:
            case RELATION:
                rdr.append(new Relation(id.getUniqueId()));
                break;
            default: throw new AssertionError();
            }
        }
        DataSet source = rdr.parseOsm(monitor);
        for (OsmPrimitive p : source.allPrimitives()) {
            if (!p.isVisible() && !p.isDeleted()) {
                p.setDeleted(true);
                p.setModified(false);
            }
        }
        layer.mergeFrom(source);
        missing.clear();
    }

    private static Conflict<? extends OsmPrimitive> CreateConflict(OsmPrimitive p, boolean isMyDeleted) {
        switch (p.getType()) {
        case NODE:
            return new Conflict<Node>((Node)p,new Node((Node)p), isMyDeleted);
        case CLOSEDWAY:
        case WAY:
            return new Conflict<Way>((Way)p,new Way((Way)p), isMyDeleted);
        case MULTIPOLYGON:
        case RELATION:
            return new Conflict<Relation>((Relation)p,new Relation((Relation)p), isMyDeleted);
        default: throw new AssertionError();
        }
    }

    private static boolean hasEqualSemanticAttributes(OsmPrimitive current,HistoryOsmPrimitive history) {
        if (!current.getKeys().equals(history.getTags())) return false;
        switch (current.getType()) {
        case NODE:
            LatLon currentCoor = ((Node)current).getCoor();
            LatLon historyCoor = ((HistoryNode)history).getCoords();
            return currentCoor == historyCoor || (currentCoor != null && historyCoor != null && currentCoor.equals(historyCoor));
        case CLOSEDWAY:
        case WAY:
            List<Node> currentNodes = ((Way)current).getNodes();
            List<Long> historyNodes = ((HistoryWay)history).getNodes();
            if (currentNodes.size() != historyNodes.size()) return false;
            for (int i = 0; i < currentNodes.size(); i++) {
                if (currentNodes.get(i).getId() != historyNodes.get(i)) return false;
            }
            return true;
        case MULTIPOLYGON:
        case RELATION:
            List<org.openstreetmap.josm.data.osm.RelationMember> currentMembers =
                ((Relation)current).getMembers();
            List<RelationMemberData> historyMembers = ((HistoryRelation)history).getMembers();
            if (currentMembers.size() != historyMembers.size()) return false;
            for (int i = 0; i < currentMembers.size(); i++) {
                org.openstreetmap.josm.data.osm.RelationMember currentMember =
                    currentMembers.get(i);
                RelationMemberData historyMember = historyMembers.get(i);
                if (!currentMember.getRole().equals(historyMember.getRole())) return false;
                if (!currentMember.getMember().getPrimitiveId().equals(new SimplePrimitiveId(
                        historyMember.getMemberId(),historyMember.getMemberType()))) return false;
            }
            return true;
        default: throw new AssertionError();
        }
    }

    /**
     * Builds a list of commands that will revert the changeset
     *
     */
    public List<Command> getCommands() {
        if (this.nds == null) return null;

        //////////////////////////////////////////////////////////////////////////
        // Create commands to restore/update all affected objects
        DataSetCommandMerger merger = new DataSetCommandMerger(nds,ds);
        LinkedList<Command> cmds = merger.getCommandList();

        //////////////////////////////////////////////////////////////////////////
        // Create a set of objects to be deleted

        HashSet<OsmPrimitive> toDelete = new HashSet<OsmPrimitive>();
        // Mark objects that has visible=false to be deleted
        for (OsmPrimitive p : nds.allPrimitives()) {
            if (!p.isVisible()) {
                OsmPrimitive dp = ds.getPrimitiveById(p);
                if (dp != null) toDelete.add(dp);
            }
        }
        // Mark all created objects to be deleted
        for (HistoryOsmPrimitive id : created) {
            OsmPrimitive p = ds.getPrimitiveById(id.getPrimitiveId());
            if (p != null) toDelete.add(p);
        }

        //////////////////////////////////////////////////////////////////////////
        // Check reversion against current dataset and create necessary conflicts

        HashSet<OsmPrimitive> conflicted = new HashSet<OsmPrimitive>();

        for (Conflict<? extends OsmPrimitive> conflict : merger.getConflicts()) {
            cmds.add(new ConflictAddCommand(layer,conflict));
        }

        // Check objects versions
        for (Iterator<ChangesetDataSetEntry> it = cds.iterator();it.hasNext();) {
            ChangesetDataSetEntry entry = it.next();
            if (!checkOsmChangeEntry(entry)) continue;
            HistoryOsmPrimitive hp = entry.getPrimitive();
            OsmPrimitive dp = ds.getPrimitiveById(hp.getPrimitiveId());
            if (dp == null || dp.isIncomplete())
                throw new IllegalStateException(tr("Missing merge target for {0} with id {1}",
                        hp.getType(), hp.getId()));

            if (hp.getVersion() != dp.getVersion()
                    && (hp.isVisible() || dp.isVisible()) &&
                    /* Don't create conflict if changeset object and dataset object
                     * has same semantic attributes (but different versions) */
                    !hasEqualSemanticAttributes(dp,hp)
                    /* Don't create conflict if the object has to be deleted but has already been deleted */
                    && !(toDelete.contains(dp) && dp.isDeleted())) {
                cmds.add(new ConflictAddCommand(layer,CreateConflict(dp,
                        entry.getModificationType() == ChangesetModificationType.CREATED)));
                conflicted.add(dp);
            }
        }

        /* Check referrers for deleted objects: if object is referred by another object that
         * isn't going to be deleted or modified, create a conflict.
         */
        for (Iterator<OsmPrimitive> it = toDelete.iterator(); it.hasNext();) {
            OsmPrimitive p = it.next();
            if (p.isDeleted()) {
                it.remove();
                continue;
            }
            for (OsmPrimitive referrer : p.getReferrers()) {
                if (toDelete.contains(referrer)) continue; // object is going to be deleted
                if (nds.getPrimitiveById(referrer) != null)
                    continue; /* object is going to be modified so it cannot refer to
                               * objects created in changeset to be reverted
                               */
                if (!conflicted.contains(p)) {
                    cmds.add(new ConflictAddCommand(layer,CreateConflict(p, true)));
                    conflicted.add(p);
                }
                it.remove();
                break;
            }
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
    
    public boolean hasMissingObjects() {
        return !missing.isEmpty();
    }
    
    public void fixNodesWithoutCoordinates(ProgressMonitor progressMonitor) throws OsmTransferException {
        for (Node n : nds.getNodes()) {
            if (!n.isDeleted() && n.getCoor() == null) {
                PrimitiveId id = n.getPrimitiveId();
                OsmPrimitive p = ds.getPrimitiveById(id);
                if (p instanceof Node && p.getVersion() > 1) {
                    LatLon coor = ((Node)p).getCoor();
                    if (coor == null) {
                        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();
                        readObjectVersion(rdr, id, p.getVersion()-1, progressMonitor);
                        Collection<OsmPrimitive> result = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true)).allPrimitives();
                        if (!result.isEmpty()) {
                            coor = ((Node)result.iterator().next()).getCoor();
                        }
                    }
                    if (coor != null) {
                        n.setCoor(coor);
                    }
                }
            }
        }
    }
}
