// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.preferences.CadastrePreferenceSetting;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * This class handles the WMS layer cache mechanism. The design is oriented for a good performance (no
 * wait status on GUI, fast saving even in big file). A separate thread is created for each WMS
 * layer to not suspend the GUI until disk I/O is terminated (a file for the cache can take
 * several MB's). If the cache file already exists, new images are just appended to the file
 * (performance). Since we use the ObjectStream methods, it is required to modify the standard
 * ObjectOutputStream in order to have objects appended readable (otherwise a stream header
 * is inserted before each append and an exception is raised at objects read).
 */
public class CacheControl implements Runnable {

    public static final String C_LAMBERT_CC_9Z = "CC";

    public static final String C_UTM20N = "UTM";

    public static class ObjectOutputStreamAppend extends ObjectOutputStream {
        public ObjectOutputStreamAppend(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }

    public static boolean cacheEnabled = true;

    public static int cacheSize = 500;

    public WMSLayer wmsLayer;

    private ArrayList<GeorefImage> imagesToSave = new ArrayList<>();
    private Lock imagesLock = new ReentrantLock();

    public boolean isCachePipeEmpty() {
        imagesLock.lock();
        try {
            return imagesToSave.isEmpty();
        } finally {
            imagesLock.unlock();
        }
    }

    public CacheControl(WMSLayer wmsLayer) {
        cacheEnabled = Config.getPref().getBoolean("cadastrewms.enableCaching", true);
        this.wmsLayer = wmsLayer;
        try {
            cacheSize = Integer.parseInt(Config.getPref().get("cadastrewms.cacheSize",
                    String.valueOf(CadastrePreferenceSetting.DEFAULT_CACHE_SIZE)));
        } catch (NumberFormatException e) {
            cacheSize = CadastrePreferenceSetting.DEFAULT_CACHE_SIZE;
        }
        File path = new File(CadastrePlugin.cacheDir);
        if (!path.exists())
            path.mkdirs();
        else // check directory capacity
            checkDirSize(path);
        new Thread(this).start();
    }

    private static void checkDirSize(File path) {
        if (cacheSize != 0) {
            long size = 0;
            long oldestFileDate = Long.MAX_VALUE;
            int oldestFile = 0;
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                size += files[i].length();
                if (files[i].lastModified() < oldestFileDate) {
                    oldestFile = i;
                    oldestFileDate = files[i].lastModified();
                }
            }
            if (size > (long) cacheSize*1024*1024) {
                Logging.info("Delete oldest file  \""+ files[oldestFile].getName()
                        + "\" in cache dir to stay under the limit of " + cacheSize + " MB.");
                files[oldestFile].delete();
                checkDirSize(path);
            }
        }
    }

    public boolean loadCacheIfExist() {
        if (!CadastrePlugin.isCadastreProjection()) {
            CadastrePlugin.askToChangeProjection();
        }
        File file = new File(CadastrePlugin.cacheDir + wmsLayer.getName() + "." + WMSFileExtension());
        if (file.exists()) {
            int reply = GuiHelper.runInEDTAndWaitAndReturn(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    JOptionPane pane = new JOptionPane(
                            tr("Location \"{0}\" found in cache.\n"+
                            "Load cache first ?\n"+
                            "(No = new cache)", wmsLayer.getName()),
                            JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null);
                    // this below is a temporary workaround to fix the "always on top" issue
                    JDialog dialog = pane.createDialog(MainApplication.getMainFrame(), tr("Select Feuille"));
                    CadastrePlugin.prepareDialog(dialog);
                    dialog.setVisible(true);
                    return (Integer) pane.getValue();
                    // till here
                }
            });

            if (reply == JOptionPane.OK_OPTION && loadCache(file, wmsLayer.getLambertZone())) {
                return true;
            } else {
                delete(file);
            }
        }
        return false;
    }

    public void deleteCacheFile() {
        delete(new File(CadastrePlugin.cacheDir + wmsLayer.getName() + "." + WMSFileExtension()));
    }

    private static void delete(File file) {
        Logging.info("Delete file "+file);
        if (file.exists())
            file.delete();
        while (file.exists()) { // wait until file is really gone (otherwise appends to existing one)
            CadastrePlugin.safeSleep(500);
        }
    }

    public boolean loadCache(File file, int currentLambertZone) {
        boolean successfulRead = false;
        try (
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            wmsLayer.setAssociatedFile(file);
            successfulRead = wmsLayer.read(ois, currentLambertZone);
        } catch (IOException | ClassNotFoundException ex) {
            Logging.error(ex);
            GuiHelper.runInEDTAndWait(() -> JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Error loading file.\nProbably an old version of the cache file."),
                    tr("Error"), JOptionPane.ERROR_MESSAGE));
            return false;
        }
        if (successfulRead && wmsLayer.isRaster()) {
            // serialized raster bufferedImage hangs-up on Java6. Recreate them here
            wmsLayer.getImage(0).image = RasterImageModifier.fixRasterImage(wmsLayer.getImage(0).image);
        }
        return successfulRead;
    }

    public synchronized void saveCache(GeorefImage image) {
        imagesLock.lock();
        try {
            this.imagesToSave.add(image);
            this.notifyAll();
        } finally {
            imagesLock.unlock();
        }
    }

    /**
     * Thread saving the grabbed images in background.
     */
    @Override
    public synchronized void run() {
        for (;;) {
            imagesLock.lock();
            int size = imagesToSave.size();
            imagesLock.unlock();
            if (size > 0) {
                File file = new File(CadastrePlugin.cacheDir + wmsLayer.getName() + "." + WMSFileExtension());
                try {
                    if (file.exists()) {
                        try (ObjectOutputStreamAppend oos = new ObjectOutputStreamAppend(
                                new BufferedOutputStream(new FileOutputStream(file, true)))) {
                            for (int i = 0; i < size; i++) {
                                oos.writeObject(imagesToSave.get(i));
                            }
                        }
                    } else {
                        try (ObjectOutputStream oos = new ObjectOutputStream(
                                new BufferedOutputStream(new FileOutputStream(file)))) {
                            wmsLayer.setAssociatedFile(file);
                            wmsLayer.write(oos);
                            for (int i = 0; i < size; i++) {
                                oos.writeObject(imagesToSave.get(i));
                            }
                        }
                    }
                } catch (IOException e) {
                    Logging.error(e);
                }
                imagesLock.lock();
                try {
                    for (int i = 0; i < size; i++) {
                        imagesToSave.remove(0);
                    }
                } finally {
                    imagesLock.unlock();
                }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                Logging.error(e);
            }
        }
    }

    private String WMSFileExtension() {
        String ext = String.valueOf(wmsLayer.getLambertZone() + 1);
        if (CadastrePlugin.isLambert_cc9())
            ext = C_LAMBERT_CC_9Z + ext;
        else if (CadastrePlugin.isUtm_france_dom())
            ext = C_UTM20N + ext;
        return ext;
    }
}
