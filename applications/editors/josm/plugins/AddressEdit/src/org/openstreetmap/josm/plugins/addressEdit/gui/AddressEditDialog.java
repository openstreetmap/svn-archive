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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;

public class AddressEditDialog extends JFrame implements ActionListener, ListSelectionListener {
	private static final String CANCEL_COMMAND = "Cancel";
	private static final String OK_COMMAND = "Ok";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251676464816335631L;
	private AddressEditContainer addressContainer;
	private JTable unresolvedTable;
	private JTable incompleteTable;
	private JTable streetList;
	
	private AssignAddressToStreetAction resolveAction = new AssignAddressToStreetAction();
	
	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[] {
		resolveAction	
	};
	
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public AddressEditDialog(AddressEditContainer addressEditContainer) throws HeadlessException  {
		super(tr("Edit Addresses"));
	
		this.addressContainer = addressEditContainer; 
		setLayout(new BorderLayout());
		setSize(800,600);
		// TODO: Center on screen
		setLocation(100, 100);
		
		// TODO: Proper init, if model is null
		if (addressEditContainer != null) {
			JPanel streetPanel = new JPanel(new BorderLayout());
			streetList = new JTable(new StreetTableModel(addressContainer));
			streetList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			streetList.getSelectionModel().addListSelectionListener(this);
			
			JScrollPane scroll1 = new JScrollPane(streetList);
			streetPanel.add(scroll1, BorderLayout.CENTER);
			streetPanel.add(new JLabel("Streets"), BorderLayout.NORTH);
			streetPanel.setMinimumSize(new Dimension(350, 400));
			
			JPanel unresolvedPanel = new JPanel(new BorderLayout());		
			unresolvedTable = new JTable(new UnresolvedAddressesTableModel(addressContainer));
			unresolvedTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			unresolvedTable.getSelectionModel().addListSelectionListener(this);
			
			JScrollPane scroll2 = new JScrollPane(unresolvedTable);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedPanel.add(new JLabel("Unresolved Addresses"), BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(350, 200));
			
			JPanel unresolvedButtons = new JPanel(new FlowLayout());
			JButton assign = new JButton(resolveAction);
			unresolvedButtons.add(assign);
			unresolvedPanel.add(unresolvedButtons, BorderLayout.SOUTH);
			
			JPanel incompletePanel = new JPanel(new BorderLayout());
			
			incompleteTable = new JTable(new IncompleteAddressesTableModel(addressContainer));
			incompleteTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			incompleteTable.getSelectionModel().addListSelectionListener(this);
			JScrollPane scroll3 = new JScrollPane(incompleteTable);
			
			incompletePanel.add(scroll3, BorderLayout.CENTER);
			incompletePanel.add(new JLabel("Incomplete Addresses"), BorderLayout.NORTH);
			incompletePanel.setMinimumSize(new Dimension(350, 200));
			
			JSplitPane addrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, unresolvedPanel, incompletePanel);
			
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, streetPanel, addrSplitPane);
			this.getContentPane().add(pane, BorderLayout.CENTER);
		} else {
			this.getContentPane().add(new JLabel(tr("(No data)")), BorderLayout.CENTER);
		}
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,10));
		JButton ok = new JButton(OK_COMMAND);
		ok.addActionListener(this);
		buttonPanel.add(ok);
		
		// Murks
		for (int i = 0; i < 8; i++) {
			buttonPanel.add(new JSeparator());
		}
		
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (OK_COMMAND.equals(e.getActionCommand())) {
			this.setVisible(false);
		}
		
		// TODO: Check, if there is some kind of undo; otherwise this button is not necessary
		if (CANCEL_COMMAND.equals(e.getActionCommand())) {
			this.setVisible(false);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		AddressEditSelectionEvent ev = new AddressEditSelectionEvent(e.getSource(),
				streetList, unresolvedTable, incompleteTable, addressContainer);
		
		for (AbstractAddressEditAction action : actions) {
			action.updateEnabledState(ev);
		}
	}

}
