package org.openstreetmap.josm.plugins.measurement;
/// @author Raphael Mack <ramack@raphael-mack.de>
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This is a layer that draws a grid
 */
public class MeasurementLayer extends Layer {

    public MeasurementLayer(String arg0) {
        super(arg0);
    }

    private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(MeasurementPlugin.class.getResource("/images/measurement.png")));
    private Collection<WayPoint> points = new ArrayList<WayPoint>(32);

    @Override public Icon getIcon() {
        return icon;
    }

    @Override public String getToolTipText() {
        return tr("Layer to make measurements");
    }

    @Override public boolean isMergable(Layer other) {
        //return other instanceof MeasurementLayer;
        return false;
    }

    @Override public void mergeFrom(Layer from) {
        // TODO: nyi - doubts about how this should be done are around. Ideas?

    }

    @Override public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        g.setColor(Color.green);
        Point l = null;
        for(WayPoint p:points){
            Point pnt = Main.map.mapView.getPoint(p.getCoor());
            if (l != null){
                g.drawLine(l.x, l.y, pnt.x, pnt.y);
            }
            g.drawOval(pnt.x - 2, pnt.y - 2, 4, 4);
            l = pnt;
        }
    }

    @Override public void visitBoundingBox(BoundingXYVisitor v) {
        // nothing to do here
    }

    @Override public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override public Component[] getMenuEntries() {
        return new Component[]{
            new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
            // TODO: implement new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
            new JSeparator(),
            new JMenuItem(new GPXLayerImportAction(this)),
            new JSeparator(),
            new JMenuItem(new LayerListPopup.InfoAction(this))};
    }

    public void removeLastPoint(){
        WayPoint l = null;
        for(WayPoint p:points) l = p;
        if(l != null) points.remove(l);
        recalculate();
        Main.map.repaint();
    }

    public void mouseClicked(MouseEvent e){
        if (e.getButton() != MouseEvent.BUTTON1) return;

        LatLon coor = Main.map.mapView.getLatLon(e.getX(), e.getY());
        points.add(new WayPoint(coor));

        Main.map.repaint();
        recalculate();
    }

    public void reset(){
        points.clear();
        recalculate();
        Main.map.repaint();
    }

    private void recalculate(){
        double pathLength = 0.0, segLength = 0.0; // in meters
        WayPoint last = null;

        pathLength = 0.0;
        for(WayPoint p : points){
            if(last != null){
                segLength = calcDistance(last, p);
                pathLength += segLength;
            }
            last = p;
        }
        DecimalFormat nf = new DecimalFormat("#0.00");
        DecimalFormat nf2 = new DecimalFormat("#0.0");
        MeasurementPlugin.measurementDialog.pathLengthLabel.setText(pathLength < 800?nf2.format(pathLength) + " m":nf.format(pathLength/1000) + " km");
    }

    public static double calcDistance(LatLon p1, LatLon p2){
        double lat1, lon1, lat2, lon2;
        double dlon, dlat;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = p2.lat() * Math.PI / 180.0;
        lon2 = p2.lon() * Math.PI / 180.0;

        dlon = lon2 - lon1;
        dlat = lat2 - lat1;

        double a = (Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6367000 * c;
    }

    public static double calcX(LatLon p1){
        double lat1, lon1, lat2, lon2;
        double dlon, dlat;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = lat1;
        lon2 = 0;

        dlon = lon2 - lon1;
        dlat = lat2 - lat1;

        double a = (Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6367000 * c;
    }

    public static double calcY(LatLon p1){
        double lat1, lon1, lat2, lon2;
        double dlon, dlat;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = 0;
        lon2 = lon1;

        dlon = lon2 - lon1;
        dlat = lat2 - lat1;

        double a = (Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6367000 * c;
    }

    public static double calcDistance(WayPoint p1, WayPoint p2){
        return calcDistance(p1.getCoor(), p2.getCoor());
    }

    public static double angleBetween(WayPoint p1, WayPoint p2){
        return angleBetween(p1.getCoor(), p2.getCoor());
    }

    public static double angleBetween(LatLon p1, LatLon p2){
        double lat1, lon1, lat2, lon2;
        double dlon;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = p2.lat() * Math.PI / 180.0;
        lon2 = p2.lon() * Math.PI / 180.0;

        dlon = lon2 - lon1;
        double coslat2 = Math.cos(lat2);

        return (180 * Math.atan2(coslat2 * Math.sin(dlon),
                          (Math.cos(lat1) * Math.sin(lat2)
                                    -
                           Math.sin(lat1) * coslat2 * Math.cos(dlon)))) / Math.PI;
    }

    public static double OldangleBetween(LatLon p1, LatLon p2){
        double lat1, lon1, lat2, lon2;
        double dlon, dlat;
        double heading;

        lat1 = p1.lat() * Math.PI / 180.0;
        lon1 = p1.lon() * Math.PI / 180.0;
        lat2 = p2.lat() * Math.PI / 180.0;
        lon2 = p2.lon() * Math.PI / 180.0;

        dlon = lon2 - lon1;
        dlat = lat2 - lat1;

        double a = (Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2));
        double d = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        heading = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d))
                            / (Math.sin(d) * Math.cos(lat1)));
        if (Math.sin(lon2 - lon1) < 0) {
            heading = 2 * Math.PI - heading;
        }

        return heading * 180 / Math.PI;
    }


    private class GPXLayerImportAction extends AbstractAction {

    /**
     * The data model for the list component.
     */
    private DefaultListModel model = new DefaultListModel();

    /**
     * @param layer the targeting measurement layer
     */
    public GPXLayerImportAction(MeasurementLayer layer) {
        super(tr("Import path from GPX layer"), ImageProvider.get("dialogs", "edit")); // TODO: find better image
    }

    public void actionPerformed(ActionEvent e) {
        Box panel = Box.createVerticalBox();
        final JList layerList = new JList(model);
        Collection<Layer> data = Main.map.mapView.getAllLayers();
        Layer lastLayer = null;
        int layerCnt = 0;

        for (Layer l : data){
                if(l instanceof GpxLayer){
                    model.addElement(l);
                    lastLayer = l;
                    layerCnt++;
                }
        }
        if(layerCnt == 1){
                layerList.setSelectedValue(lastLayer, true);
            }
            if(layerCnt > 0){

                layerList.setCellRenderer(new DefaultListCellRenderer(){
                        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            Layer layer = (Layer)value;
                            JLabel label = (JLabel)super.getListCellRendererComponent(list,
                                                                                      layer.getName(), index, isSelected, cellHasFocus);
                            Icon icon = layer.getIcon();
                            label.setIcon(icon);
                            label.setToolTipText(layer.getToolTipText());
                            return label;
                        }
                    });

                JCheckBox dropFirst = new JCheckBox(tr("Drop existing path"));

                panel.add(layerList);
                panel.add(dropFirst);

                final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION){
                        @Override public void selectInitialValue() {
                            layerList.requestFocusInWindow();
                        }
                    };
                final JDialog dlg = optionPane.createDialog(Main.parent, tr("Import path from GPX layer"));
                dlg.setVisible(true);

                Object answer = optionPane.getValue();
                if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE ||
                    (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION)) {
                    return;
                }

                GpxLayer gpx = (GpxLayer)layerList.getSelectedValue();
                if(dropFirst.isSelected()){
                    points = new ArrayList<WayPoint>(32);
                }

                for (GpxTrack trk : gpx.data.tracks) {
                    for (Collection<WayPoint> trkseg : trk.trackSegs) {
                        for(WayPoint p: trkseg){
                            points.add(p);
                        }
                    }
            }
                recalculate();
                Main.parent.repaint();
            }else{
                // TODO: register a listener and show menu entry only if gps layers are available
                // no gps layer
                JOptionPane.showMessageDialog(Main.parent,tr("No GPX data layer found."));
            }
        }
    }

}
