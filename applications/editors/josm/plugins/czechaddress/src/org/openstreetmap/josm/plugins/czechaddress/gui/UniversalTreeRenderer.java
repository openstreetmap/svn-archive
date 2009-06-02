package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalRenderer;

/**
 * Renderer for rendering trees with {@link OsmPrimitive}s and
 * {@link AddressElement}s.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class UniversalTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Component c = super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);

        setIcon(UniversalRenderer.getIcon(value));
        setText(UniversalRenderer.getText(value));

        return c;
    }
}
