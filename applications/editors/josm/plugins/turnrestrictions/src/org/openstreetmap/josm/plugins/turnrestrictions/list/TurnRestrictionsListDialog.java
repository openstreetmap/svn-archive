package org.openstreetmap.josm.plugins.turnrestrictions.list;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionEditor;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionEditorManager;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This is the toggle dialog for turn restrictions. The user can switch between
 * two lists of turn restrictions:
 * <ol>
 *   <li>the list of turn restrictions in the current data set</li>
 *   <li>the list of turn restrictions related to OSM objects in the current selection</li>
 * </ol>
 * 
 */
public class TurnRestrictionsListDialog extends ToggleDialog{
    private static final Logger logger = Logger.getLogger(TurnRestrictionsListDialog.class.getName());

    /** checkbox for switching between the two list views */
    private JCheckBox cbInSelectionOnly;
    /** the view for the turn restrictions in the current data set */
    private TurnRestrictionsInDatasetView pnlTurnRestrictionsInDataSet;
    /** the view for the turn restrictions related to the current selection */
    private TurnRestrictionsInSelectionView pnlTurnRestrictionsInSelection;
    
    /** three actions */
    private NewAction actNew;
    private EditAction actEdit;
    private DeleteAction actDelete;  
    private SelectSelectedTurnRestrictions actSelectSelectedTurnRestrictions;
    private ZoomToAction actZoomTo;
    private SwitchListViewHandler switchListViewHandler;
    
    private AbstractTurnRestrictionsListView currentListView = null;
    
    /** the main content panel in this toggle dialog */
    private JPanel pnlContent;
    private PreferenceChangeHandler preferenceChangeHandler;
    
    @Override
    public void showNotify() {
        pnlTurnRestrictionsInDataSet.registerAsListener();      
        pnlTurnRestrictionsInSelection.registerAsListener();
        MapView.addEditLayerChangeListener(actNew);
        actNew.updateEnabledState();
        Main.pref.addPreferenceChangeListener(preferenceChangeHandler);
        preferenceChangeHandler.refreshIconSet();
    }

    @Override
    public void hideNotify() {
        pnlTurnRestrictionsInDataSet.unregisterAsListener();
        pnlTurnRestrictionsInSelection.unregisterAsListener();
        MapView.removeEditLayerChangeListener(actNew);
        Main.pref.removePreferenceChangeListener(preferenceChangeHandler);
    }

    /**
     * Builds the UI
     */
    protected void build() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        pnl.setBorder(null);
        pnl.add(cbInSelectionOnly = new JCheckBox(tr("Only participating in selection")));
        cbInSelectionOnly.setToolTipText(tr(
           "<html>Select to display turn restrictions related to object in the current selection only.<br>"
         + "Deselect to display all turn restrictions in the current data set.</html>"));

        pnlContent = new JPanel(new BorderLayout(0,0));
        pnlContent.setBorder(null);
        pnlContent.add(pnl,  BorderLayout.NORTH);

        actNew = new NewAction();
        actEdit = new EditAction();
        actDelete = new DeleteAction();
        actSelectSelectedTurnRestrictions = new SelectSelectedTurnRestrictions();
        actZoomTo = new ZoomToAction();

        createLayout(pnlContent, false, Arrays.asList(new SideButton[] {
            new SideButton(actNew, false /* don't show the name */),
            new SideButton(actEdit, false /* don't show the name */),
            new SideButton(actDelete, false /* don't show the name */)
        }));
        
        // create the two list views 
        pnlTurnRestrictionsInDataSet = new TurnRestrictionsInDatasetView();
        pnlTurnRestrictionsInSelection = new TurnRestrictionsInSelectionView();
        
        // wire the handler for switching between list views 
        switchListViewHandler = new SwitchListViewHandler();
        switchListViewHandler.activateListView(pnlTurnRestrictionsInDataSet);
        cbInSelectionOnly.addItemListener(switchListViewHandler);
        
        // wire the popup menu launcher to the two turn restriction lists  
        TurnRestrictionsPopupLauncher launcher = new TurnRestrictionsPopupLauncher();
        pnlTurnRestrictionsInDataSet.getList().addMouseListener(launcher);
        pnlTurnRestrictionsInSelection.getList().addMouseListener(launcher);
        
