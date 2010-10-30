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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;

import org.openstreetmap.josm.plugins.addressEdit.AddressNode;
import org.openstreetmap.josm.plugins.addressEdit.INodeEntity;

public class IncompleteAddressesMouseListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		JTable table = (JTable)e.getSource();
		Point p = e.getPoint();
		if(e.getClickCount() == 2) {						
			AddressEditTableModel model = (AddressEditTableModel) table.getModel();
			if (model != null) {
				int row = table.rowAtPoint(p);
				INodeEntity node = model.getEntityOfRow(row);
				if (node instanceof AddressNode) {
					AddressNode aNode = (AddressNode) node;
					if (aNode.hasGuessedStreetName()) {
						aNode.applyGuessedStreet();
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
