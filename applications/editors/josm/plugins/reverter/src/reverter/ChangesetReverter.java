// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.conflict.ConflictAddCommand;
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
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.MultiFetchServerObjectReader;
import org.openstreetmap.josm.io.OsmApiException;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

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

    public enum RevertType {
        FULL,
        SELECTION,
        SELECTION_WITH_UNDELETE
    }

    public static final Collection<Long> MODERATOR_REDACTION_ACCOUNTS = Collections.unmodifiableCollection(Arrays.asList(
            722_137L, // OSMF Redaction Account
            760_215L  // pnorman redaction revert
            ));

    public final int changesetId;
    public final Changeset changeset;
    public final RevertType revertType;

    private final OsmDataLayer layer; // data layer associated with reverter
    private final DataSet ds; // DataSet associated with reverter
    private final ChangesetDataSet cds; // Current changeset data
    private DataSet nds; // Dataset that contains new objects downloaded by reverter

    private final HashSet<PrimitiveId> missing = new HashSet<>();

    private final HashSet<HistoryOsmPrimitive> created = new HashSet<>();
    private final HashSet<HistoryOsmPrimitive> updated = new HashSet<>();
    private final HashSet<HistoryOsmPrimitive> deleted = new HashSet<>();

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
        }
    }

    private void addMissingId(OsmPrimitive p) {
        addIfMissing(p);
        if (p.getType() == OsmPrimitiveType.WAY) {
            for (Node nd : ((Way) p).getNodes()) {
                addIfMissing(nd);
            }
        }
    }

    /**
     * Checks if {@link ChangesetDataSetEntry} conforms to current RevertType
     * @param entry entry to be checked
     * @return <code>true</code> if {@link ChangesetDataSetEntry} conforms to current RevertType
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
     * @param changesetId changeset id
     * @param revertType type of revert
     * @param newLayer set to true if a new layer should be created
     * @param monitor progress monitor
     * @throws OsmTransferException if data transfer errors occur
     * @throws RevertRedactedChangesetException if a redacted changeset is requested
     */
    public ChangesetReverter(int changesetId, RevertType revertType, boolean newLayer, ProgressMonitor monitor)
            throws OsmTransferException, RevertRedactedChangesetException {
        this.changesetId = changesetId;
        if (newLayer) {
            this.ds = new DataSet();
            this.layer = new OsmDataLayer(this.ds, tr("Reverted changeset") + tr(" [id: {0}]", String.valueOf(changesetId)), null);
        } else {
            this.layer = MainApplication.getLayerManager().getEditLayer();
            this.ds = layer.data;
        }
        this.revertType = revertType;

        OsmServerChangesetReader csr = new OsmServerChangesetReader();
        monitor.beginTask("", 2);
        changeset = csr.readChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
        if (MODERATOR_REDACTION_ACCOUNTS.contains(changeset.getUser().getId())) {
            throw new RevertRedactedChangesetException(tr("It is not allowed to revert changeset from {0}", changeset.getUser().getName()));
        }
        try {
            cds = csr.downloadChangeset(changesetId, monitor.createSubTaskMonitor(1, false));
        } finally {
            monitor.finishTask();
            if (newLayer) {
                GuiHelper.runInEDT(() -> MainApplication.getLayerManager().addLayer(layer));
            }
        }

        // Build our own lists of created/updated/modified objects for better performance
        for (Iterator<ChangesetDataSetEntry> it = cds.iterator(); it.hasNext();) {
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

    private static void readObjectVersion(OsmServerMultiObjectReader rdr, PrimitiveId id, int version, ProgressMonitor progressMonitor)
            throws OsmTransferException {
        boolean readOK = false;
        while (!readOK && version >= 1) {
            try {
                rdr.readObject(id, version, progressMonitor.createSubTaskMonitor(1, true));
                readOK = true;
            } catch (OsmApiException e) {
                if (e.getResponseCode() != HttpURLConnection.HTTP_FORBIDDEN) {
                    throw e;
                }
                String message = "Version " + version + " of " + id + " is unauthorized";
                Logging.info(version <= 1 ? message : message + ", requesting previous one");
                version--;
            }
        }
        if (!readOK) {
            Logging.warn("Cannot retrieve any previous version of "+id);
        }
    }

    /**
     * fetch objects that were updated or deleted by changeset
     * @param progressMonitor progress monitor
     * @throws OsmTransferException if data transfer errors occur
     */
    public void downloadObjectsHistory(ProgressMonitor progressMonitor) throws OsmTransferException {
        final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();

        int num = updated.size() + deleted.size();
        progressMonitor.beginTask(
                addChangesetIdPrefix(
                        trn("Downloading history for {0} object", "Downloading history for {0} objects", num, num)), num + 1);
        try {
            for (HashSet<HistoryOsmPrimitive> collection : Arrays.asList(updated, deleted)) {
                for (HistoryOsmPrimitive entry : collection) {
                    PrimitiveId id = entry.getPrimitiveId();
                    readObjectVersion(rdr, id, cds.getEarliestVersion(id)-1, progressMonitor);
                    if (progressMonitor.isCanceled()) return;
                }
            }
            nds = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true));
            for (OsmPrimitive p : nds.allPrimitives()) {
                if (!p.isIncomplete()) {
                    addMissingId(p);
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
        MultiFetchServerObjectReader rdr = MultiFetchServerObjectReader.create();
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

    private static Conflict<? extends OsmPrimitive> createConflict(OsmPrimitive p, boolean isMyDeleted) {
        switch (p.getType()) {
        case NODE:
            return new Conflict<>((Node) p, new Node((Node) p), isMyDeleted);
        case CLOSEDWAY:
        case WAY:
            return new Conflict<>((Way) p, new Way((Way) p), isMyDeleted);
        case MULTIPOLYGON:
        case RELATION:
            return new Conflict<>((Relation) p, new Relation((Relation) p), isMyDeleted);
        default: throw new AssertionError();
        }
    }

    private boolean hasEqualSemanticAttributes(OsmPrimitive current, HistoryOsmPrimitive history) {
        if (!current.getKeys().equals(history.getTags())) return false;
        switch (current.getType()) {
        case NODE:
            LatLon currentCoor = ((Node) current).getCoor();
            LatLon historyCoor = ((HistoryNode) history).getCoords();
            if (currentCoor == historyCoor || (currentCoor != null && historyCoor != null && currentCoor.equals(historyCoor)))
                return true;
            // Handle case where a deleted note has been restored to avoid false conflicts (fix #josm8660)
            if (currentCoor != null && historyCoor == null) {
                LatLon previousCoor = ((Node) nds.getPrimitiveById(history.getPrimitiveId())).getCoor();
                return previousCoor != null && previousCoor.equals(currentCoor);
            }
            return false;
        case CLOSEDWAY:
        case WAY:
            List<Node> currentNodes = ((Way) current).getNodes();
            List<Long> historyNodes = ((HistoryWay) history).getNodes();
            if (currentNodes.size() != historyNodes.size()) return false;
            for (int i = 0; i < currentNodes.size(); i++) {
                if (currentNodes.get(i).getId() != historyNodes.get(i)) return false;
            }
            return true;
        case MULTIPOLYGON:
        case RELATION:
            List<org.openstreetmap.josm.data.osm.RelationMember> currentMembers =
                ((Relation) current).getMembers();
            List<RelationMemberData> historyMembers = ((HistoryRelation) history).getMembers();
            if (currentMembers.size() != historyMembers.size()) return false;
            for (int i = 0; i < currentMembers.size(); i++) {
                org.openstreetmap.josm.data.osm.RelationMember currentMember =
                    currentMembers.get(i);
                RelationMemberData historyMember = historyMembers.get(i);
                if (!currentMember.getRole().equals(historyMember.getRole())) return false;
                if (!currentMember.getMember().getPrimitiveId().equals(new SimplePrimitiveId(
                        historyMember.getMemberId(), historyMember.getMemberType()))) return false;
            }
            return true;
        default: throw new AssertionError();
        }
    }

    /**
     * Builds a list of commands that will revert the changeset
     * @return list of commands
     *
     */
    public List<Command> getCommands() {
        List<Command> cmds = new ArrayList<>();
        if (this.nds == null) return cmds;

        //////////////////////////////////////////////////////////////////////////
        // Create commands to restore/update all affected objects
        DataSetCommandMerger merger = new DataSetCommandMerger(nds, ds);
        cmds.addAll(merger.getCommandList());

        //////////////////////////////////////////////////////////////////////////
        // Create a set of objects to be deleted

        HashSet<OsmPrimitive> toDelete = new HashSet<>();
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

        HashSet<OsmPrimitive> conflicted = new HashSet<>();

        for (Conflict<? extends OsmPrimitive> conflict : merger.getConflicts()) {
            cmds.add(new ConflictAddCommand(layer.data, conflict));
        }

        // Check objects versions
        for (Iterator<ChangesetDataSetEntry> it = cds.iterator(); it.hasNext();) {
            ChangesetDataSetEntry entry = it.next();
            if (!checkOsmChangeEntry(entry)) continue;
            HistoryOsmPrimitive hp = entry.getPrimitive();
            OsmPrimitive dp = ds.getPrimitiveById(hp.getPrimitiveId());
            if (dp == null || dp.isIncomplete()) {
                throw new IllegalStateException(addChangesetIdPrefix(tr("Missing merge target for {0}", hp.getPrimitiveId())));
            }

            if (hp.getVersion() != dp.getVersion()
                    && (hp.isVisible() || dp.isVisible()) &&
                    /* Don't create conflict if changeset object and dataset object
                     * has same semantic attributes (but different versions) */
                    !hasEqualSemanticAttributes(dp, hp)
                    /* Don't create conflict if the object has to be deleted but has already been deleted */
                    && !(toDelete.contains(dp) && dp.isDeleted())) {
                cmds.add(new ConflictAddCommand(layer.data, createConflict(dp,
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
                    cmds.add(new ConflictAddCommand(layer.data, createConflict(p, true)));
                    conflicted.add(p);
                }
                it.remove();
                break;
            }
        }

        // Create a Command to delete all marked objects
        Collection<? extends OsmPrimitive> collection;
        collection = Utils.filteredCollection(toDelete, Relation.class);
        if (!collection.isEmpty()) cmds.add(new DeleteCommand(collection));
        collection = Utils.filteredCollection(toDelete, Way.class);
        if (!collection.isEmpty()) cmds.add(new DeleteCommand(collection));
        collection = Utils.filteredCollection(toDelete, Node.class);
        if (!collection.isEmpty()) cmds.add(new DeleteCommand(collection));
        return cmds;
    }

    public boolean hasMissingObjects() {
        return !missing.isEmpty();
    }

    public void fixNodesWithoutCoordinates(ProgressMonitor progressMonitor) throws OsmTransferException {
        Collection<Node> nodes = nds.getNodes();
        int num = nodes.size();
        progressMonitor.beginTask(addChangesetIdPrefix(
                trn("Checking coordinates of {0} node", "Checking coordinates of {0} nodes", num, num)), num);

        try {
            for (Node n : nodes) {
                if (!n.isDeleted() && n.getCoor() == null) {
                    PrimitiveId id = n.getPrimitiveId();
                    OsmPrimitive p = ds.getPrimitiveById(id);
                    if (p instanceof Node && !((Node) p).isLatLonKnown()) {
                        int version = p.getVersion();
                        while (version > 1) {
                            // find the version that was in use when the current changeset was closed
                            --version;
                            final OsmServerMultiObjectReader rdr = new OsmServerMultiObjectReader();
                            readObjectVersion(rdr, id, version, progressMonitor);
                            DataSet history = rdr.parseOsm(progressMonitor.createSubTaskMonitor(1, true));
                            if (!history.isEmpty()) {
                                Node historyNode = (Node) history.allPrimitives().iterator().next();
                                if (historyNode.isLatLonKnown() && changeset.getClosedAt().after(historyNode.getTimestamp())) {
                                    n.load(historyNode.save());
                                    break;
                                }
                            }
                        }
                    }
                }
                if (progressMonitor.isCanceled())
                    return;
                progressMonitor.worked(1);
            }
        } finally {
            progressMonitor.finishTask();
        }
    }

    /**
     * Add prefix with properly formatted changeset id to a message.
     * @param msg the message string
     * @return prefixed message
     */
    String addChangesetIdPrefix(String msg) {
        return tr("Changeset {0}: {1}", Long.toString(changesetId), msg);
    }
}
