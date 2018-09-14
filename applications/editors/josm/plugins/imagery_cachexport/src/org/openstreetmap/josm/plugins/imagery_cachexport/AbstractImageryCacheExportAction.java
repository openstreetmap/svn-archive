package org.openstreetmap.josm.plugins.imagery_cachexport;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.AbstractCachedTileSourceLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Imagery tile export action.  This is a menu entry in a imagery layer
 * context menu that shows a dialog with tile export information and then
 * exports the tiles.
 */
public abstract class AbstractImageryCacheExportAction
    extends AbstractAction implements LayerAction {

    /** Define menu entry (text and image). */
    public AbstractImageryCacheExportAction() {
        super(tr("Export tiles"), ImageProvider.get("imageryexport"));
        putValue(SHORT_DESCRIPTION, tr("Export cached tiles to file system."));
    }

    /**
     * Get the layer this menu entry belongs to.
     *
     * @return Currently selected layer.
     */
    private AbstractCachedTileSourceLayer<?> getSelectedLayer() {
        return (AbstractCachedTileSourceLayer<?>)LayerListDialog.getInstance().getModel()
            .getSelectedLayers().get(0);
    }

    /**
     * Get the cache object of the imagery layer.
     *
     * @return Cache object of the imagery layer.
     */
    protected abstract CacheAccess<String, BufferedImageCacheEntry> getCache();

    /**
     * Get file name for a cache key.
     *
     * @param key Tile cache key.  That is the full cache key with the key
     * 		  prefix removed.
     *
     * @return File name for tile.
     */
    protected abstract String getFilename(String key);

    /**
     * Get the cache key prefix of the imagery layer.
     *
     * @param layer Imagery layer.
     *
     * @return Cache key prefix.
     */
    protected String getCacheKeyPrefix(final AbstractCachedTileSourceLayer<?> layer) {
        return layer.getName().replace(':', '_');
    }

    /**
     * This is called after the menu entry was selected.
     *
     * @param evt Menu item selection event.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        final AbstractCachedTileSourceLayer<?> layer = getSelectedLayer();
        final String cacheName = layer.getName();
        final CacheAccess<String, BufferedImageCacheEntry> cache = getCache();
        final String cacheKeyPrefix = getCacheKeyPrefix(layer);
        ImageryTileExportDialog dialog =
            new ImageryTileExportDialog(cache, cacheName, cacheKeyPrefix);
        if (dialog.getValue() == 1) {
            // OK button was pushed.
            final String exportPath = dialog.getExportPath();
            dialog.storePrefs();
            exportImagery(exportPath, layer, cache);
        }
    }

    /**
     * Class that does the tile export in a task.
     */
    private class ExportImageryTask extends PleaseWaitRunnable {
        private String exportPath;
        private final CacheAccess<String, BufferedImageCacheEntry> cache;
        private String cacheName;
        private String cacheKeyPrefix;
        private final Set<String> keySet;
        private int numberOfObjects;
        private boolean cancel = false;

        public ExportImageryTask(String exportPath,
                                 final CacheAccess<String, BufferedImageCacheEntry> cache,
                                 String cacheName,
                                 String cacheKeyPrefix,
                                 final Set<String> keySet,
                                 int numberOfObjects) {
            super(tr("Exporting cached tiles"));
            this.exportPath = exportPath;
            this.cache = cache;
            this.cacheName = cacheName;
            this.cacheKeyPrefix = cacheKeyPrefix;
            this.keySet = keySet;
            this.numberOfObjects = numberOfObjects;
        }

        @Override
        protected void cancel() {
            cancel = true;
        }

        @Override
        protected void finish() {
            // Do nothing.
        }

        @Override
        protected void realRun() {
            progressMonitor.setTicksCount(numberOfObjects);

            int objectNum = 0;
            for (String key: keySet) {
                String[] keyParts = key.split(":", 2);
                if (keyParts.length == 2) {
                    if (cacheKeyPrefix.equals(keyParts[0])) {
                        final String filename = getFilename(keyParts[1]);
                        if (filename != null) {
                            File file = new File(exportPath, filename);
                            BufferedImageCacheEntry entry = cache.get(key);
                            try {
                                BufferedImage image = entry.getImage();
                                if (image != null) {
                                    writeImage(image, file);
                                    objectNum++;
                                }
                            } catch (IOException exn) {
                                final String ioMessage = exn.getLocalizedMessage();
                                final String message = (ioMessage != null ?
                                                        // {0} is the file name, {1} is the error message.
                                                        tr("Failed to write image file {0}: {1}", file.getAbsolutePath(), ioMessage) :
                                                        // {0} is the file name.
                                                        tr("Failed to write image file {0}.", file.getAbsolutePath()));
                                GuiHelper.runInEDT(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), message,
                                                                      tr("Error"), JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
                progressMonitor.worked(1);
                if (cancel) {
                    break;
                }
            }
            if (objectNum > 0) {
                // {0} is a number, {1} is the layer name.
                final String message = trn("Exported {0} tile from layer {1}.",
                                           "Exported {0} tiles from layer {1}.",
                                          objectNum, objectNum, cacheName);
                GuiHelper.runInEDT(new Runnable() {
                    @Override
                    public void run() {
                        new Notification(message).show();
                    }
                });
            }
        }
    }

    /**
     * Export tiles.
     *
     * @param exportPath Export directory name.
     * @param layer 	 Imagery layer whose tiles are to be exported.
     * @param cache	 Cache object.
     */
    private void exportImagery(final String exportPath,
                               final AbstractCachedTileSourceLayer<?> layer,
                               final CacheAccess<String, BufferedImageCacheEntry> cache) {
        try {
            Files.createDirectories(Paths.get(exportPath));
        } catch (FileAlreadyExistsException exn) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                          tr("Export file system path already exists but is not a directory."),
                                          tr("Error"),
                                          JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException exn) {
            final String message = exn.getLocalizedMessage();
            if (message != null) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                              tr("Failed to create export directory: {0}", message),
                                              tr("Error"),
                                              JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                              tr("Failed to create export directory."),
                                              tr("Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        final String cacheName = layer.getName();
        final String cacheKeyPrefix = getCacheKeyPrefix(layer);
        final Set<String> keySet = cache.getCacheControl().getKeySet();

        int objects = 0;
        for (String key: keySet) {
            String[] keyParts = key.split(":", 2);
            if (keyParts.length == 2) {
                if (cacheKeyPrefix.equals(keyParts[0])) {
                    objects++;
                }
            }
        }
        if (objects < 1) {
            return;
        }

        final ExportImageryTask task =
            new ExportImageryTask(exportPath, cache, cacheName,
                                  cacheKeyPrefix, keySet, objects);
        if (task != null) {
            MainApplication.worker.submit(task);
        }
    }
 
    /**
     * Write an image into a file.
     *
     * @param image Image to be written.
     * @param file  File the image is to be written to.
     * @throws IOException in case of I/O error
     */
    public void writeImage(BufferedImage image, File file) throws IOException {
        // File must exist for ImageIO.write();
        file.createNewFile();
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException exn) {
            if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                // https://stackoverflow.com/questions/3432388/imageio-not-able-to-write-a-jpeg-file
                int width = image.getWidth();
                int height = image.getHeight();
                BufferedImage imgBGR = new BufferedImage(width, height,
                                                         BufferedImage.TYPE_3BYTE_BGR);
                int[] pixels = new int[width * height];
                image.getRGB(0, 0, width, height, pixels, 0, width);
                imgBGR.setRGB(0, 0, width, height, pixels, 0, width);
                ImageIO.write(imgBGR, "jpg", file);
            } else {
                throw exn;
            }
        }
    }

    /**
     * Create actual menu entry.
     *
     * @return The menu component.
     */
    @Override
    public Component createMenuComponent() {
        JMenuItem toggleItem = new JMenuItem(this);
        return toggleItem;
    }

    /**
     * Check if the current layer is supported.
     *
     * @param layers List of layers that is to be checked.
     *
     * @return {@code true} if this action supports the given list of layers,
     * 	       {@code false} otherwise.
     */
    @Override
    public boolean supportLayers(List<Layer> layers) {
        return layers.size() == 1 && layers.get(0) instanceof AbstractCachedTileSourceLayer;
    }
}
