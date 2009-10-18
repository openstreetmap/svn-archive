package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.gui.layer.Layer;

public class MenuActionLoadFromCache extends JosmAction {
    private static final long serialVersionUID = 1L;

    public static String name = "Load layer from cache";

    public MenuActionLoadFromCache() {
        super(tr(name), "cadastre_small", tr("Load location from cache (only if cache is enabled)"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = createAndOpenFileChooser();
        if (fc == null)
            return;

        File[] files = fc.getSelectedFiles();
        nextFile:
        for (File file : files) {
            if (file.exists()) {
                String filename = file.getName();
                String ext = (filename.lastIndexOf(".")==-1)?"":filename.substring(filename.lastIndexOf(".")+1,filename.length());
                if ((ext.length() > 2 && ext.substring(0, CacheControl.cLambertCC9Z.length()).equals(CacheControl.cLambertCC9Z) &&
                    !(Main.proj instanceof LambertCC9Zones))
                    || (ext.length() == 1) && !(Main.proj instanceof Lambert)) {
                        JOptionPane.showMessageDialog(Main.parent, tr("{0} not allowed with the current projection", filename), tr("Error"), JOptionPane.ERROR_MESSAGE);
                        continue;
                } else {
                    String location = filename.substring(0, filename.lastIndexOf("."));
                    if (ext.length() > 2 && ext.substring(0, CacheControl.cLambertCC9Z.length()).equals(CacheControl.cLambertCC9Z))
                        ext = ext.substring(2);
                    // check the extension and its Lambert zone consistency
                    try {
                        int cacheZone = Integer.parseInt(ext) - 1;
                        if (cacheZone >=0 && cacheZone <= 3) {
                            if (Lambert.layoutZone == -1) {
                                Lambert.layoutZone = cacheZone;
                                System.out.println("Load cache \"" + filename + "\" in Lambert Zone " + (Lambert.layoutZone+1));
                            } else if (cacheZone != Lambert.layoutZone) {
                                System.out.println("Cannot load cache \"" + filename + "\" which is not in current Lambert Zone "
                                        + Lambert.layoutZone);
                                continue nextFile;
                            } else
                                System.out.println("Load cache " + filename);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Selected file \"" + filename + "\" is not a WMS cache file (invalid extension)");
                        continue;
                    }
                    // check if the selected cache is not already displayed
                    if (Main.map != null) {
                        for (Layer l : Main.map.mapView.getAllLayers()) {
                            if (l instanceof WMSLayer && l.getName().equals(location)) {
                                System.out.println("The location " + filename + " is already on screen. Cache not loaded.");
                                continue nextFile;
                            }
                        }
                    }
                    // create layer and load cache
                    WMSLayer wmsLayer = new WMSLayer("", "", Integer.parseInt(ext)-1);
                    if (wmsLayer.getCacheControl().loadCache(file, Lambert.layoutZone))
                        Main.main.addLayer(wmsLayer);
                    
                }
            }
        }

    }

    protected static JFileChooser createAndOpenFileChooser() {
        JFileChooser fc = new JFileChooser(new File(CadastrePlugin.cacheDir));
        fc.setMultiSelectionEnabled(true);
        if (Lambert.layoutZone != -1)
            fc.addChoosableFileFilter(CacheFileFilter.filters[Lambert.layoutZone]);
        fc.setAcceptAllFileFilterUsed(false);

        int answer = fc.showOpenDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        return fc;
    }

}
