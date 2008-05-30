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

package org.openstreetmap.josmng.gpx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openstreetmap.josmng.osm.CoordinateImpl;
import org.openstreetmap.josmng.view.*;

/**
 * A GPX layer is heavily memory optimized view of Gpx data.
 * Only the projected coordinates are kept.
 * TODO: In case of projection change, retransform the coordinates
 * to the new projection on the fly.
 * 
 * @author nenik
 */
public class GpxLayer extends Layer {
    private Projection projCache;
    private String name;
    private ViewCoords[][] coords;

    public GpxLayer(MapView parent, String name, InputStream src) throws IOException {
        super(parent);
        projCache = parent.getProjection();
        this.name = name;
        coords = parse(projCache, src);
    }
    
    @Override
    public void paint(Graphics g) {
        checkProjection();
        final int ptSize = 3;
        g.setColor(Color.GRAY);
        
        for (ViewCoords[] track : coords) {
            assert(track.length > 0);

            Point lastP = parent.getPoint(track[0]);
            drawPoint(g, lastP, ptSize);
            for (int i = 1; i<track.length; i++) {
                Point p = parent.getPoint(track[i]);
                drawPoint(g, lastP, ptSize);
                g.drawLine(lastP.x, lastP.y, p.x, p.y);
                lastP = p;
            }
        }
    }

    private void checkProjection() {
        if (projCache != parent.getProjection()) {
            // projection changed, recompute to the new one...
            Projection old = projCache;
            Projection nw = parent.getProjection();

            for (ViewCoords[] track : coords) {
                for (int i = 0; i<track.length; i++) {
                    track[i] = nw.coordToView(old.viewToCoord(track[i]));
                }
            }
            
            projCache = nw;
        }
    }

    private void drawPoint(Graphics g, Point pt, int size) {
        g.fillRect(pt.x-size/3, pt.y-size/3, size, size);
    }
    
    @Override
    public String getName() {
        return name;
    }

    private static ViewCoords[][] parse(Projection proj, InputStream src) throws IOException {
        InputSource inputSource = new InputSource(new InputStreamReader(src, "UTF-8"));
        Parser parser = new Parser(proj);
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, parser);
            return parser.getData();
        } catch (Exception e) {
            e.printStackTrace(); // broken SAXException chaining
            throw (IOException)new IOException().initCause(e);
        }
    }
    
    private static double getDouble(Attributes atts, String name) {
        return Double.parseDouble(atts.getValue(name));
    }

    
    private static class Parser extends DefaultHandler {
        private List<ViewCoords> currTrack = new ArrayList<ViewCoords>();
        private List<ViewCoords[]> tracks = new ArrayList();
        private Stack<String> state = new Stack<String>();
        private final Projection proj;

        public Parser(Projection proj) {
            this.proj = proj;
        }

        public @Override void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            state.push(qName);
            if (qName.equals("trkseg")) { // new track segment
                if (currTrack.size() > 1) {
                    tracks.add(currTrack.toArray(new ViewCoords[currTrack.size()]));
                }
                currTrack.clear();
            } else if(qName.equals("trkpt")) {
                double lon = getDouble(atts,"lon");
                double lat = getDouble(atts, "lat");
                ViewCoords vc = proj.coordToView(new CoordinateImpl(lat, lon));
                currTrack.add(vc);
            }
        }
        
        public @Override void endElement(String namespaceURI, String localName, String qName) {
            String expected = state.pop();
            if (!qName.equals(expected)) throw new IllegalStateException();
        }
        
        public ViewCoords[][] getData() {
            return tracks.toArray(new ViewCoords[tracks.size()][]);
        }
    }
}
