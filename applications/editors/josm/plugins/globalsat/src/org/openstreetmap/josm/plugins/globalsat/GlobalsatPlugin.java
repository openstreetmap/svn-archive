package org.openstreetmap.josm.plugins.globalsat;
/// @author Raphael Mack <ramack@raphael-mack.de>
import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import org.xml.sax.SAXException;

import java.util.Enumeration;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;

import gnu.io.*;

public class GlobalsatPlugin extends Plugin {

    private static class ImportTask extends PleaseWaitRunnable {
        public GpxData data;
        private GlobalsatDg100 dg100;
        public Exception eee;
        private boolean deleteAfter;

        public ImportTask(GlobalsatDg100 dg100, boolean delete){
            super(tr("Importing data from device."));
            this.dg100 = dg100;
            deleteAfter = delete;
        }

        @Override public void realRun() throws IOException, SAXException {
            Main.pleaseWaitDlg.progress.setValue(0);
            Main.pleaseWaitDlg.currentAction.setText(tr("Importing data from DG100..."));
            try{
                data = dg100.readData();
            }catch(Exception e){
                eee = e;
            }
        }

        @Override protected void finish() {
            if(deleteAfter && dg100.isCancelled() == false){
                Main.pref.put("globalsat.deleteAfterDownload", true);
                try{
                    dg100.deleteData();
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(Main.parent, tr("Error deleting data.") + " " + ex.toString());
                }
            }else{
                Main.pref.put("globalsat.deleteAfterDownload", false);
            }
            if(data != null && data.hasTrackPoints()){
                Main.main.addLayer(new GpxLayer(data, tr("imported data from {0}", "DG 100")));
                Main.map.repaint();
            }else{
                JOptionPane.showMessageDialog(Main.parent, tr("No data found on device."));
            }
            if(eee != null){
                eee.printStackTrace();
                System.out.println(eee.getMessage());
                JOptionPane.showMessageDialog(Main.parent, tr("Connection failed.") + " (" + eee.toString() + ")");
            }
            dg100.disconnect();
        }

        @Override protected void cancel() {
            dg100.cancel();
            dg100.disconnect();
        }
    }


    GlobalsatImportAction importAction;
    public GlobalsatPlugin() {
        boolean error = false;
        try{
            Enumeration e = CommPortIdentifier.getPortIdentifiers();
        }catch(java.lang.UnsatisfiedLinkError e){
            error = true;
            JOptionPane.showMessageDialog(Main.parent, "<html>" + tr("Cannot load library rxtxSerial. If you need support to install it try Globalsat homepage at http://www.raphael-mack.de/josm-globalsat-gpx-import-plugin/") + "</html>");
        }
        if(!error){
            importAction = new GlobalsatImportAction();
            Main.main.menu.toolsMenu.add(importAction);
        }
    }
    
    class GlobalsatImportAction extends JosmAction{
        public GlobalsatImportAction(){
            super(tr("Globalsat Import"), "globalsatImport", tr("Import Data from Globalsat Datalogger DG100 into GPXLayer."), KeyEvent.VK_I, KeyEvent.CTRL_MASK, false);
        }
        public void actionPerformed(ActionEvent e){
            GlobalsatImportDialog dialog = new GlobalsatImportDialog();
            JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dlg = pane.createDialog(Main.parent, tr("Import"));
            dialog.setOptionPane(pane);
            dlg.setVisible(true);
            if(((Integer)pane.getValue()) == JOptionPane.OK_OPTION){
                GlobalsatDg100 dg100 = new GlobalsatDg100(dialog.getPort());
                ImportTask task = new ImportTask(dg100, dialog.deleteFilesAfterDownload());
                Main.worker.execute(task);
            }
            dlg.dispose();
        }
    }
}
