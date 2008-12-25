// License: GPL. Copyright 2007 by Christian Gallioz (aka khris78)
// Parts of code from Geotagged plugin (by Rob Neild) 
// and the core JOSM source code (by Immanuel Scholz and others)

package org.openstreetmap.josm.plugins.agpifoj;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;

public class AgpifojPlugin extends Plugin {
    
    private class Action extends JosmAction {

        public Action() {
            super(tr("Open images with AgPifoJ"),
                  "agpifoj-open",
                  tr("Load set of images as a new layer."),
                  null, false);
        }

        public void actionPerformed(ActionEvent e) {

            JFileChooser fc = new JFileChooser(Main.pref.get("tagimages.lastdirectory"));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setMultiSelectionEnabled(true);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(
                    new FileFilter() {
                        @Override public boolean accept(File f) {
                            return f.isDirectory()
                                    || f.getName().toLowerCase().endsWith(".jpg");
                        }

                        @Override public String getDescription() {
                            return tr("JPEG images (*.jpg)");
                        }
                    });
            
            fc.showOpenDialog(Main.parent);
            
            File[] sel = fc.getSelectedFiles();
            if (sel == null || sel.length == 0) {
                return;
            }
            
            List<File> files = new ArrayList<File>();
            addRecursiveFiles(files, sel);
            Main.pref.put("tagimages.lastdirectory", fc.getCurrentDirectory().getPath());
            
            AgpifojLayer.create(files);
        }

        private void addRecursiveFiles(List<File> files, File[] sel) {
            for (File f : sel) {
                if (f.isDirectory()) {
                    addRecursiveFiles(files, f.listFiles());
                } else if (f.getName().toLowerCase().endsWith(".jpg")) {
                    files.add(f);
                }
            }
        }
    }

    public AgpifojPlugin() {
        MainMenu.add(Main.main.menu.fileMenu, new Action());
    }

    /**
     * Called after Main.mapFrame is initalized. (After the first data is loaded).
     * You can use this callback to tweak the newFrame to your needs, as example install
     * an alternative Painter.
     */
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
