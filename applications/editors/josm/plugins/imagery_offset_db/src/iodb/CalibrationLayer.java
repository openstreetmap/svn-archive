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
 * @author Zverik
 * @license WTFPL
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

    /**
     * Draw the calibration geometry with thin bright lines (or a crosshair
     * in case of a point).
     */
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

    /**
     * This is for determining a bounding box for the layer.
     */
    @Override
    public void visitBoundingBox( BoundingXYVisitor v ) {
        for( LatLon ll : obj.getGeometry() )
            v.visit(ll);
    }

    /**
     * A simple tooltip with geometry type, status and author.
     */
    @Override
    public String getToolTipText() {
        if(obj.isDeprecated())
            return tr("A deprecated calibration of type {0} by {1}",
                OffsetInfoAction.getGeometryType(obj), obj.getAuthor());
        else
            return tr("A calibration of type {0} by {1}",
                OffsetInfoAction.getGeometryType(obj), obj.getAuthor());
    }

    @Override
    public Object getInfoComponent() {
        return OffsetInfoAction.getInformationObject(obj);
    }

    /**
     * This method returns standard actions plus "zoom to layer" and "change color".
     */
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

    /**
     * This method pans to the geometry, preserving zoom. It is used
     * from {@link GetImageryOffsetAction}, because {@link AutoScaleAction}
     * doesn't have a relevant method.
     */
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

    /**
     * An action to change a color of a geometry. The color
     * is specified in the constuctor. See {@link #getMenuEntries()} for
     * the list of enabled colors.
     */
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

    /**
     * A simple icon with a colored rectangle.
     */
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

    /**
     * An action that calls {@link AutoScaleAction} which in turn
     * uses {@link #visitBoundingBox} to pan and zoom to the calibration geometry.
     */
    class ZoomToLayerAction extends AbstractAction {
        public ZoomToLayerAction() {
            super(tr("Zoom to layer"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs/autoscale/layer"));
        }

        public void actionPerformed( ActionEvent e ) {
            AutoScaleAction.autoScale("layer");
        }
    }
}
