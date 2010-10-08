package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.DefaultListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * <strong>RelationMemberColumnModel</strong> is the column model for the table of relation members
 * displayed in the {@link AdvancedEditorPanel}.
 */
public class RelationMemberColumnModel extends DefaultTableColumnModel{
    protected void build() {
        TableColumn col = new TableColumn();
        
         // the role column
         col.setHeaderValue(tr("Role"));
         col.setResizable(true);
         col.setPreferredWidth(100);    
         col.setCellEditor(new MemberRoleCellEditor());
         col.setCellRenderer(new RelationMemberRoleCellRenderer());
         addColumn(col);
         
          // column 1 - the member
          col = new TableColumn(1);
          col.setHeaderValue(tr("Refers to"));
          col.setResizable(true);
          col.setPreferredWidth(300);
          col.setCellRenderer(new RelationMemberTargetCellRenderer());
          addColumn(col);         
    }
    
    /**
     * Creates the column model with a given column selection model.
     * 
     * @param colSelectionModel the column selection model. Must not be null.
     * @throws IllegalArgumentException thrown if {@code colSelectionModel} is null
     */
    public RelationMemberColumnModel(DefaultListSelectionModel colSelectionModel) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(colSelectionModel, "colSelectionModel");
        setSelectionModel(colSelectionModel);
        build();
    }
}
