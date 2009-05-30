package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.plugins.tageditor.tagspec.KeyValuePair;



public class TabularTagSelector extends JPanel {

	private TagsTable tagsTable;
	private JTextField tfFilter;
	private JButton btnApply;
	private JScrollPane scrollPane;
	private final ArrayList<ITagSelectorListener> listeners = new ArrayList<ITagSelectorListener>();


	protected JPanel buildFilterPanel() {
		JPanel pnl = new JPanel();
		JLabel lbl = new JLabel(tr("Search: "));
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT));
		tfFilter = new JTextField(20);
		pnl.add(lbl);
		pnl.add(tfFilter,BorderLayout.CENTER);
		JButton btn = new JButton(tr("Filter"));
		pnl.add(btn);
		btn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						filter(tfFilter.getText());
					}

				}
		);
		btn = new JButton(tr("Clear"));
		pnl.add(btn);
		btn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tfFilter.setText("");
						tfFilter.requestFocus();
					}
				}
		);
		return pnl;
	}

	protected JScrollPane buildPresetGrid() {

		tagsTable = new TagsTable(new TagsTableModel(),new TagsTableColumnModel());
		getModel().initFromTagSpecifications();

		scrollPane = new JScrollPane(tagsTable);

		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		// this adapters ensures that the width of the tag table columns is adjusted
		// to the width of the scroll pane viewport. Also tried to overwrite
		// getPreferredViewportSize() in JTable, but did not work.
		//
		scrollPane.addComponentListener(
				new ComponentAdapter() {
					@Override public void componentResized(ComponentEvent e) {
						super.componentResized(e);
						Dimension d = scrollPane.getViewport().getExtentSize();
						tagsTable.adjustColumnWidth(d.width);
					}
				}
		);

		// add the double click listener
		//
		tagsTable.addMouseListener(new DoubleClickAdapter());

		// replace Enter action. apply the current preset on enter
		//
		tagsTable.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
		ActionListener enterAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowNum = tagsTable.getSelectedRow();
				if (rowNum >= 0) {
					KeyValuePair item = getModel().getVisibleItem(rowNum);
					fireItemSelected(item);
				}
			}
		};

		tagsTable.registerKeyboardAction(
				enterAction,
				"Enter",
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),
				JComponent.WHEN_FOCUSED
		);

		return scrollPane;
	}


	protected JPanel buildControlButtonPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT));
		btnApply = new JButton("Apply");
		pnl.add(btnApply);
		btnApply.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						int row = tagsTable.getSelectedRow();
						if (row >=0) {
							KeyValuePair item = getModel().getVisibleItem(row);
							fireItemSelected(item);
						}
					}
				}
		);
		return pnl;
	}

	protected void build() {
		setLayout(new BorderLayout());
		add(buildFilterPanel(), BorderLayout.NORTH);
		add(buildPresetGrid(), BorderLayout.CENTER);
		add(buildControlButtonPanel(), BorderLayout.SOUTH);

		// wire the text field for filter expressions to the prests
		// table
		//
		tfFilter.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent arg0) {
						onUpdate();
					}

					public void insertUpdate(DocumentEvent arg0) {
						onUpdate();
					}

					public void removeUpdate(DocumentEvent arg0) {
						onUpdate();
					}

					protected void onUpdate() {
						filter(tfFilter.getText());
					}
				}
		);

		tfFilter.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						filter(tfFilter.getText());
					}
				}
		);

		// wire the apply button to the selection model of the preset table
		//
		tagsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						btnApply.setEnabled(tagsTable.getSelectedRowCount() != 0);
					}
				}
		);


		// load the set of presets and bind them to the preset table
		//
		tagsTable.getSelectionModel().clearSelection();
		btnApply.setEnabled(false);

	}

	public TabularTagSelector() {
		build();
	}


	public void filter(String filter) {
		tagsTable.getSelectionModel().clearSelection();
		getModel().filter(filter);

		tagsTable.scrollRectToVisible(tagsTable.getCellRect(0, 0, false));

		// we change the number of rows by applying a filter condition. Because
		// the table is embedded in a JScrollPane which again may be embedded in
		// other JScrollPanes or JSplitPanes it seems that we have to recalculate
		// the layout and repaint the component tree. Maybe there is a more efficient way
		// to keep the GUI in sync with the number of rows in table. By trial
		// and error I ended up with the following lines.
		//
		Component c = tagsTable;
		while(c != null) {
			c.doLayout();
			c.repaint();
			c = c.getParent();
		}
	}

	protected TagsTableModel getModel() {
		return (TagsTableModel)tagsTable.getModel();
	}


	public void addTagSelectorListener(ITagSelectorListener listener) {
		synchronized(this.listeners) {
			if (listener != null && ! listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removeTagSelectorListener(ITagSelectorListener listener) {
		synchronized(this.listeners) {
			if (listener != null) {
				listeners.remove(listener);
			}
		}
	}

	protected void fireItemSelected(KeyValuePair pair) {
		synchronized(this.listeners) {
			for(ITagSelectorListener listener: listeners) {
				listener.itemSelected(pair);
			}
		}
	}

	private class DoubleClickAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int rowNum = tagsTable.rowAtPoint(e.getPoint());
				KeyValuePair pair = getModel().getVisibleItem(rowNum);
				fireItemSelected(pair);
			}
		}
	}
}
