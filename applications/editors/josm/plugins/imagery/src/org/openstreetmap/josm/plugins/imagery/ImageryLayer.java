package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.tms.TMSLayer;
import org.openstreetmap.josm.plugins.imagery.wms.WMSLayer;
import org.openstreetmap.josm.tools.ImageProvider;

public abstract class ImageryLayer extends Layer {
    protected static final Icon icon =
        new ImageIcon(Toolkit.getDefaultToolkit().createImage(ImageryPlugin.class.getResource("/images/imagery_small.png")));

    protected final ImageryInfo info;
    protected MapView mv;

    protected double dx = 0.0;
    protected double dy = 0.0;

    protected int sharpenLevel;

    public ImageryLayer(ImageryInfo info) {
        super(info.getName());
        this.info = info;
        this.mv = Main.map.mapView;
        this.sharpenLevel = ImageryPreferences.PROP_SHARPEN_LEVEL.get();
    }

    public double getPPD(){
        ProjectionBounds bounds = mv.getProjectionBounds();
        return mv.getWidth() / (bounds.max.east() - bounds.min.east());
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public void setOffset(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void displace(double dx, double dy) {
        setOffset(this.dx += dx, this.dy += dy);
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

    class ApplyOffsetAction extends AbstractAction {
        private OffsetBookmark b;
        ApplyOffsetAction(OffsetBookmark b) {
            super(b.name);
            this.b = b;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            setOffset(b.dx, b.dy);
            Main.map.repaint();
            if (!(ev.getSource() instanceof Component)) return;
            Component source = (Component)ev.getSource();
            if (!(source.getParent() instanceof Container)) return;
            Container m = source.getParent();
            for (Component c : m.getComponents()) {
                if (c instanceof JCheckBoxMenuItem && c != source) {
                    ((JCheckBoxMenuItem)c).setSelected(false);
                }
            }
        }
    }

    public class OffsetAction extends AbstractAction implements LayerAction {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public Component createMenuComponent() {
            JMenu menu = new JMenu(trc("layer", "Offset"));
            menu.setIcon(ImageProvider.get("mapmode", "adjustimg"));
            for (Component item : getOffsetMenu()) {
                menu.add(item);
            }
            return menu;
        }
        @Override
        public boolean supportLayers(List<Layer> layers) {
            return false;
        }
    }

    public List<Component> getOffsetMenu() {
        List<Component> result = new ArrayList<Component>();
        result.add(new JMenuItem(new ImageryAdjustAction(this)));
        if (OffsetBookmark.allBookmarks.isEmpty()) return result;

        result.add(new JSeparator(JSeparator.HORIZONTAL));
        for (OffsetBookmark b : OffsetBookmark.allBookmarks) {
            if (!b.isUsable(this)) continue;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new ApplyOffsetAction(b));
            if (b.dx == dx && b.dy == dy) {
                item.setSelected(true);
            }
            result.add(item);
        }
        return result;
    }

    public BufferedImage sharpenImage(BufferedImage img) {
        if (sharpenLevel <= 0) return img;
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        tmp.getGraphics().drawImage(img, 0, 0, null);
        Kernel kernel;
        if (sharpenLevel == 1) {
            kernel = new Kernel(2, 2, new float[] { 4, -1, -1, -1});
        } else {
            kernel = new Kernel(3, 3, new float[] { -1, -1, -1, -1, 9, -1, -1, -1, -1});
        }
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(tmp, null);
    }
}
