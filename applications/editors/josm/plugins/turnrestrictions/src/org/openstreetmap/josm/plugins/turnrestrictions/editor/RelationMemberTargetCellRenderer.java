package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.openstreetmap.josm.gui.OsmPrimitivRenderer;

public class RelationMemberTargetCellRenderer extends OsmPrimitivRenderer{
    static private final Logger logger = Logger.getLogger(RelationMemberTargetCellRenderer.class.getName());
    private JLabel mockCell;
    
    public RelationMemberTargetCellRenderer() {
        mockCell = new JLabel();
        mockCell.setText("");
        mockCell.setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null){
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        
        // FIXME: required to always draw a mock row, even if the table is empty.
        // Otherwise, drag and drop onto the table fails.
        // Replace with JTable.setFillsViewportHeight(boolean) after the migration
        // to Java 6.
        if (isSelected){
            mockCell.setBackground(UIManager.getColor("Table.selectionBackground"));
            mockCell.setForeground(UIManager.getColor("Table.selectionForeground"));
        } else {
            mockCell.setBackground(UIManager.getColor("Panel.background"));
            mockCell.setForeground(UIManager.getColor("Panel.foreground"));
        }       
        return mockCell;        
    }
}
