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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMemberData;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.CheckParameterUtil;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBBox;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Info;
import crosby.binary.file.BlockInputStream;
import crosby.binary.file.FileBlockPosition;

/**
 * @author Don-vip
 *
 */
public class PbfReader extends AbstractReader {
    
    protected class PbfParser extends BinaryParser {

        public IllegalDataException exception = null;
        
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
                if (!b.isCollapsed() && LatLon.isValidLat(minlat) && LatLon.isValidLat(maxlat) 
                                     && LatLon.isValidLon(minlon) && LatLon.isValidLon(maxlon)) {
                    ds.dataSources.add(new DataSource(b, header.getSource()));
                } else {
                    Main.error("Invalid Bounds: "+b);
                }
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
                        Node node = new Node(nodeId+=nodes.getId(i), nodes.getDenseinfo().getVersion(i));
                        // Lat/Lon (delta)
                        node.setCoor(new LatLon(parseLat(nodeLat+=nodes.getLat(i)), parseLon(nodeLon+=nodes.getLon(i))).getRoundedToOsmPrecisionStrict());
                        checkCoordinates(node.getCoor());
                        // Changeset (delta)
                        checkChangesetId(changesetId+=nodes.getDenseinfo().getChangeset(i));
                        node.setChangesetId((int) changesetId);
                        // User (delta)
                        node.setUser(User.createOsmUser(uid+=nodes.getDenseinfo().getUid(i), getStringById(suid+=nodes.getDenseinfo().getUserSid(i))));
                        // Timestamp (delta)
                        checkTimestamp(timestamp+=nodes.getDenseinfo().getTimestamp(i));
                        node.setTimestamp(new Date(date_granularity * timestamp));
                        // A single table contains all keys/values of all nodes.
                        // Each node's tags are encoded in alternating <key_id> <value_id>.
                        // A single stringid of 0 delimit when the tags of a node ends and the tags of the next node begin.
                        Map<String, String> keys = new HashMap<>();
                        while (keyIndex < nodes.getKeysValsCount()) {
                            int key_id = nodes.getKeysVals(keyIndex++);
                            if (key_id == 0) {
                                break; // End of current node's tags
                            } else if (keyIndex < nodes.getKeysValsCount()) {
                                int value_id = nodes.getKeysVals(keyIndex++);
                                keys.put(getStringById(key_id), getStringById(value_id));
                            } else {
                                throw new IllegalDataException(tr("Invalid DenseNodes key/values table"));
                            }
                        }
                        node.setKeys(keys);
                        externalIdMap.put(node.getPrimitiveId(), node);
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
                        final Node node = new Node(n.getId(), info.getVersion());
                        node.setCoor(new LatLon(parseLat(n.getLat()), parseLon(n.getLon())).getRoundedToOsmPrecisionStrict());
                        checkCoordinates(node.getCoor());
                        checkChangesetId(info.getChangeset());
                        node.setChangesetId((int) info.getChangeset());
                        node.setUser(User.createOsmUser(info.getUid(), getStringById(info.getUserSid())));
                        checkTimestamp(info.getTimestamp());
                        node.setTimestamp(getDate(info));
                        Map<String, String> keys = new HashMap<>();
                        for (int i=0; i<n.getKeysCount(); i++) {
                            keys.put(getStringById(n.getKeys(i)), getStringById(n.getVals(i)));
                        }
                        node.setKeys(keys);
                        externalIdMap.put(node.getPrimitiveId(), node);
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
                        final Way way = new Way(w.getId(), info.getVersion());
                        checkChangesetId(info.getChangeset());
                        way.setChangesetId((int) info.getChangeset());
                        way.setUser(User.createOsmUser(info.getUid(), getStringById(info.getUserSid())));
                        checkTimestamp(info.getTimestamp());
                        way.setTimestamp(getDate(info));
                        Map<String, String> keys = new HashMap<>();
                        for (int i=0; i<w.getKeysCount(); i++) {
                            keys.put(getStringById(w.getKeys(i)), getStringById(w.getVals(i)));
                        }
                        way.setKeys(keys);
                        long previousId = 0; // Node ids are delta coded
                        Collection<Long> nodeIds = new ArrayList<>();
                        for (Long id : w.getRefsList()) {
                            nodeIds.add(previousId+=id);
                        }
                        ways.put(way.getUniqueId(), nodeIds);
                        externalIdMap.put(way.getPrimitiveId(), way);
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
                        final Relation rel = new Relation(r.getId(), info.getVersion());
                        checkChangesetId(info.getChangeset());
                        rel.setChangesetId((int) info.getChangeset());
                        rel.setUser(User.createOsmUser(info.getUid(), getStringById(info.getUserSid())));
                        checkTimestamp(info.getTimestamp());
                        rel.setTimestamp(getDate(info));
                        Map<String, String> keys = new HashMap<>();
                        for (int i=0; i<r.getKeysCount(); i++) {
                            keys.put(getStringById(r.getKeys(i)), getStringById(r.getVals(i)));
                        }
                        rel.setKeys(keys);
                        long previousId = 0; // Member ids are delta coded
                        Collection<RelationMemberData> members = new ArrayList<>();
                        for (int i = 0; i<r.getMemidsCount(); i++) {
                            long id = previousId+=r.getMemids(i);
                            String role = getStringById(r.getRolesSid(i));
                            OsmPrimitiveType type = null;
                            switch (r.getTypes(i)) {
                                case NODE:
                                    type = OsmPrimitiveType.NODE;
                                    break;
                                case WAY:
                                    type = OsmPrimitiveType.WAY;
                                    break;
                                case RELATION:
                                    type = OsmPrimitiveType.RELATION;
                                    break;
                            }
                            members.add(new RelationMemberData(role, type, id));
                        }
                        relations.put(rel.getUniqueId(), members);
                        externalIdMap.put(rel.getPrimitiveId(), rel);
                    }
                } catch (IllegalDataException e) {
                    exception = e;
                }
            }
        }

        @Override
        public void complete() {
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
        if (progressMonitor == null) {
            progressMonitor = NullProgressMonitor.INSTANCE;
        }
        CheckParameterUtil.ensureParameterNotNull(source, "source");

        PbfReader reader = new PbfReader();
        
        try {
            progressMonitor.beginTask(tr("Prepare OSM data...", 2));
            progressMonitor.indeterminateSubTask(tr("Reading OSM data..."));

            reader.parse(source);
            progressMonitor.worked(1);

            progressMonitor.indeterminateSubTask(tr("Preparing data set..."));
            reader.prepareDataSet();
            progressMonitor.worked(1);
            return reader.getDataSet();
        } catch (IllegalDataException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalDataException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }

    public void parse(InputStream source) throws IOException, IllegalDataException {
        new BlockInputStream(source, parser).process();
        if (parser.exception != null) {
            throw parser.exception;
        }
    }
}
