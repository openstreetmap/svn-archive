package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import seamap.MapHelper;
import seamap.Renderer;
import seamap.SeaMap;
import seamap.SeaMap.Coord;
import seamap.SeaMap.Feature;

public class ShowFrame extends JFrame implements MapHelper {
	private static final long serialVersionUID = 1L;
	
	public SeaMap showMap;

	public ShowFrame(String title) {
		super(title);
	}
	
	public void showFeature(OsmPrimitive osm, SeaMap map) {
		long id;
		Feature feature;
		
		id = osm.getUniqueId();
		feature = map.index.get(id);
		showMap = new SeaMap();
		showMap.nodes = map.nodes;
		showMap.ways = map.ways;
		showMap.mpolys = map.mpolys;
		showMap.index = map.index;
		if (feature != null) {
			showMap.features.put(feature.type, new ArrayList<Feature>());
			showMap.features.get(feature.type).add(feature);
		}
		repaint();
	}
	
	@Override
	public Point2D getPoint(Coord coord) {
		return new Point2D.Double(150, 150);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setBackground(new Color(0xb5d0d0));
		g2.clearRect(0, 0, 300, 300);
		Renderer.reRender(g2, 16, 32, showMap, this);
	}

}
