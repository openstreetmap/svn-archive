// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.o5m.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author GerdP
 *
 */
public class O5mReader extends AbstractReader {
    public IllegalDataException exception = null;
    private boolean discourageUpload;
    
    private static void checkCoordinates(LatLon coor) throws IllegalDataException {
        if (!coor.isValid()) {
            throw new IllegalDataException(tr("Invalid coordinates: {0}", coor));
        }
    }

    private static void checkChangesetId(long id) throws IllegalDataException {
        if (id > Integer.MAX_VALUE) {
            throw new IllegalDataException(tr("Invalid changeset id: {0}", id));
        }
    }
    
    private static void checkTimestamp(long timestamp) throws IllegalDataException {
        if (timestamp < 0) {
            throw new IllegalDataException(tr("Invalid timestamp: {0}", timestamp));
        }
    }
    
        // O5M data set constants
        private static final int NODE_DATASET = 0x10;
        private static final int WAY_DATASET = 0x11;
        private static final int REL_DATASET = 0x12;
        private static final int BBOX_DATASET = 0xdb;
        private static final int TIMESTAMP_DATASET = 0xdc;
        private static final int HEADER_DATASET = 0xe0;
        private static final int EOD_FLAG = 0xfe;
        private static final int RESET_FLAG = 0xff;
        
        private static final int EOF_FLAG = -1;
        
        // o5m constants
        private static final int STRING_TABLE_SIZE = 15000;
        private static final int MAX_STRING_PAIR_SIZE = 250 + 2;
        private static final String[] REL_REF_TYPES = {"node", "way", "relation", "?"};
        private static final double FACTOR = 1d/1000000000; // used with 100*<Val>*FACTOR 
        
        private final BufferedInputStream fis;
        private InputStream is;
        private ByteArrayInputStream bis;
        
        // buffer for byte -> String conversions
        private byte[] cnvBuffer; 
        
        private byte[] ioBuf;
        private int ioPos;
        // the o5m string table
        private String[][] stringTable;
        private String[] stringPair;
        private int currStringTablePos;
        // a counter that must be maintained by all routines that read data from the stream
        private int bytesToRead;
        // total number of bytes read from stream
        long countBytes;

        // for delta calculations
        private long lastNodeId;
        private long lastWayId;
        private long lastRelId;
        private long[] lastRef;
        private long lastTs;
        private long lastChangeSet;
        private int lastLon, lastLat;
        private int version;
        private User osmUser;
        private String header; 
        /**
         * A parser for the o5m format
         * @param stream The InputStream that contains the OSM data in o5m format 
         */
        O5mReader(InputStream stream) {
            this.fis = new BufferedInputStream(stream);
            is = fis;
            this.cnvBuffer = new byte[4000]; // OSM data should not contain string pairs with length > 512
            this.ioBuf = new byte[8192];
            this.ioPos = 0;
            this.stringPair = new String[2];
            this.lastRef = new long[3];
            reset();
        }

