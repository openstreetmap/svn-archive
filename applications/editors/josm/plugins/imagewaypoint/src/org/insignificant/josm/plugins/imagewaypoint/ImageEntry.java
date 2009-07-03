/**
 *
 */
package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;

import javax.swing.Icon;

import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.ImageProvider;

public final class ImageEntry implements Comparable<ImageEntry> {
    public interface IImageReadyListener {
    void onImageReady(ImageEntry imageEntry, Image image);
    }

    private static final class Observer implements ImageObserver {
    private final ImageEntry imageEntry;

    public Observer(final ImageEntry imageEntry) {
        this.imageEntry = imageEntry;
    }

    /**
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int,
     *      int, int, int, int)
     * @return true if this ImageObserver still wants to be updates about
     *         image loading progress
     */
    public final boolean imageUpdate(final Image image,
        final int infoflags, final int x, final int y, final int width,
        final int height) {
        final boolean complete = ImageObserver.ALLBITS == (infoflags | ImageObserver.ALLBITS);
        if (complete) {
        this.imageEntry.imageLoaded(image);
        }

        return !complete;
    }
    }

    public static final class Orientation {
    private static final Orientation[] orientations = new Orientation[4];

    public static final Orientation NORMAL = new Orientation(tr("Normal"), 0);
    public static final Orientation ROTATE_90 = new Orientation(tr("Rotate 90"),
        1);
    public static final Orientation ROTATE_180 = new Orientation(tr("Rotate 180"),
        2);
    public static final Orientation ROTATE_270 = new Orientation(tr("Rotate 270"),
        3);

    private final String name;
    private final int index;

    private Orientation(final String name, final int index) {
        this.name = name;
        this.index = index;
        Orientation.orientations[index] = this;
    }

    public final Orientation rotateRight() {
        if (this.index < Orientation.orientations.length - 1) {
        return Orientation.orientations[this.index + 1];
        } else {
        return Orientation.orientations[0];
        }
    }

    public final Orientation rotateLeft() {
        if (this.index == 0) {
        return Orientation.orientations[Orientation.orientations.length - 1];
        } else {
        return Orientation.orientations[this.index - 1];
        }
    }

    @Override
    public String toString() {
        return "[" + this.name + "]";
    }
    }

    public static final Icon ICON = ImageProvider.get("dialogs/imagewaypoint-marker");
    public static final Icon SELECTED_ICON = ImageProvider.get("dialogs/imagewaypoint-marker-selected");
    private static final int ICON_WIDTH = ImageEntry.ICON.getIconWidth();
    private static final int ICON_HEIGHT = ImageEntry.ICON.getIconHeight();

    private final String filePath;
    private final String fileName;
    private final ImageObserver observer;
    private WayPoint wayPoint;
    private Orientation orientation;
    private IImageReadyListener listener;
    private Image normalImage;
    private Image rotatedImage;

    public ImageEntry(final File file) {
    this.filePath = file.getAbsolutePath();
    this.fileName = file.getName();
    this.observer = new Observer(this);

    this.wayPoint = null;
    this.orientation = Orientation.NORMAL;
    this.listener = null;
    this.normalImage = null;
    this.rotatedImage = null;
    }

    public final int compareTo(final ImageEntry image) {
    return this.fileName.compareTo(image.fileName);
    }

    public final String getFileName() {
    return fileName;
    }

    public final WayPoint getWayPoint() {
    return wayPoint;
    }

    public final void setWayPoint(final WayPoint wayPoint) {
    this.wayPoint = wayPoint;
    }

    public final Orientation getOrientation() {
    return orientation;
    }

    public final void setOrientation(final Orientation orientation) {
    this.orientation = orientation;
    this.normalImage = null;
    this.rotatedImage = null;
    }

    public final Rectangle getBounds(final MapView mapView) {
    final Rectangle bounds;

    if (null == this.wayPoint) {
        bounds = null;
    } else {
        final Point point = mapView.getPoint(this.getWayPoint().getCoor());
        bounds = new Rectangle(point.x - ImageEntry.ICON_WIDTH,
        point.y - ImageEntry.ICON_HEIGHT,
        ImageEntry.ICON_WIDTH,
        ImageEntry.ICON_WIDTH);
    }

    return bounds;
    }

    public final void requestImage(final IImageReadyListener imageReadyListener) {
    this.listener = imageReadyListener;

    if (null == this.rotatedImage) {
        final Image image = Toolkit.getDefaultToolkit()
        .getImage(this.filePath);
        if (Toolkit.getDefaultToolkit().prepareImage(image,
        -1,
        -1,
        this.observer)) {
        this.imageLoaded(image);
        }
    } else if (null != this.listener) {
        this.listener.onImageReady(this, this.rotatedImage);
    }
    }

    public final void flush() {
    if (null != this.normalImage) {
        this.normalImage.flush();
        this.normalImage = null;
    }

    if (null != this.rotatedImage) {
        this.rotatedImage.flush();
        this.rotatedImage = null;
    }
    }

    private final void imageLoaded(final Image image) {
    if (Orientation.NORMAL == this.getOrientation()) {
        this.rotatedImage = image;
    } else {
        final int[] buffer = new int[image.getWidth(null)
            * image.getHeight(null)];
        PixelGrabber grabber = new PixelGrabber(image,
        0,
        0,
        image.getWidth(null),
        image.getHeight(null),
        buffer,
        0,
        image.getWidth(null));
        try {
        grabber.grabPixels();

        final int newHeight;
        final int newWidth;

        if (Orientation.ROTATE_180 == this.getOrientation()) {
            newHeight = image.getHeight(null);
            newWidth = image.getWidth(null);
        } else {
            newHeight = image.getWidth(null);
            newWidth = image.getHeight(null);
        }

        final int[] destination = new int[image.getWidth(null)
            * image.getHeight(null)];
        for (int x = 0; x < image.getWidth(null); x++) {
            for (int y = 0; y < image.getHeight(null); y++) {
            final int pix = buffer[x + (y * image.getWidth(null))];
            final int newX;
            final int newY;
            if (Orientation.ROTATE_90 == this.getOrientation()) {
                newX = newWidth - y;
                newY = x;
            } else if (Orientation.ROTATE_180 == this.getOrientation()) {
                newX = newWidth - x;
                newY = newHeight - y;
            } else { // Orientation.ROTATE_270 ==
                // this.getOrientation()
                newX = y;
                newY = newHeight - x;
            }
            final int newIndex = newX + (newY * newWidth);
            if (newIndex < destination.length) {
                destination[newIndex] = pix;
            }
            }
        }

        this.rotatedImage = Toolkit.getDefaultToolkit()
            .createImage(new MemoryImageSource(newWidth,
            newHeight,
            destination,
            0,
            newWidth));
        } catch (final InterruptedException e) {
        this.rotatedImage = null;
        }
    }

    if (null != this.listener) {
        this.listener.onImageReady(this, this.rotatedImage);
    }
    }
}