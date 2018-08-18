// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * This class is a modification of the PropertiesDialog for the OSMRec.
 *
 *
 * This dialog displays the tags of the current selected primitives.
 *
 * If no object is selected, the dialog list is empty.
 * If only one is selected, all tags of this object are selected.
 * If more than one object are selected, the sum of all tags are displayed. If the
 * different objects share the same tag, the shared value is displayed. If they have
 * different values, all of them are put in a combo box and the string "&lt;different&gt;"
 * is displayed in italic.
 *
 * Below the list, the user can click on an add, modify and delete tag button to
 * edit the table selection value.
 *
 * The command is applied to all selected entries.
 *
 * @author imi
 * @author nkaragiannakis
 */
public class OSMRecToggleDialog extends ToggleDialog
implements DataSelectionListener, DataSetListenerAdapter.Listener {

    /**
     * The tag data of selected objects.
     */
    private final DefaultTableModel tagData = new ReadOnlyTableModel();

    /**
     * The membership data of selected objects.
     */
    private final DefaultTableModel membershipData = new ReadOnlyTableModel();

    /**
     * The tags table.
     */
    private final JTable tagTable = new JTable(tagData);

    /**
     * The membership table.
     */
    private final JTable membershipTable = new JTable(membershipData);

    /** JPanel containing both previous tables */
    private final JPanel bothTables = new JPanel();

    private final transient Map<String, Map<String, Integer>> valueCount = new TreeMap<>();
    /**
     * This sub-object is responsible for all adding and editing of tags
     */
    private final transient OSMRecPluginHelper editHelper = new OSMRecPluginHelper(tagData, valueCount);

    private final AddAction addAction = new AddAction();
    private final EditActionTrain editAction = new EditActionTrain();
    //    private final DeleteAction deleteAction = new DeleteAction();
    //    private final JosmAction[] josmActions = new JosmAction[]{addAction, editAction, deleteAction};

    /**
     * The Add button (needed to be able to disable it)
     */
    private final SideButton btnAdd = new SideButton(addAction);
    /**
     * The Edit button (needed to be able to disable it)
     */
    private final SideButton btnEdit = new SideButton(editAction);

    /**
     * Text to display when nothing selected.
     */
    private final JLabel selectSth = new JLabel("<html><p>"
            + tr("Select objects or create new objects and get recommendation.") + "</p></html>");

    // <editor-fold defaultstate="collapsed" desc="Dialog construction and helper methods">

    /**
     * Create a new OSMRecToggleDialog
     */
    public OSMRecToggleDialog() {
        super(tr("Tags/Memberships"), "propertiesdialog", tr("Tags for selected objects."),
                Shortcut.registerShortcut("subwindow:properties", tr("Toggle: {0}", tr("Tags/Memberships")), KeyEvent.VK_P,
                        Shortcut.ALT_SHIFT), 150, true);

        System.out.println("cleaning test..");
        bothTables.setLayout(new GridBagLayout());
        bothTables.setVisible(false); //my
        // Let the actions know when selection in the tables change
        tagTable.getSelectionModel().addListSelectionListener(editAction);
        membershipTable.getSelectionModel().addListSelectionListener(editAction);

        JScrollPane scrollPane = (JScrollPane) createLayout(bothTables, true,
                Arrays.asList(this.btnAdd, this.btnEdit));

        MouseClickWatch mouseClickWatch = new MouseClickWatch();
        tagTable.addMouseListener(mouseClickWatch);
        membershipTable.addMouseListener(mouseClickWatch);
        scrollPane.addMouseListener(mouseClickWatch);
        editHelper.loadTagsIfNeeded();

    }

    /**
     * This simply fires up an {@link RelationEditor} for the relation shown; everything else
     * is the editor's business.
     *
     * @param row position
     */
    private void editMembership(int row) {
        Relation relation = (Relation) membershipData.getValueAt(row, 0);
        MainApplication.getMap().relationListDialog.selectRelation(relation);
    }

    private int findRow(TableModel model, Object value) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(value))
                return i;
        }
        return -1;
    }

    /**
     * Update selection status, call @{link #selectionChanged} function.
     */
    private void updateSelection() {
        // Parameter is ignored in this class
        selectionChanged(null);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Event listeners methods">

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        if (tagTable == null)
            return; // selection changed may be received in base class constructor before init
        if (tagTable.getCellEditor() != null) {
            tagTable.getCellEditor().cancelCellEditing();
        }

        // Ignore parameter as we do not want to operate always on real selection here, especially in draw mode
        Collection<OsmPrimitive> newSel = OsmDataManager.getInstance().getInProgressSelection();
        if (newSel == null) {
            newSel = Collections.<OsmPrimitive>emptyList();
        }

        String selectedTag;
        Relation selectedRelation = null;
        selectedTag = editHelper.getChangedKey(); // select last added or last edited key by default
        if (selectedTag == null && tagTable.getSelectedRowCount() == 1) {
            selectedTag = (String) tagData.getValueAt(tagTable.getSelectedRow(), 0);
        }
        if (membershipTable.getSelectedRowCount() == 1) {
            selectedRelation = (Relation) membershipData.getValueAt(membershipTable.getSelectedRow(), 0);
        }

        // re-load tag data
        tagData.setRowCount(0);

        final Map<String, String> tags = new HashMap<>();
        valueCount.clear();
        for (Entry<String, Map<String, Integer>> e : valueCount.entrySet()) {
            int count = 0;
            for (Entry<String, Integer> e1 : e.getValue().entrySet()) {
                count += e1.getValue();
            }
            if (count < newSel.size()) {
                e.getValue().put("", newSel.size() - count);
            }
            tagData.addRow(new Object[]{e.getKey(), e.getValue()});
            tags.put(e.getKey(), e.getValue().size() == 1
                    ? e.getValue().keySet().iterator().next() : tr("<different>"));
        }

        membershipData.setRowCount(0);

        Map<Relation, MemberInfo> roles = new HashMap<>();
        for (OsmPrimitive primitive: newSel) {
            for (OsmPrimitive ref: primitive.getReferrers(true)) {
                if (ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted()) {
                    Relation r = (Relation) ref;
                    MemberInfo mi = roles.get(r);
                    if (mi == null) {
                        mi = new MemberInfo();
                    }
                    roles.put(r, mi);
                    int i = 1;
                    for (RelationMember m : r.getMembers()) {
                        if (m.getMember() == primitive) {
                            mi.add(m, i);
                        }
                        ++i;
                    }
                }
            }
        }

        List<Relation> sortedRelations = new ArrayList<>(roles.keySet());
        Collections.sort(sortedRelations, new Comparator<Relation>() {
            @Override public int compare(Relation o1, Relation o2) {
                int comp = Boolean.valueOf(o1.isDisabledAndHidden()).compareTo(o2.isDisabledAndHidden());
                return comp != 0 ? comp : DefaultNameFormatter.getInstance().getRelationComparator().compare(o1, o2);
            } });

        for (Relation r: sortedRelations) {
            membershipData.addRow(new Object[]{r, roles.get(r)});
        }

        membershipTable.getTableHeader().setVisible(membershipData.getRowCount() > 0);
        membershipTable.setVisible(membershipData.getRowCount() > 0);

        boolean hasSelection = !newSel.isEmpty();
        boolean hasTags = hasSelection && tagData.getRowCount() > 0;
        boolean hasMemberships = hasSelection && membershipData.getRowCount() > 0;

        addAction.setEnabled(hasSelection);
        //editAction.setEnabled(hasTags || hasMemberships);
        editAction.setEnabled(true);
        tagTable.setVisible(hasTags);
        tagTable.getTableHeader().setVisible(hasTags);
        selectSth.setVisible(!hasSelection);

        int selectedIndex;
        if (selectedTag != null && (selectedIndex = findRow(tagData, selectedTag)) != -1) {
            tagTable.changeSelection(selectedIndex, 0, false, false);
        } else if (selectedRelation != null && (selectedIndex = findRow(membershipData, selectedRelation)) != -1) {
            membershipTable.changeSelection(selectedIndex, 0, false, false);
        } else if (hasTags) {
            tagTable.changeSelection(0, 0, false, false);
        } else if (hasMemberships) {
            membershipTable.changeSelection(0, 0, false, false);
        }
    }

    @Override
    public void processDatasetEvent(AbstractDatasetChangedEvent event) {
        updateSelection();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Methods that are called by plugins to extend fuctionality ">


    /**
     * Returns the selected tag.
     * @return The current selected tag
     */
    @SuppressWarnings("unchecked")
    public Tag getSelectedProperty() {
        int row = tagTable.getSelectedRow();
        if (row == -1) return null;
        Map<String, Integer> map = (TreeMap<String, Integer>) tagData.getValueAt(row, 1);
        return new Tag(
                tagData.getValueAt(row, 0).toString(),
                map.size() > 1 ? "" : map.keySet().iterator().next());
    }

    /**
     * Returns the selected relation membership.
     * @return The current selected relation membership
     */
    public IRelation<?> getSelectedMembershipRelation() {
        int row = membershipTable.getSelectedRow();
        return row > -1 ? (IRelation<?>) membershipData.getValueAt(row, 0) : null;
    }

    // </editor-fold>

    /**
     * Class that watches for mouse clicks
     * @author imi
     */
    public class MouseClickWatch extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() < 2) {
                // single click, clear selection in other table not clicked in
                if (e.getSource() == tagTable) {
                    membershipTable.clearSelection();
                } else if (e.getSource() == membershipTable) {
                    tagTable.clearSelection();
                }
            } else if (e.getSource() == tagTable) {
                // double click, edit or add tag
                int row = tagTable.rowAtPoint(e.getPoint());
                if (row > -1) {
                    boolean focusOnKey = tagTable.columnAtPoint(e.getPoint()) == 0;
                    editHelper.editTag(row, focusOnKey);
                } else {
                    editHelper.addTag();
                }
            } else if (e.getSource() == membershipTable) {
                int row = membershipTable.rowAtPoint(e.getPoint());
                if (row > -1) {
                    editMembership(row);
                }
            } else {
                editHelper.addTag();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == tagTable) {
                membershipTable.clearSelection();
            } else if (e.getSource() == membershipTable) {
                tagTable.clearSelection();
            }
        }
    }

    static class MemberInfo {
        private List<RelationMember> role = new ArrayList<>();
        private Set<OsmPrimitive> members = new HashSet<>();
        private List<Integer> position = new ArrayList<>();
        private String positionString;
        private String roleString;

        MemberInfo() {
        }

        void add(RelationMember r, Integer p) {
            role.add(r);
            members.add(r.getMember());
            position.add(p);
        }

        @Override
        public String toString() {
            return "MemberInfo{" +
                    "roles='" + roleString + '\'' +
                    ", positions='" + positionString + '\'' +
                    '}';
        }
    }

    /**
     * Class that allows fast creation of read-only table model with String columns
     */
    public static class ReadOnlyTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }

    /**
     * Action handling add button press in properties dialog.
     */
    class AddAction extends JosmAction {
        AddAction() {
            super(tr("Add Recommendation"), /* ICON() */ "dialogs/add", tr("Add a recommended key/value pair to your object"),
                    Shortcut.registerShortcut("properties:add", tr("Add Tag"), KeyEvent.VK_A,
                            Shortcut.ALT), false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            editHelper.addTag();
            btnAdd.requestFocusInWindow();
        }
    }

    /**
     * Action handling edit button press in properties dialog.
     * training process dialog/configuration
     */
    class EditActionTrain extends JosmAction implements ListSelectionListener {
        EditActionTrain() {
            super(tr("Train a Model"), /* ICON() */ "dialogs/fix", tr("Start the training engine!"),
                    Shortcut.registerShortcut("properties:edit", tr("Edit Tags"), KeyEvent.VK_S,
                            Shortcut.ALT), false);
            setEnabled(true);
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            if (tagTable.getSelectedRowCount() == 1) {
                int row = tagTable.getSelectedRow();
                editHelper.editTag(row, false);
            } else if (membershipTable.getSelectedRowCount() == 1) {
                int row = membershipTable.getSelectedRow();
                editHelper.editTag(row, false);
            } else {
                editHelper.editTag(1, false);
            }
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(true);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }
}
