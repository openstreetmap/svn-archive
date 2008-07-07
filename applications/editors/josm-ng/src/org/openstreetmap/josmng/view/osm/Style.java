/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view.osm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.view.MapView;
import org.openstreetmap.josmng.view.ViewCoords;

/**
 * A "style" info for rendering an OSMPrimitive.
 * 
 * @author nenik
 */
abstract class Style<V extends View> {    
    public abstract void collect(Drawer drawer, MapView parent, V view, boolean selected);
    
    private static Map<String,Style> rules;
    
    
    private static synchronized Map<String,Style> getRules() {
        if (rules == null) {
            try {
                rules = parseRules(Style.class.getResource("/styles/standard/elemstyles.xml"));
            } catch (IOException ex) {
                throw (InternalError)new InternalError().initCause(ex);
            }
        }
        return rules;
     }

    public static <V extends View> Style get(V v) {
        getRules();
        OsmPrimitive prim = v.getPrimitive();
        String suffix = prim instanceof Node ? "node" : "way";

        for (String k : prim.getTags()) {
            String val = prim.getTag(k);
            String key = k + "=" + val + ":" + suffix;
            
            Style s = rules.get(key);
            if (s != null) return s;
        }
        
        return prim instanceof Node ? WF_BIG : WIRE;
    }

    private static Style WF_BIG = new GenericNodeStyle(20000, false, null);
    private static Style WIRE = new GenericRoadStyle(Integer.MAX_VALUE, Color.BLUE, 1, 0, false);

    private static class LazyPoly {
        private final ViewWay vw;
        private final MapView parent;
        private Polygon poly;

        public LazyPoly(ViewWay vw, MapView parent) {
            this.vw = vw;
            this.parent = parent;
        }
        
        public Polygon getPoly() {
            if (poly == null) {
                poly = new Polygon();
                Iterator<ViewNode> it = vw.getNodes().iterator();
                Point lastP = parent.getPoint(it.next());
                poly.addPoint(lastP.x, lastP.y);
                while(it.hasNext()) {
                    Point p = parent.getPoint(it.next());
                    if (!it.hasNext() || lastP.distanceSq(p) > 25) {
                        poly.addPoint(p.x, p.y);
                    }
                }
            }
            return poly;
        }
    }
    
    private static class GenericRoadStyle extends Style<ViewWay> {
        private final int maxScale;
        private final Color color;
        private final boolean dashed;
        private final int width;
        private final int realWidth;

        public GenericRoadStyle(int scale, Color color, int w, int realWidth, boolean dashed) {
            this.maxScale = scale;  
            this.color = color;
            this.dashed = dashed;
            this.width = w;
            this.realWidth = realWidth;
        }

        public @Override void collect(Drawer drawer, MapView parent, ViewWay w, boolean selected) {
            if (parent.getScaleFactor() > maxScale && !selected) return;
            int scale = parent.getScaleFactor();
            int size = w.getSize();
            if (size < scale) return;

            int strokeWidth = parent.getWidthInPixels(realWidth, width);
            if (strokeWidth < 0 || strokeWidth > 1000) {
                System.err.println("bad Stroke (" + strokeWidth + ") for real=" + realWidth + ", min=" + width);
            }
            
            List<ViewNode> nodes = w.getNodes();
            if (nodes.size() < 2) return;

            LazyPoly poly = new LazyPoly(w, parent);

            int z = 60+10*getWayLayer(w.way);
            int j = getWayType(w.way);
            if (scale > 5000 || strokeWidth == 1) {
                drawer.put(z+j, new PolyPart(poly, selected ? Color.RED : color, getStroke(1, false)));
                if (selected) drawer.put(z+1, new PathPart(dirArrows(parent, w, 10), Color.RED, getStroke(1, false)));
            } else {
                drawer.put(z+1, new PolyPart(poly, selected ? Color.RED : Color.BLACK, getShortStroke(strokeWidth+2)));
                drawer.put(z+j, new PolyPart(poly, color, getStroke(strokeWidth, dashed)));
                if (selected) drawer.put(z+1, new PathPart(dirArrows(parent, w, 4*strokeWidth), Color.RED, getStroke(1, false)));
            }
        }
    }
    
    private static final double PHI = Math.toRadians(30);
    
