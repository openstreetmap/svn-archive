//License: GPL.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class SdsSaveAction extends SdsDiskAccessAction {

    @SuppressWarnings("deprecation")
	public SdsSaveAction() {
        super(tr("Save..."), "sds_save", tr("Save the current separate data store information to a file."),
            null);
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        doSave();
    }

    public boolean doSave() {
        Layer layer = null;
        if (Main.isDisplayingMapView() && (Main.map.mapView.getActiveLayer() instanceof OsmDataLayer))
            layer = Main.map.mapView.getActiveLayer();
        
        if (layer == null)
            return false;
        return doSave(layer);
    }

     public boolean doSave(Layer iLayer) {
        if (iLayer == null)
            return false;
        if (!(iLayer instanceof OsmDataLayer))
            return false;
        OsmDataLayer layer = (OsmDataLayer) iLayer;

        File file = createAndOpenSaveFileChooser(tr("Save SDS file"));

        if (file == null)
            return false;

        File tmpFile = null;

        try {

            if (file.exists()) {
                tmpFile = new File(file.getPath() + "~");
                copy(file, tmpFile);
            }

            OutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, "UTF-8");

            SdsWriter w = new SdsWriter(new PrintWriter(writer));
            layer.data.getReadLock().lock();
            try {
                w.header();
                for (IPrimitive p : layer.data.allNonDeletedPrimitives()) {
                    w.write(p, p.getKeys());
                }
                w.footer();
                w.close();
            } finally {
                layer.data.getReadLock().unlock();
            }
            // FIXME - how to close?
            if (!Main.pref.getBoolean("save.keepbackup", false) && (tmpFile != null)) {
                tmpFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("<html>An error occurred while saving.<br>Error is:<br>{0}</html>", e.getMessage()),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );

            try {
                // if the file save failed, then the tempfile will not
                // be deleted.  So, restore the backup if we made one.
                if (tmpFile != null && tmpFile.exists()) {
                    copy(tmpFile, file);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                JOptionPane.showMessageDialog(
                        Main.parent,
                        tr("<html>An error occurred while restoring backup file.<br>Error is:<br>{0}</html>", e2.getMessage()),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return true;
    }

    private void copy(File src, File dst) throws IOException {
        FileInputStream srcStream;
        FileOutputStream dstStream;
        try {
            srcStream = new FileInputStream(src);
            dstStream = new FileOutputStream(dst);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(Main.parent, tr("Could not back up file. Exception is: {0}", e
                    .getMessage()), tr("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        byte buf[] = new byte[1 << 16];
        int len;
        while ((len = srcStream.read(buf)) != -1) {
            dstStream.write(buf, 0, len);
        }
        srcStream.close();
        dstStream.close();
    }
}
