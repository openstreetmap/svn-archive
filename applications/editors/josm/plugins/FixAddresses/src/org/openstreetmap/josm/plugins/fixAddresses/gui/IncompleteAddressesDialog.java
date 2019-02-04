// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.LinkedList;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OsmUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AddressActions;

/**
 * Incomplete addresses dialog.
 */
@SuppressWarnings("serial")
public class IncompleteAddressesDialog extends ToggleDialog implements DataSetListener, ListSelectionListener, IAddressEditContainerListener {
    private static final String FIXED_DIALOG_TITLE = tr("Incomplete Addresses");

    private AddressEditContainer container;

    // Array containing the available actions
    private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[]{
            AddressActions.getSelectAction(),
            AddressActions.getGuessAddressAction(),
            AddressActions.getApplyGuessesAction(),
            AddressActions.getRemoveTagsAction(),
    };

    private JTable incompleteAddr;

    /**
     * Instantiates a new "incomplete addresses" dialog.
     *
     */
    public IncompleteAddressesDialog() {
        super(FIXED_DIALOG_TITLE, "incompleteaddress_24", tr("Show incomplete addresses"), null, 150);

        this.container = new AddressEditContainer();
        container.addChangedListener(this);
        // Table containing address entities
        IncompleteAddressesTableModel model = new IncompleteAddressesTableModel(container);
        incompleteAddr = new JTable(model);
        JTableHeader header = incompleteAddr.getTableHeader();
        header.addMouseListener(model.new ColumnListener(incompleteAddr));
        incompleteAddr.getSelectionModel().addListSelectionListener(this);

        LinkedList<SideButton> buttons = new LinkedList<>();
        // Link actions with address container
        for (AbstractAddressEditAction action : actions) {
            buttons.add(new SideButton(action));
            action.setContainer(container);
        }
        createLayout(incompleteAddr, true, buttons);
     }

    @Override
    public void hideNotify() {
        super.hideNotify();
        DatasetEventManager.getInstance().removeDatasetListener(this);
    }

    @Override
    public void showNotify() {
        super.showNotify();
        DatasetEventManager.getInstance().addDatasetListener(this, FireMode.IN_EDT_CONSOLIDATED);
    }

    @Override
    public void dataChanged(DataChangedEvent event) {
        container.invalidate();
    }

    @Override
    public void nodeMoved(NodeMovedEvent event) {

    }

    @Override
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {
        container.invalidate();
    }

    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        container.invalidate();
    }

    @Override
    public void relationMembersChanged(RelationMembersChangedEvent event) {
        container.invalidate();
    }

    @Override
    public void tagsChanged(TagsChangedEvent event) {
        container.invalidate();
    }

    @Override
    public void wayNodesChanged(WayNodesChangedEvent event) {
        container.invalidate();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        AddressEditSelectionEvent event = new AddressEditSelectionEvent(e, null, null, incompleteAddr, container);

        for (AbstractAddressEditAction action : actions) {
            action.setEvent(event);
        }

        OsmUtils.zoomAddresses(event.getSelectedIncompleteAddresses());
    }

    @Override
    public void containerChanged(AddressEditContainer container) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (container != null && container.getNumberOfIncompleteAddresses() > 0) {
                setTitle(String.format("%s (%d %s)", FIXED_DIALOG_TITLE, container.getNumberOfIncompleteAddresses(), tr("items")));
            } else {
                setTitle(String.format("%s (%s)", FIXED_DIALOG_TITLE, tr("no items")));
            }
        }
    }

    @Override
    public void entityChanged(IOSMEntity node) {
        if (SwingUtilities.isEventDispatchThread()) {
            container.invalidate();
        }
    }
}
