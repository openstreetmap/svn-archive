// License: GPL. For details, see LICENSE file.
package relcontext;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeRelationMemberRoleCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PrimitiveRenderer;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import relcontext.actions.AddRemoveMemberAction;
import relcontext.actions.ClearChosenRelationAction;
import relcontext.actions.CreateMultipolygonAction;
import relcontext.actions.CreateRelationAction;
import relcontext.actions.DeleteChosenRelationAction;
import relcontext.actions.DownloadChosenRelationAction;
import relcontext.actions.DownloadParentsAction;
import relcontext.actions.DuplicateChosenRelationAction;
import relcontext.actions.EditChosenRelationAction;
import relcontext.actions.FindRelationAction;
import relcontext.actions.ReconstructPolygonAction;
import relcontext.actions.ReconstructRouteAction;
import relcontext.actions.RelationHelpAction;
import relcontext.actions.SelectInRelationPanelAction;
import relcontext.actions.SelectMembersAction;
import relcontext.actions.SelectRelationAction;
import relcontext.actions.SortAndFixAction;

/**
 * The new, advanced relation editing panel.
 *
 * @author Zverik
 */
public class RelContextDialog extends ToggleDialog implements ActiveLayerChangeListener, ChosenRelationListener, DataSelectionListener {

    public static final String PREF_PREFIX = "reltoolbox";

    private final DefaultTableModel relationsData;
    private ChosenRelation chosenRelation;
    private JPanel chosenRelationPanel;
    private ChosenRelationPopupMenu popupMenu;
    private MultipolygonSettingsPopup multiPopupMenu;
    private RoleComboBoxModel roleBoxModel;
    private SortAndFixAction sortAndFixAction;
    // actions saved for unregistering on dialog destroying
    private final EnterRoleAction enterRoleAction;
    private final FindRelationAction findRelationAction;
    private final CreateMultipolygonAction createMultipolygonAction;
    private final CreateRelationAction createRelationAction;
    private final AddRemoveMemberAction addRemoveMemberAction;

    public RelContextDialog() {
        super(tr("Relation Toolbox"), PREF_PREFIX, tr("Open relation/multipolygon editor panel"), null, 150, true);

        chosenRelation = new ChosenRelation();
        chosenRelation.addChosenRelationListener(this);
        MainApplication.getLayerManager().addActiveLayerChangeListener(chosenRelation);

        popupMenu = new ChosenRelationPopupMenu(chosenRelation);
        multiPopupMenu = new MultipolygonSettingsPopup();

        JPanel rcPanel = new JPanel(new BorderLayout());

        relationsData = new RelationTableModel();
        relationsData.setColumnIdentifiers(new String[] {tr("Member Of"), tr("Role")});
        final JTable relationsTable = new JTable(relationsData);
        configureRelationsTable(relationsTable);
        rcPanel.add(new JScrollPane(relationsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        final MouseListener relationMouseAdapter = new ChosenRelationMouseAdapter();
        final JComboBox<String> roleBox = new JComboBox<>();
        roleBoxModel = new RoleComboBoxModel(roleBox);
        roleBox.setModel(roleBoxModel);
        roleBox.addMouseListener(relationMouseAdapter);
        roleBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                String memberRole = roleBoxModel.getSelectedMembersRole();
                String selectedRole = roleBoxModel.isAnotherRoleSelected() ? askForRoleName() : roleBoxModel.getSelectedRole();
                if (memberRole != null && selectedRole != null && !memberRole.equals(selectedRole)) {
                    applyRoleToSelection(selectedRole.trim());
                }
            }
        });
        roleBox.setVisible(false);
        enterRoleAction = new EnterRoleAction(); // just for the shortcut
        sortAndFixAction = new SortAndFixAction(chosenRelation);

