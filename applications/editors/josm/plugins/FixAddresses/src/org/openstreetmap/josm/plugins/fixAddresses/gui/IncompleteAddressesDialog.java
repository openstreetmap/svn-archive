/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OsmUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.ApplyAllGuessesAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.GuessAddressDataAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.RemoveAddressTagsAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.SelectAddressesInMapAction;

@SuppressWarnings("serial")
public class IncompleteAddressesDialog extends ToggleDialog implements DataSetListener, ListSelectionListener, IAddressEditContainerListener {
	private static final String FIXED_DIALOG_TITLE = tr("Incomplete Addresses");

	private AddressEditContainer container;

	// Create action objects
	private SelectAddressesInMapAction selectAction = new SelectAddressesInMapAction();
	private GuessAddressDataAction guessDataAction = new GuessAddressDataAction();
	private ApplyAllGuessesAction applyGuessesAction = new ApplyAllGuessesAction();
	private RemoveAddressTagsAction removeTagsAction = new RemoveAddressTagsAction();

	// Array containing the available actions
	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[]{
			selectAction,
			guessDataAction,
			applyGuessesAction,
			removeTagsAction
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
		// Top-level panel
		JPanel p = new JPanel(new BorderLayout());
		// Table containing address entities
		IncompleteAddressesTableModel model = new IncompleteAddressesTableModel(container);
		incompleteAddr = new JTable(model);
		JTableHeader header = incompleteAddr.getTableHeader();
		header.addMouseListener(model.new ColumnListener(incompleteAddr));
		incompleteAddr.getSelectionModel().addListSelectionListener(this);

		// Scroll pane hosting the table
		JScrollPane sp = new JScrollPane(incompleteAddr);
		p.add(sp, BorderLayout.CENTER);
		this.add(p);

		// Button panel containing the commands
		JPanel buttonPanel = getButtonPanel(actions.length);

		// Populate panel with actions
		for (int i = 0; i < actions.length; i++) {
			SideButton sb = new SideButton(actions[i]);
			buttonPanel.add(sb);
		}

		this.add(buttonPanel, BorderLayout.SOUTH);

		// Link actions with address container
		for (AbstractAddressEditAction action : actions) {
			action.setContainer(container);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.dialogs.ToggleDialog#hideNotify()
	 */
	@Override
	public void hideNotify() {
		super.hideNotify();
		DatasetEventManager.getInstance().removeDatasetListener(this);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.dialogs.ToggleDialog#showNotify()
	 */
	@Override
	public void showNotify() {
		super.showNotify();
		DatasetEventManager.getInstance().addDatasetListener(this, FireMode.IN_EDT_CONSOLIDATED);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#dataChanged(org.openstreetmap.josm.data.osm.event.DataChangedEvent)
	 */
	@Override
	public void dataChanged(DataChangedEvent event) {
		container.invalidate();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#nodeMoved(org.openstreetmap.josm.data.osm.event.NodeMovedEvent)
	 */
	@Override
	public void nodeMoved(NodeMovedEvent event) {

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#otherDatasetChange(org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent)
	 */
	@Override
	public void otherDatasetChange(AbstractDatasetChangedEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#primitivesAdded(org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent)
	 */
	public void primitivesAdded(PrimitivesAddedEvent event) {
		container.invalidate();

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#primitivesRemoved(org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent)
	 */
	public void primitivesRemoved(PrimitivesRemovedEvent event) {
		container.invalidate();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#relationMembersChanged(org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent)
	 */
	@Override
	public void relationMembersChanged(RelationMembersChangedEvent event) {
		container.invalidate();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#tagsChanged(org.openstreetmap.josm.data.osm.event.TagsChangedEvent)
	 */
	@Override
	public void tagsChanged(TagsChangedEvent event) {
		container.invalidate();

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.event.DataSetListener#wayNodesChanged(org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent)
	 */
	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {
		container.invalidate();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		AddressEditSelectionEvent event = new AddressEditSelectionEvent(e, null, null, incompleteAddr, container);

		for (AbstractAddressEditAction action : actions) {
			action.setEvent(event);
		}

		OsmUtils.zoomAddresses(event.getSelectedIncompleteAddresses());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener#containerChanged(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void containerChanged(AddressEditContainer container) {
		if (container != null && container.getNumberOfIncompleteAddresses() > 0) {
			setTitle(String.format("%s (%d %s)", FIXED_DIALOG_TITLE, container.getNumberOfIncompleteAddresses(), tr("items")));
		} else {
			setTitle(String.format("%s (%s)", FIXED_DIALOG_TITLE, tr("no items")));
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener#entityChanged(org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity)
	 */
	@Override
	public void entityChanged(IOSMEntity node) {
		container.invalidate();
	}

	@Override
	public void primtivesAdded(PrimitivesAddedEvent event) {
		// do something here?		
	}

	@Override
	public void primtivesRemoved(PrimitivesRemovedEvent event) {
		// do something here?		
	}
}
