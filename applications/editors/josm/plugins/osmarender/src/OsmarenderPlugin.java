import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.AddVisitor;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.tools.GBC;


public class OsmarenderPlugin extends Plugin {

    private class Action extends AbstractAction {

        public Action() {
            super("Osmarender");
        }

        public void actionPerformed(ActionEvent e) {
            // get all stuff visible on screen
            LatLon bottomLeft = Main.map.mapView.getLatLon(0,Main.map.mapView.getHeight());
            LatLon topRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
            Bounds b = new Bounds(bottomLeft, topRight);
            Collection<Node> nodes = new HashSet<Node>();
            DataSet fromDataSet = new DataSet();
            AddVisitor adder = new AddVisitor(fromDataSet);
            for (Node n : Main.ds.nodes) {
                if (n.coor.isWithin(b)) {
                    n.visit(adder);
                    nodes.add(n);
                }
            }
            Collection<Segment> segments = new HashSet<Segment>();
            for (Segment s : Main.ds.segments) {
                if (nodes.contains(s.from) || nodes.contains(s.to)) {
                    s.visit(adder);
                    segments.add(s);
                }
            }
            for (Way w : Main.ds.ways) {
                for (Segment s : w.segments) {
                    if (segments.contains(s)) {
                        w.visit(adder);
                        break;
                    }
                }
            }

            String firefox = Main.pref.get("osmarender.firefox", "firefox");
            try {
                // write to plugin dir
                OsmWriter.output(new FileOutputStream(getPluginDir()+"data.osm"), new OsmWriter.All(fromDataSet, true));

                // get the exec line
                String exec = firefox;
                if (System.getProperty("os.name").startsWith("Windows"))
                    exec += " file:///"+getPluginDir().replace('\\','/').replace(" ","%20")+"osm-map-features.xml\"";
                else
                    exec += " "+getPluginDir()+"osm-map-features.xml";

                // launch up the viewer
                Runtime.getRuntime().exec(exec);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(Main.parent, tr("FireFox not found. Please set firefox executable in the preferences."));
            }
        }
    }

    private JMenu view;
    private JMenuItem osmarenderMenu = new JMenuItem(new Action());

    public OsmarenderPlugin() throws IOException {
        JMenuBar menu = Main.main.menu;
        view = null;
        for (int i = 0; i < menu.getMenuCount(); ++i) {
            if (menu.getMenu(i) != null && tr("View").equals(menu.getMenu(i).getText())) {
                view = menu.getMenu(i);
                break;
            }
        }
        if (view == null) {
            view = new JMenu(tr("View"));
            menu.add(view, 2);
            view.setVisible(false);
        }
        view.add(osmarenderMenu);
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
            if (view.getMenuComponentCount() == 1)
                view.setVisible(false);
        } else if (oldFrame == null && newFrame != null) {
            // enable
            osmarenderMenu.setVisible(true);
            if (view.getMenuComponentCount() == 1)
                view.setVisible(true);
        }
    }

    @Override public PreferenceSetting getPreferenceSetting() {
        return new PreferenceSetting(){
            private JTextField firefox = new JTextField(10);
            public void addGui(PreferenceDialog gui) {
                gui.map.add(new JLabel(tr("osmarender options")), GBC.eol().insets(0,5,0,0));
                gui.map.add(new JLabel(tr("FireFox executable")), GBC.std().insets(10,5,5,0));
                gui.map.add(firefox, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
                firefox.setText(Main.pref.get("osmarender.firefox"));
            }
            public void ok() {
                Main.pref.put("osmarender.firefox", firefox.getText());
            }
        };
    }
}
