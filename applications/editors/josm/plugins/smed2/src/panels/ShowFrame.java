package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import render.MapContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.*;

public class ShowFrame extends JFrame {
	
	S57map showMap;
	Picture picture;

	class Picture extends JPanel implements MapContext {

		public void drawPicture(OsmPrimitive osm, S57map map) {
			long id;
			Feature feature;
			
			id = osm.getUniqueId();
			feature = map.index.get(id);
			showMap = new S57map();
			showMap.nodes = map.nodes;
			showMap.edges = map.edges;
			showMap.areas = map.areas;
			showMap.index = map.index;
			if (feature != null) {
				showMap.features.put(feature.type, new ArrayList<Feature>());
				showMap.features.get(feature.type).add(feature);
			}
			repaint();
		}
		
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setBackground(new Color(0xb5d0d0));
			g2.clearRect(0, 0, 300, 300);
			Renderer.reRender(g2, 16, 32, showMap, this);
		}

		public Point2D getPoint(Snode coord) {
			return new Point2D.Double(150, 150);
		}

		public double mile(Feature feature) {
			return 1000;
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