        /**
         * parse the input stream
         */
        public void parse() {
            try {
                int start = is.read();
                ++countBytes;
                if (start != RESET_FLAG) 
                    throw new IOException(tr("wrong header byte ") + Integer.toHexString(start));
                readFile();
                if (discourageUpload)
                    ds.setUploadPolicy(UploadPolicy.DISCOURAGED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void readFile() throws IOException {
            boolean done = false;
            while (!done) {
                is = fis;
                long size = 0;
                int fileType = is.read();
                ++countBytes;
                if (fileType >= 0 && fileType < 0xf0) {
                    bytesToRead = 0;
                    size = readUnsignedNum64FromStream();
                    countBytes += size - bytesToRead; // bytesToRead is negative 
                    bytesToRead = (int) size;
                    
                    switch(fileType) {
                    case NODE_DATASET: 
                    case WAY_DATASET: 
                    case REL_DATASET: 
                    case BBOX_DATASET:
                    case TIMESTAMP_DATASET:
                    case HEADER_DATASET:
                        if (bytesToRead > ioBuf.length) {
                            ioBuf = new byte[bytesToRead+100];
                        }
                        int bytesRead = 0;
                        int neededBytes = bytesToRead;
                        while (neededBytes > 0) {
                            bytesRead += is.read(ioBuf, bytesRead, neededBytes);
                            neededBytes -= bytesRead;
                        } 
                        ioPos = 0;
                        bis = new ByteArrayInputStream(ioBuf, 0, bytesToRead);
                        is = bis;
                        break;                    
                    default:    
                    }
                }
                if (fileType == EOF_FLAG) done = true; 
                else if (fileType == NODE_DATASET) readNode();
                else if (fileType == WAY_DATASET) readWay();
                else if (fileType == REL_DATASET) readRel();
                else if (fileType == BBOX_DATASET) readBBox();
                else if (fileType == TIMESTAMP_DATASET) readFileTimestamp();
                else if (fileType == HEADER_DATASET) readHeader();
                else if (fileType == EOD_FLAG) done = true;
                else if (fileType == RESET_FLAG) reset();
                else {
                    if (fileType < 0xf0) skip(size); // skip unknown data set 
                }
            }
        }
        
        /**
         * read (and ignore) the file timestamp data set
         */
        private void readFileTimestamp() {
            /*long fileTimeStamp = */readSignedNum64();
        }
        
        /**
         * Skip the given number of bytes
         * @param bytes number of bytes to skip 
         * @throws IOException in case of I/O error
         */
        private void skip(long bytes) throws IOException {
            long toSkip = bytes;
            while (toSkip > 0) {
                toSkip -= is.skip(toSkip);
            }
        }
        
        /**
         * read the bounding box data set
         * @throws IOException in case of I/O error
         */
        private void readBBox() {
            double minlon = FACTOR * 100L * readSignedNum32();
            double minlat = FACTOR * 100L * readSignedNum32();
            double maxlon = FACTOR * 100L * readSignedNum32();
            double maxlat = FACTOR * 100L * readSignedNum32();

            Bounds b = new Bounds(minlat, minlon, maxlat, maxlon);
            if (!b.isCollapsed() && LatLon.isValidLat(minlat) && LatLon.isValidLat(maxlat) 
                                 && LatLon.isValidLon(minlon) && LatLon.isValidLon(maxlon)) {
                ds.addDataSource(new DataSource(b, header));
            } else {
                Logging.error("Invalid Bounds: "+b);
            }
        }

        /**
         * read a node data set 
         * @throws IOException in case of I/O error
         */
        private void readNode() throws IOException {
            if (exception != null)
                return;
            try {
                lastNodeId += readSignedNum64();
                if (bytesToRead == 0)
                    return; // only nodeId: this is a delete action, we ignore it
                readVersionTsAuthor();

                if (bytesToRead == 0)
                    return; // only nodeId+version: this is a delete action, we ignore it 
                int lon = readSignedNum32() + lastLon; lastLon = lon;
                int lat = readSignedNum32() + lastLat; lastLat = lat;

                double flon = FACTOR * (100L*lon);
                double flat = FACTOR * (100L*lat);
                assert flat >= -90.0 && flat <= 90.0;  
                assert flon >= -180.0 && flon <= 180.0;  
                if (version == 0)
                    discourageUpload = true;
                Node node = new Node(lastNodeId, version == 0 ? 1 : version);
                node.setCoor(new LatLon(flat, flon).getRoundedToOsmPrecision());
                

                checkCoordinates(node.getCoor());
                checkChangesetId(lastChangeSet);
                node.setChangesetId((int) lastChangeSet);
                // User id
                if (lastTs != 0) {
                    checkTimestamp(lastTs);
                    node.setTimestamp(new Date(lastTs * 1000));
                    if (osmUser != null)
                        node.setUser(osmUser);
                }
                if (bytesToRead > 0) {
                    Map<String, String> keys = readTags();
                    node.setKeys(keys);
                }
                externalIdMap.put(node.getPrimitiveId(), node);
            } catch (IllegalDataException e) {
                exception = e;
            }

        }
        
        /**
         * read a way data set
         * @throws IOException in case of I/O error
         */
        private void readWay() throws IOException {
            if (exception != null)
                return;
            try {
                lastWayId += readSignedNum64();
                if (bytesToRead == 0)
                    return; // only wayId: this is a delete action, we ignore it 

                readVersionTsAuthor();
                if (bytesToRead == 0)
                    return; // only wayId + version: this is a delete action, we ignore it
                if (version == 0)
                    discourageUpload = true;
                final Way way = new Way(lastWayId, version == 0 ? 1 : version);
                checkChangesetId(lastChangeSet);
                way.setChangesetId((int) lastChangeSet);
                // User id
                if (lastTs != 0) {
                    checkTimestamp(lastTs);
                    way.setTimestamp(new Date(lastTs * 1000));
                    if (osmUser != null)
                        way.setUser(osmUser);
                }

                long refSize = readUnsignedNum32();
                long stop = bytesToRead - refSize;
                Collection<Long> nodeIds = new ArrayList<>();

                while (bytesToRead > stop) {
                    lastRef[0] += readSignedNum64();
                    nodeIds.add(lastRef[0]);
                }

                Map<String, String> keys = readTags();
                way.setKeys(keys);
                ways.put(way.getUniqueId(), nodeIds);
                externalIdMap.put(way.getPrimitiveId(), way);
            } catch (IllegalDataException e) {
                exception = e;
            }

        }

        /**
         * read a relation data set
         * @throws IOException in case of I/O error
         */
        private void readRel() throws IOException {
            if (exception != null)
                return;
            try {
                lastRelId += readSignedNum64(); 
                if (bytesToRead == 0)
                    return; // only relId: this is a delete action, we ignore it 
                readVersionTsAuthor();
                if (bytesToRead == 0)
                    return; // only relId + version: this is a delete action, we ignore it 
                if (version == 0)
                    discourageUpload = true;
                final Relation rel = new Relation(lastRelId, version == 0 ? 1 : version);
                checkChangesetId(lastChangeSet);
                rel.setChangesetId((int) lastChangeSet);
                if (lastTs != 0) {
                    checkTimestamp(lastTs);
                    rel.setTimestamp(new Date(lastTs * 1000));
                    if (osmUser != null)
                        rel.setUser(osmUser);
                }

                long refSize = readUnsignedNum32();
                long stop = bytesToRead - refSize;
                Collection<RelationMemberData> members = new ArrayList<>();
                while (bytesToRead > stop) {
                    long deltaRef = readSignedNum64();
                    int refType = readRelRef();
                    String role = stringPair[1];
                    lastRef[refType] += deltaRef;
                    long memId = lastRef[refType];
                    OsmPrimitiveType type = null;

                    if (refType == 0) {
                        type = OsmPrimitiveType.NODE;
                    } else if (refType == 1) {
                        type = OsmPrimitiveType.WAY;
                    } else if (refType == 2) {
                        type = OsmPrimitiveType.RELATION;
                    }
                    members.add(new RelationMemberData(role, type, memId));
                }
                Map<String, String> keys = readTags();
                rel.setKeys(keys);
                relations.put(rel.getUniqueId(), members);
                externalIdMap.put(rel.getPrimitiveId(), rel);
            } catch (IllegalDataException e) {
                exception = e;
            }
        }
        
        private Map<String, String> readTags() throws IOException {
            Map<String, String> keys = new HashMap<>();
            while (bytesToRead > 0) {
                readStringPair();
                keys.put(stringPair[0], stringPair[1]);
            }
            assert bytesToRead == 0;
            return keys;
        }
        
        /**
         * Store a new string pair (length check must be performed by caller)
         */
        private void storeStringPair() {
            stringTable[0][currStringTablePos] = stringPair[0];
            stringTable[1][currStringTablePos] = stringPair[1];
            ++currStringTablePos;
            if (currStringTablePos >= STRING_TABLE_SIZE)
                currStringTablePos = 0;
        }

        /**
         * set stringPair to the values referenced by given string reference
         * No checking is performed.
         * @param ref valid values are 1 .. STRING_TABLE_SIZE
         */
        private void setStringRefPair(int ref) {
            int pos = currStringTablePos - ref;
            if (pos < 0) 
                pos += STRING_TABLE_SIZE;
            stringPair[0] = stringTable[0][pos];
            stringPair[1] = stringTable[1][pos];
        }

        /**
         * Read version, time stamp and change set and author.  
         * We are not interested in the values, but we have to maintain the string table.
         * @throws IOException in case of I/O error
         */
        private void readVersionTsAuthor() throws IOException {
            stringPair[0] = null;
            stringPair[1] = null;
            version = readUnsignedNum32(); 
            if (version != 0) {
                // version info
                long ts = readSignedNum64() + lastTs; lastTs = ts;
                if (ts != 0) {
                    long changeSet = readSignedNum32() + lastChangeSet; lastChangeSet = changeSet;
                    readAuthor();
                }
            }
        }
        
        /**
         * Read author . 
         * @throws IOException in case of I/O error
         */
        private void readAuthor() throws IOException {
            int stringRef = readUnsignedNum32();
            if (stringRef == 0) {
                long toReadStart = bytesToRead;
                long uidNum = readUnsignedNum64();
                if (uidNum == 0)
                    stringPair[0] = "";
                else {
                    stringPair[0] = Long.toString(uidNum);
                    ioPos++; // skip terminating zero from uid
                    --bytesToRead;
                }
                int start = 0;
                int buffPos = 0; 
                stringPair[1] = null;
                while (stringPair[1] == null) {
                    final int b = ioBuf[ioPos++];
                    --bytesToRead;
                    cnvBuffer[buffPos++] = (byte) b;

                    if (b == 0)
                        stringPair[1] = new String(cnvBuffer, start, buffPos-1, "UTF-8");
                }
                long bytes = toReadStart - bytesToRead;
                if (bytes <= MAX_STRING_PAIR_SIZE)
                    storeStringPair();
            } else 
                setStringRefPair(stringRef);
            if (stringPair[0] != null && stringPair[0].isEmpty() == false) {
                long uid = Long.parseLong(stringPair[0]);
                osmUser = User.createOsmUser(uid, stringPair[1]);
            } else 
                osmUser = null;
        }
        
        /**
         * read object type ("0".."2") concatenated with role (single string) 
         * @return 0..3 for type (3 means unknown)
         */
        private int readRelRef() throws IOException {
            int refType = -1;
            long toReadStart = bytesToRead;
            int stringRef = readUnsignedNum32();
            if (stringRef == 0) {
                refType = ioBuf[ioPos++] - 0x30;
                --bytesToRead;

                if (refType < 0 || refType > 2)
                    refType = 3;
                stringPair[0] = REL_REF_TYPES[refType];
                    
                int start = 0;
                int buffPos = 0; 
                stringPair[1] = null;
                while (stringPair[1] == null) {
                    final int b = ioBuf[ioPos++];
                    --bytesToRead;
                    cnvBuffer[buffPos++] = (byte) b;

                    if (b == 0)
                        stringPair[1] = new String(cnvBuffer, start, buffPos-1, "UTF-8");
                }
                long bytes = toReadStart - bytesToRead;
                if (bytes <= MAX_STRING_PAIR_SIZE)
                    storeStringPair();
            } else {
                setStringRefPair(stringRef);
                char c = stringPair[0].charAt(0);
                switch (c) {
                case 'n': refType = 0; break;
                case 'w': refType = 1; break;
                case 'r': refType = 2; break;
                default: refType = 3;
                }
            }
            return refType;
        }
        
        /**
         * read a string pair (see o5m definition)
         * @throws IOException in case of I/O error
         */
        private void readStringPair() throws IOException {
            int stringRef = readUnsignedNum32();
            if (stringRef == 0) {
                long toReadStart = bytesToRead;
                int cnt = 0;
                int buffPos = 0; 
                int start = 0;
                while (cnt < 2) {
                    final int b = ioBuf[ioPos++];
                    --bytesToRead;
                    cnvBuffer[buffPos++] = (byte) b;

                    if (b == 0) {
                        stringPair[cnt] = new String(cnvBuffer, start, buffPos-start-1, "UTF-8");
                        ++cnt;
                        start = buffPos;
                    }
                }
                long bytes = toReadStart - bytesToRead;
                if (bytes <= MAX_STRING_PAIR_SIZE)
                    storeStringPair();
            } else 
                setStringRefPair(stringRef);
        }
        
        /** reset the delta values and string table */
        private void reset() {
            lastNodeId = 0; lastWayId = 0; lastRelId = 0;
            lastRef[0] = 0; lastRef[1] = 0; lastRef[2] = 0;
            lastTs = 0; lastChangeSet = 0;
            lastLon = 0; lastLat = 0;
            stringTable = new String[2][STRING_TABLE_SIZE];
            currStringTablePos = 0;
        }

        /**
         * read and verify o5m header (known values are o5m2 and o5c2)
         * @throws IOException in case of I/O error
         */
        private void readHeader() throws IOException {
            if (ioBuf[0] != 'o' || ioBuf[1] != '5' || (ioBuf[2] != 'c' && ioBuf[2] != 'm') || ioBuf[3] != '2') {
                throw new IOException(tr("unsupported header"));
            }
            header = new String(ioBuf, 0, 3, "UTF-8");
        }
        
        /**
         * read a varying length signed number (see o5m definition)
         * @return the number
         * @throws IOException in case of I/O error
         */
        private int readSignedNum32() {
            int result;
            int b = ioBuf[ioPos++];
            --bytesToRead;
            result = b;
            if ((b & 0x80) == 0) {  // just one byte
                if ((b & 0x01) == 1)
                    return -1-(result >> 1); 
                return result >> 1;
            }
            int sign = b & 0x01;
            result = (result & 0x7e) >> 1;
            int fac = 0x40;
            while (((b = ioBuf[ioPos++]) & 0x80) != 0) { // more bytes will follow
                --bytesToRead;
                result += fac * (b & 0x7f);
                fac <<= 7;
            }
            --bytesToRead;
            result += fac * b;
            if (sign == 1) // negative
                return -1-result;
            return result;

        }

        /**
         * read a varying length signed number (see o5m definition)
         * @return the number
         * @throws IOException in case of I/O error
         */
        private long readSignedNum64() {
            long result;
            int b = ioBuf[ioPos++];
            --bytesToRead;
            result = b;
            if ((b & 0x80) == 0) {  // just one byte
                if ((b & 0x01) == 1)
                    return -1-(result >> 1); 
                return result >> 1;
            }
            int sign = b & 0x01;
            result = (result & 0x7e) >> 1;
            long fac = 0x40;
            while (((b = ioBuf[ioPos++]) & 0x80) != 0) { // more bytes will follow
                --bytesToRead;
                result += fac * (b & 0x7f);
                fac <<= 7;
            }
            --bytesToRead;
            result += fac * b;
            if (sign == 1) // negative
                return -1-result;
            return result;

        }

        /**
         * read a varying length unsigned number (see o5m definition)
         * @return a long
         * @throws IOException in case of I/O error
         */
        private long readUnsignedNum64FromStream()throws IOException {
            int b = is.read();
            --bytesToRead;
            long result = b;
            if ((b & 0x80) == 0) {  // just one byte
                return result;
            }
            result &= 0x7f;
            long fac = 0x80;
            while (((b = is.read()) & 0x80) != 0) { // more bytes will follow
                --bytesToRead;
                result += fac * (b & 0x7f);
                fac <<= 7;
            }
            --bytesToRead;
            result += fac * b;
            return result;
        }
        
        
        /**
         * read a varying length unsigned number (see o5m definition)
         * @return a long
         * @throws IOException in case of I/O error
         */
        private long readUnsignedNum64() {
            int b = ioBuf[ioPos++];
            --bytesToRead;
            long result = b;
            if ((b & 0x80) == 0) {  // just one byte
                return result;
            }
            result &= 0x7f;
            long fac = 0x80;
            while (((b = ioBuf[ioPos++]) & 0x80) != 0) { // more bytes will follow
                --bytesToRead;
                result += fac * (b & 0x7f);
                fac <<= 7;
            }
            --bytesToRead;
            result += fac * b;
            return result;
        }

        /**
         * read a varying length unsigned number (see o5m definition)
         * is similar to the 64 bit version.
         * @return an int 
         * @throws IOException in case of I/O error
         */
        private int readUnsignedNum32() {
            int b = ioBuf[ioPos++];
            --bytesToRead;
            int result = b;
            if ((b & 0x80) == 0) {  // just one byte
                return result;
            }
            result &= 0x7f;
            long fac = 0x80;
            while (((b = ioBuf[ioPos++]) & 0x80) != 0) { // more bytes will follow
                --bytesToRead;
                result += fac * (b & 0x7f);
                fac <<= 7;
            }
            --bytesToRead;
            result += fac * b;
            return result;
        }

        
    
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
        return new O5mReader(source).doParseDataSet(source, progressMonitor);
    }

    @Override
    protected DataSet doParseDataSet(InputStream source, ProgressMonitor progressMonitor)
            throws IllegalDataException {
        try {
            progressMonitor.beginTask(tr("Prepare OSM data...", 2));
            progressMonitor.indeterminateSubTask(tr("Reading OSM data..."));

            parse();
            progressMonitor.worked(1);

            progressMonitor.indeterminateSubTask(tr("Preparing data set..."));
            prepareDataSet();
            progressMonitor.worked(1);
            return getDataSet();
        } catch (IllegalDataException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalDataException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
