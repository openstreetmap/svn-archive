// License: GPL. For details, see LICENSE file.
package josmtos57;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.CRC32;

import s57.S57dat;
import s57.S57dat.Fparams;
import s57.S57dat.S57field;
import s57.S57enc;
import s57.S57map;
import s57.S57osm;

/**
 * @author Malcolm Herring
 */
public final class Josmtos57 {
    private Josmtos57() {
        // Hide default constructor for utilities classes
    }

    // http://opendatacommons.org/licenses/odbl/1-0/

    /*
    URL website = new URL("http://www.website.com/information.asp");
    try (InputStream in = website.openStream()) { Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING); }
     */

    /*
     * To do:
     * 1. Geometry truncation at cell boundary.
     * 2. Geometry validation/correction to comply with S57 limitations.
     * 3. Improvements in mapping of OSM features to S57 objects.
     */

    static byte[] header = {
            // CHECKSTYLE.OFF: LineLength
            '0', '0', '2', '6', '2', '3', 'L', 'E', '1', ' ', '0', '9', '0', '0', '0', '7', '3', ' ', ' ', ' ', '6', '6', '0', '4', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '9', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0', '4', '8', '0', '0', '0', '0', '1', '9', 'C', 'A', 'T', 'D', '0', '0', '0', '1',
            '2', '2', '0', '0', '0', '0', '6', '7', 0x1e, '0', '0', '0', '0', ';', '&', ' ', ' ', ' ', 0x1f, '0', '0', '0', '1', 'C', 'A', 'T', 'D', 0x1e, '0', '1', '0', '0',
            ';', '&', ' ', ' ', ' ', 'I', 'S', 'O', '/', 'I', 'E', 'C', ' ', '8', '2', '1', '1', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'I', 'd', 'e', 'n', 't', 'i', 'f',
            'i', 'e', 'r', 0x1f, 0x1f, '(', 'I', '(', '5', ')', ')', 0x1e, '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'C', 'a', 't', 'a', 'l', 'o', 'g', 'u', 'e', ' ', 'D',
            'i', 'r', 'e', 'c', 't', 'o', 'r', 'y', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'F', 'I', 'L', 'E', '!', 'L', 'F',
            'I', 'L', '!', 'V', 'O', 'L', 'M', '!', 'I', 'M', 'P', 'L', '!', 'S', 'L', 'A', 'T', '!', 'W', 'L', 'O', 'N', '!', 'N', 'L', 'A', 'T', '!', 'E', 'L', 'O', 'N',
            '!', 'C', 'R', 'C', 'S', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'A', '(', '2', ')', ',', 'I', '(', '1', '0', ')', ',', '3', 'A', ',', 'A', '(', '3', ')', ',', '4',
            'R', ',', '2', 'A', ')', 0x1e,
            '0', '0', '1', '0', '1', ' ', 'D', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '5', '3', ' ', ' ', ' ', '5', '5', '0', '4',
            '0', '0', '0', '1', '0', '0', '0', '0', '6', '0', '0', '0', '0', '0', 'C', 'A', 'T', 'D', '0', '0', '0', '4', '2', '0', '0', '0', '0', '6', 0x1e,
            '0', '0', '0', '0', '0', 0x1e, 'C', 'D', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', 'C', 'A', 'T', 'A', 'L', 'O', 'G', '.', '0', '3', '1', 0x1f,
            0x1f, 'V', '0', '1', 'X', '0', '1', 0x1f, 'A', 'S', 'C', 0x1f, 0x1f, 0x1f, 0x1f, 0x1f, 0x1f, 0x1e
            // CHECKSTYLE.ON: LineLength
    };

    static BufferedReader in;
    static FileOutputStream out;
    static S57map map;
    static byte[] buf;
    static HashMap<String, String> meta;
    static ArrayList<Fparams> fields;
    static byte[] record;

    public static void main(String[] args) throws IOException {

        map = new S57map(false);
        int idx = 0;

        if (args.length < 4) {
            System.err.println("Usage: java -jar josmtos57.jar OSM_filename meta_data_filename S57_ENC_ROOT_directory S57_filename");
            System.exit(-1);
        }
        try {
            Scanner min = new Scanner(new FileInputStream(args[1]));
            meta = new HashMap<>();
            meta.put("FILE", args[3]);
            while (min.hasNext()) {
                String[] tokens = min.next().split("=");
                if (tokens.length >= 2)
                    meta.put(tokens[0], tokens[1].split("[ #]")[0]);
            }
            min.close();
        } catch (IOException e) {
            System.err.println("Meta data file: " + e.getMessage());
            System.exit(-1);
        }
        try {
            in = new BufferedReader(new FileReader(new File(args[0])));
            try {
                S57osm.OSMmap(in, map, false);
            } catch (Exception e) {
                System.err.println("Input data error");
                System.exit(-1);
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Input file: " + e.getMessage());
            System.exit(-1);
        }

        try {
            buf = new byte[5242880];
            idx = S57enc.encodeChart(map, meta, buf);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Output file too big (limit 5 MB) - try smaller areas");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Input data error" + e.getMessage());
            System.exit(-1);
        }

        CRC32 crc = new CRC32();
        crc.update(buf, 0, idx);
        try {
            File file = new File(args[2] + args[3]);
            if (file.exists()) file.delete();
            out = new FileOutputStream(file, false);
            out.write(buf, 0, idx);
        } catch (IOException e) {
            System.err.println("Output file: " + e.getMessage());
            System.exit(-1);
        }
        out.close();

        buf = new byte[header.length];
        System.arraycopy(header, 0, buf, 0, header.length);
        idx = header.length;
        int recs = 2;
        fields = new ArrayList<>();
        fields.add(new Fparams(S57field.CATD, new Object[]{"CD", recs, args[3], "", "V01X01", "BIN", Math.toDegrees(map.bounds.minlat),
                Math.toDegrees(map.bounds.minlon), Math.toDegrees(map.bounds.maxlat), Math.toDegrees(map.bounds.maxlon),
                String.format("%08X", crc.getValue()), ""}));
        record = S57dat.encRecord(String.valueOf(recs++), fields);
        buf = Arrays.copyOf(buf, (buf.length + record.length));
        System.arraycopy(record, 0, buf, idx, record.length);
        idx += record.length;

        try {
            File file = new File(args[2] + "CATALOG.031");
            if (file.exists()) file.delete();
            out = new FileOutputStream(file, false);
            out.write(buf, 0, idx);
        } catch (IOException e) {
            System.err.println("Catalogue file: " + e.getMessage());
            System.exit(-1);
        }
        out.close();

        //        String[] dir = (new File(args[2]).list());
        //        for (String item : dir) {
        //            System.err.println(item);
        //        }

        System.err.println("Finished");
    }

}
