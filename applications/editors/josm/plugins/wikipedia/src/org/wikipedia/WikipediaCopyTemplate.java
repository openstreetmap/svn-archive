package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.tools.Utils;

public class WikipediaCopyTemplate {

    public WikipediaCopyTemplate() {
        final CoordCopyTemplate coord = new CoordCopyTemplate(
                tr("Copy '{{Coord}}' template"), "wikipedia-coord",
                "{{Coord|{lat}|{lon}}}");
        final CoordCopyTemplate coordinate = new CoordCopyTemplate(
                tr("Copy '{{Coordinate}}' template"), "wikipedia-coordinate",
                "{{Coordinate|NS={lat}|EW={lon}|type=landmark|region=}}");
        MainMenu.addAfter(Main.main.menu.editMenu, coord, false, Main.main.menu.copyCoordinates);
        MainMenu.addAfter(Main.main.menu.editMenu, coordinate, false, Main.main.menu.copyCoordinates);
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
