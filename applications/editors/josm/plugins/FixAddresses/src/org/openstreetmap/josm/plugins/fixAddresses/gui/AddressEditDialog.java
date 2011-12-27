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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IAddressEditContainerListener;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;
import org.openstreetmap.josm.plugins.fixAddresses.StringUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AddressActions;
import org.openstreetmap.josm.tools.ImageProvider;

@SuppressWarnings("serial")
public class AddressEditDialog extends JDialog implements ActionListener, ListSelectionListener, IAddressEditContainerListener {
	private static final String UNRESOLVED_ADDRESS = tr("Unresolved Addresses");
	private static final String STREETS = tr("Streets");
	private static final String UNRESOLVED_HEADER_FMT = "%s (%d)";
	private static final String STREET_HEADER_FMT = "%s (%d)";
	private static final String OK_COMMAND = tr("Close");
	private static final String SELECT_AND_CLOSE = tr("Select and close");

	private AddressEditContainer editContainer;
	private JTable unresolvedTable;
	private JTable streetTable;

	private AbstractAddressEditAction[] actions = new AbstractAddressEditAction[] {
		AddressActions.getResolveAction(),
		AddressActions.getGuessAddressAction(),
		AddressActions.getApplyGuessesAction(),
		AddressActions.getSelectAction(),
		AddressActions.getRemoveTagsAction(),
		AddressActions.getConvertToRelationAction(),
		AddressActions.getConvertAllToRelationAction()
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
			streetTable.addKeyListener(new JumpToEntryListener(1));

			JScrollPane scroll1 = new JScrollPane(streetTable);
			streetPanel.add(scroll1, BorderLayout.CENTER);

			streetLabel = createHeaderLabel(STREET_HEADER_FMT,
					tr(STREETS),
					editContainer.getNumberOfStreets());

			JPanel headerPanel = new JPanel(new GridLayout(1, 4));
			headerPanel.setMinimumSize(new Dimension(100, 30));
			headerPanel.add(streetLabel);

			/*
			JPanel streetButtonPanel = new JPanel(new GridLayout(1, 3));
			SideButton convertToRel = new SideButton(convertToRelationAction);
			streetButtonPanel.add(convertToRel);
			// SideButton convertAllToRel = new SideButton(convertAllToRelationAction);
			// streetButtonPanel.add(convertAllToRel);
			// add filler
			streetButtonPanel.add(new JPanel());
			streetButtonPanel.add(new JPanel());


			streetPanel.add(streetButtonPanel, BorderLayout.SOUTH);
			*/
			streetPanel.add(headerPanel, BorderLayout.NORTH);
			streetPanel.setMinimumSize(new Dimension(500, 200));

			/* Panel for unresolved addresses table */
			JPanel unresolvedPanel = new JPanel(new BorderLayout());
			UnresolvedAddressesTableModel uaModel = new UnresolvedAddressesTableModel(editContainer);
			unresolvedTable = new JTable(uaModel);
			unresolvedTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			unresolvedTable.getSelectionModel().addListSelectionListener(this);
			unresolvedTable.getSelectionModel().addListSelectionListener(new IncompleteAddressListener());
			unresolvedTable.addMouseListener(AddressActions.getApplyGuessesAction());

			JTableHeader header = unresolvedTable.getTableHeader();
			header.addMouseListener(uaModel.new ColumnListener(unresolvedTable));

			JScrollPane scroll2 = new JScrollPane(unresolvedTable);
			unresolvedPanel.add(scroll2, BorderLayout.CENTER);
			unresolvedAddressesLabel = createHeaderLabel(
					UNRESOLVED_HEADER_FMT,
					tr(UNRESOLVED_ADDRESS),
					editContainer.getNumberOfUnresolvedAddresses());

			JPanel headerPanel2 = new JPanel(new GridLayout(1, 4));
			headerPanel2.setMinimumSize(new Dimension(100, 30));
			headerPanel2.add(unresolvedAddressesLabel);
			unresolvedPanel.add(headerPanel2 , BorderLayout.NORTH);
			unresolvedPanel.setMinimumSize(new Dimension(500, 200));


			try {
				JPanel unresolvedButtons = new JPanel(new GridLayout(2,5, 5, 5));
				SideButton assign = new SideButton(AddressActions.getResolveAction());
				unresolvedButtons.add(assign);

				SideButton guess = new SideButton(AddressActions.getGuessAddressAction());
				unresolvedButtons.add(guess);
				SideButton applyAllGuesses = new SideButton(AddressActions.getApplyGuessesAction());
				unresolvedButtons.add(applyAllGuesses);

				SideButton removeAddressTags = new SideButton(AddressActions.getRemoveTagsAction());
				unresolvedButtons.add(removeAddressTags);

				unresolvedButtons.add(new JPanel());

				SideButton selectInMap = new SideButton(AddressActions.getSelectAction());
				unresolvedButtons.add(selectInMap);
				headerPanel2.setMinimumSize(new Dimension(100, 70));

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

			JPanel mapControl = new JPanel(new GridLayout(1, 4));
			JLabel mapL1 = new JLabel(tr("Complete Addresses"));
			mapL1.setForeground(Color.BLUE);
			mapControl.add(mapL1);

			JLabel mapL2 = new JLabel(tr("Incomplete Addresses"));
			mapL2.setForeground(Color.RED);
			mapControl.add(mapL2);

			JLabel mapL3 = new JLabel(tr("Selected Addresses"));
			mapL3.setForeground(Color.ORANGE);
			mapControl.add(mapL3);

			JLabel mapL4 = new JLabel(tr("Selected Street"));
			mapL4.setForeground(Color.GREEN);
			mapControl.add(mapL4);

			mapPanel.add(mapControl, BorderLayout.SOUTH);

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

	/**
	 * Creates a header label in the form "title (number)" with bold font.
	 * @param fmtString The format string having a string and a numeric placeholder.
	 * @param title The title of the header.
	 * @param n The number to show in the header.
	 * @return
	 */
	private JLabel createHeaderLabel(String fmtString, String title, int n) {
		JLabel label = new JLabel(String.format(fmtString, title, n));
		label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize() + 2));
		label.setBorder(new EmptyBorder(5,2,4,5));
		return label;
	}

