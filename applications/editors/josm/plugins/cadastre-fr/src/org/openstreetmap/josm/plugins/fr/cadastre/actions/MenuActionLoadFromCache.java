// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.CacheControl;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.CacheFileLambert4ZoneFilter;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.CacheFileLambert9ZoneFilter;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.CacheFileUTM20NFilter;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.tools.Logging;

/**
 * Load location from cache (only if cache is enabled)
 */
public class MenuActionLoadFromCache extends JosmAction {
    private static final long serialVersionUID = 1L;

    private static final String name = marktr("Load layer from cache");

    /**
     * Constructs a new {@code MenuActionLoadFromCache}.
     */
    public MenuActionLoadFromCache() {
        super(tr(name), "cadastre_small",
                tr("Load location from cache (only if cache is enabled)"), null, false, "cadastrefr/loadfromcache", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = createAndOpenFileChooser();
        if (fc == null)
            return;

        File[] files = fc.getSelectedFiles();
        int layoutZone = CadastrePlugin.getCadastreProjectionLayoutZone();
        nextFile:
        for (File file : files) {
            if (file.exists()) {
                String filename = file.getName();
                String ext = (filename.lastIndexOf('.') == -1) ? "" : filename.substring(filename.lastIndexOf('.')+1, filename.length());
                if ((ext.length() == 3 && ext.substring(0, CacheControl.C_LAMBERT_CC_9Z.length()).equals(CacheControl.C_LAMBERT_CC_9Z) &&
                    !CadastrePlugin.isLambert_cc9())
                    || (ext.length() == 4 && ext.substring(0, CacheControl.C_UTM20N.length()).equals(CacheControl.C_UTM20N) &&
                            !CadastrePlugin.isUtm_france_dom())
                    || (ext.length() == 1 && !CadastrePlugin.isLambert())) {
                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                tr("{0} not allowed with the current projection", filename),
                                tr("Error"), JOptionPane.ERROR_MESSAGE);
                        continue;
                } else {
                    String location = filename.substring(0, filename.lastIndexOf('.'));
                    if (ext.length() == 3 && ext.substring(0, CacheControl.C_LAMBERT_CC_9Z.length()).equals(CacheControl.C_LAMBERT_CC_9Z))
                        ext = ext.substring(2);
                    else if (ext.length() == 4 && ext.substring(0, CacheControl.C_UTM20N.length()).equals(CacheControl.C_UTM20N))
                        ext = ext.substring(3);
                    // check the extension and its compatibility with current projection
                    try {
                        int cacheZone = Integer.parseInt(ext) - 1;
                        if (cacheZone >= 0 && cacheZone <= 9) {
                            if (cacheZone != layoutZone) {
                                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                        tr("Cannot load cache {0} which is not compatible with current projection zone", filename),
                                        tr("Error"), JOptionPane.ERROR_MESSAGE);
                                continue nextFile;
                            } else
                                Logging.info("Load cache " + filename);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                tr("Selected file {0} is not a cache file from this plugin (invalid extension)", filename),
                                tr("Error"), JOptionPane.ERROR_MESSAGE);
                        continue nextFile;
                    }
                    // check if the selected cache is not already displayed
                    if (MainApplication.getMap() != null) {
                        for (Layer l : MainApplication.getLayerManager().getLayers()) {
                            if (l instanceof WMSLayer && l.getName().equals(location)) {
                                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                                        tr("The location {0} is already on screen. Cache not loaded.", filename),
                                        tr("Error"), JOptionPane.ERROR_MESSAGE);
                                continue nextFile;
                            }
                        }
                    }
                    // create layer and load cache
                    WMSLayer wmsLayer = new WMSLayer("", "", Integer.parseInt(ext)-1);
                    if (wmsLayer.grabThread.getCacheControl().loadCache(file, layoutZone)) {
                        CadastrePlugin.addWMSLayer(wmsLayer);
                    }
                }
            }
        }
    }

    protected static JFileChooser createAndOpenFileChooser() {
        JFileChooser fc = new JFileChooser(new File(CadastrePlugin.cacheDir));
        fc.setMultiSelectionEnabled(true);
        int layoutZone = CadastrePlugin.getCadastreProjectionLayoutZone();
        if (layoutZone != -1) {
            if (CadastrePlugin.isLambert())
                fc.addChoosableFileFilter(CacheFileLambert4ZoneFilter.filters[layoutZone]);
            else if (CadastrePlugin.isLambert_cc9())
                fc.addChoosableFileFilter(CacheFileLambert9ZoneFilter.filters[layoutZone]);
            else if (CadastrePlugin.isUtm_france_dom())
                fc.addChoosableFileFilter(CacheFileUTM20NFilter.filters[layoutZone]);
        }
        fc.setAcceptAllFileFilterUsed(false);

        int answer = fc.showOpenDialog(MainApplication.getMainFrame());
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        return fc;
    }

}
