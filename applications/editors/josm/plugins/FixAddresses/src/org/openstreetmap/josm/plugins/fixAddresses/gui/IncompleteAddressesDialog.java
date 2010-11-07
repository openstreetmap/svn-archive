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
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;

@SuppressWarnings("serial")
public class IncompleteAddressesDialog extends ToggleDialog implements DataSetListener {
	private AddressEditContainer container;

	/**
	 * @param name
	 * @param iconName
	 * @param tooltip
	 * @param shortcut
	 * @param preferredHeight
	 * @param container
	 */
	public IncompleteAddressesDialog() {
		super(tr("Incomplete Addresses"), "incompleteaddress_24", tr("Show incomplete addresses"), null, 150);
		this.container = new AddressEditContainer();
		
		JPanel p = new JPanel(new BorderLayout());
		
		JTable incompleteAddr = new JTable(new IncompleteAddressesTableModel(container));
		JScrollPane sp = new JScrollPane(incompleteAddr);
		p.add(sp, BorderLayout.CENTER);
		this.add(p);
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
	public void primtivesAdded(PrimitivesAddedEvent event) {
		container.invalidate();
		
	}

	@Override
	public void primtivesRemoved(PrimitivesRemovedEvent event) {
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
	
	
}
