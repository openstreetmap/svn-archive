package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JMenuItem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.tools.Utils;

public class WikipediaCopyTemplate {

    private static final List<CoordCopyTemplate> TEMPLATES = Arrays.asList(
            new CoordCopyTemplate(tr("Copy {0} template", "{{Coordinate}}"), "wikipedia-coordinate", "{{Coordinate|NS={lat}|EW={lon}|type=landmark|region=}}"),
            new CoordCopyTemplate(tr("Copy {0} template", "{{Coord}}"), "wikipedia-coord", "{{Coord|{lat}|{lon}}}"),
            new CoordCopyTemplate(tr("Copy {0} template", "{{Location dec}}"), "wikipedia-location-dec", "{{Location dec|{lat}|{lon}}}"),
            new CoordCopyTemplate(tr("Copy {0} template", "{{Object location dec}}"), "wikipedia-object-location-dec", "{{Object location dec|{lat}|{lon}}}")
            );

    public WikipediaCopyTemplate() {
        JosmAction previous = Main.main.menu.copyCoordinates;
        for (final CoordCopyTemplate t : TEMPLATES) {
            final JMenuItem menu = MainMenu.addAfter(Main.main.menu.editMenu, t, false, previous);
            menu.setToolTipText(tr("Copies the template to the system clipboard instantiated with the coordinates of the first selected node"));
            previous = t;
            //MainMenu.addAfter(Main.main.menu.editMenu, coord, false, Main.main.menu.copyCoordinates);
        }
    }

    private static class CoordCopyTemplate extends JosmAction {

        protected final String pattern;

        public CoordCopyTemplate(String name, String toolbarId, String pattern) {
            super(name, "dialogs/wikipedia", null, null, false, toolbarId, true);
            this.pattern = pattern;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Node node = getSelectedNode();
            if (node == null) {
                return;
            }
            Utils.copyToClipboard(pattern
                    .replace("{lat}", Double.toString(node.getCoor().lat()))
                    .replace("{lon}", Double.toString(node.getCoor().lon())));
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(getSelectedNode() != null);
        }

        @Override
        protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
            updateEnabledState();
        }

        protected Node getSelectedNode() {
            if (getCurrentDataSet() == null || getCurrentDataSet().getSelected() == null) {
                return null;
            } else {
                Collection<Node> nodes = Utils.filteredCollection(getCurrentDataSet().getSelected(), Node.class);
                return nodes.isEmpty() ? null : nodes.iterator().next();
            }
        }
    }
}
