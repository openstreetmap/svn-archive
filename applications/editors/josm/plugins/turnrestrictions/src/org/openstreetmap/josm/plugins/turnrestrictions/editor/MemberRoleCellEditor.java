// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionItemPritority;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;

/**
 * The cell editor for member roles of relation members in a turn restriction.
 * 
 */
public class MemberRoleCellEditor extends AbstractCellEditor implements TableCellEditor {
    //static private Logger logger = Logger.getLogger(MemberRoleCellEditor.class.getName());

    private AutoCompletingTextField editor = null;

    /** user input is matched against this list of auto completion items */
    private AutoCompletionList autoCompletionList = null;

    /**
     * constructor
     */
    public MemberRoleCellEditor() {
        editor = new AutoCompletingTextField();
        autoCompletionList = new AutoCompletionList();
        editor.setAutoCompletionList(autoCompletionList);
        autoCompletionList.add(new AutoCompletionListItem("from", AutoCompletionItemPritority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionListItem("to", AutoCompletionItemPritority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionListItem("via", AutoCompletionItemPritority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionListItem("location_hint", AutoCompletionItemPritority.IS_IN_STANDARD));
    }

    /**
     * replies the table cell editor
     */
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {

        String role = (String)value;
        editor.setText(role);        
        return editor;
    }

    public Object getCellEditorValue() {
        return editor.getText();
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }

    @Override
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }
}
