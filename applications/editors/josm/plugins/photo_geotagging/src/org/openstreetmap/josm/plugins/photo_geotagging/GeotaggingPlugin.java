//License: GPL (v2 or above)
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import java.text.DecimalFormat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This plugin is used to write latitude and longitude information
 * to the EXIF header of jpg files.
 * It extends the core geoimage feature of JOSM by adding a new entry
 * to the right click menu of any image layer.
 * 
 * The real work (writing lat/lon values to file) is done by the pure Java
 * sanselan library.
 */
public class GeotaggingPlugin extends Plugin {
    final static boolean debug = false;
    final static String KEEP_BACKUP = "plugins.photo_geotagging.keep_backup";
    
    public GeotaggingPlugin(PluginInformation info) {
        super(info);
        GeoImageLayer.registerMenuAddition(new GeotaggingMenuAddition());
    }
    
    class GeotaggingMenuAddition implements GeoImageLayer.LayerMenuAddition {
        public Component getComponent(Layer layer) {
            JMenuItem geotaggingItem = new JMenuItem(tr("Write coordinates to image header"), ImageProvider.get("geotagging"));;
            geotaggingItem.addActionListener(new GeotagImages((GeoImageLayer) layer));
            return geotaggingItem;
        }
    }
    
    class GeotagImages implements ActionListener {
        final private GeoImageLayer layer;
        public GeotagImages(GeoImageLayer layer) {
            this.layer = layer;
        }
        
        public void actionPerformed(ActionEvent arg0) {
            final List<ImageEntry> images = new ArrayList<ImageEntry>();
            for (ImageEntry e : layer.getImages()) {
                if (e.getPos() != null) {
                    images.add(e);
                }
            }

            final JPanel cont = new JPanel(new GridBagLayout());
            cont.add(new JLabel(tr("Write position information into the exif header of the following files:")), GBC.eol());
            
            FileList files = new FileList();
            files.setVisibleRowCount(Math.min(files.getModel().getSize(), 10));
            final List<String> strs = new ArrayList<String>();
            DecimalFormat cDdFormatter = new DecimalFormat("###0.000000");

            for (ImageEntry e : images) {
                strs.add(e.getFile().getAbsolutePath()+" ("+cDdFormatter.format(e.getPos().lat())+","+cDdFormatter.format(e.getPos().lon())+")");
            }
            files.getFileListModel().setFiles(strs);
            JScrollPane scroll = new JScrollPane(files);
            scroll.setPreferredSize(new Dimension(300, 250));
            cont.add(scroll, GBC.eol().fill(GBC.BOTH));
            
            final JCheckBox backups = new JCheckBox(tr("keep backup files"), Main.pref.getBoolean(KEEP_BACKUP, true));
            cont.add(backups, GBC.eol());
            
            int result = new ExtendedDialog(
                    Main.parent,
                    tr("Photo Geotagging Plugin"),
                    new String[] {tr("OK"), tr("Cancel")})
                .setButtonIcons(new String[] {"ok.png", "cancel.png"})
                .setContent(cont)
                .setCancelButton(2)
                .setDefaultButton(1)
                .showDialog()
                .getValue();

            if (result != 1) 
                return;
            
            final boolean keep_backup = backups.isSelected();
            Main.pref.put(KEEP_BACKUP, keep_backup);

            Main.worker.execute(new GeoTaggingRunnable(images, keep_backup));
        }
    }

    class GeoTaggingRunnable extends PleaseWaitRunnable {
        private boolean cancelled = false;
        final private boolean keep_backup;
        final List<ImageEntry> images;
        private Boolean override_backup = null;

        private File fileFrom;
        private File fileTo;
        private File fileDelete;
        
