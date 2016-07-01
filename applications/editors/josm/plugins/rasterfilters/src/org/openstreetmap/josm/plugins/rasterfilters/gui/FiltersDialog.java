package org.openstreetmap.josm.plugins.rasterfilters.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.rasterfilters.model.FiltersManager;
import org.openstreetmap.josm.plugins.rasterfilters.preferences.FiltersDownloader;

/**
 * This filters is responsible for creating filter's dialog where user can
 * choose and add new filter at this dialog.
 *
 * @author Nipel-Crumple
 *
 */
public class FiltersDialog {

	public JFrame frame;
	private JComboBox<String> filterChooser;
	private JPanel pane;
	private JButton addButton;
	private DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
	private Set<String> showedFiltersTitles = new HashSet<>();
	private JPanel filterContainer;
	private Layer layer;
	private FiltersManager filtersManager;
	private JScrollPane filterContainerScroll;

	public FiltersDialog(ImageryLayer layer) {
		this.layer = layer;
		this.filtersManager = new FiltersManager(this);
		layer.addImageProcessor(filtersManager);
	}

	public JPanel createFilterContainer() {
		if (filterContainer == null) {

			filterContainer = new JPanel();
			filterContainer.setLayout(new BoxLayout(getFilterContainer(),
					BoxLayout.Y_AXIS));
			filterContainer.setBackground(Color.white);

			filterContainerScroll = new JScrollPane(getFilterContainer(),
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			pane.add(filterContainerScroll);

		}

		return filterContainer;
	}

	public void deleteFilterContainer() {

		Component parent = filterContainerScroll.getParent();
		filterContainerScroll.removeAll();
		((JPanel) parent).remove(filterContainerScroll);

		filterContainer = null;

		parent.revalidate();
		parent.repaint();
	}

	public JFrame createAndShowGUI() throws MalformedURLException {
		listModel.removeAllElements();

		Set<String> filterTitles = FiltersDownloader.filterTitles;

		for (String temp : filterTitles) {

			if (!showedFiltersTitles.contains(temp)) {
				listModel.addElement(temp);
			}

		}

		if (frame != null) {

			filterChooser.setModel(listModel);
			filterChooser.revalidate();
			frame.setVisible(true);

		} else {

			frame = new JFrame();
			String title = "Filters | " + layer.getName();
			frame.setTitle(title);
			frame.setMinimumSize(new Dimension(350, 420));
			frame.setPreferredSize(new Dimension(350, 420));

			pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

			pane.setBorder(new EmptyBorder(10, 5, 10, 5));
			pane.setPreferredSize(new Dimension(300, 400));
			pane.setBackground(Color.white);

			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
			topPanel.setMaximumSize(new Dimension(300, 50));
			topPanel.setMinimumSize(new Dimension(300, 50));
			topPanel.setBackground(Color.white);

			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
			labelPanel.setMaximumSize(new Dimension(300, 20));
			labelPanel.setBackground(Color.white);

			JLabel label = new JLabel("Add filter");
			labelPanel.add(label);
			// pane.add(labelPanel);

			// TODO why after add clicked the top panel is resized???

			// panel that contains the checkBox and add button
			JPanel chooseFilterPanel = new JPanel();
			chooseFilterPanel.setMinimumSize(new Dimension(300, 30));
			chooseFilterPanel.setLayout(new BoxLayout(chooseFilterPanel,
					BoxLayout.X_AXIS));
			chooseFilterPanel.setBackground(Color.white);

			filterChooser = new JComboBox<>(getListModel());
			filterChooser.setMaximumSize(new Dimension(200, 30));
			chooseFilterPanel.add(filterChooser);

			// empty space area between select and add button
			chooseFilterPanel.add(Box.createRigidArea(new Dimension(10, 0)));

			addButton = new JButton();
			addButton.setText("add");
			addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			addButton.setMaximumSize(new Dimension(90, 30));
			addButton.addActionListener(new AddFilterToPanelListener());
			//
			// // check if there is no meta information
			// Main.debug("Empty " +
			// String.valueOf(FiltersDownloader.filterTitles.isEmpty()));
			// if (FiltersDownloader.filterTitles.isEmpty() ||
			// listModel.getSize() == 0) {
			// addButton.setEnabled(false);
			// filterChooser.setEnabled(false);
			// } else {
			// addButton.setEnabled(true);
			// filterChooser.setEnabled(true);
			// }

			chooseFilterPanel.add(getAddButton());

			topPanel.add(labelPanel);
			topPanel.add(chooseFilterPanel);
			pane.add(topPanel);
			// pane.add(chooseFilterPanel);
			// pane.add(Box.createRigidArea(new Dimension(0, 20)));

			frame.setContentPane(pane);
			frame.pack();
			frame.setVisible(true);
		}

		if (FiltersDownloader.filterTitles.isEmpty()
				|| listModel.getSize() == 0) {
			addButton.setEnabled(false);
			filterChooser.setEnabled(false);
		} else {
			addButton.setEnabled(true);
			filterChooser.setEnabled(true);
		}

		return frame;
	}

	public FiltersManager createFilterManager() {
		return new FiltersManager(this);
	}

	public void closeFrame() {
		if (frame != null) {
			if (frame.isShowing()) {
				frame.dispose();
			}
		}
	}

	public FiltersManager getFiltersManager() {
		return filtersManager;
	}

	class AddFilterToPanelListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String title = (String) listModel.getSelectedItem();
			JPanel panel = null;

			panel = filtersManager.createPanelByTitle(title);

			if (panel != null) {
				filterContainer = createFilterContainer();
				filterContainer.add(panel);
			}

			listModel.removeElement(title);
			showedFiltersTitles.add(title);

			if (listModel.getSize() == 0) {
				filterChooser.setEnabled(false);
				addButton.setEnabled(false);
			}

		}
	}

	public Set<String> getShowedFiltersTitles() {
		return showedFiltersTitles;
	}

	public Layer getLayer() {
		return layer;
	}

	public JPanel getFilterContainer() {
		return filterContainer;
	}

	public DefaultComboBoxModel<String> getListModel() {
		return listModel;
	}

	public JComboBox<String> getFilterChooser() {
		return filterChooser;
	}

	public JButton getAddButton() {
		return addButton;
	}
}
