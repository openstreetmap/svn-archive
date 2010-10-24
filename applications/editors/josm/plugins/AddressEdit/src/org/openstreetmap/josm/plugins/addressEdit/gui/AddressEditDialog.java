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
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class AddressEditDialog extends JFrame implements ActionListener, TreeSelectionListener {
	private static final String CANCEL_COMMAND = "Cancel";
	private static final String OK_COMMAND = "Ok";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251676464816335631L;
	private AddressEditModel model;
	private JTree unresolvedTree;
	private JTree incompleteTree;
	private JTree streetsTree;
	private DefaultMutableTreeNode selStreet;
	private DefaultMutableTreeNode selUnrAddr;
	private DefaultMutableTreeNode selIncAddr;
	
	private AssignAddressToStreetAction resolveAction = new AssignAddressToStreetAction();
	
	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[] {
		resolveAction	
	};
	
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public AddressEditDialog(AddressEditModel model) throws HeadlessException  {
		super(tr("Edit Addresses"));
	
		this.model = model; 
		setLayout(new BorderLayout());
		setSize(800,600);
		// TODO: Center on screen
		setLocation(100, 100);

		// TODO: Proper init, if model is null
		if (model != null) {
			JPanel streetPanel = new JPanel(new BorderLayout());
			streetsTree = new JTree(new DefaultTreeModel(model.getStreetsTree()));
			streetsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			streetsTree.addTreeSelectionListener(this);
			streetsTree.setCellRenderer(new StreetTreeCellRenderer());
			
			JScrollPane scroll1 = new JScrollPane(streetsTree);
			streetPanel.add(scroll1, BorderLayout.CENTER);
			streetPanel.add(new JLabel("Unresolved Addresses"), BorderLayout.NORTH);
			streetPanel.setMinimumSize(new Dimension(300, 400));
			
			JPanel unresolvedPanel = new JPanel(new BorderLayout());		
			unresolvedTree = new JTree(new DefaultTreeModel(model.getUnresolvedAddressesTree()));
			unresolvedTree.addTreeSelectionListener(this);
			
			JScrollPane scroll2 = new JScrollPane(unresolvedTree);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedPanel.add(new JLabel("Unresolved Addresses"), BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(300, 200));
			
			JPanel unresolvedButtons = new JPanel(new FlowLayout());
			JButton assign = new JButton(resolveAction);
			unresolvedButtons.add(assign);
			unresolvedPanel.add(unresolvedButtons, BorderLayout.SOUTH);
			
			JPanel incompletePanel = new JPanel(new BorderLayout());
			incompleteTree = new JTree(new DefaultTreeModel(model.getIncompleteAddressesTree()));
			incompleteTree.addTreeSelectionListener(this);
			JScrollPane scroll3 = new JScrollPane(incompleteTree);
			incompletePanel.add(scroll3, BorderLayout.CENTER);
			incompletePanel.add(new JLabel("Incomplete Addresses"), BorderLayout.NORTH);
			incompletePanel.setMinimumSize(new Dimension(300, 200));
			
			JSplitPane addrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, unresolvedPanel, incompletePanel);
			
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, streetPanel, addrSplitPane);
			this.getContentPane().add(pane, BorderLayout.CENTER);
		} else {
			this.getContentPane().add(new JLabel(tr("(No data)")), BorderLayout.CENTER);
		}
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,10));
		JButton ok = new JButton(OK_COMMAND);
		ok.addActionListener(this);
		JButton cancel = new JButton(CANCEL_COMMAND);
		cancel.addActionListener(this);
		
		buttonPanel.add(cancel);
		buttonPanel.add(ok);
		// Murks
		for (int i = 0; i < 8; i++) {
			buttonPanel.add(new JSeparator());
		}
		
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	public AddressEditModel getModel() {
		return model;
	}

	public void setModel(AddressEditModel model) {
		if (this.model != model) {
			this.model = model;
			if (model != null) {
				streetsTree.setModel(new DefaultTreeModel(model.getStreetsTree()));
				unresolvedTree.setModel(new DefaultTreeModel(model.getUnresolvedAddressesTree()));
				incompleteTree.setModel(new DefaultTreeModel(model.getIncompleteAddressesTree()));
			}
		}
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
	public void valueChanged(TreeSelectionEvent event) {
		// Updates the selection
		if (event.getSource() == streetsTree) {
			selStreet = (DefaultMutableTreeNode) streetsTree.getLastSelectedPathComponent();
		}
		
		if (event.getSource() == unresolvedTree) {
			selUnrAddr = (DefaultMutableTreeNode) unresolvedTree.getLastSelectedPathComponent();
		}
		
		if (event.getSource() == incompleteTree) {
			selIncAddr = (DefaultMutableTreeNode) incompleteTree.getLastSelectedPathComponent();
		}
		
		AddressSelectionEvent ev = new AddressSelectionEvent(event.getSource(),
				selStreet, selUnrAddr, selIncAddr);		
		for (AbstractAddressEditAction action : actions) {
			action.updateEnabledState(ev);
		}
	}
}