	/**
	 * Updates the list headings.
	 */
	private void updateHeaders() {
		if (editContainer != null) {
			streetLabel.setText(String.format(
					STREET_HEADER_FMT,
					STREETS,
					editContainer.getNumberOfStreets()));
			unresolvedAddressesLabel.setText(
					String.format(UNRESOLVED_HEADER_FMT,
							UNRESOLVED_ADDRESS,
							editContainer.getNumberOfUnresolvedAddresses()));
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
				streetTable, unresolvedTable, null, editContainer);

		for (AbstractAddressEditAction action : actions) {
			action.setEvent(ev);
		}

		clearMapViewer();
		OSMStreet sNode = ev.getSelectedStreet();
		if (sNode != null) {

			//mapViewer.addMapRectangle(new BBoxMapRectangle(bb));
			for (IOSMEntity seg : sNode.getChildren()) {
				Way way = (Way) seg.getOsmObject();
				//BBox bb = way.getBBox();

				for (Node node : way.getNodes()) {
					mapViewer.addMapMarker(new MapMarkerDot(Color.GREEN, node.getCoor().lat(), node.getCoor().lon()));
				}
			}

			// show addresses as blue marker
			if (sNode.hasAddresses()) {
				for (OSMAddress aNode : sNode.getAddresses()) {
					Color markerCol = Color.BLUE;
					if (!aNode.isComplete()) {
						markerCol = Color.RED;
					}
					mapViewer.addMapMarker(new MapMarkerDot(markerCol, aNode.getCoor().lat(), aNode.getCoor().lon()));
				}
			}
		}

		List<OSMAddress> unrAddresses = ev.getSelectedUnresolvedAddresses();
		if (unrAddresses != null) {
			for (OSMAddress aNode : unrAddresses) {
				mapViewer.addMapMarker(new MapMarkerDot(Color.ORANGE, aNode.getCoor().lat(), aNode.getCoor().lon()));
			}
		}
		mapViewer.setDisplayToFitMapMarkers();
		mapViewer.setVisible(true);
	}

	/**
	 * Removes all markers and rectangles from the map viewer.
	 */
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
			actions[i].setEvent(null);
			actions[i].setContainer(container);
		}
	}

	@Override
	public void entityChanged(IOSMEntity entity) {
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

	/**
	 * The listener interface for receiving key events of a table.
	 * The class that is interested in processing a jumpToEntry
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addJumpToEntryListener<code> method. When
	 * the jumpToEntry event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see JumpToEntryEvent
	 */
	class JumpToEntryListener implements KeyListener {
		private int column;

		/**
		 * Instantiates a new jump-to-entry listener.
		 * @param column the column of the table to use for the comparison
		 */
		public JumpToEntryListener(int column) {
			super();
			this.column = column;
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			JTable table  = (JTable) arg0.getSource();

			if (table == null) return;

			TableModel model = table.getModel();

			if (model == null || model.getColumnCount() == 0) {
				return;
			}
			// clip column
			if (column < 0 || column >= model.getColumnCount()) {
				column = 0; // use the first column
			}

			char firstChar = Character.toLowerCase(arg0.getKeyChar());

			// visit every row and find a matching entry
			for (int i = 0; i < model.getRowCount(); i++) {
				Object obj = model.getValueAt(i, column);
				if (obj != null) {
					String s = obj.toString();
					if (s.length() > 0 && firstChar == Character.toLowerCase(s.charAt(0))) {
						// select entry and make it visible in the table
						table.getSelectionModel().setSelectionInterval(i, i);
						table.scrollRectToVisible(streetTable.getCellRect(i, 0, true));
						return;
					}
				}
			}
		}
	}
}
