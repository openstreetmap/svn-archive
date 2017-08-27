// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public final class ImageWayPointLayer extends Layer {
    private static final class ImageWayPointMouseListener extends MouseAdapter {
        private final ImageWayPointLayer layer;

        ImageWayPointMouseListener(final ImageWayPointLayer layer) {
            this.layer = layer;
        }

        @Override
        public void mouseClicked(final MouseEvent event) {
            if (MouseEvent.BUTTON1 == event.getButton() && this.layer.isVisible()) {
                final ImageEntry[] images = ImageEntries.getInstance()
                    .getImages();

                if (null != images) {
                    boolean found = false;
                    // Note: the images are checked in the *reverse* order to
                    // which they're painted - this means than an image which
                    // partly obscures another will match the click first
                    for (int index = images.length - 1; !found && index >= 0; index--) {
                        final Rectangle bounds = images[index].getBounds(MainApplication.getMap().mapView);
                        if (null != bounds && bounds.contains(event.getPoint())) {
                            found = true;
                            ImageEntries.getInstance()
                            .setCurrentImageEntry(images[index]);
                        }
                    }
                }
            }
        }
    }

    private static final class ImageChangeListener implements IImageChangeListener {
        @Override
        public void onAvailableImageEntriesChanged(final ImageEntries entries) {
            MainApplication.getMap().repaint();
        }

        @Override
        public void onSelectedImageEntryChanged(final ImageEntries entries) {
            MainApplication.getMap().repaint();
        }
    }

    private final MouseListener layerMouseListener;
    private final IImageChangeListener imageChangeListener;

    public ImageWayPointLayer() {
        super(tr("Imported Images"));

        MainApplication.getLayerManager().addLayer(this);

        this.layerMouseListener = new ImageWayPointMouseListener(this);
        MainApplication.getMap().mapView.addMouseListener(this.layerMouseListener);

        this.imageChangeListener = new ImageChangeListener();
        ImageEntries.getInstance().addListener(this.imageChangeListener);
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("dialogs/imagewaypoint");
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[0];
    }

    @Override
    public String getToolTipText() {
        // TODO
        return "";
    }

    @Override
    public boolean isMergable(final Layer other) {
        // TODO
        return false;
    }

    @Override
    public void mergeFrom(final Layer from) {
        // TODO not supported yet
    }

    @Override
    public void paint(final Graphics2D graphics, final MapView mapView, Bounds box) {
        final ImageEntry[] images = ImageEntries.getInstance().getImages();

        if (null != images) {
            final ImageEntry currentImage = ImageEntries.getInstance()
            .getCurrentImageEntry();

            for (int index = 0; index < images.length; index++) {
                final Rectangle bounds = images[index].getBounds(mapView);
                if (null != bounds) {
                    if (images[index] == currentImage) {
                    ImageEntry.SELECTED_ICON.paintIcon(mapView,
                        graphics,
                        bounds.x,
                        bounds.y);
                    } else {
                    ImageEntry.ICON.paintIcon(mapView,
                        graphics,
                        bounds.x,
                        bounds.y);
                    }
                }
            }
        }
    }

    @Override
    public void visitBoundingBox(final BoundingXYVisitor visitor) {
        final ImageEntry[] images = ImageEntries.getInstance().getImages();

        if (null != images) {
            for (int index = 0; index < images.length; index++) {
                final ImageEntry imageEntry = images[index];

                if (imageEntry.getWayPoint() != null)
                    visitor.visit(imageEntry.getWayPoint().getCoor());
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        MainApplication.getMap().mapView.removeMouseListener(this.layerMouseListener);
        ImageEntries.getInstance().removeListener(this.imageChangeListener);
    }
}
