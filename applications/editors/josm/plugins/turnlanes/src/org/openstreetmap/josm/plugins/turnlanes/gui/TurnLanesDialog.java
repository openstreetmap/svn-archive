package org.openstreetmap.josm.plugins.turnlanes.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnlanes.model.ModelContainer;

public class TurnLanesDialog extends ToggleDialog {
    private class EditAction extends JosmAction {
        private static final long serialVersionUID = 4114119073563457706L;
        
        public EditAction() {
            super(tr("Edit"), "dialogs/edit", tr("Edit turn relations and lane lengths for selected node."), null,
                    false);
            putValue("toolbar", "turnlanes/edit");
            Main.toolbar.register(this);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final CardLayout cl = (CardLayout) body.getLayout();
            cl.show(body, CARD_EDIT);
            editing = true;
            editButton.setSelected(true);
            refresh();
        }
    }
    
    private class ValidateAction extends JosmAction {
        private static final long serialVersionUID = 7510740945725851427L;
        
        public ValidateAction() {
            super(tr("Validate"), "dialogs/validator", tr("Validate turn- and lane-length-relations for consistency."),
                    null, false);
            putValue("toolbar", "turnlanes/validate");
            Main.toolbar.register(this);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final CardLayout cl = (CardLayout) body.getLayout();
            cl.show(body, CARD_VALIDATE);
            editing = false;
            validateButton.setSelected(true);
        }
    }
    
    private final DataSetListener dataSetListener = new DataSetListener() {
        @Override
        public void wayNodesChanged(WayNodesChangedEvent event) {
            refresh();
        }
        
        @Override
        public void tagsChanged(TagsChangedEvent event) {
            refresh();
            
        }
        
        @Override
        public void relationMembersChanged(RelationMembersChangedEvent event) {
            refresh();
        }
        
        @Override
        public void primitivesRemoved(PrimitivesRemovedEvent event) {
            refresh();
        }
        
        @Override
        public void primitivesAdded(PrimitivesAddedEvent event) {
            refresh();
        }
        
        @Override
        public void otherDatasetChange(AbstractDatasetChangedEvent event) {
            refresh();
        }
        
        @Override
        public void nodeMoved(NodeMovedEvent event) {
            refresh();
        }
        
        @Override
        public void dataChanged(DataChangedEvent event) {
            refresh();
        }
        
        private void refresh() {
            if (editing) {
                junctionPane.refresh();
            }
        }
    };
    
    private final Action editAction = new EditAction();
    private final Action validateAction = new ValidateAction();
    
    private static final long serialVersionUID = -1998375221636611358L;
    
    private static final String CARD_EDIT = "EDIT";
    private static final String CARD_VALIDATE = "VALIDATE";
    
    private final JPanel body = new JPanel();
    private final JunctionPane junctionPane = new JunctionPane(GuiContainer.empty());
    
    private final JToggleButton editButton = new JToggleButton(editAction);
    private final JToggleButton validateButton = new JToggleButton(validateAction);
    
    private final Set<OsmPrimitive> selected = new HashSet<OsmPrimitive>();
    
    private boolean editing = true;
    private boolean wasShowing = false;
    
    public TurnLanesDialog() {
        super(tr("Turn Lanes"), "turnlanes.png", tr("Edit turn lanes"), null, 200);
        
        MapView.addEditLayerChangeListener(new EditLayerChangeListener() {
            @Override
            public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
                if (oldLayer != null) {
                    oldLayer.data.removeDataSetListener(dataSetListener);
                }
                
                if (newLayer != null) {
                    newLayer.data.addDataSetListener(dataSetListener);
                }
            }
        });
        
        DataSet.addSelectionListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
                if (selected.equals(new HashSet<OsmPrimitive>(newSelection))) {
                    return;
                }
                selected.clear();
                selected.addAll(newSelection);
                
                refresh();
            }
        });
        
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        final ButtonGroup group = new ButtonGroup();
        group.add(editButton);
        group.add(validateButton);
        buttonPanel.add(editButton);
        buttonPanel.add(validateButton);
        
        body.setLayout(new CardLayout(4, 4));
        
        add(buttonPanel, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
        
        body.add(junctionPane, CARD_EDIT);
        body.add(new ValidationPanel(), CARD_VALIDATE);
        
        editButton.doClick();
    }
    
    @Override
    protected void stateChanged() {
        if (isShowing && !wasShowing) {
            refresh();
        }
        wasShowing = isShowing;
    }
    
    void refresh() {
        if (isShowing && editing) {
            final Collection<OsmPrimitive> s = Collections.unmodifiableCollection(selected);
            final List<Node> nodes = OsmPrimitive.getFilteredList(s, Node.class);
            final List<Way> ways = OsmPrimitive.getFilteredList(s, Way.class);
            
            final ModelContainer mc = nodes.isEmpty() ? ModelContainer.empty() : ModelContainer
                    .createEmpty(nodes, ways);
            
            junctionPane.setJunction(new GuiContainer(mc));
        }
    }
}
