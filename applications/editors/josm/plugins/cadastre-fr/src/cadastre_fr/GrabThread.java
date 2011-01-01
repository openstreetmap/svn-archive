package cadastre_fr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.OsmTransferException;

public class GrabThread extends Thread {

    private boolean cancelled;

    private CadastreGrabber grabber;

    private WMSLayer wmsLayer;

    private Lock lock = new ReentrantLock();

    private ArrayList<EastNorthBound> imagesToGrab = new ArrayList<EastNorthBound>();

    private CacheControl cacheControl = null;
    
    private EastNorthBound currentGrabImage;

    public void addImages(ArrayList<EastNorthBound> moreImages) {
        lock.lock();
        imagesToGrab.addAll(moreImages);
        lock.unlock();
        synchronized(this) {
            this.notify();
        }
        System.out.println("Added " + moreImages.size() + " to the grab thread");
    }

    public int getImagesToGrabSize() {
        lock.lock();
        int size = imagesToGrab.size();
        lock.unlock();
        return size;
    }
    
    public ArrayList<EastNorthBound> getImagesToGrabCopy() {
        ArrayList<EastNorthBound> copyList = new ArrayList<EastNorthBound>(); 
        lock.lock();
        for (EastNorthBound img : imagesToGrab) {
            EastNorthBound imgCpy = new EastNorthBound(img.min, img.max);
            copyList.add(imgCpy);
        }
        lock.unlock();
        return copyList;
    }
    
    public void clearImagesToGrab() {        
        lock.lock();
        imagesToGrab.clear();
        lock.unlock();
    }
    
    @Override
    public void run() {
        for (;;) {
            while (getImagesToGrabSize() > 0) {
                lock.lock();
                currentGrabImage = imagesToGrab.get(0);
                imagesToGrab.remove(0);
                lock.unlock();
                if (cancelled) {
                    break;
                } else {
                    GeorefImage newImage;
                    try {
                        newImage = grabber.grab(wmsLayer, currentGrabImage.min, currentGrabImage.max);
                    } catch (IOException e) {
                        System.out
                                .println("Download action cancelled by user or server did not respond");
                        setCancelled(true);
                        break;
                    } catch (OsmTransferException e) {
                        System.out.println("OSM transfer failed");
                        setCancelled(true);
                        break;
                    }
                    if (grabber.getWmsInterface().downloadCancelled) {
                        System.out.println("Download action cancelled by user");
                        setCancelled(true);
                        break;
                    }
                    if (CadastrePlugin.backgroundTransparent) {
                        wmsLayer.imagesLock.lock();
                        for (GeorefImage img : wmsLayer.getImages()) {
                            if (img.overlap(newImage))
                                // mask overlapping zone in already grabbed image
                                img.withdraw(newImage);
                            else
                                // mask overlapping zone in new image only when new image covers completely the 
                                // existing image
                                newImage.withdraw(img);
                        }
                        wmsLayer.imagesLock.unlock();
                    }
                    wmsLayer.addImage(newImage);
                    Main.map.mapView.repaint();
                    saveToCache(newImage);
                }
            }
            System.out.println("grab thread list empty");
            currentGrabImage = null;
            if (cancelled) {
                clearImagesToGrab();
                cancelled = false;
            }
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public void saveToCache(GeorefImage image) {
        if (CacheControl.cacheEnabled && !wmsLayer.isRaster()) {
            getCacheControl().saveCache(image);
        }
    }

    public void saveNewCache() {
        if (CacheControl.cacheEnabled) {
            getCacheControl().deleteCacheFile();
            wmsLayer.imagesLock.lock();
            for (GeorefImage image : wmsLayer.getImages())
                getCacheControl().saveCache(image);
            wmsLayer.imagesLock.unlock();
        }
    }

    public void cancel() {
        clearImagesToGrab();
        if (cacheControl != null) {
            while (!cacheControl.isCachePipeEmpty()) {
                System.out
                        .println("Try to close a WMSLayer which is currently saving in cache : wait 1 sec.");
                CadastrePlugin.safeSleep(1000);
            }
        }
    }

    public CacheControl getCacheControl() {
        if (cacheControl == null)
            cacheControl = new CacheControl(wmsLayer);
        return cacheControl;
    }

    public GrabThread(WMSLayer wmsLayer) {
        this.wmsLayer = wmsLayer;
    }

    public void paintBoxesToGrab(Graphics g, MapView mv) {
        ArrayList<EastNorthBound> imagesToGrab = getImagesToGrabCopy();
        for (EastNorthBound img : imagesToGrab) {
            paintBox(g, mv, img, Color.red);
        }
        if (currentGrabImage != null) {
            paintBox(g, mv, currentGrabImage, Color.orange);
        }
    }
    
    private void paintBox(Graphics g, MapView mv, EastNorthBound img, Color color) {
        Point[] croppedPoint = new Point[5];
        croppedPoint[0] = mv.getPoint(img.min);
        croppedPoint[1] = mv.getPoint(new EastNorth(img.min.east(), img.max.north()));
        croppedPoint[2] = mv.getPoint(img.max);
        croppedPoint[3] = mv.getPoint(new EastNorth(img.max.east(), img.min.north()));
        croppedPoint[4] = croppedPoint[0];
        for (int i=0; i<4; i++) {
            g.setColor(color);
            g.drawLine(croppedPoint[i].x, croppedPoint[i].y, croppedPoint[i+1].x, croppedPoint[i+1].y);
        }
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CadastreGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(CadastreGrabber grabber) {
        this.grabber = grabber;
    }

}
