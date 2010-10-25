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
package org.openstreetmap.josm.plugins.addressEdit.gui;

import java.awt.event.ActionEvent;

import javax.swing.tree.DefaultMutableTreeNode;

public class AddressSelectionEvent extends ActionEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -93034483427803409L;
	private DefaultMutableTreeNode selStreet;
	private DefaultMutableTreeNode unresolvedAddr;
	private DefaultMutableTreeNode incomplete;
	
	public AddressSelectionEvent(Object source, DefaultMutableTreeNode selStreet, DefaultMutableTreeNode unresolvedAddr, DefaultMutableTreeNode incomplete ) {
		super(source, -1, "");
		this.selStreet = selStreet;
		this.unresolvedAddr = unresolvedAddr;
		this.incomplete = incomplete;
	}
	
	public DefaultMutableTreeNode getSelectedStreet() {
		return selStreet;
	}

	public DefaultMutableTreeNode getSelectedUnresolvedAddress() {
		return unresolvedAddr;
	}

	public DefaultMutableTreeNode getSelectedIncompleteAddress() {
		return incomplete;
	}
}
