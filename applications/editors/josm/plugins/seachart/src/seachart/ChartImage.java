// License: GPL. For details, see LICENSE file.
package seachart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.layer.ImageryLayer;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.GeomIterator;
import s57.S57map.Pflag;
import s57.S57map.Snode;
import s57.S57obj.Obj;
import symbols.Symbols;

/**
 * @author Malcolm Herring
 */
public class ChartImage extends ImageryLayer implements ZoomChangeListener, ChartContext {

    double top;
    double bottom;
    double left;
    double right;
    double width;
    double height;
    int zoom;

    public ChartImage(ImageryInfo info) {
        super(info);
        MapView.addZoomChangeListener(this);
        zoomChanged();
    }

    @Override
    public Action[] getMenuEntries() {
        return null;
    }

    @Override
    protected List<OffsetMenuEntry> getOffsetMenuEntries() {
        return Collections.emptyList();
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    protected Action getAdjustAction() {
        return null;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {
    }

    @Override
    public void paint(Graphics2D g2, MapView mv, Bounds bb) {
        Rectangle rect = MainApplication.getMap().mapView.getBounds();
        Renderer.reRender(g2, rect, zoom, Math.pow(2, (zoom-12)), SeachartAction.map, this);
        g2.setPaint(Color.black);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        Rectangle crect = g2.getClipBounds();
        if ((crect.y + crect.height) < (rect.y + rect.height - 10)) {
            g2.drawString(("Z" + zoom), (crect.x + crect.width - 40), (crect.y + crect.height - 10));
        } else {
            g2.drawString(("Z" + zoom), (rect.x + rect.width - 40), (rect.y + rect.height - 10));
        }
    }

    @Override
    public void zoomChanged() {
        if ((MainApplication.getMap() != null) && (MainApplication.getMap().mapView != null)) {
            Bounds bounds = MainApplication.getMap().mapView.getRealBounds();
            top = bounds.getMax().lat();
            bottom = bounds.getMin().lat();
            left = bounds.getMin().lon();
            right = bounds.getMax().lon();
            width = MainApplication.getMap().mapView.getBounds().getWidth();
            height = MainApplication.getMap().mapView.getBounds().getHeight();
            zoom = ((int) Math.min(18, Math.max(9, Math.round(Math.floor(Math.log(1024 / bounds.asRect().height) / Math.log(2))))));
        }
    }

    @Override
    public Point2D.Double getPoint(Snode coord) {
        return (Double) MainApplication.getMap().mapView.getPoint2D(new LatLon(Math.toDegrees(coord.lat), Math.toDegrees(coord.lon)));
    }

    @Override
    public double mile(Feature feature) {
        return 185000 / MainApplication.getMap().mapView.getDist100Pixel();
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
