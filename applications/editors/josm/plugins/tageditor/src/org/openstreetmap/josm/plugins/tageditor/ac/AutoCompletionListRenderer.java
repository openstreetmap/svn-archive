package org.openstreetmap.josm.plugins.tageditor.ac;

import java.awt.Component;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionItemPritority;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * This is the table cell renderer for the list of auto completion list items.
 * 
 */
public class AutoCompletionListRenderer extends JLabel implements TableCellRenderer {

    static public final String RES_OSM_ICON = "/resources/osm.gif";
    static public final String RES_SELECTION_ICON = "/resources/selection.gif";

    /** the icon used to decorate items of priority
     *  {@link AutoCompletionItemPritority#IS_IN_STANDARD}
     */
    private Icon iconStandard;

    /** the icon used to decorate items of priority
     *  {@link AutoCompletionItemPritority#IS_IN_SELECTION}
     */
    private Icon iconSelection;

    /**
     * constructor
     */
    public AutoCompletionListRenderer() {
        setOpaque(true);
        loadIcons();
    }

    /**
     * loads the icons
     */
    protected void loadIcons() {
        URL imgURL = getClass().getResource(RES_OSM_ICON);
        if (imgURL != null) {
            iconStandard = new ImageIcon(imgURL);
        } else {
            System.err.println("Could not load icon: " + RES_OSM_ICON);
            iconStandard = null;
        }

        imgURL = getClass().getResource(RES_SELECTION_ICON);
        if (imgURL != null) {
            iconSelection = new ImageIcon(imgURL);
        } else {
            System.err.println("Could not load icon: " + RES_SELECTION_ICON);
            iconSelection = null;
        }
    }

    /**
     * prepares the renderer for rendering a specific icon
     * 
     * @param item the item to be rendered
     */
    protected void prepareRendererIcon(AutoCompletionListItem item) {
        if (item.getPriority().equals(AutoCompletionItemPritority.IS_IN_STANDARD)) {
            if (iconStandard != null) {
                setIcon(iconStandard);
            }
        } else if (item.getPriority().equals(AutoCompletionItemPritority.IS_IN_SELECTION)) {
            if (iconSelection != null) {
                setIcon(iconSelection);
            }
        }
    }

    /**
     * resets the renderer
     */
    protected void resetRenderer() {
        setIcon(null);
        setText("");
        setFont(UIManager.getFont("Table.font"));
        setOpaque(true);
        setBackground(UIManager.getColor("Table.background"));
        setForeground(UIManager.getColor("Table.foreground"));
    }

    /**
     * prepares background and text colors for a selected item
     */
    protected void renderSelected() {
        setBackground(UIManager.getColor("Table.selectionBackground"));
        setForeground(UIManager.getColor("Table.selectionForeground"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        resetRenderer();
        // set icon and text
        //
        if (value instanceof AutoCompletionListItem) {
            AutoCompletionListItem item = (AutoCompletionListItem)value;
            prepareRendererIcon(item);
            setText(item.getValue());
            setToolTipText(item.getValue());
        } else if (value != null) {
            setText(value.toString());
            setToolTipText(value.toString());
        } else {
            setText(tr("unknown"));
            setFont(getFont().deriveFont(Font.ITALIC));
        }

        // prepare background and foreground for a selected item
        //
        if (isSelected) {
            renderSelected();
        }
        return this;
    }
}
