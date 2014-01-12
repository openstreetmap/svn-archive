package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.openstreetmap.josm.plugins.tageditor.preset.INameIconProvider;



public class NameIconCellRenderer extends JLabel implements TableCellRenderer {

    //private static Logger logger = Logger.getLogger(NameIconCellRenderer.class.getName());
    public static final Color BG_COLOR_SELECTED = new Color(143,170,255);


    protected void init() {
        setOpaque(true);
        setFont(new Font("SansSerif",Font.PLAIN,10));
    }

    public NameIconCellRenderer() {
        init();
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {

        if (isSelected) {
            setBackground(BG_COLOR_SELECTED);
        } else  {
            setBackground(Color.WHITE);
        }
        INameIconProvider provider = (INameIconProvider) value;
        setText(provider.getName());
        setIcon(provider.getIcon());
        return this;
    }
}
