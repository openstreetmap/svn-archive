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
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;

public class AddressEditDialog extends JFrame implements ActionListener, TreeSelectionListener, ListSelectionListener {
	private static final String CANCEL_COMMAND = "Cancel";
	private static final String OK_COMMAND = "Ok";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251676464816335631L;
	private AddressEditContainer model;
	private JTree unresolvedTree;
	private JTree incompleteTree;
	private JTable streetList;
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
	public AddressEditDialog(AddressEditContainer addressEditContainer) throws HeadlessException  {
		super(tr("Edit Addresses"));
	
		this.model = addressEditContainer; 
		setLayout(new BorderLayout());
		setSize(800,600);
		// TODO: Center on screen
		setLocation(100, 100);
		
		// TODO: Proper init, if model is null
		if (addressEditContainer != null) {
			JPanel streetPanel = new JPanel(new BorderLayout());
			/*
			streetsTree = new JTree(new DefaultTreeModel(addressEditContainer.getStreetsTree()));
			streetsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			streetsTree.addTreeSelectionListener(this);
			streetsTree.setCellRenderer(new StreetTreeCellRenderer());
			*/
			streetList = new JTable(new StreetTableModel(model));
			streetList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			JScrollPane scroll1 = new JScrollPane(streetList);
			streetPanel.add(scroll1, BorderLayout.CENTER);
			streetPanel.add(new JLabel("Unresolved Addresses"), BorderLayout.NORTH);
			streetPanel.setMinimumSize(new Dimension(350, 400));
			
			JPanel unresolvedPanel = new JPanel(new BorderLayout());		
			unresolvedTree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
			unresolvedTree.addTreeSelectionListener(this);
			
			JScrollPane scroll2 = new JScrollPane(unresolvedTree);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedPanel.add(new JLabel("Unresolved Addresses"), BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(350, 200));
			
			JPanel unresolvedButtons = new JPanel(new FlowLayout());
			JButton assign = new JButton(resolveAction);
			unresolvedButtons.add(assign);
			unresolvedPanel.add(unresolvedButtons, BorderLayout.SOUTH);
			
			JPanel incompletePanel = new JPanel(new BorderLayout());
			incompleteTree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
			incompleteTree.addTreeSelectionListener(this);
			JScrollPane scroll3 = new JScrollPane(incompleteTree);
			incompletePanel.add(scroll3, BorderLayout.CENTER);
			incompletePanel.add(new JLabel("Incomplete Addresses"), BorderLayout.NORTH);
			incompletePanel.setMinimumSize(new Dimension(350, 200));
			
			JSplitPane addrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, unresolvedPanel, incompletePanel);
			
			JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, streetPanel, addrSplitPane);
			this.getContentPane().add(pane, BorderLayout.CENTER);
		} else {
			streetList = new JTable(new DefaultTableModel());
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
		if (event.getSource() == streetList) {
			int selStr = streetList.getSelectedRow();
			 
		}
		
		/*
		AddressSelectionEvent ev = new AddressSelectionEvent(event.getSource(),
				selStreet, selUnrAddr, selIncAddr);		
		for (AbstractAddressEditAction action : actions) {
			action.updateEnabledState(ev);
		}*/
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
