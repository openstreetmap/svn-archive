package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JMenuItem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.Preferences.pref;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.tools.Utils;

public class WikipediaCopyTemplate {

    private static final List<CoordCopyTemplateEntry> DEFAULT_TEMPLATES = Arrays.asList(
            new CoordCopyTemplateEntry("{{Coordinate}}", "wikipedia-coordinate", "{{Coordinate|NS={lat}|EW={lon}|type=landmark|region=}}"),
            new CoordCopyTemplateEntry("{{Coord}}", "wikipedia-coord", "{{Coord|{lat}|{lon}}}"),
            new CoordCopyTemplateEntry("{{Location dec}}", "wikipedia-location-dec", "{{Location dec|{lat}|{lon}}}"),
            new CoordCopyTemplateEntry("{{Object location dec}}", "wikipedia-object-location-dec", "{{Object location dec|{lat}|{lon}}}")
    );

    private static final List<CoordCopyTemplateEntry> TEMPLATE_ENTRIES =
            Main.pref.getListOfStructs("wikipedia.copytemplates", DEFAULT_TEMPLATES, CoordCopyTemplateEntry.class);

    public WikipediaCopyTemplate() {
        JosmAction previous = Main.main.menu.copyCoordinates;
        for (final CoordCopyTemplateEntry templateEntry : TEMPLATE_ENTRIES) {
            CoordCopyTemplate t = new CoordCopyTemplate(templateEntry);
            final JMenuItem menu = MainMenu.addAfter(Main.main.menu.editMenu, t, false, previous);
            menu.setToolTipText(tr("Copies the {0} template to the system clipboard instantiated with the coordinates of the first selected node", templateEntry.name));
            previous = t;
        }
    }

    /**
     * Class to hold copy templates for serialization using {@link Preferences}.
     * Public visibility is needed for reflection used in {@link Preferences#getListOfStructs}.
     */
    public static class CoordCopyTemplateEntry {
        @pref
        public String name;
        @pref
        public String id;
        @pref
        public String pattern;

        public CoordCopyTemplateEntry() {
        }

        public CoordCopyTemplateEntry(String name, String id, String pattern) {
            this.name = name;
            this.id = id;
            this.pattern = pattern;
        }
    }

    private static class CoordCopyTemplate extends JosmAction {

        protected final String pattern;

        public CoordCopyTemplate(final CoordCopyTemplateEntry entry) {
            this(tr("Copy {0} template", entry.name), entry.id, entry.pattern);
        }

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
