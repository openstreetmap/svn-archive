package org.insignificant.josm.plugins.imagewaypoint;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.insignificant.josm.plugins.imagewaypoint.ImageEntry.IImageReadyListener;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;

public final class ImageEntries {
    private static final class ImageReadyListener implements
        IImageReadyListener {
        private final ImageEntries imageEntries;

        public ImageReadyListener(final ImageEntries imageEntries) {
            this.imageEntries = imageEntries;
        }

        public final void onImageReady(final ImageEntry imageEntry,
            final Image image) {
            this.imageEntries.setCurrentImage(imageEntry, image);
        }
    }

    private static final ImageEntries INSTANCE = new ImageEntries();

    private final List<ImageEntry> images;
    private final List<ImageEntry> locatedImages;
    private final List<IImageChangeListener> listeners;
    private final IImageReadyListener listener;

    private ImageEntry currentImageEntry;
    private Image currentImage;

    private ImageEntries() {
        this.images = new ArrayList<ImageEntry>();
        this.locatedImages = new ArrayList<ImageEntry>();
        this.listeners = new ArrayList<IImageChangeListener>();
        this.listener = new ImageReadyListener(this);

        this.currentImageEntry = null;
        this.currentImage = null;
    }

    public static final ImageEntries getInstance() {
        return ImageEntries.INSTANCE;
    }

    public final void addListener(final IImageChangeListener listener) {
        this.listeners.add(listener);
    }

    public final void removeListener(final IImageChangeListener listener) {
        this.listeners.remove(listener);
    }

    public final void add(final File[] imageFiles) {
        if (null != imageFiles) {
            for (int index = 0; index < imageFiles.length; index++) {
            this.images.add(new ImageEntry(imageFiles[index]));
            }
            this.associateAllLayers();
        }
    }

    public final void associateAllLayers() {
        for (int index = 0; index < this.images.size(); index++) {
            this.images.get(index).setWayPoint(null);
        }
        this.locatedImages.clear();

        if (null != Main.map && null != Main.map.mapView) {
            final Collection<Layer> layerCollection = Main.map.mapView.getAllLayers();
            final Layer[] layers = layerCollection.toArray(new Layer[layerCollection.size()]);

            for (int index = 0; index < layers.length; index++) {
                if (layers[index] instanceof GpxLayer
                && null != ((GpxLayer) layers[index]).data
                && !((GpxLayer) layers[index]).data.fromServer) {
                    this.doAssociateLayer((GpxLayer) layers[index]);
                }
            }

            for (IImageChangeListener listener : this.listeners) {
                listener.onSelectedImageEntryChanged(this);
            }
        }
    }

    private final void doAssociateLayer(final GpxLayer gpxLayer) {
        if (null != gpxLayer && null != gpxLayer.data
        && !gpxLayer.data.fromServer) {
            for (WayPoint wayPoint : gpxLayer.data.waypoints) {
                final List<String> texts = this.getTextContentsFromWayPoint(wayPoint);

                for (String text : texts) {
                    final ImageEntry image = this.findImageEntryWithFileName(text);
                    if (null != image) {
                    image.setWayPoint(wayPoint);
                    this.locatedImages.add(image);
                    }
                }
            }
        }
    }

    private final List<String> getTextContentsFromWayPoint(
    final WayPoint wayPoint) {
    final List<String> texts = new ArrayList<String>();
    for(String s : new String[]{"name", "cmt", "desc"})
    {
        String t = wayPoint.getString(s);
        if(null != t && 0 < t.length())
            texts.add(t);
    }

    return texts;
    }

    // private final String getFileNameFromWayPointAttribute(
    // final String attributeValue) {
    // return null != attributeValue
    // && (attributeValue.toLowerCase().endsWith(".jpg")
    // || attributeValue.toLowerCase().endsWith(".jpeg")
    // || attributeValue.toLowerCase().endsWith(".png") ||
    // attributeValue.toLowerCase()
    // .endsWith(".gif")) ? attributeValue : null;
    // }

    private final ImageEntry findImageEntryWithFileName(final String fileName) {
        ImageEntry foundimage = null;

        for (int index = 0; index < this.images.size() && null == foundimage; index++) {
            final ImageEntry image = this.images.get(index);
            if (null == image.getWayPoint()
                && image.getFileName().startsWith(fileName)) {
            foundimage = image;
            }
        }

        return foundimage;
    }

    private final void setCurrentImage(final ImageEntry imageEntry,
    final Image image) {
        if (imageEntry == this.currentImageEntry) {
            this.currentImage = image;
        }

        for (IImageChangeListener listener : this.listeners) {
            listener.onSelectedImageEntryChanged(this);
        }
    }

    public final ImageEntry[] getImages() {
        return this.locatedImages.toArray(new ImageEntry[this.locatedImages.size()]);
    }

    public final ImageEntry getCurrentImageEntry() {
        return this.currentImageEntry;
    }

    public final Image getCurrentImage() {
        return this.currentImage;
    }

    public final boolean hasNext() {
        return null != this.currentImageEntry
        && this.locatedImages.indexOf(this.currentImageEntry) < this.locatedImages.size() - 1;
    }

    public final boolean hasPrevious() {
        return null != this.currentImageEntry
        && this.locatedImages.indexOf(this.currentImageEntry) > 0;
    }

    public final void next() {
        if (null != this.currentImageEntry
        && this.locatedImages.indexOf(this.currentImageEntry) < this.locatedImages.size() - 1) {
            this.setCurrentImageEntry(this.locatedImages.get(this.locatedImages.indexOf(this.currentImageEntry) + 1));
        }
    }

    public final void previous() {
        if (null != this.currentImageEntry
        && this.locatedImages.indexOf(this.currentImageEntry) > 0) {
            this.setCurrentImageEntry(this.locatedImages.get(this.locatedImages.indexOf(this.currentImageEntry) - 1));
        }
    }

    public final void rotateCurrentImageLeft() {
        if (null != this.currentImageEntry) {
            this.currentImageEntry.setOrientation(this.currentImageEntry.getOrientation()
            .rotateLeft());
        }

        this.setCurrentImageEntry(this.currentImageEntry);
    }

    public final void rotateCurrentImageRight() {
        if (null != this.currentImageEntry) {
            this.currentImageEntry.setOrientation(this.currentImageEntry.getOrientation()
            .rotateRight());
        }

        this.setCurrentImageEntry(this.currentImageEntry);
    }

    public final void setCurrentImageEntry(final ImageEntry imageEntry) {
        if (null == imageEntry || this.locatedImages.contains(imageEntry)) {
            if (null != this.currentImageEntry)
                this.currentImageEntry.flush();

            this.currentImageEntry = imageEntry;
            this.currentImage = null;

            for (IImageChangeListener listener : this.listeners)
                listener.onSelectedImageEntryChanged(this);

            if(imageEntry != null) // now try to get the image
                this.currentImageEntry.requestImage(this.listener);
        }
    }
}
