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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;

public class AddressEditModel {
	private List<OSMStreet> streets;
	private List<OSMAddress> unresolvedAddresses;
	private List<OSMAddress> incompleteAddresses = new ArrayList<OSMAddress>();
	private DefaultMutableTreeNode streetRoot;
	private DefaultMutableTreeNode unresolvedRoot;
	private DefaultMutableTreeNode incompleteRoot;

	/**
	 * @param streets
	 * @param unresolvedAddresses
	 */
	public AddressEditModel(List<OSMStreet> streets,
			List<OSMAddress> unresolvedAddresses) {
		super();
		this.streets = streets;
		this.unresolvedAddresses = unresolvedAddresses;
	}

	public TreeNode getStreetsTree() {
		if (streets == null) return new DefaultMutableTreeNode(tr("(No data)"));

		if (streetRoot == null) {
			streetRoot = new DefaultMutableTreeNode();
			for (OSMStreet sNode : streets) {
				DefaultMutableTreeNode treeStreetNode = new DefaultMutableTreeNode(sNode);

				DefaultMutableTreeNode segmentsNode = new DefaultMutableTreeNode(tr("Segments"));
				treeStreetNode.add(segmentsNode);

				// Add street segment(s)
				for (IOSMEntity child : sNode.getChildren()) {
					segmentsNode.add(new DefaultMutableTreeNode(child));
				}

				if (sNode.hasAddresses()) {
					// Add address nodes
					DefaultMutableTreeNode addressNode = new DefaultMutableTreeNode(tr("Addresses"));
					treeStreetNode.add(addressNode);

					for (OSMAddress addr : sNode.getAddresses()) {
						addressNode.add(new DefaultMutableTreeNode(addr));
						if (!addr.isComplete()) {
							incompleteAddresses.add(addr);
						}
					}
				}
				streetRoot.add(treeStreetNode);
			}
		}

		return streetRoot;
	}

	/**
	 * Gets the tree node containing all unresolved addresses.
	 * @return
	 */
	public TreeNode getUnresolvedAddressesTree() {
		if (unresolvedAddresses == null) return new DefaultMutableTreeNode(tr("(No data)"));

		if (unresolvedRoot == null) {
			unresolvedRoot = new DefaultMutableTreeNode();

			for (OSMAddress addr : unresolvedAddresses) {
				// Add address nodes
				unresolvedRoot.add(new DefaultMutableTreeNode(addr));
			}
		}

		return unresolvedRoot;
	}

	/**
	 * Gets the tree node containing all incomplete addresses.
	 * @return
	 */
	public TreeNode getIncompleteAddressesTree() {
		if (incompleteAddresses == null) return new DefaultMutableTreeNode(tr("(No data)"));

		if (incompleteRoot == null) {
			incompleteRoot = new DefaultMutableTreeNode();

			for (OSMAddress addr : incompleteAddresses) {
				// Add address nodes
				incompleteRoot.add(new DefaultMutableTreeNode(addr));
			}
		}

		return incompleteRoot;
	}
}
