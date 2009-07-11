package org.openstreetmap.josm.plugins.routes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.routes.paint.NarrowLinePainter;
import org.openstreetmap.josm.plugins.routes.paint.PathPainter;
import org.openstreetmap.josm.plugins.routes.paint.WideLinePainter;
import org.openstreetmap.josm.plugins.routes.xml.RoutesXMLLayer;
import org.openstreetmap.josm.plugins.routes.xml.RoutesXMLRoute;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;

public class RouteLayer extends Layer {

	private final PathPainter pathPainter;
	private final PathBuilder pathBuilder = new PathBuilder();
	private final List<RouteDefinition> routes = new ArrayList<RouteDefinition>();
	
	public RouteLayer(RoutesXMLLayer xmlLayer) {
		super(xmlLayer.getName());
		
		int index = 0;
		for (RoutesXMLRoute route:xmlLayer.getRoute()) {
			Color color = ColorHelper.html2color(route.getColor());
			if (color == null) {
				color = Color.RED;
				System.err.printf("Routes plugin - unable to convert color (%s)\n", route.getColor());
			}
			routes.add(new RouteDefinition(index++, color, route.getPattern()));
		}
		
		/*routes.add(new RouteDefinition(Color.RED, 
			"((type:relation | type:way) kct_red=*) | (color=red type=route route=hiking network=cz:kct)"));
		
		routes.add(new RouteDefinition(Color.YELLOW, 
			"((type:relation | type:way) kct_yellow=*) | (color=yellow type=route route=hiking network=cz:kct)"));

		routes.add(new RouteDefinition(Color.BLUE, 
			"((type:relation | type:way) kct_blue=*) | (color=blue type=route route=hiking network=cz:kct)"));

		routes.add(new RouteDefinition(Color.GREEN, 
			"((type:relation | type:way) kct_green=*) | (color=green type=route route=hiking network=cz:kct)"));
		
		routes.add(new RouteDefinition(Color.MAGENTA,
				"(type:way (ncn=* | (lcn=* | rcn=* ))) | (type:relation type=route route=bicycle)"));
				
				*/
		if ("wide".equals(Main.pref.get("routes.painter"))) {
			pathPainter = new WideLinePainter(this);
		} else {
			pathPainter = new NarrowLinePainter(this);
		}
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "osmdata_small");
	}

	@Override
	public Object getInfoComponent() {
		return null;
	}

	@Override
	public Component[] getMenuEntries() {
		return new Component[0];
	}

	@Override
	public String getToolTipText() {
		return "Hiking routes";
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {

	}

	private void addRelation(Relation relation, RouteDefinition route) {
		for (RelationMember member:relation.members) {
			if (member.member instanceof Way) {
				Way way = (Way)member.member;
				pathBuilder.addWay(way, route);
			}
		}		
	}

	@Override
	public void paint(Graphics g, MapView mv) {

		pathBuilder.clear();

		for (Relation relation:Main.ds.relations) {
			for (RouteDefinition route:routes) {
				if (route.matches(relation)) {
					addRelation(relation, route);
				}
			}			
		}

		for (Way way:Main.ds.ways) {
			for (RouteDefinition route:routes) {
				if (route.matches(way)) {
					pathBuilder.addWay(way, route);
				}
			}
		}

		for (ConvertedWay way:pathBuilder.getConvertedWays()) {
			pathPainter.drawWay(way, mv, (Graphics2D) g);
		}
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {

	}
	
	public List<RouteDefinition> getRoutes() {
		return routes;
	}

}
