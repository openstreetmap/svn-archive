// License: GPL. For details, see LICENSE file.
package jicons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import s57.S57map.Snode;

/**
 * @author Malcolm Herring
 */
public final class Jicons {
    private Jicons() {
        // Hide default constructor for utilities classes
    }

    static int x = 0;
    static int y = 0;
    static int w = 0;
    static int h = 0;
    static double s = 0;

    public static void main(String[] args) throws IOException {
        Context context;
        S57map map = null;
        BufferedReader in;
        int line = 0;
        String format = "";
        String file = "";
        String k = "";
        String v = "";

        BufferedImage img;
        Graphics2D g2;
        boolean inIcons = false;
        boolean inIcon = false;

        if (args.length < 2) {
            System.err.println("Usage: java -jar jicons.jar icon_definition_file icons_directory");
            System.exit(-1);
        }
        in = new BufferedReader(new FileReader(args[0]));

        context = new Context();
        String ln;
        while ((ln = in.readLine()) != null) {
            line++;
            if (inIcons) {
                if (inIcon) {
                    if (ln.contains("</icon")) {
                        inIcon = false;
                        map.tagsDone(0);
                        // generate icon file
                        switch (format) {
                        case "PNG":
                            img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            g2 = img.createGraphics();
                            Renderer.reRender(g2, new Rectangle(x, y, w, h), 16, s / Renderer.symbolScale[16], map, context);
                            try {
                                ImageIO.write(img, "png", new File(args[1] + file + ".png"));
                            } catch (Exception e) {
                                System.err.println("Line " + line + ": PNG write Exception");
                            }
                            System.err.println(file + ".png");
                            break;
                        case "SVG":
                            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                            String svgNS = "http://www.w3.org/2000/svg";
                            Document document = domImpl.createDocument(svgNS, "svg", null);
                            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                            svgGenerator.setSVGCanvasSize(new Dimension(w, h));
                            Renderer.reRender(svgGenerator, new Rectangle(x, y, w, h), 16, s / Renderer.symbolScale[16], map, context);
                            boolean useCSS = true;
                            Writer out = null;
                            try {
                                out = new OutputStreamWriter(new FileOutputStream(args[1] + file + ".svg"), "UTF-8");
                            } catch (IOException e1) {
                                System.err.println("Line " + line + ": SVG file Exception");
                            }
                            try {
                                svgGenerator.stream(out, useCSS);
                            } catch (SVGGraphics2DIOException e) {
                                System.err.println("Line " + line + ": SVG write Exception");
                            }
                            System.err.println(file + ".svg");
                            break;
                        }
                    } else if (ln.contains("<tag")) {
                        k = v = "";
                        String[] token = ln.split("k=");
                        k = token[1].split("[\"\']")[1];
                        token = token[1].split("v=");
                        v = token[1].split("[\"\']")[1];
                        if (k.isEmpty()) {
                            System.err.println("Line " + line + ": No key in tag");
                            System.exit(-1);
                        }
                        if (v.isEmpty()) {
                            System.err.println("Line " + line + ": No value in tag");
                            System.exit(-1);
                        }
                        map.addTag(k, v);
                    }
                } else if (ln.contains("<icon")) {
                    inIcon = true;
                    h = w = x = y = -1;
                    s = 0;
                    file = format = "";
                    map = new S57map(true);
                    map.addNode(0, 0, 0);
                    for (String token : ln.split("[ ]+")) {
                        if (token.matches("^width=.+")) {
                            w = Integer.parseInt(token.split("[\"\']")[1]);
                        } else if (token.matches("^height=.+")) {
                            h = Integer.parseInt(token.split("[\"\']")[1]);
                        } else if (token.matches("^x=.+")) {
                            x = Integer.parseInt(token.split("[\"\']")[1]);
                        } else if (token.matches("^y=.+")) {
                            y = Integer.parseInt(token.split("[\"\']")[1]);
                        } else if (token.matches("^scale=.+")) {
                            s = Double.parseDouble(token.split("[\"\']")[1]);
                        } else if (token.matches("^file=.+")) {
                            file = (token.split("[\"\']")[1]);
                        } else if (token.matches("^format=.+")) {
                            format = (token.split("[\"\']")[1]);
                        }
                    }
                    if (file.isEmpty()) {
                        System.err.println("Line " + line + ": No filename");
                        System.exit(-1);
                    }
                    if (format.isEmpty()) {
                        System.err.println("Line " + line + ": No format");
                        System.exit(-1);
                    }
                    if ((h < 0) && (w < 0)) {
                        System.err.println("Line " + line + ": No icon size");
                        System.exit(-1);
                    }
                    if (w < 0) {
                        w = h;
                    }
                    if (h < 0) {
                        h = w;
                    }
                    if (x < 0) {
                        x = w / 2;
                    }
                    if (y < 0) {
                        y = h / 2;
                    }
                    if (s == 0) {
                        s = 1;
                    }
                } else if (ln.contains("</icons")) {
                    inIcons = false;
                    break;
                }
            } else if (ln.contains("<icons")) {
                inIcons = true;
            }
        }
        in.close();
        System.err.println("Finished");
        System.exit(0);
    }

    static class Context implements ChartContext {

        @Override
        public Point2D getPoint(Snode coord) {
            return new Point2D.Double(x, y);
        }

        @Override
        public double mile(Feature feature) {
            return Math.min(w, h);
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
            return RuleSet.ALL;
        }
    }
}
