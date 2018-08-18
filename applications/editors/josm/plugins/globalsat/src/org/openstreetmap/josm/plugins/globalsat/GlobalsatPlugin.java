// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.globalsat;
/// @author Raphael Mack <ramack@raphael-mack.de>
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

import gnu.io.CommPortIdentifier;

public class GlobalsatPlugin extends Plugin {
    private static GlobalsatDg100 device = null;
    public static GlobalsatDg100 dg100() {
        return device;
    }

    public static void setPortIdent(CommPortIdentifier port) {
        if (device != null) {
            device.disconnect();
        }
        device = new GlobalsatDg100(port);
    }

    private static class ImportTask extends PleaseWaitRunnable {
        public GpxData data;
        public Exception eee;
        private boolean deleteAfter;

        ImportTask(boolean delete) {
            super(tr("Importing data from device."));
            deleteAfter = delete;
        }

        @Override public void realRun() throws IOException, SAXException {
            progressMonitor.subTask(tr("Importing data from DG100..."));
            try {
                data = GlobalsatPlugin.dg100().readData(progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, true));
            } catch (Exception e) {
                eee = e;
            }
        }

        @Override protected void finish() {
            if (deleteAfter && GlobalsatPlugin.dg100().isCanceled() == false) {
                Config.getPref().putBoolean("globalsat.deleteAfterDownload", true);
                try {
                    GlobalsatPlugin.dg100().deleteData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Error deleting data.") + " " + ex.toString());
                }
            } else {
                Config.getPref().putBoolean("globalsat.deleteAfterDownload", false);
            }
            if (data != null && data.hasTrackPoints()) {
                MainApplication.getLayerManager().addLayer(new GpxLayer(data, tr("imported data from {0}", "DG 100")));
                MainApplication.getMap().repaint();
            } else {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("No data found on device."));
            }
            if (eee != null) {
                eee.printStackTrace();
                System.out.println(eee.getMessage());
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Connection failed.") + " (" + eee.toString() + ")");
            }
            GlobalsatPlugin.dg100().disconnect();
        }

        @Override protected void cancel() {
            GlobalsatPlugin.dg100().cancel();
            GlobalsatPlugin.dg100().disconnect();
        }
    }

    GlobalsatImportAction importAction;
    public GlobalsatPlugin(PluginInformation info) {
        super(info);
        boolean error = false;
        try {
            CommPortIdentifier.getPortIdentifiers();
        } catch (UnsatisfiedLinkError e) {
            error = true;
            // CHECKSTYLE.OFF: LineLength
            String msg = tr("Cannot load library rxtxSerial. If you need support to install it try Globalsat homepage at http://www.raphael-mack.de/josm-globalsat-gpx-import-plugin/");
            // CHECKSTYLE.ON: LineLength
            Logging.error(msg);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), "<html>" + msg + "</html>");
            }
        }
        if (!error) {
            importAction = new GlobalsatImportAction();
            MainApplication.getMenu().toolsMenu.add(importAction);
        }
    }

    static class GlobalsatImportAction extends JosmAction {
        GlobalsatImportAction() {
            super(tr("Globalsat Import"), "globalsatImport",
            tr("Import Data from Globalsat Datalogger DG100 into GPX layer."),
            Shortcut.registerShortcut("menu:globalsatimport", tr("Menu: {0}", tr("Globalsat Import")),
            KeyEvent.VK_G, Shortcut.ALT_CTRL), false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GlobalsatImportDialog dialog = new GlobalsatImportDialog();
            JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dlg = pane.createDialog(MainApplication.getMainFrame(), tr("Import"));
            dlg.setVisible(true);
            if (((Integer) pane.getValue()) == JOptionPane.OK_OPTION) {
                setPortIdent(dialog.getPort());
                ImportTask task = new ImportTask(dialog.deleteFilesAfterDownload());
                MainApplication.worker.execute(task);
            }
            dlg.dispose();
        }
    }
}
