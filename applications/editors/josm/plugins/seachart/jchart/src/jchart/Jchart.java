// License: GPL. For details, see LICENSE file.
package jchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
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
import symbols.Symbols;
import s57.S57osm;

/**
 * @author Malcolm Herring
 */
public final class Jchart {
    private Jchart() {
        // Hide default constructor for utilities classes
    }

    static int zoom;
    static Context context;
    static S57map map;

    static class Context implements ChartContext {

        static double mile;
        static double xscale;
        static double yscale;

        Context() {
            double x = map.bounds.maxlon - map.bounds.minlon;
            double y = (map.bounds.maxlat - map.bounds.minlat) / Math.cos((map.bounds.maxlat + map.bounds.minlat) / 2.0 );
            if (x > y) {
                xscale = (4096.0 * x / y) / x;
                yscale = 4096.0 / (map.bounds.maxlat - map.bounds.minlat);
            } else {
                xscale = 4096.0 / x;
                yscale = (4096.0 * y / x) / (map.bounds.maxlat - map.bounds.minlat);
            }
            mile = yscale / (Math.toDegrees(y) * 60.0);
        }

        @Override
        public Point2D getPoint(Snode coord) {
            double x = (coord.lon - map.bounds.minlon) * xscale;
            double y = (map.bounds.maxlat - coord.lat) * yscale;
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
        public int grid() {
            return 5;
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
                if (map.features.containsKey(Obj.ROADWY) || map.features.containsKey(Obj.RAILWY)
                        || map.features.containsKey(Obj.LAKARE) || map.features.containsKey(Obj.RIVERS) || map.features.containsKey(Obj.CANALS)) {
                    return Symbols.Yland;
                } else {
                    return Symbols.Bwater;
                }
            }
        }

        @Override
        public RuleSet ruleset() {
            return RuleSet.ALL;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: java -jar jrender.jar <osm data file> <zoom> <scale> <output image file>");
            System.exit(-1);
        }
        File in = new File(args[0]);
        zoom = Integer.parseInt(args[1]);
        map = new S57map(false);
        S57osm.OSMmap(in, map, false);
//        in.close();
        context = new Context();
        Point2D size = context.getPoint(new Snode(map.bounds.minlat, map.bounds.maxlon));
        BufferedImage img = new BufferedImage((int)size.getX(), (int)size.getY(), BufferedImage.TYPE_INT_ARGB);
        String[] ext = args[3].split("\\.");
        if (ext[1].equalsIgnoreCase("png")) {
            Graphics2D g2 = img.createGraphics();
            Renderer.reRender(g2, new Rectangle((int) size.getX(), (int) size.getY()), zoom, Double.parseDouble(args[2]), map, context);
            try {
                ImageIO.write(img, "png", new File(args[3]));
            } catch (Exception e) {
                System.err.println("PNG write Exception");
            }
        } else if (ext[1].equalsIgnoreCase("svg")) {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new Dimension((int) size.getX(), (int) size.getY()));
            Renderer.reRender(svgGenerator, new Rectangle((int) size.getX(), (int) size.getY()), 16, Double.parseDouble(args[2]), map, context);
            boolean useCSS = true;
            Writer out = null;
            try {
                out = new OutputStreamWriter(new FileOutputStream(args[3]), "UTF-8");
            } catch (IOException e1) {
                System.err.println("SVG file Exception");
            }
            try {
                svgGenerator.stream(out, useCSS);
            } catch (SVGGraphics2DIOException e) {
                System.err.println("SVG write Exception");
            }
        } else {
            System.err.println("Output file not PNG nor SVG");
        }
        System.exit(0);
    }
}
