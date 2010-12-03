package org.openstreetmap.josm.plugins.imagery;

import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.tms.TMSLayer;
import org.openstreetmap.josm.plugins.imagery.wms.WMSLayer;

public abstract class ImageryLayer extends Layer {
    protected static final Icon icon =
        new ImageIcon(Toolkit.getDefaultToolkit().createImage(ImageryPlugin.class.getResource("/images/imagery_small.png")));

    protected ImageryInfo info;
    protected MapView mv;

    protected double dx = 0.0;
    protected double dy = 0.0;

    public ImageryLayer(ImageryInfo info) {
        super(info.getName());
        this.info = info;
        this.mv = Main.map.mapView;
    }


    public double getPPD(){
        ProjectionBounds bounds = mv.getProjectionBounds();
        return mv.getWidth() / (bounds.max.east() - bounds.min.east());
    }

    public void displace(double dx, double dy) {
        this.dx += dx;
        this.dy += dy;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public ImageryInfo getInfo() {
        return info;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    public static ImageryLayer create(ImageryInfo info) {
        if (info.imageryType == ImageryType.WMS || info.imageryType == ImageryType.HTML) {
            return new WMSLayer(info);
        } else if (info.imageryType == ImageryType.TMS || info.imageryType == ImageryType.BING) {
            return new TMSLayer(info);
        } else throw new AssertionError();
    }
}
