// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.tools.Logging;

public class GrabThread extends Thread {

    private boolean canceled;

    private CadastreGrabber grabber;

    private WMSLayer wmsLayer;

    private Lock lockImagesToGrag = new ReentrantLock();

    private ArrayList<EastNorthBound> imagesToGrab = new ArrayList<>();

    private CacheControl cacheControl = null;

    private EastNorthBound currentGrabImage;

    private Lock lockCurrentGrabImage = new ReentrantLock();

    /**
     * Call directly grabber for raster images or prepare thread for vector images
     * @param moreImages more images to grab
     */
    void addImages(ArrayList<EastNorthBound> moreImages) {
        lockImagesToGrag.lock();
        try {
            imagesToGrab.addAll(moreImages);
        } finally {
            lockImagesToGrag.unlock();
        }
        synchronized (this) {
            this.notify();
        }
        Logging.info("Added " + moreImages.size() + " to the grab thread");
        if (wmsLayer.isRaster()) {
            waitNotification();
        }
    }

    public int getImagesToGrabSize() {
        lockImagesToGrag.lock();
        try {
            return imagesToGrab.size();
        } finally {
            lockImagesToGrag.unlock();
        }
    }

    ArrayList<EastNorthBound> getImagesToGrabCopy() {
        ArrayList<EastNorthBound> copyList = new ArrayList<>();
        lockImagesToGrag.lock();
        try {
            for (EastNorthBound img : imagesToGrab) {
                EastNorthBound imgCpy = new EastNorthBound(img.min, img.max);
                copyList.add(imgCpy);
            }
        } finally {
            lockImagesToGrag.unlock();
        }
        return copyList;
    }

    void clearImagesToGrab() {
        lockImagesToGrag.lock();
        try {
            imagesToGrab.clear();
        } finally {
            lockImagesToGrag.unlock();
        }
    }

    @Override
    public void run() {
        for (;;) {
            while (getImagesToGrabSize() > 0) {
                lockImagesToGrag.lock();
                lockCurrentGrabImage.lock();
                currentGrabImage = imagesToGrab.get(0);
                lockCurrentGrabImage.unlock();
                imagesToGrab.remove(0);
                lockImagesToGrag.unlock();
                if (canceled) {
                    break;
                } else {
                    GeorefImage newImage;
                    try {
                        wmsLayer.invalidate(); // paint the current grab box
                        newImage = grabber.grab(wmsLayer, currentGrabImage.min, currentGrabImage.max);
                    } catch (IOException e) {
                        Logging.warn("Download action canceled by user or server did not respond");
                        setCanceled(true);
                        break;
                    } catch (OsmTransferException e) {
                        Logging.error("OSM transfer failed");
                        setCanceled(true);
                        break;
                    }
                    if (grabber.getWmsInterface().downloadCanceled) {
                        Logging.info("Download action canceled by user");
                        setCanceled(true);
                        break;
                    }
                    try {
                        if (CadastrePlugin.backgroundTransparent) {
                            wmsLayer.imagesLock.lock();
                            try {
                                for (GeorefImage img : wmsLayer.getImages()) {
                                    if (img.overlap(newImage))
                                        // mask overlapping zone in already grabbed image
                                        img.withdraw(newImage);
                                    else
                                        // mask overlapping zone in new image only when new image covers completely the
                                        // existing image
                                        newImage.withdraw(img);
                                }
                            } finally {
                                wmsLayer.imagesLock.unlock();
                            }
                        }
                        wmsLayer.addImage(newImage);
                        wmsLayer.invalidate();
                        saveToCache(newImage);
                    } catch (NullPointerException e) {
                        Logging.info("Layer destroyed. Cancel grab thread");
                        setCanceled(true);
                    }
                }
            }
            Logging.info("grab thread list empty");
            lockCurrentGrabImage.lock();
            currentGrabImage = null;
            lockCurrentGrabImage.unlock();
            if (canceled) {
                clearImagesToGrab();
                canceled = false;
            }
            if (wmsLayer.isRaster()) {
                notifyWaiter();
            }
            waitNotification();
        }
    }

    void saveToCache(GeorefImage image) {
        if (CacheControl.cacheEnabled && !wmsLayer.isRaster()) {
            getCacheControl().saveCache(image);
        }
    }

    public void saveNewCache() {
        if (CacheControl.cacheEnabled) {
            getCacheControl().deleteCacheFile();
            wmsLayer.imagesLock.lock();
            for (GeorefImage image : wmsLayer.getImages()) {
                getCacheControl().saveCache(image);
            }
            wmsLayer.imagesLock.unlock();
        }
    }

    /**
     * Cancel grab.
     */
    public void cancel() {
        clearImagesToGrab();
        if (cacheControl != null) {
            while (!cacheControl.isCachePipeEmpty()) {
                Logging.info("Try to close a WMSLayer which is currently saving in cache : wait 1 sec.");
                CadastrePlugin.safeSleep(1000);
            }
        }
    }

    /**
     * Returns the Cache control.
     * @return the Cache control
     */
    public CacheControl getCacheControl() {
        if (cacheControl == null)
            cacheControl = new CacheControl(wmsLayer);
        return cacheControl;
    }

    GrabThread(WMSLayer wmsLayer) {
        this.wmsLayer = wmsLayer;
    }

    void paintBoxesToGrab(Graphics g, MapView mv) {
        if (getImagesToGrabSize() > 0) {
            ArrayList<EastNorthBound> imagesToGrab = getImagesToGrabCopy();
            for (EastNorthBound img : imagesToGrab) {
                paintBox(g, mv, img, Color.red);
            }
        }
        lockCurrentGrabImage.lock();
        if (currentGrabImage != null) {
            paintBox(g, mv, currentGrabImage, Color.orange);
        }
        lockCurrentGrabImage.unlock();
    }

    private void paintBox(Graphics g, MapView mv, EastNorthBound img, Color color) {
        Point[] croppedPoint = new Point[5];
        croppedPoint[0] = mv.getPoint(img.min);
        croppedPoint[1] = mv.getPoint(new EastNorth(img.min.east(), img.max.north()));
        croppedPoint[2] = mv.getPoint(img.max);
        croppedPoint[3] = mv.getPoint(new EastNorth(img.max.east(), img.min.north()));
        croppedPoint[4] = croppedPoint[0];
        for (int i = 0; i < 4; i++) {
            g.setColor(color);
            g.drawLine(croppedPoint[i].x, croppedPoint[i].y, croppedPoint[i+1].x, croppedPoint[i+1].y);
        }
    }

    boolean isCanceled() {
        return canceled;
    }

    void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    CadastreGrabber getGrabber() {
        return grabber;
    }

    void setGrabber(CadastreGrabber grabber) {
        this.grabber = grabber;
    }

    private synchronized void notifyWaiter() {
        this.notify();
    }

    private synchronized void waitNotification() {
        try {
            wait();
        } catch (InterruptedException e) {
            Logging.error(e);
        }
    }
}
