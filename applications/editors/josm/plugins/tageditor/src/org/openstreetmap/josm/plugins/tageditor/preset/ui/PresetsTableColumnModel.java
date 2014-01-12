package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import static org.openstreetmap.josm.tools.I18n.tr;

public class PresetsTableColumnModel extends DefaultTableColumnModel  {

    protected void createColumns() {
        TableCellRenderer renderer = new NameIconCellRenderer();
        
        TableColumn col = null;
        
        // column 0 - Group   
        col = new TableColumn(0);
        col.setHeaderValue(tr("Group"));
        col.setResizable(true);
        col.setCellRenderer(renderer);
        addColumn(col);
        
        // column 1 - Item   
        col = new TableColumn(1);
        col.setHeaderValue(tr("Item"));
        col.setResizable(true);
        col.setCellRenderer(renderer);
        addColumn(col);

    }

    public PresetsTableColumnModel() {
        createColumns();
    }
    
}
