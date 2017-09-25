// License: GPL. For details, see LICENSE file.
package org.wikipedia.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.StructUtils;
import org.openstreetmap.josm.data.StructUtils.StructEntry;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.datatransfer.ClipboardUtils;

public class WikipediaCopyTemplate {

    private static final List<CoordCopyTemplateEntry> DEFAULT_TEMPLATES = Arrays.asList(
            new CoordCopyTemplateEntry("{{Coordinate}}", "wikipedia-coordinate", "{{Coordinate|NS={lat}|EW={lon}|type=landmark|region=}}"),
            new CoordCopyTemplateEntry("{{Coord}}", "wikipedia-coord", "{{Coord|{lat}|{lon}}}"),
            new CoordCopyTemplateEntry("{{Location dec}}", "wikipedia-location-dec", "{{Location dec|{lat}|{lon}}}"),
            new CoordCopyTemplateEntry("{{Object location dec}}", "wikipedia-object-location-dec", "{{Object location dec|{lat}|{lon}}}")
    );

    private static final List<CoordCopyTemplateEntry> TEMPLATE_ENTRIES =
            StructUtils.getListOfStructs(Main.pref, "wikipedia.copytemplates", DEFAULT_TEMPLATES, CoordCopyTemplateEntry.class);

    public WikipediaCopyTemplate() {
        JosmAction previous = MainApplication.getMenu().copyCoordinates;
        for (final CoordCopyTemplateEntry templateEntry : TEMPLATE_ENTRIES) {
            CoordCopyTemplate t = new CoordCopyTemplate(templateEntry);
            final JMenuItem menu = MainMenu.addAfter(MainApplication.getMenu().editMenu, t, false, previous);
            menu.setToolTipText(tr("Copies the {0} template to the system clipboard instantiated with the coordinates of the first selected node", templateEntry.name));
            previous = t;
        }
    }

    /**
     * Class to hold copy templates for serialization using {@link Preferences}.
     * Public visibility is needed for reflection used in {@link Preferences#getListOfStructs}.
     */
    @SuppressWarnings("WeakerAccess")
    public static class CoordCopyTemplateEntry {
        @StructEntry
        public String name;
        @StructEntry
        public String id;
        @StructEntry
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

        CoordCopyTemplate(final CoordCopyTemplateEntry entry) {
            this(tr("Copy {0} template", entry.name), entry.id, entry.pattern);
        }

        CoordCopyTemplate(String name, String toolbarId, String pattern) {
            super(name, "dialogs/wikipedia", null, null, true, toolbarId, true);
            this.pattern = pattern;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Node node = getSelectedNode();
            if (node == null) {
                return;
            }
            ClipboardUtils.copyString(pattern
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

        Node getSelectedNode() {
            DataSet ds = getLayerManager().getEditDataSet();
            if (ds == null) {
                return null;
            } else {
                return (Node) ds.getSelected().stream().filter(Node.class::isInstance).findFirst().orElse(null);
            }
        }
    }
}
