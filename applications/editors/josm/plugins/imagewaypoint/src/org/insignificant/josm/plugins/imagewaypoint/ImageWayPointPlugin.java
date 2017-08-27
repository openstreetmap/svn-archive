// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.PluginInformation;

public final class ImageWayPointPlugin extends org.openstreetmap.josm.plugins.Plugin {
    private final class ImageWaypointImporter extends FileImporter {

        ImageWaypointImporter() {
            super(new ExtensionFileFilter("jpg,jpeg,png,gif", "jpg", "Image files [by ImageWayPoint plugin] (*.jpg, *.jpeg, *.png, *.gif)"));
        }

        @Override
        public boolean isBatchImporter() {
            return true;
        }

        @Override
        public double getPriority() {
            return -3;
        }

        @Override
        public void importData(List<File> files, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
            if (null != files && !files.isEmpty()) {

                // recursively find all files
                final List<File> allFiles = new ArrayList<>();
                addFiles(allFiles, files.toArray(new File[0]));

                // add files to ImageEntries
                ImageEntries.getInstance().add(allFiles.toArray(new File[allFiles.size()]));

                // check to see whether there's already an ImageWayPointLayer
                boolean foundImageWayPointLayer = false;
                final Collection<Layer> layerCollection = MainApplication.getLayerManager().getLayers();
                final Iterator<Layer> layerIterator = layerCollection.iterator();
                while (layerIterator.hasNext() && !foundImageWayPointLayer) {
                    if (layerIterator.next() instanceof ImageWayPointLayer) {
                        foundImageWayPointLayer = true;
                    }
                }
                if (!foundImageWayPointLayer) {
                    GuiHelper.runInEDT(ImageWayPointLayer::new);
                }
            }
        }
    }

    /**
     * no-arg constructor is required by JOSM
     */
    public ImageWayPointPlugin(PluginInformation info) {
        super(info);
        ExtensionFileFilter.addImporter(new ImageWaypointImporter());
    }

    @Override
    public void mapFrameInitialized(final MapFrame oldFrame, final MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(new ImageWayPointDialog());
        } else {
            ImageEntries.getInstance().setCurrentImageEntry(null);
        }
    }

    private void addFiles(List<File> allFiles, File[] selectedFiles) {
        for (int index = 0; index < selectedFiles.length; index++) {
            final File selectedFile = selectedFiles[index];
            if (selectedFile.isDirectory())
                this.addFiles(allFiles, selectedFile.listFiles());
            else if (selectedFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".jpg"))
                allFiles.add(selectedFile);
        }
    }
}
