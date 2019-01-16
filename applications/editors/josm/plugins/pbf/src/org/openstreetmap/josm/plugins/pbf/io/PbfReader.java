// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.NodeData;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.RelationData;
import org.openstreetmap.josm.data.osm.RelationMemberData;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.data.osm.WayData;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Logging;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseInfo;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBBox;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Info;
import crosby.binary.Osmformat.Relation.MemberType;
import crosby.binary.file.BlockInputStream;
import crosby.binary.file.FileBlockPosition;

/**
 * OSM reader for the PBF file format.
 * @author Don-vip
 */
public class PbfReader extends AbstractReader {

    protected class PbfParser extends BinaryParser {

        private IllegalDataException exception = null;
        private boolean discourageUpload;
        private double parseRawDegrees(long raw) {
            return raw * .000000001;
        }

        @Override
        protected void parse(HeaderBlock header) {

            for (String requiredFeature : header.getRequiredFeaturesList()) {
                switch (requiredFeature) {
                case "OsmSchema-V0.6":
                    ds.setVersion("0.6");
                    break;
                case "DenseNodes":
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feature: "+requiredFeature);
                }
            }

            HeaderBBox bbox = header.getBbox();
            if (bbox != null) {
                double minlat = parseRawDegrees(bbox.getBottom());
                double minlon = parseRawDegrees(bbox.getLeft());
                double maxlat = parseRawDegrees(bbox.getTop());
                double maxlon = parseRawDegrees(bbox.getRight());
                Bounds b = new Bounds(minlat, minlon, maxlat, maxlon);
                if (!b.isCollapsed() && areCoordinatesValid(minlat, minlon, maxlat, maxlon)) {
                    ds.addDataSource(new DataSource(b, header.getSource()));
                } else {
                    Logging.error("Invalid Bounds: "+b);
                }
            }
        }

        private boolean areCoordinatesValid(double minlat, double minlon, double maxlat, double maxlon) {
            return LatLon.isValidLat(minlat) && LatLon.isValidLat(maxlat)
                && LatLon.isValidLon(minlon) && LatLon.isValidLon(maxlon);
        }

        private void setMetadata(PrimitiveData osm, Info info) throws IllegalDataException {
            if (info.hasChangeset()) {
                checkChangesetId(info.getChangeset());
                osm.setChangesetId((int) info.getChangeset());
            }
            if (info.hasUid() && info.hasUserSid()) {
                osm.setUser(User.createOsmUser(info.getUid(), getStringById(info.getUserSid())));
            }
            if (info.hasTimestamp()) {
                checkTimestamp(info.getTimestamp());
                osm.setTimestamp(getDate(info));
            }
        }

        @Override
        public boolean skipBlock(FileBlockPosition block) {
            return exception != null;
        }

        protected void checkCoordinates(LatLon coor) throws IllegalDataException {
            if (!coor.isValid()) {
                throw new IllegalDataException(tr("Invalid coordinates: {0}", coor));
            }
        }

        protected void checkChangesetId(long id) throws IllegalDataException {
            if (id > Integer.MAX_VALUE) {
                throw new IllegalDataException(tr("Invalid changeset id: {0}", id));
            }
        }

        protected void checkTimestamp(long timestamp) throws IllegalDataException {
            if (timestamp < 0) {
                throw new IllegalDataException(tr("Invalid timestamp: {0}", timestamp));
            }
        }