        // [±][X] relation U [AZ][Down][Edit]
        chosenRelationPanel = new JPanel(new GridBagLayout());
        addRemoveMemberAction = new AddRemoveMemberAction(chosenRelation, sortAndFixAction);
        chosenRelationPanel.add(new JButton(addRemoveMemberAction), GBC.std());
        chosenRelationPanel.add(sizeButton(new JButton(new ClearChosenRelationAction(chosenRelation)), 32, 0), GBC.std());
        final ChosenRelationComponent chosenRelationComponent = new ChosenRelationComponent(chosenRelation);
        chosenRelationComponent.addMouseListener(relationMouseAdapter);
        chosenRelationPanel.add(chosenRelationComponent, GBC.std().fill().insets(5, 0, 5, 0));
        chosenRelationPanel.add(roleBox, GBC.std().fill().insets(5, 0, 5, 0));
        final JButton sortAndFixButton = (JButton) sizeButton(new JButton(sortAndFixAction), 32, 0);
        chosenRelationPanel.add(sortAndFixButton, GBC.std().fill(GBC.VERTICAL));
        final Action downloadChosenRelationAction = new DownloadChosenRelationAction(chosenRelation);
        final JButton downloadButton = (JButton) sizeButton(new JButton(downloadChosenRelationAction), 32, 0);
        chosenRelationPanel.add(downloadButton, GBC.std().fill(GBC.VERTICAL));
        chosenRelationPanel.add(new JButton(new EditChosenRelationAction(chosenRelation)), GBC.eol().fill(GBC.VERTICAL));

        rcPanel.add(chosenRelationPanel, BorderLayout.NORTH);

