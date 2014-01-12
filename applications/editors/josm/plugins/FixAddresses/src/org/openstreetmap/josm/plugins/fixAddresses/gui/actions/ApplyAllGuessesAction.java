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
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.StringUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel;

/**
 * Applies the guessed values for a set of addresses.
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */

@SuppressWarnings("serial")
public class ApplyAllGuessesAction extends AbstractAddressEditAction implements MouseListener{
	private String tag;
	/**
	 * Instantiates a new "apply all guesses" action.
	 */
	public ApplyAllGuessesAction(String tag) {
		super(tr("Apply"), "applyguesses_24", tr("Turns all guesses into the corresponding tag values."),
			"fixaddresses/applyallguesses");
		this.tag = tag;
	}

	/**
	 * Instantiates a new "apply all guesses" action.
	 */
	public ApplyAllGuessesAction() {
		this(null);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
		if (ev == null) return;

		if (ev.getSelectedUnresolvedAddresses() != null) {
			List<OSMAddress> addrToFix = ev.getSelectedUnresolvedAddresses();
			applyGuesses(addrToFix);
		}

		if (ev.getSelectedIncompleteAddresses() != null) {
			List<OSMAddress> addrToFix = ev.getSelectedIncompleteAddresses();
			applyGuesses(addrToFix);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	protected void updateEnabledState(AddressEditContainer container) {
		setEnabled(container != null && container.getNumberOfGuesses() > 0);
	}

	/**
	 * Apply guesses.
	 *
	 * @param addrToFix the addr to fix
	 */
	private void applyGuesses(List<OSMAddress> addrToFix) {
		beginTransaction(tr("Applied guessed values"));
		List<OSMAddress> addrToFixShadow = new ArrayList<OSMAddress>(addrToFix);
		for (OSMAddress aNode : addrToFixShadow) {
			beginObjectTransaction(aNode);

			if (StringUtils.isNullOrEmpty(tag)) { // tag given?
				aNode.applyAllGuesses(); // no -> apply all guesses
			} else { // apply guessed values for single tag only
				aNode.applyGuessForTag(tag);
			}
			finishObjectTransaction(aNode);
		}
		finishTransaction();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#updateEnabledState(org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent)
	 */
	@Override
	protected void updateEnabledState(AddressEditSelectionEvent event) {
		setEnabled(event.hasAddressesWithGuesses());
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction#addressEditActionPerformed(org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer)
	 */
	@Override
	public void addressEditActionPerformed(AddressEditContainer container) {
		if (container == null || container.getNumberOfIncompleteAddresses() == 0) return;

		List<OSMAddress> addrToFix = container.getUnresolvedAddresses();
		applyGuesses(addrToFix);

		addrToFix = container.getIncompleteAddresses();
		applyGuesses(addrToFix);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		JTable table = (JTable)e.getSource();
		Point p = e.getPoint();
		if(e.getClickCount() == 2) {
			AddressEditTableModel model = (AddressEditTableModel) table.getModel();
			if (model != null) {
				int row = table.rowAtPoint(p);
				IOSMEntity node = model.getEntityOfRow(row);
				if (node instanceof OSMAddress) {
					beginTransaction(tr("Applied guessed values for ") + node.getOsmObject());
					beginObjectTransaction(node);
					OSMAddress aNode = (OSMAddress) node;

					aNode.applyAllGuesses();

					finishObjectTransaction(node);
					finishTransaction();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
