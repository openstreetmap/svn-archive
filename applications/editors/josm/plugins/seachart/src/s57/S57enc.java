// License: GPL. For details, see LICENSE file.
package s57;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;

import s57.S57att.Att;
import s57.S57dat.Fparams;
import s57.S57dat.S57field;
import s57.S57map.AttMap;
import s57.S57map.Feature;
import s57.S57map.Nflag;
import s57.S57map.ObjTab;
import s57.S57map.Pflag;
import s57.S57map.Prim;
import s57.S57obj.Obj;
import s57.S57val.AttVal;

/**
 * @author Malcolm Herring
 */
public final class S57enc { // S57 ENC file generation
    private S57enc() {
        // Hide default constructor for utilities classes
    }
    // CHECKSTYLE.OFF: LineLength

    private static final byte[] header = {

            '0', '1', '5', '7', '6', '3', 'L', 'E', '1', ' ', '0', '9', '0', '0', '2', '0', '1', ' ', '!', ' ', '3', '4', '0', '4', // Leader
            '0', '0', '0', '0', '1', '2', '3', '0', '0', '0', '0', '0', '0', '0', '1', '0', '4', '7', '0', '1', '2', '3',
            'D', 'S', 'I', 'D', '1', '5', '9', '0', '1', '7', '0', 'D', 'S', 'S', 'I', '1', '1', '3', '0', '3', '2', '9',
            'D', 'S', 'P', 'M', '1', '3', '0', '0', '4', '4', '2', 'F', 'R', 'I', 'D', '1', '0', '0', '0', '5', '7', '2',
            'F', 'O', 'I', 'D', '0', '7', '0', '0', '6', '7', '2', 'A', 'T', 'T', 'F', '0', '5', '9', '0', '7', '4', '2',
            'N', 'A', 'T', 'F', '0', '6', '8', '0', '8', '0', '1', 'F', 'F', 'P', 'T', '0', '8', '6', '0', '8', '6', '9',
            'F', 'S', 'P', 'T', '0', '9', '0', '0', '9', '5', '5', 'V', 'R', 'I', 'D', '0', '7', '8', '1', '0', '4', '5',
            'A', 'T', 'T', 'V', '0', '5', '8', '1', '1', '2', '3', 'V', 'R', 'P', 'T', '0', '7', '6', '1', '1', '8', '1',
            'S', 'G', '2', 'D', '0', '4', '8', '1', '2', '5', '7', 'S', 'G', '3', 'D', '0', '7', '0', '1', '3', '0', '5', 0x1e,
            // File control field
            '0', '0', '0', '0', ';', '&', ' ', ' ', ' ', 0x1f,
            '0', '0', '0', '1', 'D', 'S', 'I', 'D', 'D', 'S', 'I', 'D', 'D', 'S', 'S', 'I', '0', '0', '0', '1', 'D', 'S', 'P', 'M',
            '0', '0', '0', '1', 'F', 'R', 'I', 'D', 'F', 'R', 'I', 'D', 'F', 'O', 'I', 'D', 'F', 'R', 'I', 'D', 'A', 'T', 'T', 'F',
            'F', 'R', 'I', 'D', 'N', 'A', 'T', 'F', 'F', 'R', 'I', 'D', 'F', 'F', 'P', 'T', 'F', 'R', 'I', 'D', 'F', 'S', 'P', 'T',
            '0', '0', '0', '1', 'V', 'R', 'I', 'D', 'V', 'R', 'I', 'D', 'A', 'T', 'T', 'V', 'V', 'R', 'I', 'D', 'V', 'R', 'P', 'T',
            'V', 'R', 'I', 'D', 'S', 'G', '2', 'D', 'V', 'R', 'I', 'D', 'S', 'G', '3', 'D', 0x1e,
            // Record identifier fields
            '0', '5', '0', '0', ';', '&', ' ', ' ', ' ', 'I', 'S', 'O', '/', 'I', 'E', 'C', ' ', '8', '2', '1', '1', ' ',
            'R', 'e', 'c', 'o', 'r', 'd', ' ', 'I', 'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', 0x1f, 0x1f, '(', 'b', '1', '2', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 'S', 'e', 't', ' ', 'I', 'd', 'e', 'n', 't', 'i',
            'f', 'i', 'c', 'a', 't', 'i', 'o', 'n', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'E', 'X', 'P', 'P', '!',
            'I', 'N', 'T', 'U', '!', 'D', 'S', 'N', 'M', '!', 'E', 'D', 'T', 'N', '!', 'U', 'P', 'D', 'N', '!', 'U', 'A', 'D', 'T',
            '!', 'I', 'S', 'D', 'T', '!', 'S', 'T', 'E', 'D', '!', 'P', 'R', 'S', 'P', '!', 'P', 'S', 'D', 'N', '!', 'P', 'R', 'E',
            'D', '!', 'P', 'R', 'O', 'F', '!', 'A', 'G', 'E', 'N', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'b', '1', '1', ',', 'b', '1',
            '4', ',', '2', 'b', '1', '1', ',', '3', 'A', ',', '2', 'A', '(', '8', ')', ',', 'R', '(', '4', ')', ',', 'b', '1', '1',
            ',', '2', 'A', ',', 'b', '1', '1', ',', 'b', '1', '2', ',', 'A', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 's', 'e', 't', ' ', 's', 't', 'r', 'u', 'c', 't',
            'u', 'r', 'e', ' ', 'i', 'n', 'f', 'o', 'r', 'm', 'a', 't', 'i', 'o', 'n', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f,
            'D', 'S', 'T', 'R', '!', 'A', 'A', 'L', 'L', '!', 'N', 'A', 'L', 'L', '!', 'N', 'O', 'M', 'R', '!', 'N', 'O', 'C', 'R',
            '!', 'N', 'O', 'G', 'R', '!', 'N', 'O', 'L', 'R', '!', 'N', 'O', 'I', 'N', '!', 'N', 'O', 'C', 'N', '!', 'N', 'O', 'E',
            'D', '!', 'N', 'O', 'F', 'A', 0x1f, '(', '3', 'b', '1', '1', ',', '8', 'b', '1', '4', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'D', 'a', 't', 'a', ' ', 's', 'e', 't', ' ', 'p', 'a', 'r', 'a', 'm', 'e',
            't', 'e', 'r', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'H', 'D', 'A', 'T',
            '!', 'V', 'D', 'A', 'T', '!', 'S', 'D', 'A', 'T', '!', 'C', 'S', 'C', 'L', '!', 'D', 'U', 'N', 'I', '!', 'H', 'U', 'N',
            'I', '!', 'P', 'U', 'N', 'I', '!', 'C', 'O', 'U', 'N', '!', 'C', 'O', 'M', 'F', '!', 'S', 'O', 'M', 'F', '!', 'C', 'O',
            'M', 'T', 0x1f, '(', 'b', '1', '1', ',', 'b', '1', '4', ',', '3', 'b', '1', '1', ',', 'b', '1', '4', ',', '4', 'b', '1',
            '1', ',', '2', 'b', '1', '4', ',', 'A', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'i',
            'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D',
            '!', 'P', 'R', 'I', 'M', '!', 'G', 'R', 'U', 'P', '!', 'O', 'B', 'J', 'L', '!', 'R', 'V', 'E', 'R', '!', 'R', 'U', 'I', 'N', 0x1f,
            '(', 'b', '1', '1', ',', 'b', '1', '4', ',', '2', 'b', '1', '1', ',', '2', 'b', '1', '2', ',', 'b', '1', '1', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'o', 'b', 'j', 'e', 'c', 't', ' ', 'i',
            'd', 'e', 'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, 'A', 'G', 'E', 'N', '!', 'F', 'I', 'D', 'N',
            '!', 'F', 'I', 'D', 'S', 0x1f, '(', 'b', '1', '2', ',', 'b', '1', '4', ',', 'b', '1', '2', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', '-', 'A', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'a',
            't', 't', 'r', 'i', 'b', 'u', 't', 'e', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, '*', 'A', 'T', 'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f,
            '(', 'b', '1', '2', ',', 'A', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', '-', 'A', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'n', 'a',
            't', 'i', 'o', 'n', 'a', 'l', ' ', 'a', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, '*', 'A', 'T',
            'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f, '(', 'b', '1', '2', ',', 'A', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 't', 'o',
            ' ', 'f', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'o', 'b', 'j', 'e', 'c', 't', ' ', 'p', 'o', 'i', 'n', 't', 'e', 'r', ' ', 'f', 'i',
            'e', 'l', 'd', 0x1f, '*', 'L', 'N', 'A', 'M', '!', 'R', 'I', 'N', 'D', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'B', '(', '6', '4',
            ')', ',', 'b', '1', '1', ',', 'A', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'F', 'e', 'a', 't', 'u', 'r', 'e', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 't', 'o',
            ' ', 's', 'p', 'a', 't', 'i', 'a', 'l', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'p', 'o', 'i', 'n', 't', 'e', 'r', ' ', 'f', 'i',
            'e', 'l', 'd', 0x1f, '*', 'N', 'A', 'M', 'E', '!', 'O', 'R', 'N', 'T', '!', 'U', 'S', 'A', 'G', '!', 'M', 'A', 'S', 'K', 0x1f,
            '(', 'B', '(', '4', '0', ')', ',', '3', 'b', '1', '1', ')', 0x1e,
            '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'i', 'd', 'e',
            'n', 't', 'i', 'f', 'i', 'e', 'r', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'R', 'V',
            'E', 'R', '!', 'R', 'U', 'I', 'N', 0x1f, '(', 'b', '1', '1', ',', 'b', '1', '4', ',', 'b', '1', '2', ',', 'b', '1', '1', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'a', 't', 't',
            'r', 'i', 'b', 'u', 't', 'e', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, '*', 'A', 'T', 'T', 'L', '!', 'A', 'T', 'V', 'L', 0x1f, '(', 'b',
            '1', '2', ',', 'A', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'V', 'e', 'c', 't', 'o', 'r', ' ', 'r', 'e', 'c', 'o', 'r', 'd', ' ', 'p', 'o', 'i',
            'n', 't', 'e', 'r', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, '*', 'N', 'A', 'M', 'E', '!', 'O', 'R', 'N', 'T', '!', 'U', 'S', 'A', 'G',
            '!', 'T', 'O', 'P', 'I', '!', 'M', 'A', 'S', 'K', 0x1f, '(', 'B', '(', '4', '0', ')', ',', '4', 'b', '1', '1', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', '2', '-', 'D', ' ', 'c', 'o', 'o', 'r', 'd', 'i', 'n', 'a', 't', 'e', ' ', 'f', 'i',
            'e', 'l', 'd', 0x1f, '*', 'Y', 'C', 'O', 'O', '!', 'X', 'C', 'O', 'O', 0x1f, '(', '2', 'b', '2', '4', ')', 0x1e,
            '2', '6', '0', '0', ';', '&', ' ', ' ', ' ', '3', '-', 'D', ' ', 'c', 'o', 'o', 'r', 'd', 'i', 'n', 'a', 't', 'e', ' ', '(', 's',
            'o', 'u', 'n', 'd', 'i', 'n', 'g', ' ', 'a', 'r', 'r', 'a', 'y', ')', ' ', 'f', 'i', 'e', 'l', 'd', 0x1f, '*', 'Y', 'C', 'O', 'O',
            '!', 'X', 'C', 'O', 'O', '!', 'V', 'E', '3', 'D', 0x1f, '(', '3', 'b', '2', '4', ')', 0x1e
    };

    static final double COMF = 10000000;
    static final double SOMF = 10;

    static String file = "0S000000.000";
    static int intu = 0;
    static String code = "0S";
    static int agen = 3878;
    static int cscl = 10000;
    static int vdat = 23;
    static int duni = 1;
    static int huni = 1;

    static int idx;
    static int recs;

    static int isols;
    static int conns;
    static int metas;
    static int geos;
    static int edges;

    static long hash(long val) {
        byte[] bval = ByteBuffer.allocate(Long.SIZE).putLong(val).array();
        CRC32 crc = new CRC32();
        crc.update(bval);
        return crc.getValue();
    }

    public static int encodeChart(S57map map, HashMap<String, String> meta, byte[] buf) throws IndexOutOfBoundsException, UnsupportedEncodingException {

        for (Entry<String, String> entry : meta.entrySet()) {
            try {
                switch (entry.getKey()) {
                case "FILE":
                    file = entry.getValue();
                    break;
                case "INTU":
                    intu = Integer.parseInt(entry.getValue());
                    break;
                case "AGEN":
                    String[] tokens = entry.getValue().split("/");
                    code = tokens[0];
                    agen = Integer.parseInt(tokens[1]);
                    break;
                case "VDAT":
                    vdat = Integer.parseInt(entry.getValue());
                    break;
                case "CSCL":
                    cscl = Integer.parseInt(entry.getValue());
                    break;
                case "DUNI":
                    duni = Integer.parseInt(entry.getValue());
                    break;
                case "HUNI":
                    huni = Integer.parseInt(entry.getValue());
                    break;
                }
            } catch (Exception e) {
                System.err.println("Meta data (" + entry.getKey() + "=" + entry.getValue() + "):" + e.getMessage());
                System.exit(-1);
            }
        }

        //M_COVR & MNSYS in BB if not in map
        if (!map.features.containsKey(Obj.M_COVR)) {
            S57osm.OSMmeta(map);
        }

        S57dat.S57geoms(map);

        byte[] record;
        ArrayList<Fparams> fields;

        isols = conns = metas = geos = edges = 0;
        String date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        ArrayList<Fparams> ds = new ArrayList<>();
        ds.add(new Fparams(S57field.DSID, new Object[] {10, 1, 1, intu, file, "1", "0", date, date, "03.1", 1, "ENC", "2.0", 1, agen, "Generated by OpenSeaMap.org" }));
        ds.add(new Fparams(S57field.DSSI, new Object[] {2, 1, 2, metas, 0, geos, 0, isols, conns, edges, 0 }));
        ArrayList<Fparams> dp = new ArrayList<>();
        dp.add(new Fparams(S57field.DSPM, new Object[] {20, 2, 2, vdat, vdat, cscl, duni, huni, 1, 1, 10000000, 10, "" }));

        System.arraycopy(header, 0, buf, 0, header.length);
        idx = header.length;
        record = S57dat.encRecord(1, ds);
        System.arraycopy(record, 0, buf, idx, record.length);
        idx += record.length;
        record = S57dat.encRecord(2, dp);
        System.arraycopy(record, 0, buf, idx, record.length);
        idx += record.length;
        recs = 3;

        // Depths
        Object[] depths = new Object[0];
        for (Map.Entry<Long, S57map.Snode> entry : map.nodes.entrySet()) {
            S57map.Snode node = entry.getValue();
            if (node.flg == Nflag.DPTH) {
                Object[] dval = new Object[] {(Math.toDegrees(node.lat) * COMF), (Math.toDegrees(node.lon) * COMF), (node.val * SOMF) };
                depths = Arrays.copyOf(depths, (depths.length + dval.length));
                System.arraycopy(dval, 0, depths, (depths.length - dval.length), dval.length);
            }
        }
        if (depths.length > 0) {
            fields = new ArrayList<>();
            fields.add(new Fparams(S57field.VRID, new Object[] {110, -2, 1, 1 }));
            fields.add(new Fparams(S57field.SG3D, depths));
            record = S57dat.encRecord(recs++, fields);
            System.arraycopy(record, 0, buf, idx, record.length);
            idx += record.length;
            isols++;
        }

        // Isolated nodes
        for (Map.Entry<Long, S57map.Snode> entry : map.nodes.entrySet()) {
            S57map.Snode node = entry.getValue();
            if (node.flg == Nflag.ISOL) {
                fields = new ArrayList<>();
                fields.add(new Fparams(S57field.VRID, new Object[] {110, hash(entry.getKey()), 1, 1 }));
                fields.add(new Fparams(S57field.SG2D, new Object[] {(Math.toDegrees(node.lat) * COMF), (Math.toDegrees(node.lon) * COMF) }));
                record = S57dat.encRecord(recs++, fields);
                System.arraycopy(record, 0, buf, idx, record.length);
                idx += record.length;
                isols++;
            }
        }

        // Connected nodes
        for (Map.Entry<Long, S57map.Snode> entry : map.nodes.entrySet()) {
            S57map.Snode node = entry.getValue();
            if (node.flg == Nflag.CONN) {
                fields = new ArrayList<>();
                fields.add(new Fparams(S57field.VRID, new Object[] {120, hash(entry.getKey()), 1, 1 }));
                fields.add(new Fparams(S57field.SG2D, new Object[] {(Math.toDegrees(node.lat) * COMF), (Math.toDegrees(node.lon) * COMF) }));
                record = S57dat.encRecord(recs++, fields);
                System.arraycopy(record, 0, buf, idx, record.length);
                idx += record.length;
                conns++;
            }
        }

        // Edges
        for (Map.Entry<Long, S57map.Edge> entry : map.edges.entrySet()) {
            S57map.Edge edge = entry.getValue();
            fields = new ArrayList<>();
            fields.add(new Fparams(S57field.VRID, new Object[] {130, hash(entry.getKey()), 1, 1}));
            fields.add(new Fparams(S57field.VRPT, new Object[] {(((hash(edge.first) & 0xffffffff) << 8) + 120L), 255, 255, 1, 255, (((hash(edge.last) & 0xffffffff) << 8) + 120L), 255, 255, 2, 255 }));
            Object[] nodes = new Object[0];
            for (long ref : edge.nodes) {
                Object[] nval = new Object[] {(Math.toDegrees(map.nodes.get(ref).lat) * COMF), (Math.toDegrees(map.nodes.get(ref).lon) * COMF) };
                nodes = Arrays.copyOf(nodes, (nodes.length + nval.length));
                System.arraycopy(nval, 0, nodes, (nodes.length - nval.length), nval.length);
            }
            if (nodes.length > 0) {
                fields.add(new Fparams(S57field.SG2D, nodes));
            }
            record = S57dat.encRecord(recs++, fields);
            System.arraycopy(record, 0, buf, idx, record.length);
            idx += record.length;
            edges++;
        }

        // Meta & Geo objects
        boolean soundings = false;
        for (Entry<Obj, ArrayList<Feature>> entry : map.features.entrySet()) {
            Obj obj = entry.getKey();
            for (Feature feature : entry.getValue()) {
                if (obj == Obj.SOUNDG) {
                    if (soundings) {
                        continue;
                    } else {
                        soundings = true;
                    }
                }
                int prim = feature.geom.prim.ordinal();
                prim = (prim == 0) ? 255 : prim;
                int grup = ((obj == Obj.DEPARE) || (obj == Obj.DRGARE) || (obj == Obj.FLODOC) || (obj == Obj.HULKES) || (obj == Obj.LNDARE) || (obj == Obj.PONTON) || (obj == Obj.UNSARE)) ? 1 : 2;

                ArrayList<Fparams> geom = new ArrayList<>();
                int outers = 0;
                outers = (feature.geom.prim == Pflag.POINT) ? 1 : feature.geom.comps.get(0).size;
                for (Prim elem : feature.geom.elems) {
                    if (feature.geom.prim == Pflag.POINT) {
                        if (obj == Obj.SOUNDG) {
                            geom.add(new Fparams(S57field.FSPT, new Object[] {((-2 << 8) + 110L), 255, 255, 255 }));
                        } else {
                            geom.add(new Fparams(S57field.FSPT, new Object[] {((hash(elem.id) << 8) + ((map.nodes.get(elem.id).flg == Nflag.CONN) ? 120L : 110L)), 255, 255, 255 }));
                        }
                    } else {
                        geom.add(new Fparams(S57field.FSPT, new Object[] {((hash(elem.id) << 8) + 130L), (elem.forward ? 1 : 2), ((outers-- > 0) ? 1 : 2), 2 }));
                    }
                }

                ArrayList<ArrayList<Fparams>> objects = new ArrayList<>();
                ArrayList<Long> slaves = new ArrayList<>();
                long slaveid = feature.id + 0x0100000000000000L;
                for (Entry<Obj, ObjTab> objs : feature.objs.entrySet()) {
                    Obj objobj = objs.getKey();
                    boolean master = true;
                    for (Entry<Integer, AttMap> object : objs.getValue().entrySet()) {
                        ArrayList<Fparams> objatts = new ArrayList<>();
                        master = (feature.type == objobj) && ((object.getKey() == 0) || (object.getKey() == 1));
                        long id = hash(master ? feature.id : slaveid);
                        objatts.add(new Fparams(S57field.FRID, new Object[] {100, id, prim, grup, S57obj.encodeType(objobj), 1, 1}));
                        objatts.add(new Fparams(S57field.FOID, new Object[] {agen, id, 1}));
                        Object[] attf = new Object[0];
                        Object[] natf = new Object[0];
                        AttMap atts = new AttMap();
                        atts.putAll(object.getValue());
                        if (master) {
                            atts.putAll(feature.atts);
                        }
                        for (Entry<Att, AttVal<?>> att : atts.entrySet()) {
                            if (!((obj == Obj.SOUNDG) && (att.getKey() == Att.VALSOU))) {
                                long attl = S57att.encodeAttribute(att.getKey());
                                Object[] next = new Object[] {attl, S57val.encodeValue(att.getValue(), att.getKey())};
                                if ((attl < 300) || (attl > 304)) {
                                    attf = Arrays.copyOf(attf, (attf.length + next.length));
                                    System.arraycopy(next, 0, attf, (attf.length - next.length), next.length);
                                } else {
                                    natf = Arrays.copyOf(natf, (natf.length + next.length));
                                    System.arraycopy(next, 0, natf, (natf.length - next.length), next.length);
                                }
                            }
                        }
                        if (attf.length > 0) {
                            objatts.add(new Fparams(S57field.ATTF, attf));
                        }
                        if (natf.length > 0) {
                            objatts.add(new Fparams(S57field.NATF, attf));
                        }
                        if (master) {
                            objects.add(objatts);
                        } else {
                            slaves.add(id);
                            objects.add(0, objatts);
                            slaveid += 0x0100000000000000L;
                        }
                    }
                }

                if (!slaves.isEmpty()) {
                    ArrayList<Fparams> refs = new ArrayList<>();
                    Object[] params = new Object[0];
                    while (!slaves.isEmpty()) {
                        long id = slaves.remove(0);
                        Object[] next = new Object[] {(long) ((((id & 0xffffffff) + 0x100000000L) << 16) + (agen & 0xffff)), 2, "" };
                        params = Arrays.copyOf(params, (params.length + next.length));
                        System.arraycopy(next, 0, params, (params.length - next.length), next.length);
                    }
                    refs.add(new Fparams(S57field.FFPT, params));
                    objects.get(objects.size() - 1).addAll(refs);
                }

                for (ArrayList<Fparams> object : objects) {
                    object.addAll(geom);
                    record = S57dat.encRecord(recs++, object);
                    System.arraycopy(record, 0, buf, idx, record.length);
                    idx += record.length;
                    if ((obj == Obj.M_COVR) || (obj == Obj.M_NSYS)) {
                        metas++;
                    } else {
                        geos++;
                    }
                }
            }
        }

        // Re-write DSID/DSSI with final totals
        ds = new ArrayList<>();
        ds.add(new Fparams(S57field.DSID, new Object[] {10, 1, 1, intu, file, "1", "0", date, date, "03.1", 1, "ENC", "2.0", 1, agen, "Generated by OpenSeaMap.org" }));
        ds.add(new Fparams(S57field.DSSI, new Object[] {2, 1, 2, metas, 0, geos, 0, isols, conns, edges, 0 }));
        record = S57dat.encRecord(1, ds);
        System.arraycopy(record, 0, buf, header.length, record.length);

        return idx;
    }

}
