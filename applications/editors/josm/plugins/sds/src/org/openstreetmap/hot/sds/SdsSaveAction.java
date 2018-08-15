// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

public class SdsSaveAction extends SdsDiskAccessAction {

    public SdsSaveAction() {
        super(tr("Save..."), "sds_save", tr("Save the current separate data store information to a file."),
            null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        doSave();
    }

    public boolean doSave() {
        Layer layer = null;
        if (getLayerManager().getActiveLayer() instanceof OsmDataLayer)
            layer = getLayerManager().getActiveLayer();

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
                Utils.copyFile(file, tmpFile);
            }

            OutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, "UTF-8");

            layer.data.getReadLock().lock();
            try (SdsWriter w = new SdsWriter(new PrintWriter(writer))) {
                w.header();
                for (IPrimitive p : layer.data.allNonDeletedPrimitives()) {
                    w.write(p, p.getKeys());
                }
                w.footer();
            } finally {
                layer.data.getReadLock().unlock();
            }
            // FIXME - how to close?
            if (!Config.getPref().getBoolean("save.keepbackup", false) && (tmpFile != null)) {
                tmpFile.delete();
            }
        } catch (IOException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(
                    MainApplication.getMainFrame(),
                    tr("<html>An error occurred while saving.<br>Error is:<br>{0}</html>", e.getMessage()),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );

            try {
                // if the file save failed, then the tempfile will not
                // be deleted.  So, restore the backup if we made one.
                if (tmpFile != null && tmpFile.exists()) {
                    Utils.copyFile(tmpFile, file);
                }
            } catch (IOException e2) {
                Logging.error(e2);
                JOptionPane.showMessageDialog(
                        MainApplication.getMainFrame(),
                        tr("<html>An error occurred while restoring backup file.<br>Error is:<br>{0}</html>", e2.getMessage()),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return true;
    }
}