        public GeoTaggingRunnable(List<ImageEntry> images, boolean keep_backup) {
            super(tr("Photo Geotagging Plugin"));
            this.images = images;
            this.keep_backup = keep_backup;
        }
        @Override 
        protected void realRun() {
            progressMonitor.subTask(tr("Writing position information to image files..."));
            progressMonitor.setTicksCount(images.size());
        
            
            for (int i=0; i<images.size(); ++i) {
                if (cancelled) return;

                ImageEntry e = images.get(i);
                if (debug) {
                    System.err.print("i:"+i+" "+e.getFile().getName()+" ");
                }
                
                fileFrom = null;
                fileTo = null;
                fileDelete = null;
                    
                try {
                    chooseFiles(e.getFile());
                    if (cancelled) return;
                    ExifGPSTagger.setExifGPSTag(fileFrom, fileTo, e.getPos().lat(), e.getPos().lon());
                    cleanupFiles();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    JOptionPane.showMessageDialog(Main.parent, tr("Error: ")+ioe.getMessage(), tr("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                progressMonitor.worked(1);
                if (debug) {
                System.err.println("");
                }
            }
        }

        private void chooseFiles(File file) throws IOException {
            if (debug) {                    
            System.err.println("f: "+file.getAbsolutePath());
            }

            if (!keep_backup) {
                chooseFilesNoBackup(file);
                return;
            }

            File fileBackup = new File(file.getParentFile(),file.getName()+"_");
            if (fileBackup.exists()) {
                if (debug) {
                    System.err.println("FILE EXISTS");
                }

                confirm_override();
                if (cancelled) 
                    return;
                
                if (override_backup) {
                    if (!fileBackup.delete())
                        throw new IOException(tr("File could not be deleted!"));
                } else {
                    chooseFilesNoBackup(file);
                    return;
                }
            }
            if (!file.renameTo(fileBackup))
                throw new IOException(tr("Could not rename file!"));
                
            fileFrom = fileBackup;
            fileTo = file;
            fileDelete = null;
        }

        private void chooseFilesNoBackup(File file) throws IOException {
            File fileTmp;
            fileTmp = File.createTempFile("img", ".jpg", file.getParentFile());
            if (debug) {
                System.err.println("TMP: "+fileTmp.getAbsolutePath());
            }
            if (! file.renameTo(fileTmp))
                throw new IOException(tr("Could not rename file!"));
                
            fileFrom = fileTmp;
            fileTo = file;
            fileDelete = fileTmp;
        }        

        private void confirm_override() {
            if (override_backup == null) {
                JLabel l = new JLabel(tr("<html><h3>There are old backup files in the image directory!</h3>"));
                l.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                int override = new ExtendedDialog(
                        Main.parent,
                        tr("Override old backup files?"),
                        new String[] {tr("Cancel"), tr("Keep old backups and continue"), tr("Override")})
                    .setButtonIcons(new String[] {"cancel.png", "ok.png", "dialogs/delete.png"})
                    .setContent(l)
                    .setCancelButton(1)
                    .setDefaultButton(2)
                    .showDialog()
                    .getValue();
                if (override == 2) {
                    override_backup = false;
                } else if (override == 3) {
                    override_backup = true;
                } else {
                    cancelled = true;
                    return;
                }
            }
        }

        private void cleanupFiles() throws IOException {
            if (fileDelete != null) {
                if (!fileDelete.delete()) 
                    throw new IOException(tr("Could not delete temporary file!"));
            }
        }
        
        @Override 
        protected void finish() {
        }
        
        @Override 
        protected void cancel() {
            cancelled = true;
        }
    }
    
    static class FileList extends JList {
        public FileList() {
            super(new FileListModel());
        }

        public FileListModel getFileListModel() {
            return (FileListModel)getModel();
        }
    }

    static class FileListModel extends AbstractListModel{
        private List<String> files;

        public FileListModel() {
            files = new ArrayList<String>();
        }

        public FileListModel(List<String> files) {
            setFiles(files);
        }

        public void setFiles(List<String> files) {
            if (files == null) {
                this.files = new ArrayList<String>();
            } else {
                this.files = files;
            }
            fireContentsChanged(this,0,getSize());
        }

        public Object getElementAt(int index) {
            if (files == null) return null;
            return files.get(index);
        }

        public int getSize() {
            if (files == null) return 0;
            return files.size();
        }
    }   
}
