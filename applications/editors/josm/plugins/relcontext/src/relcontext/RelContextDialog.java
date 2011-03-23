package relcontext;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

import java.util.*;
import javax.swing.*;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.tools.GBC;
import relcontext.actions.*;

/**
 * The new, advanced relation editing panel.
 * 
 * @author Zverik
 */
public class RelContextDialog extends ToggleDialog implements EditLayerChangeListener, ChosenRelationListener, SelectionChangedListener {
    private JList relationsList;
    private final DefaultListModel relationsData;
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
                KeyEvent.VK_R, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT), 150);

        JPanel rcPanel = new JPanel(new BorderLayout());

        chosenRelation = new ChosenRelation();
        chosenRelation.addChosenRelationListener(this);
        MapView.addEditLayerChangeListener(chosenRelation);

        relationsData = new DefaultListModel();
        relationsList = new JList(relationsData);
        relationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsList.setCellRenderer(new OsmPrimitivRenderer() {
            @Override
            protected String getComponentToolTipText( OsmPrimitive value ) {
                return null;
            }
        });
        relationsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( Main.main.getEditLayer() == null ) {
                    return;
                }
                chosenRelation.set((Relation)relationsList.getSelectedValue());
                relationsList.clearSelection();
            }
        });
        rcPanel.add(new JScrollPane(relationsList), BorderLayout.CENTER);

        chosenRelationPanel = new JPanel(new BorderLayout());

        // [^] roles [new role][V][Apply]
        final JPanel rolePanel = new JPanel(new GridBagLayout());
        final JButton toggleRolePanelButtonTop = new JButton(new TogglePanelAction(rolePanel));
        final JButton toggleRolePanelButtonIn = new JButton(new TogglePanelAction(rolePanel));
        rolePanel.add(toggleRolePanelButtonIn, GBC.std());
        crRoleIndicator = new JLabel();
        rolePanel.add(crRoleIndicator, GBC.std().insets(5, 0, 5, 0));

        // autocompleting role chooser
        roleBox = new AutoCompletingComboBox();
        final Action applyNewRoleAction = new ApplyNewRoleAction();
//        roleBox.getEditor().addActionListener(applyNewRoleAction);
        roleBox.setEditable(false);
        rolePanel.add(roleBox, GBC.std().fill(GBC.HORIZONTAL));
        rolePanel.add(new JButton(applyNewRoleAction), GBC.eol());
        rolePanel.setVisible(false);

        // [±][X] relation U [AZ][Down][Edit]
        JPanel topLine = new JPanel(new BorderLayout());
        JPanel topLeftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftButtons.add(new JButton(new AddRemoveMemberAction(chosenRelation)));
        topLeftButtons.add(toggleRolePanelButtonTop);
        topLeftButtons.add(new JButton(new ClearChosenRelationAction(chosenRelation)));
        topLine.add(topLeftButtons, BorderLayout.WEST);
        final ChosenRelationComponent chosenRelationComponent = new ChosenRelationComponent(chosenRelation);
        chosenRelationComponent.addMouseListener(new ChosenRelationMouseAdapter());
        topLine.add(chosenRelationComponent, BorderLayout.CENTER);
        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final Action sortAndFixAction = new SortAndFixAction(chosenRelation);
        final JButton sortAndFixButton = new JButton(sortAndFixAction);
        topRightButtons.add(sortAndFixButton);
        final Action downloadChosenRelationAction = new DownloadChosenRelationAction(chosenRelation);
        final JButton downloadButton = new JButton(downloadChosenRelationAction);
        topRightButtons.add(downloadButton);
        topRightButtons.add(new JButton(new EditChosenRelationAction(chosenRelation)));
        topLine.add(topRightButtons, BorderLayout.EAST);

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
        rcPanel.add(bottomLine, BorderLayout.SOUTH);

        add(rcPanel, BorderLayout.CENTER);

        popupMenu = new ChosenRelationPopupMenu();
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
        if( oldRelation != newRelation && Main.main.getCurrentDataSet() != null )
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
        relationsData.clear();
        if( newSelection == null )
            return;
        Set<Relation> rels = new HashSet<Relation>();
        for( OsmPrimitive element : newSelection ) {
            for( OsmPrimitive ref : element.getReferrers() ) {
                if( ref instanceof Relation && !ref.isIncomplete() && !ref.isDeleted() ) {
                    rels.add((Relation) ref);
                }
            }
        }
        for( Relation rel : rels )
            relationsData.addElement(rel);
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
    {
        possibleRoles.put("boundary", new String[] {"admin_centre", "label", "subarea"});
        possibleRoles.put("route", new String[] {"forward", "backward", "stop", "platform"});
    }

    private void updateRoleAutoCompletionList() {
        List<String> items = new ArrayList<String>();
        items.add("");
        if( chosenRelation != null && chosenRelation.get() != null ) {
            if( chosenRelation.isMultipolygon() ) {
                items.add("outer");
                items.add("inner");
            }
            if( chosenRelation.get().get("type") != null ) {
                String[] values = possibleRoles.get(chosenRelation.get().get("type"));
                if( values != null )
                    for( String value : values )
                        items.add(value);
            }
        } else if( roleBox.getSelectedItem() != null ) {
            lastSelectedRole = ((AutoCompletionListItem)roleBox.getSelectedItem()).getValue();
        }
        roleBox.setPossibleItems(items);
        if( lastSelectedRole != null && items.contains(lastSelectedRole) )
            roleBox.setSelectedItem(lastSelectedRole);
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
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class ChosenRelationPopupMenu extends JPopupMenu {
        public ChosenRelationPopupMenu() {
            add(new SelectMembersAction(chosenRelation));
            add(new DeleteChosenRelationAction(chosenRelation));
        }
    }

    private class TogglePanelAction extends AbstractAction {
        private JComponent component;

        public TogglePanelAction( JPanel panel ) {
            super("R");
            this.component = panel;
        }

        public void actionPerformed( ActionEvent e ) {
            component.setVisible(!component.isVisible());
        }
    }

    private class ApplyNewRoleAction extends AbstractAction {
        public ApplyNewRoleAction() {
            super("OK");
        }

        public void actionPerformed( ActionEvent e ) {
            Object selectedItem = roleBox == null ? null : roleBox.getSelectedItem();
            if( selectedItem != null && chosenRelation != null && chosenRelation.get() != null && Main.main.getCurrentDataSet() != null && !Main.main.getCurrentDataSet().selectionEmpty() ) {
//                String role = roleBox.getEditor().getItem().toString().trim();
                if( selectedItem instanceof AutoCompletionListItem )
                    selectedItem = ((AutoCompletionListItem)selectedItem).getValue();
                String role = selectedItem.toString().trim();
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
    }
}
