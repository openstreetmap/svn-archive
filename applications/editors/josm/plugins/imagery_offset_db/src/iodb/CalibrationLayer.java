package iodb;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A layer that displays calibration geometry for an offset.
 *
 * @author zverik
 */
public class CalibrationLayer extends Layer {
    private Color color;
    private Icon icon;
    private CalibrationObject obj;
    private LatLon center;

    public CalibrationLayer( CalibrationObject obj ) {
        super(tr("Calibration Layer"));
        color = Color.RED;
        this.obj = obj;
    }

    @Override
    public void paint( Graphics2D g, MapView mv, Bounds box ) {
        Stroke oldStroke = g.getStroke();
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
        LatLon[] geometry = obj.getGeometry();
        if( geometry.length == 1 ) {
            // draw crosshair
            Point p = mv.getPoint(geometry[0]);
            g.drawLine(p.x, p.y, p.x, p.y);
            g.drawLine(p.x - 10, p.y, p.x - 20, p.y);
            g.drawLine(p.x + 10, p.y, p.x + 20, p.y);
            g.drawLine(p.x, p.y - 10, p.x, p.y - 20);
            g.drawLine(p.x, p.y + 10, p.x, p.y + 20);
        } else if( geometry.length > 1 ) {
            // draw a line
            GeneralPath path = new GeneralPath();
            for( int i = 0; i < geometry.length; i++ ) {
                Point p = mv.getPoint(geometry[i]);
                if( i == 0 )
                    path.moveTo(p.x, p.y);
                else
                    path.lineTo(p.x, p.y);
            }
            g.draw(path);
        }
        g.setStroke(oldStroke);
    }

    @Override
    public Icon getIcon() {
        if( icon == null )
            icon = ImageProvider.get("calibration_layer");
        return icon;
    }

    @Override
    public void mergeFrom( Layer from ) {
    }

    @Override
    public boolean isMergable( Layer other ) {
        return false;
    }

    @Override
    public void visitBoundingBox( BoundingXYVisitor v ) {
        for( LatLon ll : obj.getGeometry() )
            v.visit(ll);
    }

    @Override
    public String getToolTipText() {
        return "A " + (obj.isDeprecated() ? "deprecated " : "") + "calibration " + OffsetInfoAction.getGeometryType(obj)
                + " by " + obj.getAuthor();
    }

    @Override
    public Object getInfoComponent() {
        return OffsetInfoAction.getInformationObject(obj);
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {
            LayerListDialog.getInstance().createShowHideLayerAction(),
            LayerListDialog.getInstance().createDeleteLayerAction(),
            SeparatorLayerAction.INSTANCE,
            new ZoomToLayerAction(),
            new SelectColorAction(Color.RED),
            new SelectColorAction(Color.CYAN),
            new SelectColorAction(Color.YELLOW),
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this)
        };
    }

    public void panToCenter() {
        if( center == null ) {
            LatLon[] geometry = obj.getGeometry();
            double lat = 0.0;
            double lon = 0.0;
            for( int i = 0; i < geometry.length; i++ ) {
                lon += geometry[i].lon();
                lat += geometry[i].lat();
            }
            center = new LatLon(lat / geometry.length, lon / geometry.length);
        }
        Main.map.mapView.zoomTo(center);
    }

    class SelectColorAction extends AbstractAction {
        private Color c;

        public SelectColorAction( Color color ) {
            super(tr("Change Color"));
            putValue(SMALL_ICON, new SingleColorIcon(color));
            this.c = color;
        }

        public void actionPerformed( ActionEvent e ) {
            color = c;
            Main.map.mapView.repaint();
        }
    }

    class SingleColorIcon implements Icon {
        private Color color;

        public SingleColorIcon( Color color ) {
            this.color = color;
        }

        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.setColor(color);
            g.fillRect(x, y, 24, 24);
        }

        public int getIconWidth() {
            return 24;
        }

        public int getIconHeight() {
            return 24;
        }

    }

    class ZoomToLayerAction extends AbstractAction {
        public ZoomToLayerAction() {
            super(tr("Zoom to layer"));
        }

        public void actionPerformed( ActionEvent e ) {
            AutoScaleAction.autoScale("layer");
        }
    }
}