        roleBox.addPropertyChangeListener("enabled", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean showRoleBox = roleBox.isEnabled();
                roleBox.setVisible(showRoleBox);
                chosenRelationComponent.setVisible(!showRoleBox);
            }
        });

        sortAndFixAction.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                sortAndFixButton.setVisible(sortAndFixAction.isEnabled());
            }
        });
        sortAndFixButton.setVisible(false);

        downloadChosenRelationAction.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                downloadButton.setVisible(downloadChosenRelationAction.isEnabled());
            }
        });
        downloadButton.setVisible(false);
        if (Config.getPref().getBoolean(PREF_PREFIX + ".hidetopline", false)) {
            chosenRelationPanel.setVisible(false);
        }

        // [+][Multi] [X]Adm [X]Tags [X]1
        JPanel bottomLine = new JPanel(new GridBagLayout());
        createRelationAction = new CreateRelationAction(chosenRelation);
        bottomLine.add(new JButton(createRelationAction), GBC.std());
        createMultipolygonAction = new CreateMultipolygonAction(chosenRelation);
        final JButton multipolygonButton = new JButton(createMultipolygonAction);
        bottomLine.add(multipolygonButton, GBC.std());
        //        bottomLine.add(sizeButton(new JButton(new MultipolygonSettingsAction()), 16, 0), GBC.std().fill(GBC.VERTICAL));
        bottomLine.add(Box.createHorizontalGlue(), GBC.std().fill());
        findRelationAction = new FindRelationAction(chosenRelation);
        bottomLine.add(new JButton(findRelationAction), GBC.eol());
        rcPanel.add(sizeButton(bottomLine, 0, 24), BorderLayout.SOUTH);

        multipolygonButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    multiPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        createLayout(rcPanel, false, null);
    }

    private static final Color CHOSEN_RELATION_COLOR = new Color(255, 255, 128);

    private void configureRelationsTable(final JTable relationsTable) {
        relationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsTable.setTableHeader(null);
        relationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int row = relationsTable.rowAtPoint(p);
                if (SwingUtilities.isLeftMouseButton(e) && row >= 0) {
                    Relation relation = (Relation) relationsData.getValueAt(row, 0);
                    if (e.getClickCount() > 1) {
                        MainApplication.getLayerManager().getEditLayer().data.setSelected(relation);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            public void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = e.getPoint();
                    int row = relationsTable.rowAtPoint(p);
                    if (row > -1) {
                        Relation relation = (Relation) relationsData.getValueAt(row, 0);
                        JPopupMenu menu = chosenRelation.isSame(relation) ? popupMenu
                                : new ChosenRelationPopupMenu(new StaticChosenRelation(relation));
                        menu.show(relationsTable, p.x, p.y-5);
                    }
                }
            }
        });

        TableColumnModel columns = relationsTable.getColumnModel();
        columns.getColumn(0).setCellRenderer(new PrimitiveRenderer() {
            @Override
            protected String getComponentToolTipText(IPrimitive value) {
                return null;
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value instanceof Relation && chosenRelation.isSame(value)) {
                    c.setBackground(CHOSEN_RELATION_COLOR);
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }

        });
        columns.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && chosenRelation.isSame(table.getValueAt(row, 0))) {
                    c.setBackground(CHOSEN_RELATION_COLOR);
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        });
        columns.getColumn(1).setPreferredWidth(40);
        columns.getColumn(0).setPreferredWidth(220);
        relationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = relationsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    chosenRelation.set((Relation) relationsData.getValueAt(selectedRow, 0));
                    relationsTable.clearSelection();
                }
            }
        });
    }

    private JComponent sizeButton(JComponent b, int width, int height) {
        Dimension pref = b.getPreferredSize();
        b.setPreferredSize(new Dimension(width <= 0 ? pref.width : width, height <= 0 ? pref.height : height));
        return b;
    }

    @Override
    public void hideNotify() {
        SelectionEventManager.getInstance().removeSelectionListener(this);
        MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
        DatasetEventManager.getInstance().removeDatasetListener(chosenRelation);
        chosenRelation.clear();
    }

    @Override
    public void showNotify() {
        SelectionEventManager.getInstance().addSelectionListenerForEdt(this);
        MainApplication.getLayerManager().addActiveLayerChangeListener(this);
        DatasetEventManager.getInstance().addDatasetListener(chosenRelation, FireMode.IN_EDT);
    }

    public ChosenRelation getChosenRelation() {
        return chosenRelation;
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        if (chosenRelationPanel != null && Config.getPref().getBoolean(PREF_PREFIX + ".hidetopline", false)) {
            chosenRelationPanel.setVisible(newRelation != null);
        }
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds != null) {
            doSelectionChanged(ds.getSelected());
        }
        roleBoxModel.update();
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        doSelectionChanged(event.getSelection());
    }

    private void doSelectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        if (!isVisible() || relationsData == null)
            return;
        roleBoxModel.update();
        // repopulate relations table
        relationsData.setRowCount(0);
        sortAndFixAction.chosenRelationChanged(chosenRelation.get(), chosenRelation.get());
        if (newSelection == null)
            return;

        Set<Relation> relations = new TreeSet<>(
                DefaultNameFormatter.getInstance().getRelationComparator());
        for (OsmPrimitive element : newSelection) {
            for (OsmPrimitive ref : element.getReferrers()) {
                if (ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted()) {
                    relations.add((Relation) ref);
                }
            }
        }

        for (Relation rel : relations) {
            String role = null;
            for (RelationMember m : rel.getMembers()) {
                for (OsmPrimitive element : newSelection) {
                    if (m.getMember().equals(element)) {
                        if (role == null) {
                            role = m.getRole();
                        } else if (!role.equals(m.getRole())) {
                            role = tr("<different>");
                            break;
                        }
                    }
                }
            }
            relationsData.addRow(new Object[] {rel, role == null ? "" : role});
        }
        for (OsmPrimitive element : newSelection) {
            if (element instanceof Relation && !chosenRelation.isSame(element)) {
                relationsData.addRow(new Object[] {element, ""});
            }
        }
    }

    private void updateSelection() {
        if (MainApplication.getLayerManager().getEditDataSet() == null) {
            doSelectionChanged(Collections.<OsmPrimitive>emptyList());
        } else {
            doSelectionChanged(MainApplication.getLayerManager().getEditDataSet().getSelected());
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        updateSelection();
    }

    @Override
    public void destroy() {
        enterRoleAction.destroy();
        findRelationAction.destroy();
        createMultipolygonAction.destroy();
        createRelationAction.destroy();
        addRemoveMemberAction.destroy();
        MainApplication.getLayerManager().removeActiveLayerChangeListener(chosenRelation);
        super.destroy();
    }

    private static final String POSSIBLE_ROLES_FILE = "relcontext/possible_roles.txt";
    private static final Map<String, List<String>> possibleRoles = loadRoles();

    private static Map<String, List<String>> loadRoles() {
        Map<String, List<String>> result = new HashMap<>();

        ClassLoader classLoader = RelContextDialog.class.getClassLoader();
        try (InputStream possibleRolesStream = classLoader.getResourceAsStream(POSSIBLE_ROLES_FILE);
                BufferedReader r = new BufferedReader(new InputStreamReader(possibleRolesStream, StandardCharsets.UTF_8));
                ) {
            while (r.ready()) {
                String line = r.readLine();
                String[] typeAndRoles = line.split(":", 2);
                if (typeAndRoles.length == 2 && typeAndRoles[1].length() > 0) {
                    String type = typeAndRoles[0].trim();
                    StringTokenizer t = new StringTokenizer(typeAndRoles[1], " ,;\"");
                    List<String> roles = new ArrayList<>();
                    while (t.hasMoreTokens()) {
                        roles.add(t.nextToken());
                    }
                    result.put(type, roles);
                }
            }
        } catch (Exception e) {
            Logging.error("[RelToolbox] Error reading possible roles file.");
            Logging.error(e);
        }
        return result;
    }

    private String askForRoleName() {
        JPanel panel = new JPanel(new GridBagLayout());

        List<String> items = new ArrayList<>();
        for (String role : roleBoxModel.getRoles()) {
            if (role.length() > 1) {
                items.add(role);
            }
        }
        final AutoCompletingComboBox role = new AutoCompletingComboBox();
        role.setPossibleItems(items);
        role.setEditable(true);

        panel.add(new JLabel(tr("Role")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(role, GBC.eol().fill(GBC.HORIZONTAL));

        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                role.requestFocusInWindow();
                role.getEditor().selectAll();
            }
        };
        final JDialog dlg = optionPane.createDialog(MainApplication.getMainFrame(), tr("Specify role"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        role.getEditor().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION))
            return null;

        return role.getEditor().getItem().toString().trim();
    }

    private class ChosenRelationMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isControlDown() || !(e.getComponent() instanceof JComboBox)) // do not use left click handler on combo box
                if (SwingUtilities.isLeftMouseButton(e) && chosenRelation.get() != null
                && MainApplication.getLayerManager().getEditLayer() != null) {
                    MainApplication.getLayerManager().getEditLayer().data.setSelected(chosenRelation.get());
                }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger() && chosenRelation.get() != null) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY() - 5);
            }
        }
    }

    private static class ChosenRelationPopupMenu extends JPopupMenu {
        ChosenRelationPopupMenu(ChosenRelation chosenRelation) {
            add(new SelectMembersAction(chosenRelation));
            add(new SelectRelationAction(chosenRelation));
            add(new DuplicateChosenRelationAction(chosenRelation));
            add(new DeleteChosenRelationAction(chosenRelation));
            add(new DownloadParentsAction(chosenRelation));
            add(new ReconstructPolygonAction(chosenRelation));
            add(new ReconstructRouteAction(chosenRelation));
            addSeparator();
            add(new SelectInRelationPanelAction(chosenRelation));
            add(new RelationHelpAction(chosenRelation));
        }
    }

    protected void applyRoleToSelection(String role) {
        if (chosenRelation != null && chosenRelation.get() != null
                && MainApplication.getLayerManager().getEditDataSet() != null
                && !MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {
            Collection<OsmPrimitive> selected = MainApplication.getLayerManager().getEditDataSet().getSelected();
            Relation r = chosenRelation.get();
            List<Command> commands = new ArrayList<>();
            for (int i = 0; i < r.getMembersCount(); i++) {
                RelationMember m = r.getMember(i);
                if (selected.contains(m.getMember())) {
                    if (!role.equals(m.getRole())) {
                        commands.add(new ChangeRelationMemberRoleCommand(r, i, role));
                    }
                }
            }
            if (!commands.isEmpty()) {
                //                UndoRedoHandler.getInstance().add(new ChangeCommand(chosenRelation.get(), r));
                UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Change relation member roles to {0}", role), commands));
            }
        }
    }

    private static class RelationTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Relation.class : String.class;
        }
    }

    private static class MultipolygonSettingsPopup extends JPopupMenu implements ActionListener {
        MultipolygonSettingsPopup() {
            addMenuItem("boundary", tr("Create administrative boundary relations"));
            addMenuItem("boundaryways", tr("Add tags boundary and admin_level to boundary relation ways"));
            addMenuItem("tags", tr("Move area tags from contour to relation"));
            addMenuItem("alltags", tr("When moving tags, consider even non-repeating ones"));
            addMenuItem("allowsplit", tr("Always split ways of neighbouring multipolygons"));
        }

        protected final JCheckBoxMenuItem addMenuItem(String property, String title) {
            String fullProperty = PREF_PREFIX + ".multipolygon." + property;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(tr(title));
            item.setSelected(Config.getPref().getBoolean(fullProperty, CreateMultipolygonAction.getDefaultPropertyValue(property)));
            item.setActionCommand(fullProperty);
            item.addActionListener(this);
            add(item);
            return item;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String property = e.getActionCommand();
            if (property != null && property.length() > 0 && e.getSource() instanceof JCheckBoxMenuItem) {
                boolean value = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                Config.getPref().putBoolean(property, value);
                show(getInvoker(), getX(), getY());
            }
        }
    }

    private class EnterRoleAction extends JosmAction implements ChosenRelationListener {

        EnterRoleAction() {
            super(tr("Change role"), (String) null, tr("Enter role for selected members"),
                    Shortcut.registerShortcut("reltoolbox:changerole", tr("Relation Toolbox: {0}", tr("Enter role for selected members")),
                            KeyEvent.VK_R, Shortcut.ALT_CTRL), false, "relcontext/enterrole", true);
            chosenRelation.addChosenRelationListener(this);
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (roleBoxModel.membersRole != null) {
                String role = askForRoleName();
                if (role != null) {
                    applyRoleToSelection(role);
                }
            }
        }

        @Override
        public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
            setEnabled(newRelation != null);
        }
    }

    private class RoleComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
        private List<String> roles = new ArrayList<>();
        private int selectedIndex = -1;
        private JComboBox<String> combobox;
        private String membersRole;
        private final String EMPTY_ROLE = tr("<empty>");
        private final String ANOTHER_ROLE = tr("another...");

        RoleComboBoxModel(JComboBox<String> combobox) {
            super();
            this.combobox = combobox;
            update();
        }

        public void update() {
            membersRole = getSelectedMembersRoleIntl();
            if (membersRole == null) {
                if (combobox.isEnabled()) {
                    combobox.setEnabled(false);
                }
                return;
            }
            if (!combobox.isEnabled()) {
                combobox.setEnabled(true);
            }

            List<String> items = new ArrayList<>();
            if (chosenRelation != null && chosenRelation.get() != null) {
                if (chosenRelation.isMultipolygon()) {
                    items.add("outer");
                    items.add("inner");
                }
                if (chosenRelation.get().get("type") != null) {
                    List<String> values = possibleRoles.get(chosenRelation.get().get("type"));
                    if (values != null) {
                        items.addAll(values);
                    }
                }
                for (RelationMember m : chosenRelation.get().getMembers()) {
                    if (m.getRole().length() > 0 && !items.contains(m.getRole())) {
                        items.add(m.getRole());
                    }
                }
            }
            items.add(EMPTY_ROLE);
            if (!items.contains(membersRole)) {
                items.add(0, membersRole);
            }
            items.add(ANOTHER_ROLE);
            roles = Collections.unmodifiableList(items);

            if (membersRole != null) {
                setSelectedItem(membersRole);
            } else {
                fireContentsChanged(this, -1, -1);
            }
            combobox.repaint();
        }

        public String getSelectedMembersRole() {
            return membersRole == EMPTY_ROLE ? "" : membersRole;
        }

        public boolean isAnotherRoleSelected() {
            return getSelectedRole() != null && getSelectedRole().equals(ANOTHER_ROLE);
        }

        private String getSelectedMembersRoleIntl() {
            String role = null;
            if (chosenRelation != null && chosenRelation.get() != null
                    && MainApplication.getLayerManager().getEditDataSet() != null
                    && !MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {
                Collection<OsmPrimitive> selected = MainApplication.getLayerManager().getEditDataSet().getSelected();
                for (RelationMember m : chosenRelation.get().getMembers()) {
                    if (selected.contains(m.getMember())) {
                        if (role == null) {
                            role = m.getRole();
                        } else if (m.getRole() != null && !role.equals(m.getRole())) {
                            role = tr("<different>");
                            break;
                        }
                    }
                }
            }
            return role == null ? null : role.length() == 0 ? EMPTY_ROLE : role;
        }

        public List<String> getRoles() {
            return roles;
        }

        @Override
        public int getSize() {
            return roles.size();
        }

        @Override
        public String getElementAt(int index) {
            return getRole(index);
        }

        public String getRole(int index) {
            return roles.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            int newIndex = anItem == null ? -1 : roles.indexOf(anItem);
            if (newIndex != selectedIndex) {
                selectedIndex = newIndex;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedIndex < 0 || selectedIndex >= getSize() ? null : getRole(selectedIndex);
        }

        public String getSelectedRole() {
            String role = selectedIndex < 0 || selectedIndex >= getSize() ? null : getRole(selectedIndex);
            return role != null && role.equals(EMPTY_ROLE) ? "" : role;
        }
    }
}
