package org.insignificant.josm.plugins.imagewaypoint;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
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

    private static final class LoadImagesAction extends JosmAction {
        private static final long serialVersionUID = 4480306223276347301L;

        private final ImageWayPointPlugin plugin;

        public LoadImagesAction(final ImageWayPointPlugin plugin) {
            super(tr("Open images with ImageWayPoint"),
            "imagewaypoint-open",
            tr("Load set of images as a new layer."),
            null,
            false);

            this.plugin = plugin;
        }

        public final void actionPerformed(final ActionEvent actionEvent) {
            final JFileChooser fileChooser = new JFileChooser(Main.pref.get("tagimages.lastdirectory"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(new ImageFileFilter());

            fileChooser.showOpenDialog(Main.parent);

            final File[] selectedFiles = fileChooser.getSelectedFiles();
            if (null != selectedFiles && 0 != selectedFiles.length) {
                Main.pref.put("tagimages.lastdirectory",
                    fileChooser.getCurrentDirectory().getPath());

                // recursively find all files
                final List<File> allFiles = new ArrayList<File>();
                this.plugin.addFiles(allFiles, selectedFiles);

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
                    new ImageWayPointLayer();
                }
            }
        }
    }

    /**
     * no-arg constructor is required by JOSM
     */
    public ImageWayPointPlugin(PluginInformation info) {
    	super(info);
    	
        MainMenu menu = Main.main.menu;
        menu.add(menu.fileMenu, new LoadImagesAction(this));
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