        preferenceChangeHandler = new PreferenceChangeHandler();
        
    }
    
    /**
     * Constructor
     */
    public TurnRestrictionsListDialog() {
        super(
                tr("Turn Restrictions"), 
                "turnrestrictions",
                tr("Display and manage turn restrictions in the current data set"),
                null, // no shortcut
                150   // default height
        );
        build();
        HelpUtil.setHelpContext(this, HelpUtil.ht("/Plugin/TurnRestrictions#TurnRestrictionToggleDialog"));
    }   
    
    /**
     * Switches between the two list view.
     */
    class SwitchListViewHandler implements ItemListener {
        public void activateListView(AbstractTurnRestrictionsListView view) {
            if (currentListView != null) {
                currentListView.removeListSelectionListener(actEdit);
                currentListView.removeListSelectionListener(actDelete);
                currentListView.removeListSelectionListener(actSelectSelectedTurnRestrictions);
                currentListView.removeListSelectionListener(actZoomTo);
                pnlContent.remove(currentListView);
            }
            pnlContent.add(view,BorderLayout.CENTER);
            currentListView = view;                     
            view.addListSelectionListener(actEdit);
            view.addListSelectionListener(actDelete);
            view.addListSelectionListener(actSelectSelectedTurnRestrictions);
            view.addListSelectionListener(actZoomTo);
            actEdit.updateEnabledState();
            actDelete.updateEnabledState();
            actSelectSelectedTurnRestrictions.updateEnabledState();
            actZoomTo.updateEnabledState();
            currentListView.revalidate();
            currentListView.repaint();          
        }

        public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case ItemEvent.SELECTED:
                activateListView(pnlTurnRestrictionsInSelection);
                break;
                
            case ItemEvent.DESELECTED:      
                activateListView(pnlTurnRestrictionsInDataSet);
                break;
            }
        }
    }
    
     /**
     * The edit action
     *
     */
    class EditAction extends AbstractAction implements ListSelectionListener{
        public EditAction() {
            putValue(SHORT_DESCRIPTION,tr("Open an editor for the selected turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "edit"));
            putValue(NAME, tr("Edit"));
            setEnabled(false);
        }
        protected Collection<RelationMember> getMembersForCurrentSelection(Relation r) {
            Collection<RelationMember> members = new HashSet<RelationMember>();
            Collection<OsmPrimitive> selection = Main.map.mapView.getEditLayer().data.getSelected();
            for (RelationMember member: r.getMembers()) {
                if (selection.contains(member.getMember())) {
                    members.add(member);
                }
            }
            return members;
        }

        public void launchEditor(Relation toEdit) {
            if (toEdit == null)
                return;
            OsmDataLayer layer = Main.main.getEditLayer();
            TurnRestrictionEditorManager manager = TurnRestrictionEditorManager.getInstance();
            TurnRestrictionEditor editor = manager.getEditorForRelation(layer, toEdit);
            if (editor != null) {
                editor.setVisible(true);
                editor.toFront();
            } else {
                editor = new TurnRestrictionEditor(
                        TurnRestrictionsListDialog.this, layer,toEdit);
                manager.positionOnScreen(editor);
                manager.register(layer, toEdit,editor);
                editor.setVisible(true);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            List<Relation> toEdit = currentListView.getModel().getSelectedTurnRestrictions();
            if (toEdit.size() != 1) return;
            launchEditor(toEdit.get(0));
        }

        public void updateEnabledState() {
            setEnabled(currentListView!= null && currentListView.getModel().getSelectedTurnRestrictions().size() == 1);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }

    /**
     * The delete action
     *
     */
    class DeleteAction extends AbstractAction implements ListSelectionListener {
        class AbortException extends Exception {}

        public DeleteAction() {
            putValue(SHORT_DESCRIPTION,tr("Delete the selected turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
            putValue(NAME, tr("Delete"));
            setEnabled(false);
        }

        protected void deleteRelation(Relation toDelete) {
            if (toDelete == null)
                return;
            org.openstreetmap.josm.actions.mapmode.DeleteAction.deleteRelation(
                    Main.main.getEditLayer(),
                    toDelete
            );
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) return;
            List<Relation> toDelete = currentListView.getModel().getSelectedTurnRestrictions();
            for (Relation r: toDelete) {
                deleteRelation(r);
            }
        }
        
        public void updateEnabledState() {
            setEnabled(currentListView != null && !currentListView.getModel().getSelectedTurnRestrictions().isEmpty());
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }

    /**
     * The action for creating a new turn restriction
     *
     */
     class NewAction extends AbstractAction implements EditLayerChangeListener{
        public NewAction() {
            putValue(SHORT_DESCRIPTION,tr("Create a new turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("new"));
            putValue(NAME, tr("New"));
            updateEnabledState();
        }

        public void run() {
             OsmDataLayer layer =  Main.main.getEditLayer();
             if (layer == null) return;
             Relation tr = new TurnRestrictionBuilder().buildFromSelection(layer);
             TurnRestrictionEditor editor = new TurnRestrictionEditor(TurnRestrictionsListDialog.this, layer, tr);
             TurnRestrictionEditorManager.getInstance().positionOnScreen(editor);             
             TurnRestrictionEditorManager.getInstance().register(layer, tr, editor);
             editor.setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            run();
        }

        public void updateEnabledState() {
            setEnabled(Main.main != null && Main.main.getEditLayer() != null);
        }

        public void editLayerChanged(OsmDataLayer oldLayer,
                OsmDataLayer newLayer) {
            updateEnabledState();
        }
    }
    
    /**
     * Sets the selection of the current data set to the currently selected 
     * turn restrictions.
     *
     */
    class SelectSelectedTurnRestrictions extends AbstractAction implements ListSelectionListener {
        class AbortException extends Exception {}

        public SelectSelectedTurnRestrictions() {
            putValue(SHORT_DESCRIPTION,tr("Set the current JOSM selection to the selected turn restrictions"));
            putValue(SMALL_ICON, ImageProvider.get("selectall"));
            putValue(NAME, tr("Select in current data layer"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) return;
            List<Relation> toSelect = currentListView.getModel().getSelectedTurnRestrictions();
            if (toSelect.isEmpty()) return;
            OsmDataLayer layer= Main.main.getEditLayer();
            if (layer == null) return;
            layer.data.setSelected(toSelect);
        }
        
        public void updateEnabledState() {
            setEnabled(currentListView != null && !currentListView.getModel().getSelectedTurnRestrictions().isEmpty());
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }
    
    /**
     * Sets the selection of the current data set to the currently selected 
     * turn restrictions.
     *
     */
    class ZoomToAction extends AbstractAction implements ListSelectionListener {
        class AbortException extends Exception {}

        public ZoomToAction() {
            putValue(SHORT_DESCRIPTION,tr("Zoom to the currently selected turn restrictions"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs/autoscale/selection"));
            putValue(NAME, tr("Zoom to"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) return;
            List<Relation> toSelect = currentListView.getModel().getSelectedTurnRestrictions();
            if (toSelect.isEmpty()) return;
            OsmDataLayer layer= Main.main.getEditLayer();
            if (layer == null) return;
            layer.data.setSelected(toSelect);
            new AutoScaleAction("selection").autoScale();
        }
        
        public void updateEnabledState() {
            setEnabled(currentListView != null && !currentListView.getModel().getSelectedTurnRestrictions().isEmpty());
        }

        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }
    
    /**
     * The launcher for the popup menu.
     * 
     */
    class TurnRestrictionsPopupLauncher extends PopupMenuLauncher {
        @Override
        public void launch(MouseEvent evt) {
            JList lst = currentListView.getList();
            if (lst.getSelectedIndices().length == 0) {
                int idx = lst.locationToIndex(evt.getPoint());
                if (idx >=0) {
                    lst.getSelectionModel().addSelectionInterval(idx, idx);
                }
            }
            TurnRestrictionsPopupMenu popup = new TurnRestrictionsPopupMenu();
            popup.show(lst, evt.getX(), evt.getY());
        }
    }

    /**
     * The popup menu 
     *
     */
    class TurnRestrictionsPopupMenu extends JPopupMenu {
        public TurnRestrictionsPopupMenu() {
            add(actNew);
            add(actEdit);
            add(actDelete);
            addSeparator();
            add(actSelectSelectedTurnRestrictions);
            add(actZoomTo);
        }
    }
    
    /**
     * Listens the changes of the preference {@link PreferenceKeys#ROAD_SIGNS}
     * and refreshes the set of road icons 
     *
     */
    class PreferenceChangeHandler implements PreferenceChangedListener {        
        public void refreshIconSet() {
            pnlTurnRestrictionsInDataSet.initIconSetFromPreferences(Main.pref);
            pnlTurnRestrictionsInSelection.initIconSetFromPreferences(Main.pref);
            repaint();
        }
        
        public void preferenceChanged(PreferenceChangeEvent evt) {          
            if (!evt.getKey().equals(PreferenceKeys.ROAD_SIGNS)) return;
            refreshIconSet();
        }
    }
}
