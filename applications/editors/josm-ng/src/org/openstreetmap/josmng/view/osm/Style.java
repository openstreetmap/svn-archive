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
import org.openstreetmap.josmng.view.MapView;

/**
 * A "style" info for rendering an OSMPrimitive.
 * 
 * @author nenik
 */
abstract class Style<V extends View> {    
    public abstract void paint(Graphics2D g, MapView parent, V view);
    
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
        OsmPrimitive prim = v.getOsmPrimitive();
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
    private static Style WIRE = new GenericRoadStyle(Integer.MAX_VALUE, Color.BLUE, 1, false);

    private abstract static class AbstractWayStyle extends Style<ViewWay> {
        public abstract void setup(Graphics2D g, MapView parent, ViewWay w);

        public @Override void paint(Graphics2D g, MapView parent, ViewWay w) {
            int scale = parent.getScaleFactor();
            int size = w.getSize();
            if (size < scale) return;
            
            setup(g, parent, w);

            List<ViewNode> nodes = w.getNodes();
            if (nodes.size() < 2) return;
            
            if (size < scale*5) {
                Point first = parent.getPoint(nodes.get(0));
                Point last = parent.getPoint(nodes.get(nodes.size()-1));
                g.drawLine(first.x, first.y, last.x, last.y);
                return;
            }

            Polygon poly = new Polygon();
            Iterator<ViewNode> it = nodes.iterator();
            Point lastP = parent.getPoint(it.next());
            poly.addPoint(lastP.x, lastP.y);
            while(it.hasNext()) {
                Point p = parent.getPoint(it.next());
                if (!it.hasNext() || lastP.distanceSq(p) > 25) {
                    poly.addPoint(p.x, p.y);
                }
            }
            g.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
        }
    }

    private static class AreaStyle extends Style<ViewWay> {
        Color color;
        Icon icon;

        public AreaStyle(int scale, Color color, Icon icon) {
            this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
            this.icon = icon;
        }

        private void paintIcon(Graphics2D g, MapView parent, ViewWay vn) {
            if (icon != null) {
                Point p = new Point((int)vn.bbox.getCenterX(), (int)vn.bbox.getCenterY());
                p.x -= icon.getIconWidth()/2;
                p.y -= icon.getIconHeight()/2;
                
                icon.paintIcon(null, g, p.x, p.y);
            }
        }

        public @Override void paint(Graphics2D g, MapView parent, ViewWay w) {
            g.setColor(color);
            Polygon poly = new Polygon();
            for (ViewNode vn : w.getNodes()) {
                Point p = parent.getPoint(vn);
                poly.addPoint(p.x, p.y);
            }
            g.fillPolygon(poly);
            g.setColor(color.darker());
            g.drawPolygon(poly);
            paintIcon(g, parent, w);
        }
    }

    private static class GenericRoadStyle extends AbstractWayStyle {
        private final int maxScale;
        private final Color color;
        private final Stroke stroke;
        private final Stroke thin;

        public GenericRoadStyle(int scale, Color color, int w, boolean dashed) {
            this.maxScale = scale;
            this.color = color;
            if (dashed) {
                float[] dash = new float[] {2*w, 2*w};
                this.stroke = new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, dash, 0);                
            } else {
                this.stroke = new BasicStroke(w);
            }
            
            this.thin = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        }

        public @Override void paint(Graphics2D g, MapView parent, ViewWay w) {
            if (parent.getScaleFactor() < maxScale) super.paint(g, parent, w);
        }
        
        public @Override void setup(Graphics2D g, MapView parent, ViewWay w) {
            g.setColor(color);
            if (parent.getScaleFactor() < 5000) {
                g.setStroke(stroke);
            } else {
                g.setStroke(thin);
            }
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

        private Font getFont(Graphics2D g, int size) {
            // XXX cache fonts
            return g.getFont().deriveFont((float)size);
        }
        
        private void paintName(Graphics2D g, MapView parent, ViewNode vn) {
            int scale = parent.getScaleFactor();
            // font size algorithm:
            // maxScale - 0.5max    7pt
            // 0.5-0.25             12pt
            // <0.25                24pt
            int size = (scale > maxScale/2) ? 7 : (scale > maxScale/4) ? 12 : 24;
            Font font = getFont(g, size); 
            Point p = parent.getPoint(vn);
            String txt = vn.getNode().getTag("name");
            if (txt == null) return;
            
            
            Rectangle2D r = font.getStringBounds(txt, g.getFontRenderContext());
            
            p.x -= r.getWidth()/2;
            p.y += r.getMinY() + 5;

            g.setColor(Color.WHITE);
            g.setFont(font);
            g.drawString(txt, p.x, p.y);
        }

        private void paintIcon(Graphics2D g, MapView parent, ViewNode vn) {
            if (icon != null) {
                Point p = parent.getPoint(vn);
                p.x -= icon.getIconWidth()/2;
                p.y -= icon.getIconHeight()/2;
                
                icon.paintIcon(null, g, p.x, p.y);
            }
        }
        
        private void paintMark(Graphics2D g, MapView parent, ViewNode vn) {
            int scale = parent.getScaleFactor();
            boolean big = vn.isTagged();
            Point p = parent.getPoint(vn);
            
            if (!big && scale > 1000) return;
            g.setColor(Color.RED);
            if (big && scale < 2000) {
                g.drawRect(p.x-2, p.y-2, 4, 4);
                g.drawRect(p.x, p.y, 0, 0);
            } else {
                g.drawRect(p.x-1, p.y-1, 2, 2);
            }
        }

        public @Override void paint(Graphics2D g, MapView parent, ViewNode vn) {
            int scale = parent.getScaleFactor();
            if (scale > maxScale) return;
            
            if (icon != null) {
                paintIcon(g, parent, vn);
            } else {
                paintMark(g, parent, vn);
            }
            if (annotate) paintName(g, parent, vn);
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
                lineWidth = Integer.parseInt(atts.getValue("width"));
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
                    s = new GenericRoadStyle(maxScale, c, lineWidth, dashed);
                    rules.put(rule+":way", s);
                }
                reset();
            }
	}
        public @Override void characters(char ch[], int start, int length) {
            content.append(ch, start, length);
        }
    }
    

    private static Color parseColor(String spec) {
        if (spec.length() == 7) {
            return Color.decode(spec);
        }
        return null;
    }

}