    private static GeneralPath dirArrows(MapView parent, ViewWay w, int len) {
        GeneralPath gp = new GeneralPath();

        Point lastP = null;
        for (ViewNode vn : w.getNodes()) {
            Point p = parent.getPoint(vn);
            if (lastP != null) { // draw only the arrowhead
                double t = Math.atan2(p.y-lastP.y, p.x-lastP.x) + Math.PI;
                gp.moveTo((int)(p.x + len*Math.cos(t-PHI)), (int)(p.y + len*Math.sin(t-PHI)));
                gp.lineTo(p.x, p.y);
                gp.lineTo((int)(p.x + len*Math.cos(t+PHI)), (int)(p.y + len*Math.sin(t+PHI)));
            }
            lastP = p;
        }

        return gp;
    }

    private static class AreaStyle extends Style<ViewWay> {
        Color color;
        Color outline;
        Icon icon;

        public AreaStyle(int scale, Color color, Icon icon) {
            this.outline = color.darker();
            this.color = color;
            this.icon = icon;
        }

        private void paintIcon(Drawer drawer, MapView parent, ViewWay vn) {
            if (icon != null) {
                Point p = parent.getPoint(new ViewCoords((int)vn.bbox.getCenterX(), (int)vn.bbox.getCenterY()));
                drawer.put(128, new IconPart(p, icon, null));
            }
        }

        public @Override void collect(Drawer drawer, MapView parent, ViewWay w, boolean selected) {
            int scale = parent.getScaleFactor();
            int size = w.getSize();
            if (size < 2*scale) return;

            LazyPoly poly = new LazyPoly(w, parent);
            drawer.put(0, new AreaPart(poly, color, selected ? Color.RED : null));
            if (selected) drawer.put(1, new PathPart(dirArrows(parent, w, 10), Color.RED, getStroke(1, false)));
            paintIcon(drawer, parent, w);
        }
    }

    private static class GenericNodeStyle extends Style<ViewNode> {
        private final int maxScale;
        private final boolean annotate;
        private final Icon icon;

        public GenericNodeStyle(int maxScale, boolean annotate, Icon icon) {
            this.maxScale = maxScale;
            this.annotate = annotate;
            this.icon = icon;
        }
        
        private void paintName(Drawer drawer, MapView parent, ViewNode vn) {
            int scale = parent.getScaleFactor();
            // font size algorithm:
            // maxScale - 0.5max    7pt
            // 0.5-0.25             12pt
            // <0.25                24pt
            int size = (scale > maxScale/2) ? 7 : (scale > maxScale/4) ? 12 : 24;
            Point p = parent.getPoint(vn);
            String txt = vn.getPrimitive().getTag("name");
            if (txt == null) return;
            
            drawer.put(129, new TextPart(size, p, txt));
        }

        private void paintIcon(Drawer drawer, MapView parent, ViewNode vn, boolean selected) {
            Point p = parent.getPoint(vn);
            drawer.put(128, new IconPart(p, icon, selected ? Color.RED : null));
        }
        
        private void paintMark(Drawer drawer, MapView parent, ViewNode vn, boolean selected) {
            int scale = parent.getScaleFactor();
            boolean big = vn.isTagged();
            Point p = parent.getPoint(vn);
            
            if (!big && scale > 1000) return;
            Color color = selected ? Color.RED : Color.BLACK;
            if (big && scale < 2000) {
                drawer.put(127, new MarkPart(false, p, color));
            } else {
                drawer.put(127, new MarkPart(true, p, color));
            }
        }

        public @Override void collect(Drawer drawer, MapView parent, ViewNode vn, boolean selected) {
            int scale = parent.getScaleFactor();
            if (scale > maxScale) return;
            
            if (icon != null) {
                paintIcon(drawer, parent, vn, selected);
            } else {
                paintMark(drawer, parent, vn, selected);
            }
            if (annotate) paintName(drawer, parent, vn);
        }
 
    }
    
    private static Map<String, Style> parseRules(URL style) throws  IOException {
        Exception cause;
        try {
            Handler handler = new Handler();
            handler.base = style;
            InputSource src = new InputSource(style.openStream());
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            parser.parse(src, handler);
            return handler.rules;
        } catch (ParserConfigurationException ex) {
            cause = ex;
        } catch (SAXException ex) {
            cause = ex;
        }
        IOException ioe = new IOException("Can't read the source");
        ioe.initCause(cause);
        throw ioe;
    }
    
    private static class Rule {
        String condition;
        Style style;

        public Rule(String condition, Style style) {
            this.condition = condition;
            this.style = style;
        }
    }
    
    private static class Handler extends DefaultHandler {
        URL base;
        
        private Map<String,Style> rules = new  HashMap<String, Style>();
        
        private Stack<String> ctx = new Stack<String>();
        private String rule;
        private int lineWidth;
        private int realWidth;
        private boolean dashed;
        boolean area;
        private String color;
        private Icon icon;
        private boolean annotate;
        private int minScale, maxScale;
        private StringBuilder content = new StringBuilder();
        
