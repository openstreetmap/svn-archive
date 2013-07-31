package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.PluginInformation;

public final class ImageWayPointPlugin extends org.openstreetmap.josm.plugins.Plugin {
    private static final class ImageFileFilter extends FileFilter {
        @Override
        public final boolean accept(final File file) {
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".jpg")
                || file.getName().toLowerCase().endsWith(".jpeg")
                || file.getName().toLowerCase().endsWith(".png")
                || file.getName().toLowerCase().endsWith(".gif");
        }

        @Override
        public final String getDescription() {
            return tr("Image files (*.jpg, *.jpeg, *.png, *.gif)");
        }
    }

    private final class ImageWaypointImporter extends FileImporter {

        public ImageWaypointImporter() {
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
                final List<File> allFiles = new ArrayList<File>();
                addFiles(allFiles, files.toArray(new File[0]));

                // add files to ImageEntries
                ImageEntries.getInstance()
                    .add(allFiles.toArray(new File[allFiles.size()]));

                // check to see whether there's already an ImageWayPointLayer
                boolean foundImageWayPointLayer = false;
                if (null != Main.map && null != Main.map.mapView) {
                    final Collection<Layer> layerCollection = Main.map.mapView.getAllLayers();
                    final Iterator<Layer> layerIterator = layerCollection.iterator();
                    while (layerIterator.hasNext() && !foundImageWayPointLayer) {
                        if (layerIterator.next() instanceof ImageWayPointLayer) {
                            foundImageWayPointLayer = true;
                        }
                    }
                }
                if (!foundImageWayPointLayer) {
                    GuiHelper.runInEDT(new Runnable() {
                        @Override
                        public void run() {
                            new ImageWayPointLayer();
                        }
                    });
                }
            }
        }
    }

    /**
     * no-arg constructor is required by JOSM
     */
    public ImageWayPointPlugin(PluginInformation info) {
        super(info);
	ExtensionFileFilter.importers.add(new ImageWaypointImporter());
    }

    @Override
    public final void mapFrameInitialized(final MapFrame oldFrame,
    final MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(ImageWayPointDialog.getInstance()
            .getDisplayComponent());
        } else {
            ImageEntries.getInstance().setCurrentImageEntry(null);
        }
    }

    private void addFiles(List<File> allFiles, File[] selectedFiles) {
        for (int index = 0; index < selectedFiles.length; index++) {
            final File selectedFile = selectedFiles[index];
            if (selectedFile.isDirectory())
              this.addFiles(allFiles, selectedFile.listFiles());
            else if (selectedFile.getName().toLowerCase().endsWith(".jpg"))
              allFiles.add(selectedFile);
        }
    }
}
