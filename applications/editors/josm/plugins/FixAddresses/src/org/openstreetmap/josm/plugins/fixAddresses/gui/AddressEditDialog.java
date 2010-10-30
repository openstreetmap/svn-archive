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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.properties.PresetListPanel.PresetHandler;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.AddressNode;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.INodeEntity;
import org.openstreetmap.josm.plugins.fixAddresses.StreetNode;
import org.openstreetmap.josm.plugins.fixAddresses.StringUtils;
import org.openstreetmap.josm.tools.ImageProvider;

@SuppressWarnings("serial")
public class AddressEditDialog extends JDialog implements ActionListener, ListSelectionListener, IAddressEditContainerListener {
	private static final String UNRESOLVED_HEADER_FMT = tr("Unresolved Addresses (%d)");
	private static final String STREET_HEADER_FMT = tr("Streets (%d)");
	private static final String OK_COMMAND = tr("Close");
	private static final String SELECT_AND_CLOSE = tr("Select and close");
	
	private AddressEditContainer editContainer;
	private JTable unresolvedTable;
	private JTable streetTable;
	
	/* Actions */
	private AssignAddressToStreetAction resolveAction = new AssignAddressToStreetAction();
	private ApplyAllGuessesAction applyAllGuessesAction = new ApplyAllGuessesAction();
	private GuessAddressDataAction guessAddressAction = new GuessAddressDataAction();
	private SelectAddressesInMapAction selectAddressesInMapAction = new SelectAddressesInMapAction();
	
	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[] {
		resolveAction,
		guessAddressAction,
		applyAllGuessesAction,
		selectAddressesInMapAction
	};
	private JLabel streetLabel;
	private JLabel unresolvedAddressesLabel;
	private JMapViewer mapViewer;
	
	
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public AddressEditDialog(AddressEditContainer addressEditContainer) throws HeadlessException  {
		super(JOptionPane.getFrameForComponent(Main.parent), tr("Fix unresolved addresses"), false);
	
		this.editContainer = addressEditContainer; 
		this.editContainer.addChangedListener(this);
		setLayout(new BorderLayout());
		setSize(1024,600);
		setLocationRelativeTo(null);
		
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
			unresolvedTable.addMouseListener(new IncompleteAddressesMouseListener());			
			
			JScrollPane scroll2 = new JScrollPane(unresolvedTable);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedAddressesLabel = createHeaderLabel(
					UNRESOLVED_HEADER_FMT, editContainer.getNumberOfUnresolvedAddresses());
			unresolvedPanel.add(unresolvedAddressesLabel , BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(350, 200));
			
			
			try {
				JPanel unresolvedButtons = new JPanel(new FlowLayout());
				SideButton assign = new SideButton(resolveAction);															   
				unresolvedButtons.add(assign);
				SideButton guess = new SideButton(guessAddressAction);															   
				unresolvedButtons.add(guess);
				SideButton applyAllGuesses = new SideButton(applyAllGuessesAction);															   
				unresolvedButtons.add(applyAllGuesses);
				
				unresolvedButtons.add(new JSeparator());
				SideButton selectInMap = new SideButton(selectAddressesInMapAction);															   
				unresolvedButtons.add(selectInMap);
				unresolvedPanel.add(unresolvedButtons, BorderLayout.SOUTH);
			} catch (Exception e) {				
				e.printStackTrace();
			}
			
			
			/* Map Panel */
			JPanel mapPanel = new JPanel(new BorderLayout());
			mapViewer = new JMapViewer();
			mapPanel.add(mapViewer, BorderLayout.CENTER);
			mapPanel.setMinimumSize(new Dimension(200, 200));
			mapViewer.setVisible(false);
						
			/* Combine panels */
			JSplitPane unresSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, streetPanel, unresolvedPanel);						
			JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, unresSplitPane, mapPanel);
			
			this.getContentPane().add(pane, BorderLayout.CENTER);
			//this.getContentPane().add(mapPanel, BorderLayout.SOUTH);
		} else {
			this.getContentPane().add(new JLabel(tr("(No data)")), BorderLayout.CENTER);
		}
		
		for (int i = 0; i < actions.length; i++) {
			actions[i].setContainer(addressEditContainer);
		}
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,10));
		JButton ok = new JButton(OK_COMMAND, ImageProvider.getIfAvailable(null, "ok"));
		ok.addActionListener(this);
		JButton selectAndClose = new JButton(SELECT_AND_CLOSE);
		selectAndClose.addActionListener(this);
		
		// Murks
		for (int i = 0; i < 8; i++) {
			buttonPanel.add(new JSeparator());
		}

		buttonPanel.add(ok);
		
		
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	private JLabel createHeaderLabel(String fmtString, int n) {
		JLabel label = new JLabel(String.format(fmtString, n));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}
	
	private void updateHeaders() {
		if (editContainer != null) {
			streetLabel.setText(String.format(STREET_HEADER_FMT, editContainer.getNumberOfStreets()));
			unresolvedAddressesLabel.setText(String.format(UNRESOLVED_HEADER_FMT, editContainer.getNumberOfUnresolvedAddresses()));
		} else {
			streetLabel.setText(String.format(STREET_HEADER_FMT, 0));
			unresolvedAddressesLabel.setText(String.format(UNRESOLVED_HEADER_FMT, 0));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (OK_COMMAND.equals(e.getActionCommand())) {
			this.setVisible(false);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		AddressEditSelectionEvent ev = new AddressEditSelectionEvent(e.getSource(),
				streetTable, unresolvedTable, editContainer);
		
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
		
		for (int i = 0; i < actions.length; i++) {
			actions[i].setContainer(container);
		}
	}

	@Override
	public void entityChanged(INodeEntity entity) {
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
	
	class IncompleteAddressPresetHandler implements PresetHandler {
		private List<OsmPrimitive> osmPrimitives;
		
		/**
		 * @param osmPrimitives
		 */
		public IncompleteAddressPresetHandler(List<OsmPrimitive> osmPrimitives) {
			super();
			this.osmPrimitives = osmPrimitives;
		}

		@Override
		public Collection<OsmPrimitive> getSelection() {
			// TODO Auto-generated method stub
			return osmPrimitives;
		}

		@Override
		public void updateTags(List<Tag> tags) {
			
		}
		
	}
}