        @Override
        protected void parseDense(DenseNodes nodes) {
            if (!nodes.hasDenseinfo())
                discourageUpload = true;
            if (exception == null) {
                try {
                    int keyIndex = 0;
                    // Almost all data is DELTA coded
                    long nodeId = 0;
                    long nodeLat = 0;
                    long nodeLon = 0;
                    long changesetId = 0;
                    int uid = 0;
                    int suid = 0;
                    long timestamp = 0;
                    for (int i = 0; i < nodes.getIdCount(); i++) {
                        // Id (delta) and version (normal)
                        NodeData nd = new NodeData(nodeId += nodes.getId(i));
                        nd.setVersion(nodes.hasDenseinfo() ? nodes.getDenseinfo().getVersion(i) : 1);
                        // Lat/Lon (delta)
                        nd.setCoor(new LatLon(parseLat(nodeLat += nodes.getLat(i)),
                                                parseLon(nodeLon += nodes.getLon(i))).getRoundedToOsmPrecision());
                        checkCoordinates(nd.getCoor());
                        if (nodes.hasDenseinfo()) {
                            DenseInfo info = nodes.getDenseinfo();
                            // Changeset (delta)
                            if (info.getChangesetCount() > i) {
                                checkChangesetId(changesetId += info.getChangeset(i));
                                nd.setChangesetId((int) changesetId);
                            }
                            // User (delta)
                            if (info.getUidCount() > i && info.getUserSidCount() > i) {
                                nd.setUser(User.createOsmUser(uid += info.getUid(i),
                                                 getStringById(suid += info.getUserSid(i))));
                            }
                            // Timestamp (delta)
                            if (info.getTimestampCount() > i) {
                                checkTimestamp(timestamp += info.getTimestamp(i));
                                nd.setTimestamp(new Date(date_granularity * timestamp));
                            }
                        }
                        // A single table contains all keys/values of all nodes.
                        // Each node's tags are encoded in alternating <key_id> <value_id>.
                        // A single stringid of 0 delimit when the tags of a node ends and the tags of the next node begin.
                        Map<String, String> keys = new HashMap<>();
                        while (keyIndex < nodes.getKeysValsCount()) {
                            int keyId = nodes.getKeysVals(keyIndex++);
                            if (keyId == 0) {
                                break; // End of current node's tags
                            } else if (keyIndex < nodes.getKeysValsCount()) {
                                keys.put(getStringById(keyId), getStringById(nodes.getKeysVals(keyIndex++)));
                            } else {
                                throw new IllegalDataException(tr("Invalid DenseNodes key/values table"));
                            }
                        }
                        nd.setKeys(keys);
                        buildPrimitive(nd);
                    }
                } catch (IllegalDataException e) {
                    exception = e;
                }
            }
        }

        @Override
        protected void parseNodes(List<Osmformat.Node> osmNodes) {
            if (exception == null) {
                try {
                    for (Osmformat.Node n : osmNodes) {
                        final Info info = n.getInfo();
                        if (!info.hasVersion())
                            discourageUpload = true;
                        NodeData nd = new NodeData(n.getId());
                        nd.setVersion(info.hasVersion() ? info.getVersion() : 1);
                        nd.setCoor(new LatLon(parseLat(n.getLat()), parseLon(n.getLon())).getRoundedToOsmPrecision());
                        checkCoordinates(nd.getCoor());
                        setMetadata(nd, info);
                        Map<String, String> keys = new HashMap<>();
                        for (int i = 0; i < n.getKeysCount(); i++) {
                            keys.put(getStringById(n.getKeys(i)), getStringById(n.getVals(i)));
                        }
                        nd.setKeys(keys);
                        buildPrimitive(nd);
                    }
                } catch (IllegalDataException e) {
                    exception = e;
                }
            }
        }

        @Override
        protected void parseWays(List<Osmformat.Way> osmWays) {
            if (exception == null) {
                try {
                    for (Osmformat.Way w : osmWays) {
                        final Info info = w.getInfo();
                        if (!info.hasVersion())
                            discourageUpload = true;
                        final WayData wd = new WayData(w.getId());
                        wd.setVersion(info.hasVersion() ? info.getVersion() : 1);
                        setMetadata(wd, info);
                        Map<String, String> keys = new HashMap<>();
                        for (int i = 0; i < w.getKeysCount(); i++) {
                            keys.put(getStringById(w.getKeys(i)), getStringById(w.getVals(i)));
                        }
                        wd.setKeys(keys);
                        long previousId = 0; // Node ids are delta coded
                        Collection<Long> nodeIds = new ArrayList<>();
                        for (Long id : w.getRefsList()) {
                            nodeIds.add(previousId += id);
                        }
                        ways.put(wd.getUniqueId(), nodeIds);
                        buildPrimitive(wd);
                    }
                } catch (IllegalDataException e) {
                    exception = e;
                }
            }
        }

