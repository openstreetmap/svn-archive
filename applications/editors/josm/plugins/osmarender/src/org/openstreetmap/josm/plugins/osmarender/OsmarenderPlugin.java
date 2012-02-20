package org.openstreetmap.josm.plugins.osmarender;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.GBC;

public class OsmarenderPlugin extends Plugin {

    private class Action extends JosmAction {

        public Action() {
            super(tr("Osmarender"), null, tr("Osmarender"), null, true, "osmarender", true);
        }

        public void actionPerformed(ActionEvent e) {
            // get all stuff visible on screen
            LatLon bottomLeft = Main.map.mapView.getLatLon(0,Main.map.mapView.getHeight());
            LatLon topRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
            Bounds b = new Bounds(bottomLeft, topRight);

            try {
                writeGenerated(b);
            } catch(Exception ex) {
                //how handle the exception?
            }


            String firefox = Main.pref.get("osmarender.firefox", "firefox");
            try {
                // write to plugin dir
                OsmWriter w = OsmWriterFactory.createOsmWriter(new PrintWriter(new FileOutputStream(getPluginDir()+File.separator+"data.osm")), false, "0.6");
                w.header();

                // Write nodes, make list of ways and relations
                Set<OsmPrimitive> parents = new HashSet<OsmPrimitive>();
                for (Node n : Main.main.getCurrentDataSet().getNodes()) {
                    if (n.isUsable() && n.getCoor().isWithin(b)) {
                        parents.addAll(n.getReferrers());
                        w.visit(n);
                    }
                }

                // I'm not sure why (if) is this usefull
                for (OsmPrimitive p : new HashSet<OsmPrimitive>(parents)) {
                    if (p instanceof Way) {
                        for (Node n : ((Way) p).getNodes()) {
                            if (n.getCoor().isWithin(b))
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
                String exec = firefox;
                if (System.getProperty("os.name").startsWith("Windows"))
                    exec += " file:///"+getPluginDir().replace('\\','/').replace(" ","%20")+File.separator+"generated.xml\"";
                else
                    exec += " "+getPluginDir()+File.separator+"generated.xml";

                // launch up the viewer
                Runtime.getRuntime().exec(exec);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(Main.parent, tr("Firefox not found. Please set firefox executable in the Map Settings page of the preferences."));
            }
        }
    }

    private JMenuItem osmarenderMenu;

    public OsmarenderPlugin(PluginInformation info) throws IOException {
        super(info);
        osmarenderMenu = MainMenu.add(Main.main.menu.viewMenu, new Action());
        osmarenderMenu.setVisible(false);

        // install the xsl and xml file
        copy("/osmarender.xsl", "osmarender.xsl");
        copy("/osm-map-features.xml", "osm-map-features.xml");
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

    private class OsmarenderPreferenceSetting implements SubPreferenceSetting {

        private JTextField firefox = new JTextField(10);

        @Override
        public void addGui(PreferenceTabbedPane gui) {
            final JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));

            panel.add(new JLabel(tr("Firefox executable")), GBC.std().insets(10,5,5,0));
            panel.add(firefox, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
            panel.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
            firefox.setText(Main.pref.get("osmarender.firefox"));
            gui.getMapPreference().mapcontent.addTab(tr("Osmarender"), panel);
        }

        @Override
        public boolean ok() {
            Main.pref.put("osmarender.firefox", firefox.getText());
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
        String bounds_tag = "<bounds " +
            "minlat=\"" + b.getMin().lat() + "\" " +
            "maxlat=\"" + b.getMax().lat() + "\" " +
            "minlon=\"" + b.getMin().lon() + "\" " +
            "maxlon=\"" + b.getMax().lon() + "\" " + "/>";

        BufferedReader reader = new BufferedReader(
                new FileReader( getPluginDir() + File.separator + "osm-map-features.xml") );
        PrintWriter writer = new PrintWriter( getPluginDir() + File.separator + "generated.xml");

        // osm-map-fetaures.xml contain two placemark
        // (bounds_mkr1 and bounds_mkr2). We write the bounds tag
        // between the two
        String str = null;
        while( (str = reader.readLine()) != null ) {
            if(str.contains("<!--bounds_mkr1-->")) {
                writer.println(str);
                writer.println("    " + bounds_tag);
                while(!str.contains("<!--bounds_mkr2-->")) {
                    str = reader.readLine();
                }
                writer.println(str);
            } else {
                writer.println(str);
            }
        }

        writer.close();
    }
}
