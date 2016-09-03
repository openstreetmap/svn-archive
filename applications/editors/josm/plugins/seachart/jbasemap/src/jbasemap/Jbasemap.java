// License: GPL. For details, see LICENSE file.
package jbasemap;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.GeomIterator;
import s57.S57map.Pflag;
import s57.S57map.Snode;
import s57.S57obj.Obj;
import s57.S57osm;
import symbols.Symbols;

/**
 * @author Malcolm Herring
 */
public final class Jbasemap {
    private Jbasemap() {
        // Hide default constructor for utilities classes
    }

    static String src;
    static String dst;
    static Context context;
    static S57map map;
    static int zoom;
    static double z2;

    static class Context implements ChartContext {

        static double top = 0;
        static double mile = 0;

        Context() {
            top = (1.0 - Math.log(Math.tan(map.bounds.maxlat) + 1.0 / Math.cos(map.bounds.maxlat)) / Math.PI) / 2.0 * 256.0 * z2;
            mile = 256 / ((Math.toDegrees(map.bounds.maxlat) - Math.toDegrees(map.bounds.minlat)) * 60);
        }

        @Override
        public Point2D getPoint(Snode coord) {
            double x = (Math.toDegrees(coord.lon) - Math.toDegrees(map.bounds.minlon)) * 256.0 * (z2 / 2) / 180.0;
            double y = ((1.0 - Math.log(Math.tan(coord.lat) + 1.0 / Math.cos(coord.lat)) / Math.PI) / 2.0 * 256.0 * z2) - top;
            return new Point2D.Double(x, y);
        }

        @Override
        public double mile(Feature feature) {
            return mile;
        }

        @Override
        public boolean clip() {
            return true;
        }

        @Override
        public Color background(S57map map) {
            if (map.features.containsKey(Obj.COALNE)) {
                for (Feature feature : map.features.get(Obj.COALNE)) {
                    if (feature.geom.prim == Pflag.POINT) {
                        break;
                    }
                    GeomIterator git = map.new GeomIterator(feature.geom);
                    git.nextComp();
                    while (git.hasEdge()) {
                        git.nextEdge();
                        while (git.hasNode()) {
                            Snode node = git.next();
                            if (node == null)
                                continue;
                            if ((node.lat >= map.bounds.minlat) && (node.lat <= map.bounds.maxlat)
                             && (node.lon >= map.bounds.minlon) && (node.lon <= map.bounds.maxlon)) {
                                return Symbols.Bwater;
                            }
                        }
                    }
                }
                return Symbols.Yland;
            } else {
                if (map.features.containsKey(Obj.ROADWY) || map.features.containsKey(Obj.RAILWY) || map.features.containsKey(Obj.LAKARE) ||
                    map.features.containsKey(Obj.RIVERS) || map.features.containsKey(Obj.CANALS)) {
                    return Symbols.Yland;
                } else {
                    return Symbols.Bwater;
                }
            }
        }

        @Override
        public RuleSet ruleset() {
            return RuleSet.BASE;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println("Usage: java -jar jbasemap.jar OSM_file SVG_file zoom xtile ytile");
            System.exit(-1);
        }
        src = args[0];
        dst = args[1];
        zoom = Integer.parseInt(args[2]);
        z2 = Math.pow(2, zoom);
        double scale = 0.1;
        try {
            BufferedReader in = new BufferedReader(new FileReader(src));
            map = new S57map(false);
            try {
                S57osm.OSMmap(in, map, true);
            } catch (Exception e) {
                System.err.println("Input data error");
                System.exit(-1);
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Input file: " + e.getMessage());
            System.exit(-1);
        }
        map.bounds.maxlat = Math.atan(Math.sinh(Math.PI * (1 - 2 * Double.parseDouble(args[4]) / z2)));
        map.bounds.minlat = Math.atan(Math.sinh(Math.PI * (1 - 2 * (Double.parseDouble(args[4]) + 1) / z2)));
        map.bounds.minlon = Math.toRadians(Double.parseDouble(args[3]) / z2 * 360.0 - 180.0);
        map.bounds.maxlon = Math.toRadians((Double.parseDouble(args[3]) + 1) / z2 * 360.0 - 180.0);
        context = new Context();
        Rectangle rect = new Rectangle(256, 256);
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.clearRect(rect.x, rect.y, rect.width, rect.height);
        svgGenerator.setSVGCanvasSize(rect.getSize());
        svgGenerator.setClip(rect.x, rect.y, rect.width, rect.height);
        Renderer.reRender(svgGenerator, rect, zoom, scale, map, context);
        svgGenerator.stream(dst);
        System.exit(0);
    }
}
