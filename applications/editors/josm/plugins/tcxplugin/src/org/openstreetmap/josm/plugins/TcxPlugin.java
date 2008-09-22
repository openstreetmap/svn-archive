/*
 * josm.tcx.plugin
 * (c) Copyright by M.IT 2002-2008
 * www.emaitie.de 
 */

package org.openstreetmap.josm.plugins;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.io.TcxReader;
/**
 * @author adrian
 * @since 12.08.2008
 */
public class TcxPlugin extends Plugin
{
    public class OpenAction extends DiskAccessAction
    {
        public OpenAction()
        {
            super("Import TCX File...", "tcxicon", "", KeyEvent.VK_T, KeyEvent.CTRL_MASK);
        }
        
        private void addTcxFileFilter()
        {
    //TODO doesn't work! ExtensionFileFilter has private constructor
//            new ExtensionFileFilter("tcx", "tcx", tr("TCX Files (.tcx)"));
//            add to ExtensionFileFilter.filters;
//              => check if filters contains a tcx entry, if not create a new array with tcx in it
//              assign it to filters
            
    // TODO later we can remove the filter from the file filter list, so the extension does not appear 
    // when the user wants to open a normal file.
        }
        

        public void actionPerformed(ActionEvent e)
        {
            addTcxFileFilter();
            JFileChooser fc = createAndOpenFileChooser(true, true, null);
//            removeTcxFileFiler();
            if (fc == null)
                return;
            File[] files = fc.getSelectedFiles();
            try
            {
                for (int i = files.length; i > 0; --i)
                    openFileAsTcx(files[i-1]);
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e1);
            }
        }

        private void openFileAsTcx(File file) throws IOException
        {
            String fn = file.getName();
            if (fn.toLowerCase().endsWith(".tcx"))
            {
                TcxReader tcxReader = new TcxReader(file);
                GpxData gpxData = tcxReader.getGpxData();
                gpxData.storageFile = file;
                GpxLayer gpxLayer = new GpxLayer(gpxData, fn);
                Main.main.addLayer(gpxLayer);
                if (Main.pref.getBoolean("marker.makeautomarkers", true))
                {
                    MarkerLayer ml = new MarkerLayer(gpxData, tr("Markers from {0}", fn), file, gpxLayer);
                    if (ml.data.size() > 0)
                    {
                        Main.main.addLayer(ml);
                    }
                }
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    public TcxPlugin()
    {
        JMenu menu = Main.main.menu.getMenu(0);

        JosmAction openAction = new OpenAction();
        JMenuItem actionItem = new JMenuItem(openAction);
        menu.insert(actionItem, 2);
        actionItem.setAccelerator(openAction.shortCut);
        
    }
}
