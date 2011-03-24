package relcontext;

import java.awt.Dimension;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.NameFormatter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import relcontext.actions.*;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The new, advanced relation editing panel.
 * 
 * @author Zverik
 */
public class RelContextDialog extends ToggleDialog implements EditLayerChangeListener, ChosenRelationListener, SelectionChangedListener {

    private final DefaultTableModel relationsData;
    private ChosenRelation chosenRelation;
    private JPanel chosenRelationPanel;
    private ChosenRelationPopupMenu popupMenu;
    private JLabel crRoleIndicator;
    private AutoCompletingComboBox roleBox;
    private String lastSelectedRole;

    public RelContextDialog() {
        super(tr("Advanced Relation Editor"), "icon_relcontext",
                tr("Opens advanced relation/multipolygon editor panel"),
                Shortcut.registerShortcut("view:relcontext", tr("Toggle: {0}", tr("Open Relation Editor")),
                KeyEvent.VK_R, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT), 150, true);

        // todo: shrink all panels to ~20 pixels

        JPanel rcPanel = new JPanel(new BorderLayout());

        chosenRelation = new ChosenRelation();
        chosenRelation.addChosenRelationListener(this);
        MapView.addEditLayerChangeListener(chosenRelation);

        relationsData = new RelationTableModel();
        relationsData.setColumnIdentifiers(new String[] {tr("Member Of"), tr("Role")});
        final JTable relationsTable = new JTable(relationsData);
        configureRelationsTable(relationsTable);
        rcPanel.add(new JScrollPane(relationsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        chosenRelationPanel = new JPanel(new BorderLayout());

        // [^] roles [new role][V][Apply]
        final JPanel rolePanel = new JPanel(new GridBagLayout());
        final JButton toggleRolePanelButtonTop = new JButton(new TogglePanelAction(rolePanel) {
            @Override
            protected void init() {
                putValue(Action.SMALL_ICON, ImageProvider.get("svpDown"));
                putValue(Action.SHORT_DESCRIPTION, tr("Show role panel"));
            }
        });
        final JButton toggleRolePanelButtonIn = new JButton(new TogglePanelAction(rolePanel) {
            @Override
            protected void init() {
                putValue(Action.SMALL_ICON, ImageProvider.get("svpUp"));
                putValue(Action.SHORT_DESCRIPTION, tr("Hide role panel"));
            }
        });
        rolePanel.add(sizeButton(toggleRolePanelButtonIn, 16, 20), GBC.std());
        crRoleIndicator = new JLabel();
        rolePanel.add(crRoleIndicator, GBC.std().insets(5, 0, 5, 0));
        roleBox = new AutoCompletingComboBox();
        roleBox.setEditable(false);
        rolePanel.add(roleBox, GBC.std().fill(GBC.HORIZONTAL));
        rolePanel.add(sizeButton(new JButton(new ApplyNewRoleAction()), 40, 20), GBC.std());
        rolePanel.add(sizeButton(new JButton(new EnterNewRoleAction()), 40, 20), GBC.eol());
//        rolePanel.setVisible(false); // todo: take from preferences

        // [±][X] relation U [AZ][Down][Edit]
        JPanel topLine = new JPanel(new GridBagLayout());
        topLine.add(new JButton(new AddRemoveMemberAction(chosenRelation)), GBC.std());
        topLine.add(sizeButton(toggleRolePanelButtonTop, 16, 24), GBC.std());
        topLine.add(sizeButton(new JButton(new ClearChosenRelationAction(chosenRelation)), 32, 0), GBC.std());
        final ChosenRelationComponent chosenRelationComponent = new ChosenRelationComponent(chosenRelation);
        chosenRelationComponent.addMouseListener(new ChosenRelationMouseAdapter());
        topLine.add(chosenRelationComponent, GBC.std().fill().insets(5, 0, 5, 0));
        final Action sortAndFixAction = new SortAndFixAction(chosenRelation);
        final JButton sortAndFixButton = (JButton) sizeButton(new JButton(sortAndFixAction), 32, 24);
        topLine.add(sortAndFixButton, GBC.std());
        final Action downloadChosenRelationAction = new DownloadChosenRelationAction(chosenRelation);
        final JButton downloadButton = (JButton) sizeButton(new JButton(downloadChosenRelationAction), 32, 24);
        topLine.add(downloadButton, GBC.std());
        topLine.add(new JButton(new EditChosenRelationAction(chosenRelation)), GBC.eol().fill(GBC.VERTICAL));

        chosenRelationPanel.add(topLine, BorderLayout.CENTER);
        chosenRelationPanel.add(rolePanel, BorderLayout.SOUTH);
        rcPanel.add(chosenRelationPanel, BorderLayout.NORTH);

        rolePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden( ComponentEvent e ) {
                toggleRolePanelButtonTop.setVisible(true);
            }

            @Override
            public void componentShown( ComponentEvent e ) {
                toggleRolePanelButtonTop.setVisible(false);
            }
        });
        toggleRolePanelButtonTop.setVisible(!rolePanel.isVisible());

        sortAndFixAction.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                sortAndFixButton.setVisible(sortAndFixAction.isEnabled());
            }
        });
        sortAndFixButton.setVisible(false);

        downloadChosenRelationAction.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                downloadButton.setVisible(downloadChosenRelationAction.isEnabled());
            }
        });
        downloadButton.setVisible(false);
        chosenRelationPanel.setVisible(false);

        // [+][Multi] [X]Adm [X]Tags [X]1
        JPanel bottomLine = new JPanel(new GridBagLayout());
        bottomLine.add(new JButton(new CreateRelationAction(chosenRelation)), GBC.std());
        bottomLine.add(new JButton(new CreateMultipolygonAction(chosenRelation)), GBC.std());
        bottomLine.add(Box.createHorizontalGlue(), GBC.std().fill());
        bottomLine.add(new JButton(new FindRelationAction(chosenRelation)), GBC.eol());
        rcPanel.add(sizeButton(bottomLine, 0, 24), BorderLayout.SOUTH);

        add(rcPanel, BorderLayout.CENTER);

        popupMenu = new ChosenRelationPopupMenu();
    }

    private void configureRelationsTable( final JTable relationsTable ) {
        relationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsTable.setTableHeader(null);
        TableColumnModel columns = relationsTable.getColumnModel();
        columns.getColumn(0).setCellRenderer(new OsmPrimitivRenderer() {
            @Override
            protected String getComponentToolTipText( OsmPrimitive value ) {
                return null;
            }
        });
        columns.getColumn(1).setPreferredWidth(40);
        columns.getColumn(0).setPreferredWidth(220);
        relationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
                int selectedRow = relationsTable.getSelectedRow();
                if( selectedRow >= 0 ) {
                    chosenRelation.set((Relation)relationsData.getValueAt(selectedRow, 0));
                    relationsTable.clearSelection();
                }
            }
        });
    }

    private JComponent sizeButton( JComponent b, int width, int height ) {
        Dimension pref = b.getPreferredSize();
        b.setPreferredSize(new Dimension(width <= 0 ? pref.width : width, height <= 0 ? pref.height : height));
        return b;
    }

    @Override
    public void hideNotify() {
        SelectionEventManager.getInstance().removeSelectionListener(this);
        MapView.removeEditLayerChangeListener(this);
        DatasetEventManager.getInstance().removeDatasetListener(chosenRelation);
    }

    @Override
    public void showNotify() {
        SelectionEventManager.getInstance().addSelectionListener(this, FireMode.IN_EDT_CONSOLIDATED);
        MapView.addEditLayerChangeListener(this);
        DatasetEventManager.getInstance().addDatasetListener(chosenRelation, FireMode.IN_EDT);
    }

    public ChosenRelation getChosenRelation() {
        return chosenRelation;
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        if( chosenRelationPanel != null )
            chosenRelationPanel.setVisible(newRelation != null);
        if( Main.main.getCurrentDataSet() != null )
            selectionChanged(Main.main.getCurrentDataSet().getSelected());
        updateRoleIndicator();
        updateRoleAutoCompletionList();
        // ?
    }

    private void updateRoleIndicator() {
        if( crRoleIndicator == null )
            return;
        String role = "";
        if( chosenRelation != null && chosenRelation.get() != null && Main.main.getCurrentDataSet() != null && !Main.main.getCurrentDataSet().selectionEmpty() ) {
            Collection<OsmPrimitive> selected = Main.main.getCurrentDataSet().getSelected();
            for( RelationMember m : chosenRelation.get().getMembers() ) {
                if( selected.contains(m.getMember()) ) {
                    if( role.length() == 0 && m.getRole() != null )
                        role = m.getRole();
                    else if( !role.equals(m.getRole()) ) {
                        role = tr("<different>");
                        break;
                    }
                }
            }
        }
        crRoleIndicator.setText(role);
    }

    public void selectionChanged( Collection<? extends OsmPrimitive> newSelection ) {
        if( !isVisible() || relationsData == null )
            return;
        updateRoleIndicator();
        // repopulate relations table
        relationsData.setRowCount(0);
        if( newSelection == null )
            return;

        final NameFormatter formatter = DefaultNameFormatter.getInstance();
        Set<Relation> relations = new TreeSet<Relation>(new Comparator<Relation>() {
            public int compare( Relation r1, Relation r2 ) {
                return r1.getDisplayName(formatter).compareTo(r2.getDisplayName(formatter));
            }
        });
        for( OsmPrimitive element : newSelection ) {
            for( OsmPrimitive ref : element.getReferrers() ) {
                if( ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted() ) {
                    relations.add((Relation) ref);
                }
            }
        }

        for( Relation rel : relations ) {
            String role = null;
            for( RelationMember m : rel.getMembers() ) {
                for( OsmPrimitive element : newSelection ) {
                    if( m.getMember().equals(element) ) {
                        if( role == null )
                            role = m.getRole();
                        else if( !role.equals(m.getRole()) ) {
                            role = tr("<different>");
                            break;
                        }
                    }
                }
            }
            relationsData.addRow(new Object[] {rel, role == null ? "" : role});
        }
    }

    private void updateSelection() {
        if (Main.main.getCurrentDataSet() == null) {
            selectionChanged(Collections.<OsmPrimitive>emptyList());
        } else {
            selectionChanged(Main.main.getCurrentDataSet().getSelected());
        }
    }

    public void editLayerChanged( OsmDataLayer oldLayer, OsmDataLayer newLayer ) {
        updateSelection();
    }

    private static final Map<String, String[]> possibleRoles = new HashMap<String, String[]>();
    static {
        possibleRoles.put("boundary", new String[] {"admin_centre", "label", "subarea"});
        possibleRoles.put("route", new String[] {"forward", "backward", "stop", "platform"});
        possibleRoles.put("restriction", new String[] {"from", "to", "via", "location_hint"});
        possibleRoles.put("enforcement", new String[] {"device", "from", "to", "force"});
        possibleRoles.put("destination_sign", new String[] {"to", "from", "intersection", "sign"});
        possibleRoles.put("site", new String[] {"perimeter", "entrance", "label"});
        possibleRoles.put("bridge", new String[] {"across", "under", "outline", "edge"});
        possibleRoles.put("tunnel", new String[] {"through", "outline", "edge"});
        possibleRoles.put("surveillance", new String[] {"camera", "extent", "visible", "hidden"});
    }

    private void updateRoleAutoCompletionList() {
        String currentRole = roleBox.getSelectedItem() == null ? null : ((AutoCompletionListItem)roleBox.getSelectedItem()).getValue();
        List<String> items = new ArrayList<String>();
        items.add(" ");
        if( chosenRelation != null && chosenRelation.get() != null ) {
            if( chosenRelation.isMultipolygon() ) {
                items.add("outer");
                items.add("inner");
            }
            if( chosenRelation.get().get("type") != null ) {
                String[] values = possibleRoles.get(chosenRelation.get().get("type"));
                if( values != null )
                    items.addAll(Arrays.asList(values));
            }
            for( RelationMember m : chosenRelation.get().getMembers() )
                if( !items.contains(m.getRole()) )
                    items.add(m.getRole());
        }
        if( currentRole != null && currentRole.length() > 1 ) {
            lastSelectedRole = currentRole;
        }
        roleBox.setPossibleItems(items);
        if( lastSelectedRole != null && items.contains(lastSelectedRole) )
            roleBox.setSelectedItem(lastSelectedRole);
        // todo: do we really want empty role as default one? Maybe, store last selected role in preferences
    }

    private String askForRoleName() {
        JPanel panel = new JPanel(new GridBagLayout());

        final AutoCompletingComboBox role = new AutoCompletingComboBox();
        List<AutoCompletionListItem> items = new ArrayList<AutoCompletionListItem>();
        for( int i = 0; i < roleBox.getModel().getSize(); i++ )
            items.add((AutoCompletionListItem)roleBox.getModel().getElementAt(i));
        role.setPossibleACItems(items);
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
        final JDialog dlg = optionPane.createDialog(Main.parent, tr("Specify role"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        role.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if( answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION) ) {
            return null;
        }

        return role.getEditor().getItem().toString().trim();
    }

    private class ChosenRelationMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked( MouseEvent e ) {
            if( SwingUtilities.isLeftMouseButton(e) && chosenRelation.get() != null && Main.map.mapView.getEditLayer() != null ) {
                Main.map.mapView.getEditLayer().data.setSelected(chosenRelation.get());
            }
        }

        @Override
        public void mousePressed( MouseEvent e ) {
            checkPopup(e);
        }

        @Override
        public void mouseReleased( MouseEvent e ) {
            checkPopup(e);
        }

        private void checkPopup( MouseEvent e ) {
            if( e.isPopupTrigger() && chosenRelation.get() != null ) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY() - 5);
            }
        }
    }

    private class ChosenRelationPopupMenu extends JPopupMenu {
        public ChosenRelationPopupMenu() {
            add(new SelectMembersAction(chosenRelation));
            add(new DeleteChosenRelationAction(chosenRelation));
            add(new DownloadParentsAction(chosenRelation));
            addSeparator();
            add(new SelectInRelationPanelAction(chosenRelation));
            add(new RelationHelpAction(chosenRelation));
        }
    }

    private class TogglePanelAction extends AbstractAction {
        private JComponent component;

        public TogglePanelAction( JPanel panel ) {
            super();
            this.component = panel;
            init();
            if( getValue(Action.SMALL_ICON) == null )
                putValue(Action.NAME, "R");
        }

        protected void init() {}

        public void actionPerformed( ActionEvent e ) {
            component.setVisible(!component.isVisible());
        }
    }

    protected void applyRoleToSelection( String role ) {
        if( chosenRelation != null && chosenRelation.get() != null && Main.main.getCurrentDataSet() != null && !Main.main.getCurrentDataSet().selectionEmpty() ) {
            Collection<OsmPrimitive> selected = Main.main.getCurrentDataSet().getSelected();
            Relation r = new Relation(chosenRelation.get());
            boolean fixed = false;
            for( int i = 0; i < r.getMembersCount(); i++ ) {
                RelationMember m = r.getMember(i);
                if( selected.contains(m.getMember()) ) {
                    if( !role.equals(m.getRole()) ) {
                        r.setMember(i, new RelationMember(role, m.getMember()));
                        fixed = true;
                    }
                }
            }
            if( fixed )
                Main.main.undoRedo.add(new ChangeCommand(chosenRelation.get(), r));
        }
    }

    private class ApplyNewRoleAction extends AbstractAction {
        public ApplyNewRoleAction() {
            super(null, ImageProvider.get("apply"));
            putValue(Action.SHORT_DESCRIPTION, tr("Apply chosen role to selected relation members"));
        }

        public void actionPerformed( ActionEvent e ) {
            Object selectedItem = roleBox == null ? null : roleBox.getSelectedItem();
            if( selectedItem != null ) {
                if( selectedItem instanceof AutoCompletionListItem )
                    selectedItem = ((AutoCompletionListItem)selectedItem).getValue();
                applyRoleToSelection(selectedItem.toString().trim());
            }
        }
    }

    private class EnterNewRoleAction extends AbstractAction {
        public EnterNewRoleAction() {
            super();
            putValue(Action.NAME, "…");
//            putValue(SMALL_ICON, ImageProvider.get("dialogs/mappaint", "pencil"));
            putValue(SHORT_DESCRIPTION, tr("Enter new role for selected relation members"));
        }

        public void actionPerformed( ActionEvent e ) {
            String role = askForRoleName();
            if( role != null )
                applyRoleToSelection(role);
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
}
