// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.openstreetmap.josm.data.tagging.ac.AutoCompletionItem;
import org.openstreetmap.josm.data.tagging.ac.AutoCompletionPriority;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;

/**
 * The cell editor for member roles of relation members in a turn restriction.
 *
 */
public class MemberRoleCellEditor extends AbstractCellEditor implements TableCellEditor {
    //private static Logger logger = Logger.getLogger(MemberRoleCellEditor.class.getName());

    private AutoCompletingTextField editor = null;

    /** user input is matched against this list of auto completion items */
    private AutoCompletionList autoCompletionList = null;

    /**
     * constructor
     */
    public MemberRoleCellEditor() {
        editor = new AutoCompletingTextField(0, false);
        autoCompletionList = new AutoCompletionList();
        editor.setAutoCompletionList(autoCompletionList);
        autoCompletionList.add(new AutoCompletionItem("from", AutoCompletionPriority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionItem("to", AutoCompletionPriority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionItem("via", AutoCompletionPriority.IS_IN_STANDARD));
        autoCompletionList.add(new AutoCompletionItem("location_hint", AutoCompletionPriority.IS_IN_STANDARD));
    }

    /**
     * replies the table cell editor
     */
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {

        String role = (String) value;
        editor.setText(role);
        return editor;
    }

    @Override
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
