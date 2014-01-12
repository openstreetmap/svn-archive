package org.openstreetmap.josm.plugins.tageditor.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.plugins.tageditor.preset.Item;

public class PresetItemListCellRenderer extends JLabel implements ListCellRenderer {
    //private static final Logger logger = Logger.getLogger(PresetItemListCellRenderer.class.getName());

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        Item item = (Item)value;
        if (item == null) {
            setText(tr("(none)"));
            setIcon(null);
        } else {
            if (isSelected) {
                setBackground(UIManager.getColor("Table.selectionBackground"));
                setForeground(UIManager.getColor("Table.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("Table.background"));
                setForeground(UIManager.getColor("Table.foreground"));
            }
            setIcon(item.getIcon());
            StringBuilder sb = new StringBuilder();
            sb.append(item.getParent().getName())
            .append("/")
            .append(item.getName());
            setText(sb.toString());
            setOpaque(true);
            setFont(UIManager.getFont("Table.font"));
        }
        return this;
    }
}
