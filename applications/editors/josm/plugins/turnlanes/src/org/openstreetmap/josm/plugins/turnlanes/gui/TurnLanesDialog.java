// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnlanes.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
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
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnlanes.model.ModelContainer;

public class TurnLanesDialog extends ToggleDialog implements ActiveLayerChangeListener, DataSelectionListener {
    private class EditAction extends JosmAction {
        private static final long serialVersionUID = 4114119073563457706L;

        EditAction() {
            super(tr("Edit"), "dialogs/edit", tr("Edit turn relations and lane lengths for selected node."), null,
                    false);
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

        ValidateAction() {
            super(tr("Validate"), "dialogs/validator", tr("Validate turn- and lane-length-relations for consistency."),
                    null, false);
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

    private final JosmAction editAction = new EditAction();
    private final JosmAction validateAction = new ValidateAction();

    private static final long serialVersionUID = -1998375221636611358L;

    private static final String CARD_EDIT = "EDIT";
    private static final String CARD_VALIDATE = "VALIDATE";

    private final JPanel body = new JPanel();


    private final JunctionPane junctionPane = new JunctionPane(GuiContainer.empty());

    private final JToggleButton editButton = new JToggleButton(editAction);
    private final JToggleButton validateButton = new JToggleButton(validateAction);

    private final Set<OsmPrimitive> selected = new HashSet<>();

    private boolean editing = true;
    private boolean wasShowing = false;

    private ModelContainer modelContainer;
    private boolean leftDirection = ModelContainer.empty().isLeftDirection();

    public TurnLanesDialog() {
        super(tr("Turn Lanes"), "turnlanes.png", tr("Edit turn lanes"), null, 200);

        MainApplication.getLayerManager().addActiveLayerChangeListener(this);
        SelectionEventManager.getInstance().addSelectionListener(this);

        final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        final ButtonGroup group = new ButtonGroup();
        group.add(editButton);
        group.add(validateButton);
        addTrafficDirectionCheckBox(buttonPanel);
        buttonPanel.add(editButton);
        buttonPanel.add(validateButton);

        body.setLayout(new CardLayout(4, 4));

        add(buttonPanel, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        body.add(junctionPane, CARD_EDIT);
        body.add(new ValidationPanel(), CARD_VALIDATE);

        editButton.doClick();
    }

    /**
     * Add label and checkbox for traffic direction and change flag
     * @param buttonPanel button panel
     */
    private void addTrafficDirectionCheckBox(JPanel buttonPanel) {
        GridBagConstraints constraints = new GridBagConstraints();
        JLabel aoiLabel = new JLabel(tr("Left-hand traffic direction:"));
        constraints.gridx = 0;
        constraints.gridwidth = 1; //next-to-last
        constraints.fill = GridBagConstraints.NONE;      //reset to default
        constraints.weightx = 0.0;
        buttonPanel.add(aoiLabel, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;     //end row
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        JCheckBox leftDirectionCheckbox = new JCheckBox();
        leftDirectionCheckbox.setSelected(leftDirection);
        buttonPanel.add(leftDirectionCheckbox, constraints);
        leftDirectionCheckbox.addChangeListener(e -> {
            if (modelContainer == null) {
                return;
            }
            leftDirection = leftDirectionCheckbox.isSelected();
            refresh();
        });

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
            final Collection<Node> nodes = org.openstreetmap.josm.tools.Utils.filteredCollection(selected, Node.class);
            final Collection<Way> ways = org.openstreetmap.josm.tools.Utils.filteredCollection(selected, Way.class);

            modelContainer = nodes.isEmpty() ? ModelContainer.empty() : ModelContainer
                    .createEmpty(nodes, ways);
            modelContainer.setLeftDirection(leftDirection);

            junctionPane.setJunction(new GuiContainer(modelContainer));
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        OsmDataLayer oldLayer = e.getPreviousDataLayer();
        if (oldLayer != null) {
            oldLayer.getDataSet().removeDataSetListener(dataSetListener);
        }
        OsmDataLayer newLayer = MainApplication.getLayerManager().getEditLayer();
        if (newLayer != null) {
            newLayer.getDataSet().addDataSetListener(dataSetListener);
        }
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        if (selected.equals(new HashSet<>(event.getSelection()))) {
            return;
        }
        selected.clear();
        selected.addAll(event.getSelection());

        refresh();
    }

    @Override
    public void destroy() {
        super.destroy();
        MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
        SelectionEventManager.getInstance().removeSelectionListener(this);
        editAction.destroy();
        validateAction.destroy();
    }
}
