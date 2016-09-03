// License: GPL. For details, see LICENSE file.
package s57;

import java.io.FileInputStream;
import java.io.IOException;

import s57.S57dat.S57field;
import s57.S57dat.S57subf;
import s57.S57map.Nflag;

/**
 * @author Malcolm Herring
 */
public final class S57dec { // S57 ENC file input & map conversion
    private S57dec() {
        // Hide default constructor for utilities classes
    }
    // CHECKSTYLE.OFF: LineLength

    public static void decodeChart(FileInputStream in, S57map map) throws IOException {
        S57dat.rnum = 0;
        byte[] leader = new byte[24];
        byte[] record = new byte[0];
        boolean ddr = false;
        int length = 0;
        int fields = 0;;
        int mapfl, mapfp, mapts, entry;
        String tag;
        int len;
        int pos;
        boolean inFeature = false;

        double comf = 1;
        double somf = 1;
        long name = 0;
        S57map.Nflag nflag = Nflag.ANON;
        S57map.Pflag pflag = S57map.Pflag.NOSP;
        long objl = 0;

        while (in.read(leader) == 24) {
            try {
                length = Integer.parseInt(new String(leader, 0, 5)) - 24;
                record = new byte[length];
                ddr = (leader[6] == 'L');
                fields = Integer.parseInt(new String(leader, 12, 5)) - 24;
            } catch (Exception e) {
                System.err.println("Invalid file format - Encrypted/compressed ENC file?");
                System.exit(-1);
            }
            mapfl = leader[20] - '0';
            mapfp = leader[21] - '0';
            mapts = leader[23] - '0';
            entry = mapfl + mapfp + mapts;
            if (in.read(record) != length)
                break;
            for (int idx = 0; idx < fields-1; idx += entry) {
                tag = new String(record, idx, mapts);
                len = Integer.parseInt(new String(record, idx+mapts, mapfl));
                pos = Integer.parseInt(new String(record, idx+mapts+mapfl, mapfp));
                if (!ddr) {
                    switch (tag.toString()) {
                    case "0001":
                        int i8rn = ((Long) S57dat.decSubf(record, fields + pos, S57field.I8RI, S57subf.I8RN)).intValue();
                        //                        if (i8rn != ++S57dat.rnum) {
                        //                            System.err.println("Out of order record ID");
                        //                            in.close();
                        //                            System.exit(-1);
                        //                        }
                        break;
                    case "DSSI":
                        S57dat.decSubf(record, fields + pos, S57field.DSSI, S57subf.AALL);
                        S57dat.decSubf(S57subf.NALL);
                        break;
                    case "DSPM":
                        comf = (Long) S57dat.decSubf(record, fields + pos, S57field.DSPM, S57subf.COMF);
                        somf = (Long) S57dat.decSubf(S57subf.SOMF);
                        break;
                    case "FRID":
                        inFeature = true;
                        switch (((Long) S57dat.decSubf(record, fields + pos, S57field.FRID, S57subf.PRIM)).intValue()) {
                        case 1:
                            pflag = S57map.Pflag.POINT;
                            break;
                        case 2:
                            pflag = S57map.Pflag.LINE;
                            break;
                        case 3:
                            pflag = S57map.Pflag.AREA;
                            break;
                        default:
                            pflag = S57map.Pflag.NOSP;
                        }
                        objl = (Long) S57dat.decSubf(S57subf.OBJL);
                        break;
                    case "FOID":
                        name = (long) S57dat.decSubf(record, fields + pos, S57field.LNAM, S57subf.LNAM);
                        map.newFeature(name, pflag, objl);
                        break;
                    case "ATTF":
                        S57dat.setField(record, fields + pos, S57field.ATTF, len);
                        do {
                            long attl = (Long) S57dat.decSubf(S57subf.ATTL);
                            String atvl = ((String) S57dat.decSubf(S57subf.ATVL)).trim();
                            if (!atvl.isEmpty()) {
                                map.newAtt(attl, atvl);
                            }
                        } while (S57dat.more());
                        break;
                    case "FFPT":
                        S57dat.setField(record, fields + pos, S57field.FFPT, len);
                        do {
                            name = (long) S57dat.decSubf(S57subf.LNAM);
                            int rind = ((Long) S57dat.decSubf(S57subf.RIND)).intValue();
                            S57dat.decSubf(S57subf.COMT);
                            map.refObj(name, rind);
                        } while (S57dat.more());
                        break;
                    case "FSPT":
                        S57dat.setField(record, fields + pos, S57field.FSPT, len);
                        do {
                            name = (long) S57dat.decSubf(S57subf.NAME) << 16;
                            map.newPrim(name, (Long) S57dat.decSubf(S57subf.ORNT), (Long) S57dat.decSubf(S57subf.USAG));
                            S57dat.decSubf(S57subf.MASK);
                        } while (S57dat.more());
                        break;
                    case "VRID":
                        inFeature = false;
                        name = (Long) S57dat.decSubf(record, fields + pos, S57field.VRID, S57subf.RCNM);
                        switch ((int) name) {
                        case 110:
                            nflag = Nflag.ISOL;
                            break;
                        case 120:
                            nflag = Nflag.CONN;
                            break;
                        default:
                            nflag = Nflag.ANON;
                            break;
                        }
                        name <<= 32;
                        name += (long) S57dat.decSubf(S57subf.RCID);
                        name <<= 16;
                        if (nflag == Nflag.ANON) {
                            map.newEdge(name);
                        }
                        break;
                    case "VRPT":
                        S57dat.setField(record, fields + pos, S57field.VRPT, len);
                        do {
                            long conn = (Long) S57dat.decSubf(S57subf.NAME) << 16;
                            int topi = ((Long) S57dat.decSubf(S57subf.TOPI)).intValue();
                            map.addConn(conn, topi);
                            S57dat.decSubf(S57subf.MASK);
                        } while (S57dat.more());
                        break;
                    case "SG2D":
                        S57dat.setField(record, fields + pos, S57field.SG2D, len);
                        do {
                            double lat = (double) ((Long) S57dat.decSubf(S57subf.YCOO)) / comf;
                            double lon = (double) ((Long) S57dat.decSubf(S57subf.XCOO)) / comf;
                            if (nflag == Nflag.ANON) {
                                map.newNode(++name, lat, lon, nflag);
                            } else {
                                map.newNode(name, lat, lon, nflag);
                            }
                            lat = Math.toRadians(lat);
                            lon = Math.toRadians(lon);
                            if (lat < map.bounds.minlat)
                                map.bounds.minlat = lat;
                            if (lat > map.bounds.maxlat)
                                map.bounds.maxlat = lat;
                            if (lon < map.bounds.minlon)
                                map.bounds.minlon = lon;
                            if (lon > map.bounds.maxlon)
                                map.bounds.maxlon = lon;
                        } while (S57dat.more());
                        break;
                    case "SG3D":
                        S57dat.setField(record, fields + pos, S57field.SG3D, len);
                        do {
                            double lat = (double) ((Long) S57dat.decSubf(S57subf.YCOO)) / comf;
                            double lon = (double) ((Long) S57dat.decSubf(S57subf.XCOO)) / comf;
                            double depth = (double) ((Long) S57dat.decSubf(S57subf.VE3D)) / somf;
                            map.newNode(name++, lat, lon, depth);
                            lat = Math.toRadians(lat);
                            lon = Math.toRadians(lon);
                            if (lat < map.bounds.minlat)
                                map.bounds.minlat = lat;
                            if (lat > map.bounds.maxlat)
                                map.bounds.maxlat = lat;
                            if (lon < map.bounds.minlon)
                                map.bounds.minlon = lon;
                            if (lon > map.bounds.maxlon)
                                map.bounds.maxlon = lon;
                        } while (S57dat.more());
                        break;
                    default:
                        break;
                    }
                }
                if (inFeature) {
                    map.endFeature();
                    inFeature = false;
                }
            }
        }
        map.endFile();
        in.close();

        return;
    }

}
