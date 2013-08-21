package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class KeyValueCellRenderer extends JLabel implements TableCellRenderer  {

    //private static final Logger logger = Logger.getLogger(KeyValueCellRenderer.class.getName());

    protected void init() {
        setFont(new Font("Courier",Font.PLAIN,getFont().getSize()));
        setOpaque(true);
    }

    public KeyValueCellRenderer() {
        init();
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {

        if (isSelected) {
            setBackground(UIManager.getColor("Table.selectionBackground"));
            setForeground(UIManager.getColor("Table.selectionForeground"));
        } else  {
            setBackground(UIManager.getColor("Table.background"));
            setForeground(UIManager.getColor("Table.foreground"));
        }
        setText((String)value);
        setIcon(null);
        return this;
    }
}
