package org.openstreetmap.josm.plugins.gpxpoints;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Main panel class - this shows all opened gpx waypoints in the table, allowing to select any of them 
 * to center waypoint at the map. All waypoints are sent from main plugin class to <b>addPoints</b> and <b>removePoints</b> methods.  
 * 
 * @author larry0ua
 *
 */
@SuppressWarnings("serial")
public class GpxPointsPanel extends ToggleDialog {

	/**
	 * Main plugin panel icon, stored to images/dialogs
	 */
	private static final String ICON_PANEL = "marker";
	
	private final JTable pointsList;
	private final DefaultTableModel gpxTableModel;
	
	private int lastSelected = -1;
	List<Marker> tableData = new ArrayList<Marker>();
	
	/**
	 * Action to hold hotkey action. Next button will run the same action - no code duplicating.
	 */
	JosmAction nextGpxAction;

	public GpxPointsPanel(String name, String iconName, String tooltip,
			Shortcut shortcut, int preferredHeight, boolean defShow) {
		super(name, iconName, tooltip, shortcut, preferredHeight, defShow);

		nextGpxAction = new NextGpxAction();

		gpxTableModel = new DefaultTableModel();
		pointsList = new JTable(gpxTableModel){
			@Override
			public boolean isCellEditable(int nRow, int nCol) {
				return false; // non-editable table
			}
		};

		gpxTableModel.setColumnCount(1);
		gpxTableModel.setColumnIdentifiers(new Object[]{tr("Name")});

		pointsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pointsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (pointsList == null) {
					return;
				}
				int selected = pointsList.getSelectedRow();
				if (selected == lastSelected || selected == -1) {
					return;
				}
				lastSelected = selected;
				if (Main.map != null && Main.map.mapView != null) {
					Main.map.mapView.zoomTo(tableData.get(selected).getCoor());
				}
			}
		});

		add(new JScrollPane(pointsList), BorderLayout.CENTER);

		JButton btn = new JButton(tr("Next"));

		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextGpxAction.actionPerformed(null);
			}
		});
		add(btn, BorderLayout.SOUTH);

	}
	public GpxPointsPanel() {
		this(tr("GPX Waypoints Enum"), ICON_PANEL,
				tr("Waypoint Enumerator"), null, 200, true);
	}
	
	/**
	 * Add collection of waypoints to the list. This function is called after opening new waypoint layer from file.
	 * @param points
	 */
	public void addPoints(Collection<Marker> points) {
		tableData.addAll(points);
		for(Marker p : points) {
			String name = p.getText();
			if (name == null) {
				name = "noname";
			}
			gpxTableModel.addRow(new Object[]{name});
		}
	}
	
	/**
	 * Remove collection of waypoints from the list.
	 * @param points
	 */
	public void removePoints(Collection<Marker> points) {
		for(Marker p : points) {
			int position = tableData.indexOf(p);
			if (position < 0) {
				continue;
			}
			gpxTableModel.removeRow(position);
			tableData.remove(position);
		}
	}

	/**
	 * Action class to handle shortcut or button action.
	 * @author larry0ua
	 *
	 */
	class NextGpxAction extends JosmAction {
		public NextGpxAction() {
			super(tr("Next GPX Waypoint"), "nextgpx", tr("Select to the next Waypoint in List"),
					Shortcut.registerShortcut("gpxenum:next", tr("Tool: {0}", tr("Next GPX Waypoint")),
							KeyEvent.VK_ADD, Shortcut.GROUP_MENU), false); // ctrl + Numpad '+' - default key
		}
		@Override
		public void actionPerformed(ActionEvent action) {
			int rowSel = pointsList.getSelectedRow();
			if (rowSel<0) {
				rowSel = 0;
			}
			if (rowSel < pointsList.getRowCount()-1) {
				pointsList.getSelectionModel().setSelectionInterval(rowSel+1, rowSel+1);
				pointsList.scrollRectToVisible(pointsList.getCellRect(rowSel+2, 0, true));
			}
		}

	}
}
