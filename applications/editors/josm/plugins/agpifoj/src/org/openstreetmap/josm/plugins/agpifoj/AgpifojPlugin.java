// License: GPL. Copyright 2007 by Christian Gallioz (aka khris78)
// Parts of code from Geotagged plugin (by Rob Neild)
// and the core JOSM source code (by Immanuel Scholz and others)

package org.openstreetmap.josm.plugins.agpifoj;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;

public class AgpifojPlugin extends Plugin {

    static class JpegFileFilter extends javax.swing.filechooser.FileFilter
                                        implements java.io.FileFilter {

        @Override public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg");
            }
        }

        @Override public String getDescription() {
            return tr("JPEG images (*.jpg)");
        }
    };

    static final JpegFileFilter JPEG_FILE_FILTER = new JpegFileFilter();

    private class Action extends JosmAction {

        public Action() {
            super(tr("Open images with AgPifoJ..."),
                  "agpifoj-open",
                  tr("Load set of images as a new layer."),
                  null, false);
        }

        public void actionPerformed(ActionEvent e) {

            JFileChooser fc = new JFileChooser(Main.pref.get("tagimages.lastdirectory"));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setMultiSelectionEnabled(true);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(JPEG_FILE_FILTER);

            int result = fc.showOpenDialog(Main.parent);

            File[] sel = fc.getSelectedFiles();
            if (sel == null || sel.length == 0 || result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            Main.pref.put("tagimages.lastdirectory", fc.getCurrentDirectory().getPath());

            AgpifojLayer.create(sel);
        }
    }

    public AgpifojPlugin() {
        MainMenu.add(Main.main.menu.fileMenu, new Action());
    }

    /**
     * Called after Main.mapFrame is initialized. (After the first data is loaded).
     * You can use this callback to tweak the newFrame to your needs, as example install
     * an alternative Painter.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            AgpifojDialog dialog = AgpifojDialog.getInstance();
            IconToggleButton b = newFrame.addToggleDialog(dialog);

            boolean found = false;
            for (Layer layer : newFrame.mapView.getAllLayers()) {
                if (layer instanceof AgpifojLayer) {
                    found = true;
                    break;
                }
            }
            b.setSelected(found);
        } else {
            AgpifojDialog.getInstance().displayImage(null, null);
        }
    }
}
