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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.addressEdit.AddressEditContainer;
import org.openstreetmap.josm.plugins.addressEdit.AddressNode;
import org.openstreetmap.josm.plugins.addressEdit.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.addressEdit.INodeEntity;
import org.openstreetmap.josm.plugins.addressEdit.StreetNode;
import org.openstreetmap.josm.plugins.addressEdit.StringUtils;

public class AddressEditDialog extends JFrame implements ActionListener, ListSelectionListener, IAddressEditContainerListener {
	private static final String INCOMPLETE_HEADER_FMT = tr("Incomplete Addresses (%d)");
	private static final String UNRESOLVED_HEADER_FMT = tr("Unresolved Addresses (%d)");
	private static final String STREET_HEADER_FMT = tr("Streets (%d)");
	private static final String CANCEL_COMMAND = "Cancel";
	private static final String OK_COMMAND = "Ok";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251676464816335631L;
	private AddressEditContainer editContainer;
	private JTable unresolvedTable;
	private JTable incompleteTable;
	private JTable streetTable;
	
	private AssignAddressToStreetAction resolveAction = new AssignAddressToStreetAction();
	
	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[] {
		resolveAction	
	};
	private JLabel streetLabel;
	private JLabel incompleteAddressesLabel;
	private JLabel unresolvedAddressesLabel;
	private JMapViewer mapViewer;
	private JLabel mapLabel;
	
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public AddressEditDialog(AddressEditContainer addressEditContainer) throws HeadlessException  {
		super(tr("Edit Addresses"));
	
		this.editContainer = addressEditContainer; 
		this.editContainer.addChangedListener(this);
		setLayout(new BorderLayout());
		setSize(1024,600);
		// TODO: Center on screen
		setLocation(100, 100);

		
		// TODO: Proper init, if model is null
		if (addressEditContainer != null) {
			/* Panel for street table */
			JPanel streetPanel = new JPanel(new BorderLayout());
			streetTable = new JTable(new StreetTableModel(editContainer));
			streetTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			streetTable.getSelectionModel().addListSelectionListener(this);
			
			JScrollPane scroll1 = new JScrollPane(streetTable);
			streetPanel.add(scroll1, BorderLayout.CENTER);
			
			streetLabel = createHeaderLabel(STREET_HEADER_FMT, editContainer.getNumberOfStreets());
			streetPanel.add(streetLabel, BorderLayout.NORTH);
			streetPanel.setMinimumSize(new Dimension(350, 300));
			
			/* Panel for unresolved addresses table */
			JPanel unresolvedPanel = new JPanel(new BorderLayout());		
			unresolvedTable = new JTable(new UnresolvedAddressesTableModel(editContainer));
			unresolvedTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			unresolvedTable.getSelectionModel().addListSelectionListener(this);
			unresolvedTable.getSelectionModel().addListSelectionListener(new IncompleteAddressListener());
			
			JScrollPane scroll2 = new JScrollPane(unresolvedTable);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedAddressesLabel = createHeaderLabel(
					UNRESOLVED_HEADER_FMT, editContainer.getNumberOfUnresolvedAddresses());
			unresolvedPanel.add(unresolvedAddressesLabel , BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(350, 200));
			
			JPanel unresolvedButtons = new JPanel(new FlowLayout());
			JButton assign = new JButton(resolveAction);
			unresolvedButtons.add(assign);
			unresolvedPanel.add(unresolvedButtons, BorderLayout.SOUTH);
			
			/* Panel for incomplete addresses */
			JPanel incompletePanel = new JPanel(new BorderLayout());			
			incompleteTable = new JTable(new IncompleteAddressesTableModel(editContainer));
			incompleteTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			incompleteTable.getSelectionModel().addListSelectionListener(this);
			JScrollPane scroll3 = new JScrollPane(incompleteTable);
			
			incompletePanel.add(scroll3, BorderLayout.CENTER);
			incompleteAddressesLabel = createHeaderLabel(
					INCOMPLETE_HEADER_FMT, editContainer.getNumberOfUnresolvedAddresses());
			incompletePanel.add(incompleteAddressesLabel, BorderLayout.NORTH);
			incompletePanel.setMinimumSize(new Dimension(350, 200));
			
			
			
			/* Edit panel for incomplete addresses */
			JPanel incompleteEditPanel = new JPanel();
			incompleteEditPanel.setMinimumSize(new Dimension(350, 300));

			/* Map Panel */
			JPanel mapPanel = new JPanel(new BorderLayout());
			mapViewer = new JMapViewer();
			mapPanel.add(mapViewer, BorderLayout.CENTER);
			mapPanel.setMinimumSize(new Dimension(200, 200));
			mapViewer.setVisible(false);
			
			JTabbedPane tab = new JTabbedPane();
			tab.addTab(tr("Properties"), incompleteEditPanel);
			tab.addTab(tr("Map"), mapPanel);
			
			/* Combine panels */
			JSplitPane unresSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, streetPanel, unresolvedPanel);
			JSplitPane addrSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tab, incompletePanel);			
			JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, unresSplitPane, addrSplitPane);
			
			this.getContentPane().add(pane, BorderLayout.CENTER);
			//this.getContentPane().add(mapPanel, BorderLayout.SOUTH);
		} else {
			this.getContentPane().add(new JLabel(tr("(No data)")), BorderLayout.CENTER);
		}
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,10));
		JButton ok = new JButton(OK_COMMAND);
		ok.addActionListener(this);
		buttonPanel.add(ok);
		
		// JMapViewer
		
		// Murks
		for (int i = 0; i < 8; i++) {
			buttonPanel.add(new JSeparator());
		}
		
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	private JLabel createHeaderLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}

	private JLabel createHeaderLabel(String fmtString, int n) {
		JLabel label = new JLabel(String.format(fmtString, n));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}
	
	private void updateHeaders() {
		if (editContainer != null) {
			streetLabel.setText(String.format(STREET_HEADER_FMT, editContainer.getNumberOfStreets()));
			incompleteAddressesLabel.setText(String.format(INCOMPLETE_HEADER_FMT, editContainer.getNumberOfIncompleteAddresses()));
			unresolvedAddressesLabel.setText(String.format(UNRESOLVED_HEADER_FMT, editContainer.getNumberOfUnresolvedAddresses()));
		} else {
			streetLabel.setText(String.format(STREET_HEADER_FMT, 0));
			incompleteAddressesLabel.setText(String.format(INCOMPLETE_HEADER_FMT, 0));
			unresolvedAddressesLabel.setText(String.format(UNRESOLVED_HEADER_FMT, 0));
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
	public void valueChanged(ListSelectionEvent e) {
		
		AddressEditSelectionEvent ev = new AddressEditSelectionEvent(e.getSource(),
				streetTable, unresolvedTable, incompleteTable, editContainer);
		
		for (AbstractAddressEditAction action : actions) {
			action.updateEnabledState(ev);
		}
		
		clearMapViewer();
		StreetNode sNode = ev.getSelectedStreet();
		if (sNode != null) {
						
			//mapViewer.addMapRectangle(new BBoxMapRectangle(bb));
			for (INodeEntity seg : sNode.getChildren()) {
				Way way = (Way) seg.getOsmObject();
				//BBox bb = way.getBBox();
				
				for (Node node : way.getNodes()) {
					mapViewer.addMapMarker(new MapMarkerDot(Color.GREEN, node.getCoor().lat(), node.getCoor().lon()));
				}
			}	
			
			List<AddressNode> incAddresses = ev.getSelectedIncompleteAddresses();
			if (incAddresses != null) {
				for (AddressNode aNode : incAddresses) {
					Node node = (Node) aNode.getOsmObject();
					mapViewer.addMapMarker(new MapMarkerDot(Color.RED, node.getCoor().lat(), node.getCoor().lon()));
				}
			}
			
			List<AddressNode> unrAddresses = ev.getSelectedUnresolvedAddresses();
			if (unrAddresses != null) {
				for (AddressNode aNode : unrAddresses) {
					Node node = (Node) aNode.getOsmObject();
					mapViewer.addMapMarker(new MapMarkerDot(Color.ORANGE, node.getCoor().lat(), node.getCoor().lon()));
				}
			}
			mapViewer.setDisplayToFitMapMarkers();
			mapViewer.setVisible(true);
		} 
	}

	private void clearMapViewer() {
		mapViewer.setVisible(false);
		// remove markers and rectangles from map viewer
		mapViewer.getMapMarkerList().clear();
		mapViewer.getMapRectangleList().clear();
	}

	@Override
	public void containerChanged(AddressEditContainer container) {
		updateHeaders();
	}

	@Override
	public void entityChanged() {
		updateHeaders();
	}
	
	/**
	 * Special listener to react on selection changes in the incomplete address list. 
	 * It searches the street table for the streets which matches best matching to the
	 * street name given in the address.
	 *   
	 * @author Oliver Wieland <oliver.wieland@online.de> 
	 */
	
	class IncompleteAddressListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (unresolvedTable.getSelectedRowCount() == 1) {
				String streetOfAddr = (String) unresolvedTable.
											getModel().getValueAt(unresolvedTable.getSelectedRow(), 0);
				
				int maxScore = 0, score = 0, row = -1;
				for (int i = 0; i < streetTable.getRowCount(); i++) {
					String streetName = (String) streetTable.getModel().getValueAt(i, 1);
					
					score = StringUtils.lcsLength(streetOfAddr, streetName);
					if (score > maxScore) {
						maxScore = score;
						row = i; 
					}
				}
				
				if (row > 0) {
					streetTable.getSelectionModel().clearSelection();
					streetTable.getSelectionModel().addSelectionInterval(row, row);
					streetTable.scrollRectToVisible(streetTable.getCellRect(row, 0, true));
				}
			}			
		}
		
	}

}