	public Handler() {
            reset();
	}
        
        private void reset() {
            rule = null;
            lineWidth = 1;
            dashed = false;
            area = false;
            color = null;
            icon = null;
            annotate = true;
            minScale = 0;
            maxScale = Integer.MAX_VALUE;
        }


	public @Override void startElement(String uri,String name, String qName, Attributes atts) {
            if (qName.equals("condition")) {
                String key = atts.getValue("k");
                String val = atts.getValue("v");
                rule = key + "=" + val;
            } else if (qName.equals("line")) {
                assert !area;
                // <line width="3" realwidth="8" colour="#809bc0" dashed="true"/>
                lineWidth = parseInt(atts.getValue("width"));
                realWidth = parseInt(atts.getValue("realwidth"));
                color = atts.getValue("colour");
                String dash = atts.getValue("dashed");
                if (dash != null) dashed = Boolean.valueOf(dash);
            } else if (qName.equals("scale_max")) {
                content.setLength(0);
            } else if (qName.equals("scale_min")) {
                content.setLength(0);
            } else if (qName.equals("icon")) {
		//<icon annotate="true" src="transport/airport.png" />
                String ann = atts.getValue("annotate");
                if (ann != null) annotate = Boolean.valueOf(ann);

                String src = atts.getValue("src");
                try {
                    URL iconUrl = new URL(base, "icons/" + src);
                    icon = new ImageIcon(iconUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else if (qName.equals("area")) {
                // <area colour="#0000bf" />
                area = true;
                color = atts.getValue("colour");
            }
            ctx.push(qName);
	}

	public @Override void endElement(String uri,String name, String qName) {
            String top = ctx.pop();
            assert top.equals(qName);
            
            if (qName.equals("scale_max")) {
                maxScale = Integer.parseInt(content.toString()) / 10;
            } else if (qName.equals("scale_min")) {
                minScale = Integer.parseInt(content.toString()) / 10;
            } else if (qName.equals("rule")) {
                Style s;
                if (area) {
                    Color c = parseColor(color);
                    s = new AreaStyle(maxScale, c, icon);
                    rules.put(rule+":way", s);
                } else if (icon != null) {
                    s = new GenericNodeStyle(maxScale, annotate, icon);
                    rules.put(rule+":node", s);
                } else if (color != null) {
                    Color c = parseColor(color);
                    s = new GenericRoadStyle(maxScale, c, lineWidth, realWidth, dashed);
                    rules.put(rule+":way", s);
                }
                reset();
            }
	}
        public @Override void characters(char ch[], int start, int length) {
            content.append(ch, start, length);
        }
    }
    

    private static int parseInt(String str) {
        if (str == null) return 0;
        return Integer.parseInt(str);
    }
    
    private static Color parseColor(String spec) {
        if (spec.length() == 7) {
            return Color.decode(spec);
        }
        return null;
    }

    private static final class PolyPart implements Part {
        private LazyPoly poly;
        private final Color color;
        private final Stroke stroke;

        public PolyPart(LazyPoly poly, Color color, Stroke stroke) {
            this.poly = poly;
            this.color = color;
            this.stroke = stroke;
        }

        public void paint(Graphics2D g) {
            g.setColor(color);
            g.setStroke(stroke);
            Polygon p = poly.getPoly();
            g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
            poly = null;
        }
    }

    private static final class AreaPart implements Part {
        private LazyPoly poly;
        private final Color color;
        private final Color outline;

        public AreaPart(LazyPoly poly, Color color, Color outline) {
            this.poly = poly;
            this.color = color;
            this.outline = outline;
        }
        public void paint(Graphics2D g) {
            Polygon p = poly.getPoly();
            g.setColor(color);            
            g.fillPolygon(p);
            if (outline != null) {
                g.setColor(outline);
                g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
            }
            poly = null;
        }
    }
    
    private static final class PathPart implements Part {
        private final GeneralPath path;
        private final Color color;
        private final Stroke stroke;

        public PathPart(GeneralPath path, Color color, Stroke stroke) {
            this.path = path;
            this.color = color;
            this.stroke = stroke;
        }

        public void paint(Graphics2D g) {
            g.setColor(color);
            g.setStroke(stroke);
            g.draw(path);
        }

    }

    private static final class MarkPart implements Part {
        private final boolean small;
        private final Point point;
        private final Color color;

        public MarkPart(boolean small, Point point, Color color) {
            this.small = small;
            this.point = point;
            this.color = color;
        }

        public void paint(Graphics2D g) {
            g.setColor(color);
            if (small) {
                g.drawRect(point.x-1, point.y-1, 2, 2);
            } else {
                g.drawRect(point.x-2, point.y-2, 4, 4);
                g.drawRect(point.x, point.y, 0, 0);
            }
        }
    }

    private static final class TextPart implements Part {
        private final Point point;
        private final String text;
        private final int size;
        private final Color color = Color.BLACK;

        public TextPart(int size, Point point, String text) {
            this.size = size;
            this.point = point;
            this.text = text;
        }

        public void paint(Graphics2D g) {
            Font font = g.getFont().deriveFont((float)size);
            Rectangle2D r = font.getStringBounds(text, g.getFontRenderContext());
            
            point.x -= r.getWidth()/2;
            point.y += r.getMinY() + 5;

            g.setFont(font);
            
            g.setColor(Color.WHITE);
            g.drawString(text, point.x-1, point.y-1);
            g.drawString(text, point.x-1, point.y+1);
            g.drawString(text, point.x+1, point.y+1);
            g.drawString(text, point.x+1, point.y-1);
            g.setColor(color);
            g.drawString(text, point.x, point.y);
        }
    }

    private static final class IconPart implements Part {
        private final Point point;
        private final Icon icon;
        private final Color frame;

        public IconPart(Point point, Icon icon, Color frame) {
            this.point = point;
            this.icon = icon;
            this.frame = frame;
        }

        public void paint(Graphics2D g) {
            point.x -= icon.getIconWidth()/2;
            point.y -= icon.getIconHeight()/2;
            icon.paintIcon(null, g, point.x, point.y);
            if (frame != null) {
                g.setColor(frame);
                g.drawRect(point.x-2, point.y-2, icon.getIconWidth()+4, icon.getIconHeight()+4);
            }
        }
    }
    
    private static final Stroke[] SHORT = new Stroke[50];
    private static final Stroke[] FLAT = new Stroke[50];
    private static final Stroke[] DASHED = new Stroke[50];
    private static Stroke getStroke(int width, boolean dashed) {
        if (width < 50) return (dashed ? DASHED : FLAT)[width];
        if (dashed) {
            float[] dash = new float[] {2*width, 2*width};
            return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, dash, 0);                
        } else {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
    }
    private static Stroke getShortStroke(int width) {
        if (width < 50) return SHORT[width];
        return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    }

    static {
        for (int i=1; i<50; i++) {
            FLAT[i] = new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            SHORT[i] = new BasicStroke(i, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            float[] dash = new float[] {2*i, 2*i};
            DASHED[i] = new BasicStroke(i, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1, dash, 0);                
        }
        FLAT[0] = new BasicStroke(0);
        SHORT[0] = new BasicStroke(0);
        float[] dash = new float[] {1, 1};
        DASHED[0] = new BasicStroke(0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1, dash, 0);
    }
   
    private static int getWayLayer(Way way) {
        String lay = way.getTag("layer");
        if (lay != null) {
            try {
                return Math.max(-5, Math.min(5, Integer.parseInt(lay)));
            } catch (NumberFormatException nbe) { /* ignore the tag and fall through */}
        }
        if (way.getTag("bridge") != null) return 1;
        if (way.getTag("tunnel") != null) return -1;
        return 0;
    }
    private static Map<String,Integer> TYPES = new HashMap<String, Integer>();
    static {
        TYPES.put("motorway", 8);
        TYPES.put("trunk", 7);
        TYPES.put("primary", 6);
        TYPES.put("secondary", 5);
        TYPES.put("tertiary", 4);
        TYPES.put("motorway_link", 3);
        TYPES.put("primary_link", 3);
        TYPES.put("trunk_link", 3);
    }
    
    private static int getWayType(Way way) {
        String hw = way.getTag("highway");
        Integer val = TYPES.get(hw);
        if (val != null) return val;
        return 2; // other
    }
    
    /* Way ordering: 11 "native" layers (layer=-5 .. 5) times implicit priorities:
     * 1. backgrounds
     * 2. other
     * 3. *_link
     * 4. tertiary
     * 5. secondary
     * 6. primary
     * 7. trunk
     * 8. motorway
     * 9. added railway=*
     * 
     * For total ordering, the rule is:
     * 1. Selection   (130?)
     * 2. Names       (129)
     * 3. Icons (128)
     * 4. Nodes (127)
     * 5. Ways (10-120)
     * 6. Areas (0)
     * 
     * The z-function for ways is thus:
     * z = 60 + 10*layer + waytype
     */
}
