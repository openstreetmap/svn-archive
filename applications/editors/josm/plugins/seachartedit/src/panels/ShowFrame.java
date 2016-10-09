/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;

public class ShowFrame extends JFrame {

    S57map showMap;
    Picture picture;

    class Picture extends JPanel implements ChartContext {

        public void drawPicture(OsmPrimitive osm, S57map map) {
            long id;
            Feature feature;

            id = osm.getUniqueId();
            feature = map.index.get(id);
            showMap = new S57map(true);
            showMap.nodes = map.nodes;
            showMap.edges = map.edges;
            showMap.index = map.index;
            if (feature != null) {
                showMap.features.put(feature.type, new ArrayList<Feature>());
                showMap.features.get(feature.type).add(feature);
            }
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(new Color(0xb5d0d0));
            Rectangle rect = new Rectangle(0, 0, 300, 300);
            g2.clearRect(rect.x, rect.y, rect.width, rect.height);
            Renderer.reRender(g2, rect, 16, 32, showMap, this);
        }

        @Override
        public Point2D getPoint(Snode coord) {
            return new Point2D.Double(150, 150);
        }

        @Override
        public double mile(Feature feature) {
            return 1000;
        }

        @Override
        public boolean clip() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Color background(S57map map) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RuleSet ruleset() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public ShowFrame(String title) {
        super(title);
        picture = new Picture();
        picture.setVisible(true);
        add(picture);
        pack();
    }

    public void showFeature(OsmPrimitive osm, S57map map) {
        picture.drawPicture(osm, map);
    }
}