        @Override
        protected void parseRelations(List<Osmformat.Relation> osmRels) {
            if (exception == null) {
                try {
                    for (Osmformat.Relation r : osmRels) {
                        final Info info = r.getInfo();
                        if (!info.hasVersion())
                            discourageUpload = true;
                        final RelationData rd = new RelationData(r.getId());
                        rd.setVersion(info.hasVersion() ? info.getVersion() : 1);
                        setMetadata(rd, info);
                        Map<String, String> keys = new HashMap<>();
                        for (int i = 0; i < r.getKeysCount(); i++) {
                            keys.put(getStringById(r.getKeys(i)), getStringById(r.getVals(i)));
                        }
                        rd.setKeys(keys);
                        long previousId = 0; // Member ids are delta coded
                        Collection<RelationMemberData> members = new ArrayList<>();
                        for (int i = 0; i < r.getMemidsCount(); i++) {
                            members.add(new RelationMemberData(
                                    getStringById(r.getRolesSid(i)),
                                    mapOsmType(r.getTypes(i)),
                                    previousId += r.getMemids(i)));
                        }
                        relations.put(rd.getUniqueId(), members);
                        buildPrimitive(rd);
                    }
                } catch (IllegalDataException e) {
                    exception = e;
                }
            }
            if (discourageUpload)
                ds.setUploadPolicy(UploadPolicy.DISCOURAGED);
        }

        private OsmPrimitiveType mapOsmType(MemberType type) {
            switch (type) {
            case NODE:
                return OsmPrimitiveType.NODE;
            case WAY:
                return OsmPrimitiveType.WAY;
            case RELATION:
                return OsmPrimitiveType.RELATION;
            default:
                return null;
            }
        }

        @Override
        public void complete() {
            if (discourageUpload)
                ds.setUploadPolicy(UploadPolicy.DISCOURAGED);
        }
    }

    private PbfParser parser = new PbfParser();

    /**
     * Parse the given input source and return the dataset.
     *
     * @param source the source input stream. Must not be null.
     * @param progressMonitor  the progress monitor. If null, {@see NullProgressMonitor#INSTANCE} is assumed
     *
     * @return the dataset with the parsed data
     * @throws IllegalDataException thrown if the an error was found while parsing the data from the source
     * @throws IllegalArgumentException thrown if source is null
     */
    public static DataSet parseDataSet(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
        ProgressMonitor monitor = progressMonitor == null ? NullProgressMonitor.INSTANCE : progressMonitor;
        CheckParameterUtil.ensureParameterNotNull(source, "source");
        return new PbfReader().doParseDataSet(source, monitor);
    }

    @Override
    protected DataSet doParseDataSet(InputStream source, ProgressMonitor monitor)
            throws IllegalDataException {
        try {
            monitor.beginTask(tr("Prepare OSM data...", 2));
            monitor.indeterminateSubTask(tr("Reading OSM data..."));

            parse(source);
            monitor.worked(1);

            monitor.indeterminateSubTask(tr("Preparing data set..."));
            prepareDataSet();
            monitor.worked(1);
            return getDataSet();
        } catch (IllegalDataException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalDataException(e);
        } finally {
            monitor.finishTask();
        }
    }

    public void parse(InputStream source) throws IOException, IllegalDataException {
        new BlockInputStream(source, parser).process();
        if (parser.exception != null) {
            throw parser.exception;
        }
    }
}
