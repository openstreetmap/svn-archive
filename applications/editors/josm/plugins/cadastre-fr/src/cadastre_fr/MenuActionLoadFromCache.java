// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.data.projection.UTM_France_DOM;
import org.openstreetmap.josm.gui.layer.Layer;

public class MenuActionLoadFromCache extends JosmAction {
    private static final long serialVersionUID = 1L;

    public static String name = marktr("Load layer from cache");

    public MenuActionLoadFromCache() {
        super(tr(name), "cadastre_small", tr("Load location from cache (only if cache is enabled)"), null, false, "cadastrefr/loadfromcache", true);
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = createAndOpenFileChooser();
        if (fc == null)
            return;

        File[] files = fc.getSelectedFiles();
        int layoutZone = getCurrentProjZone();
        nextFile:
        for (File file : files) {
            if (file.exists()) {
                String filename = file.getName();
                String ext = (filename.lastIndexOf(".")==-1)?"":filename.substring(filename.lastIndexOf(".")+1,filename.length());
                if ((ext.length() == 3 && ext.substring(0, CacheControl.cLambertCC9Z.length()).equals(CacheControl.cLambertCC9Z) &&
                    !(Main.getProjection() instanceof LambertCC9Zones))
                    || (ext.length() == 4 && ext.substring(0, CacheControl.cUTM20N.length()).equals(CacheControl.cUTM20N) &&
                            !(Main.getProjection() instanceof UTM_France_DOM))
                    || (ext.length() == 1) && !(Main.getProjection() instanceof Lambert)) {
                        JOptionPane.showMessageDialog(Main.parent, tr("{0} not allowed with the current projection", filename), tr("Error"), JOptionPane.ERROR_MESSAGE);
                        continue;
                } else {
                    String location = filename.substring(0, filename.lastIndexOf("."));
                    if (ext.length() == 3 && ext.substring(0, CacheControl.cLambertCC9Z.length()).equals(CacheControl.cLambertCC9Z))
                        ext = ext.substring(2);
                    else if (ext.length() == 4 && ext.substring(0, CacheControl.cUTM20N.length()).equals(CacheControl.cUTM20N))
                        ext = ext.substring(3);
                    // check the extension and its compatibility with current projection
                    try {
                        int cacheZone = Integer.parseInt(ext) - 1;
                        if (cacheZone >=0 && cacheZone <= 9) {
                            if (cacheZone != layoutZone) {
                                JOptionPane.showMessageDialog(Main.parent, tr("Cannot load cache {0} which is not compatible with current projection zone", filename), tr("Error"), JOptionPane.ERROR_MESSAGE);
                                continue nextFile;
                            } else
                                System.out.println("Load cache " + filename);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(Main.parent, tr("Selected file {0} is not a cache file from this plugin (invalid extension)", filename), tr("Error"), JOptionPane.ERROR_MESSAGE);
                        continue nextFile;
                    }
                    // check if the selected cache is not already displayed
                    if (Main.map != null) {
                        for (Layer l : Main.map.mapView.getAllLayers()) {
                            if (l instanceof WMSLayer && l.getName().equals(location)) {
                                JOptionPane.showMessageDialog(Main.parent, tr("The location {0} is already on screen. Cache not loaded.", filename), tr("Error"), JOptionPane.ERROR_MESSAGE);
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
        int layoutZone = new MenuActionLoadFromCache().getCurrentProjZone();
        if (layoutZone != -1) {
            if (Main.getProjection() instanceof Lambert)
                fc.addChoosableFileFilter(CacheFileLambert4ZoneFilter.filters[layoutZone]);
            else if (Main.getProjection() instanceof LambertCC9Zones)
                fc.addChoosableFileFilter(CacheFileLambert9ZoneFilter.filters[layoutZone]);
            else if (Main.getProjection() instanceof UTM_France_DOM)
                fc.addChoosableFileFilter(CacheFileUTM20NFilter.filters[layoutZone]);
        }
        fc.setAcceptAllFileFilterUsed(false);

        int answer = fc.showOpenDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        return fc;
    }

    private int getCurrentProjZone() {
        int zone = -1;
        if (Main.getProjection() instanceof LambertCC9Zones)
            zone = ((LambertCC9Zones)Main.getProjection()).getLayoutZone();
        else if (Main.getProjection() instanceof Lambert)
            zone = ((Lambert)Main.getProjection()).getLayoutZone();
        else if (Main.getProjection() instanceof UTM_France_DOM)
            zone = ((UTM_France_DOM)Main.getProjection()).getCurrentGeodesic();
        return zone;
    }
}
