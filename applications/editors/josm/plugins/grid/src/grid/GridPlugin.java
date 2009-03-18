package grid;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.AddVisitor;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.plugins.Plugin;

public class GridPlugin extends Plugin {

    private class Action extends AbstractAction {
    public Action() {
        super(tr("Add grid"));
    }
    public void actionPerformed(ActionEvent e) {
        GridLayer gridLayer = new GridLayer("");
        if (gridLayer != null)
            Main.main.addLayer(gridLayer);
    }
    }
    private JMenu edit;
    private JMenuItem addGridMenu = new JMenuItem(new Action());

    public GridPlugin() {
        edit = Main.main.menu.editMenu;
        edit.add(addGridMenu);
        addGridMenu.setVisible(false);
    }
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null && newFrame == null) {
            // disable
            addGridMenu.setVisible(false);
            if (edit.getMenuComponentCount() == 1)
                edit.setVisible(false);
        } else if (oldFrame == null && newFrame != null) {
            // enable
            addGridMenu.setVisible(true);
            if (edit.getMenuComponentCount() == 1)
                edit.setVisible(true);
        }
    }

}
