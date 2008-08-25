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
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;

public class AgpifojPlugin extends Plugin {
    
    private class Action extends JosmAction {

        public Action() {
            super(tr("Open images with AgPifoJ"),
                  "agpifoj-open",
                  tr("Load set of images as a new layer."),
                  0, 0, false);
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

        JMenuBar menu = Main.main.menu;
        JMenu view = null;
        JMenuItem agpifojMenu = new JMenuItem(new Action());

        for (int i = 0; i < menu.getMenuCount(); ++i) {
            if (menu.getMenu(i) != null
                    && tr("File").equals(menu.getMenu(i).getText())) {
                view = menu.getMenu(i);
                break;
            }
        }

        if (view != null) {
            view.insert(agpifojMenu, 2);
        
        } else if (menu.getMenuCount() > 0) {
            view = menu.getMenu(0);
            view.insert(agpifojMenu, 0);
        }

        agpifojMenu.setVisible(true);
    }

    /**
     * Called after Main.mapFrame is initalized. (After the first data is loaded).
     * You can use this callback to tweak the newFrame to your needs, as example install
     * an alternative Painter.
     */
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            if (newFrame.getLayout() instanceof BorderLayout) {
                newFrame.remove(newFrame.toolBarActions);
                newFrame.add(new ScrollViewport(newFrame.toolBarActions, ScrollViewport.VERTICAL_DIRECTION), 
                             BorderLayout.WEST);
                newFrame.repaint();
            }
            
            AgpifojDialog dialog = AgpifojDialog.getInstance();
            newFrame.addToggleDialog(dialog);
        
            boolean found = false;
            for (Layer layer : newFrame.mapView.getAllLayers()) {
                if (layer instanceof AgpifojLayer) {
                    found = true;
                    break;
                }
            }
            JToolBar tb = newFrame.toolBarActions;
            ((JToggleButton) tb.getComponent(tb.getComponentCount() - 1)).getModel().setSelected(found);
        
        } else {
            AgpifojDialog.getInstance().displayImage(null, null);
        }
    }


}
