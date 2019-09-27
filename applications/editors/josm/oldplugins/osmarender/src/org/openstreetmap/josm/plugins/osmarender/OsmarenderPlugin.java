// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmarender;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.PlatformManager;
import org.openstreetmap.josm.tools.Utils;

public class OsmarenderPlugin extends Plugin {

    private class Action extends JosmAction {

        public Action() {
            super(tr("Osmarender"), (String)null, tr("Osmarender"), null, true, "osmarender", true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataSet ds = MainApplication.getLayerManager().getEditDataSet();
            if (ds == null) {
                return;
            }

            // get all stuff visible on screen
            MapView mapView = MainApplication.getMap().mapView;
            LatLon bottomLeft = mapView.getLatLon(0, mapView.getHeight());
            LatLon topRight = mapView.getLatLon(mapView.getWidth(), 0);
            Bounds b = new Bounds(bottomLeft, topRight);

            try {
                writeGenerated(b);
            } catch (IOException ex) {
                // how handle the exception?
            	Logging.error(ex);
            }

            String firefox = Config.getPref().get("osmarender.firefox", "firefox");
            String pluginDir = getPluginDirs().getUserDataDirectory(false).getPath();
            try (OsmWriter w = OsmWriterFactory.createOsmWriter(new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(pluginDir+File.separator+"data.osm"), "UTF-8")), false, "0.6")) {
                // write to plugin dir
                w.header();

                // Write nodes, make list of ways and relations
                Set<OsmPrimitive> parents = new HashSet<>();
                for (Node n : ds.getNodes()) {
                    if (n.isUsable() && n.getCoor() != null && n.getCoor().isWithin(b)) {
                        parents.addAll(n.getReferrers());
                        w.visit(n);
                    }
                }

                // I'm not sure why (if) is this usefull
                for (OsmPrimitive p : new HashSet<>(parents)) {
                    if (p instanceof Way) {
                        for (Node n : ((Way) p).getNodes()) {
                            if (n.getCoor() != null && n.getCoor().isWithin(b))
                                parents.add(n);
                        }
                    }
                }

                // Write ways
                for (OsmPrimitive p: parents) {
                    if (p instanceof Way) {
                        w.visit((Way)p);
                    }
                }

                // Write relations (should be parent relation also written?)
                for (OsmPrimitive p: parents) {
                    if (p instanceof Relation) {
                        w.visit((Relation)p);
                    }
                }

                w.footer();
                w.close();

                // get the exec line
                String argument;
                if (PlatformManager.isPlatformWindows())
                    argument = "file:///"+pluginDir.replace('\\','/').replace(" ","%20")+File.separator+"generated.xml\"";
                else
                    argument = pluginDir+File.separator+"generated.xml";

                // launch up the viewer
                Runtime.getRuntime().exec(new String[]{firefox, argument});
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                        tr("Firefox not found. Please set firefox executable in the Map Settings page of the preferences."));
            }
        }
    }

    private JMenuItem osmarenderMenu;

    /**
     * Constructs a new {@code OsmarenderPlugin}.
     * @param info plugin info
     * @throws IOException if files cannot be copied
     */
    public OsmarenderPlugin(PluginInformation info) throws IOException {
        super(info);
        osmarenderMenu = MainMenu.add(MainApplication.getMenu().viewMenu, new Action());
        osmarenderMenu.setVisible(false);

        // install the xsl and xml file
        _copy("/osmarender.xsl", "osmarender.xsl");
        _copy("/osm-map-features.xml", "osm-map-features.xml");
    }

    /**
     * @return The directory for the plugin to store all kind of stuff.
     * @deprecated (since 13007) to get the same directory as this method, use {@code getPluginDirs().getUserDataDirectory(false)}.
     * However, for files that can be characterized as cache or preferences, you are encouraged to use the appropriate
     * {@link org.openstreetmap.josm.spi.preferences.IBaseDirectories} method from {@link #getPluginDirs()}.
     */
    @Deprecated
    public String _getPluginDir() {
        return new File(Preferences.main().getPluginsDirectory(), getPluginInformation().name).getPath();
    }

    /**
     * Copies the resource 'from' to the file in the plugin directory named 'to'.
     * @param from source file
     * @param to target file
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file,
     * does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if any other I/O error occurs
     * @deprecated without replacement
     */
    @Deprecated
    public void _copy(String from, String to) throws IOException {
        String pluginDirName = _getPluginDir();
        File pluginDir = new File(pluginDirName);
        if (!pluginDir.exists()) {
            Utils.mkDirs(pluginDir);
        }
        try (InputStream in = getClass().getResourceAsStream(from)) {
            if (in == null) {
                throw new IOException("Resource not found: "+from);
            }
            Files.copy(in, new File(pluginDirName, to).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null && newFrame == null) {
            // disable
            osmarenderMenu.setVisible(false);
        } else if (oldFrame == null && newFrame != null) {
            // enable
            osmarenderMenu.setVisible(true);
        }
    }

    @Override public PreferenceSetting getPreferenceSetting() {
        return new OsmarenderPreferenceSetting();
    }

    private static class OsmarenderPreferenceSetting implements SubPreferenceSetting {

        private JTextField firefox = new JTextField(10);

        @Override
        public void addGui(PreferenceTabbedPane gui) {
            final JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));

            panel.add(new JLabel(tr("Firefox executable")), GBC.std().insets(10,5,5,0));
            panel.add(firefox, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
            panel.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
            firefox.setText(Config.getPref().get("osmarender.firefox"));
            gui.getMapPreference().getTabPane().addTab(tr("Osmarender"), panel);
        }

        @Override
        public boolean ok() {
            Config.getPref().put("osmarender.firefox", firefox.getText());
            return false;
        }

        @Override
        public boolean isExpert() {
            return false;
        }

        @Override
        public TabPreferenceSetting getTabPreferenceSetting(final PreferenceTabbedPane gui) {
            return gui.getMapPreference();
        }

    }

    private void writeGenerated(Bounds b) throws IOException {
        String boundsTag = "<bounds " +
            "minlat=\"" + b.getMin().lat() + "\" " +
            "maxlat=\"" + b.getMax().lat() + "\" " +
            "minlon=\"" + b.getMin().lon() + "\" " +
            "maxlon=\"" + b.getMax().lon() + "\" " + "/>";
        String pluginDir = getPluginDirs().getUserDataDirectory(false).getPath();

        try (
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(pluginDir + File.separator + "osm-map-features.xml"), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter( pluginDir + File.separator + "generated.xml", "UTF-8");
        ) {
            // osm-map-features.xml contain two placemark
            // (bounds_mkr1 and bounds_mkr2). We write the bounds tag between the two
            String str;
            while( (str = reader.readLine()) != null ) {
                if(str.contains("<!--bounds_mkr1-->")) {
                    writer.println(str);
                    writer.println("    " + boundsTag);
                    while(!str.contains("<!--bounds_mkr2-->")) {
                        str = reader.readLine();
                    }
                    writer.println(str);
                } else {
                    writer.println(str);
                }
            }
        }
    }
}
