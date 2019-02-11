// License: GPL. For details, see LICENSE file.
package jrender;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;
import s57.S57osm;

/**
 * @author Malcolm Herring
 */
public final class Jrender {
    private Jrender() {
        // Hide default constructor for utilities classes
    }

    static String srcdir;
    static String dstdir;
    static int xtile;
    static int ytile;
    static int zoom;
    static ArrayList<String> send;
    static HashMap<String, Boolean> deletes;
    static Context context;
    static S57map map;
    static int empty;

    static class Context implements ChartContext {

        static double top;
        static double mile;

        Context() {
            top = (1.0 - Math.log(Math.tan(map.bounds.maxlat) + 1.0 / Math.cos(map.bounds.maxlat)) / Math.PI)
                    / 2.0 * 256.0 * 4096.0 * Math.pow(2, (zoom - 12));
            mile = (2 * ((zoom < 12) ? (256 / (int) (Math.pow(2, (11 - zoom)))) : 256) + 256)
                    / ((Math.toDegrees(map.bounds.maxlat) - Math.toDegrees(map.bounds.minlat)) * 60);
        }

        @Override
        public Point2D getPoint(Snode coord) {
            double x = (Math.toDegrees(coord.lon) - Math.toDegrees(map.bounds.minlon)) * 256.0 * 2048.0 * Math.pow(2, (zoom - 12)) / 180.0;
            double y = ((1.0 - Math.log(Math.tan(coord.lat) + 1.0 / Math.cos(coord.lat)) / Math.PI)
                    / 2.0 * 256.0 * 4096.0 * Math.pow(2, (zoom - 12))) - top;
            return new Point2D.Double(x, y);
        }

        @Override
        public double mile(Feature feature) {
            return mile;
        }

        @Override
        public boolean clip() {
            return false;
        }

        @Override
        public Color background(S57map map) {
            return new Color(0, true);
        }

        @Override
        public RuleSet ruleset() {
            return RuleSet.SEAMARK;
        }
    }

    static void tile(int z, int s, int xn, int yn) throws IOException {
        int border = (z < 12) ? (256 / (int) (Math.pow(2, (11 - zoom)))) : 256;
        int scale = (int) Math.pow(2, z - 12);
        int xdir = (scale > 0) ? (scale * xtile) + xn : xtile;
        int ynam = (scale > 0) ? (scale * ytile) + yn : ytile;
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.scale(s, s);
        g2.translate(-(border + (xn * 256 / s)), -(border + (yn * 256 / s)));
        Renderer.reRender(g2, new Rectangle(256, 256), z, 1.0 * Math.pow(2, (zoom - 12)), map, context);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        if (bos.size() > empty) {
            String dstnam = dstdir + z + "/" + xdir + "/" + ynam + ".png";
            deletes.remove(dstnam);
            send.add("put " + dstnam + " tiles/" + z + "/" + xdir + "/" + ynam + ".png");
            File ofile = new File(dstdir + "/" + z + "/" + xdir + "/");
            ofile.mkdirs();
            FileOutputStream fos = new FileOutputStream(dstdir + "/" + z + "/" + xdir + "/" + ynam + ".png");
            bos.writeTo(fos);
            fos.close();
            if (send.size() > 100) {
                PrintWriter writer = new PrintWriter(srcdir + z + "-" + xdir + "-" + ynam + ".send", "UTF-8");
                for (String str : send) {
                    writer.println(str);
                }
                writer.close();
                send = new ArrayList<>();
            }
        }
        if ((z >= 12) && (z < 18) && ((z < 16) || (bos.size() > empty))) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    tile((z + 1), (s * 2), (xn * 2 + x), (yn * 2 + y));
                }
            }
        }
    }

    static void clean(int z, int xn, int yn) throws Exception {
        int scale = (int) Math.pow(2, z - 12);
        int xdir = (scale * xtile) + xn;
        int ynam = (scale * ytile) + yn;
        String delnam = dstdir + z + "/" + xdir + "/" + ynam + ".png";
        File delfile = new File(delnam);
        if (delfile.exists()) {
            deletes.put(delnam, true);
            delfile.delete();
        }
        if ((z < 18)) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    clean((z + 1), (xn * 2 + x), (yn * 2 + y));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: java -jar jrender.jar <osm source directory> <tile directory> <zoom> <xtile> <ytile>");
            System.exit(-1);
        }
        srcdir = args[0];
        dstdir = args[1];
        zoom = Integer.parseInt(args[2]);
        xtile = Integer.parseInt(args[3]);
        ytile = Integer.parseInt(args[4]);
        send = new ArrayList<>();
        deletes = new HashMap<>();
        BufferedReader in = new BufferedReader(new FileReader(srcdir + xtile + "-" + ytile + "-" + zoom + ".osm"));
        map = new S57map(true);
        S57osm.OSMmap(in, map, false);
        in.close();
        context = new Context();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB), "png", bos);
        empty = bos.size();
        if (zoom == 12) {
            clean(12, 0, 0);
        }
        tile(zoom, 1, 0, 0);
        if ((send.size() > 0) || (deletes.size() > 0)) {
        	PrintWriter writer = new PrintWriter(srcdir + zoom + "-" + xtile + "-" + ytile + ".send", "UTF-8");
        	if (send.size() > 0) {
        		for (String str : send) {
        			writer.println(str);
            }
          }
          if (deletes.size() > 0) {
          	for (String del : deletes.keySet()) {
          		writer.println("rm " + del);
            }
          }
          writer.close();
        }
        System.exit(0);
    }
}
