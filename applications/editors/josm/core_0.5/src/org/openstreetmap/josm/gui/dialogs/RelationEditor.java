package org.openstreetmap.josm.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.xnap.commons.i18n.I18n.marktr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This dialog is for editing relations.
 * 
 * In the basic form, it provides two tables, one with the relation tags
 * and one with the relation members. (Relation tags can be edited through 
 * the normal properties dialog as well, if you manage to get an relation 
 * selected!)
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class RelationEditor extends JFrame {

	/**
	 * The relation that this editor is working on, and the clone made for
	 * editing.
	 */
	private final Relation relation;
	private final Relation clone;
	
	/**
	 * The property data.
	 */
	private final DefaultTableModel propertyData = new DefaultTableModel() {
		@Override public boolean isCellEditable(int row, int column) {
			return true;
		}
		@Override public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
	};

	/**
	 * The membership data.
	 */
	private final DefaultTableModel memberData = new DefaultTableModel() {
		@Override public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
		@Override public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == 1 ? OsmPrimitive.class : String.class;
		}
	};
	
	/**
	 * The properties and membership lists.
	 */
	private final JTable propertyTable = new JTable(propertyData);
	private final JTable memberTable = new JTable(memberData);
	
	/**
	 * Creates a new relation editor for the given relation. The relation
	 * will be saved if the user selects "ok" in the editor.
	 * 
	 * If no relation is given, will create an editor for a new relation.
	 * 
	 * @param relation relation to edit, or null to create a new one.
	 */
	public RelationEditor(Relation relation)
	{
		super(tr("Edit Relation"));
		this.relation = relation;
		
		if (relation == null) {
			// create a new relation
			this.clone = new Relation();
		} else {
			// edit an existing relation
			this.clone = new Relation(relation);	
		}
		
		getContentPane().setLayout(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		getContentPane().add(tabPane, BorderLayout.CENTER);
		
		// (ab)use JOptionPane to make this look familiar;
		// hook up with JOptionPane's property change event
		// to detect button click
		final JOptionPane okcancel = new JOptionPane("", 
			JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
		getContentPane().add(okcancel, BorderLayout.SOUTH);
		
		okcancel.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY) && event.getNewValue() != null) {
					if ((Integer)event.getNewValue() == JOptionPane.OK_OPTION) {
						// clicked ok!
						if (RelationEditor.this.relation == null) {
							Main.main.undoRedo.add(new AddCommand(clone));
						} else if (!RelationEditor.this.relation.realEqual(clone, true)) {
							Main.main.undoRedo.add(new ChangeCommand(RelationEditor.this.relation, clone));
						}
					}
					setVisible(false);
				}
			}
		});

		JLabel help = new JLabel("<html><em>"+
			"This is the basic relation editor which allows you to change the relation's tags " +
			"as well as the members. In addition to this we should have a smart editor that " +
			"detects the type of relationship and limits your choices in a sensible way.</em></html>");
		
		getContentPane().add(help, BorderLayout.NORTH);		
		try { setAlwaysOnTop(true); } catch (SecurityException sx) {}
		
		// Basic Editor panel has two blocks; 
		// a tag table at the top and a membership list below.

		// setting up the properties table
		
		propertyData.setColumnIdentifiers(new String[]{tr("Key"),tr("Value")});
		propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propertyData.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
					int row = tme.getFirstRow();
			
					if (!(tme.getColumn() == 0 && row == propertyData.getRowCount() -1)) {
						clone.entrySet().clear();
						for (int i = 0; i < propertyData.getRowCount(); i++) {
							String key = propertyData.getValueAt(i, 0).toString();
							String value = propertyData.getValueAt(i, 1).toString();
							if (key.length() > 0 && value.length() > 0) clone.put(key, value);
						}
						refreshTables();
					}
				}
			}
		});
		propertyTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		// setting up the member table
		
	    memberData.setColumnIdentifiers(new String[]{tr("Role"),tr("Occupied By")});
		memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memberTable.getColumnModel().getColumn(1).setCellRenderer(new OsmPrimitivRenderer());
		/*
		memberTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
				if (c instanceof JLabel) {
					((OsmPrimitive)value).visit(nameVisitor);
					((JLabel)c).setText(nameVisitor.name);
				}
				return c;
			}
		});	
		*/
		memberData.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE && tme.getColumn() == 0) {
					int row = tme.getFirstRow();
					clone.members.get(row).role = memberData.getValueAt(row, 0).toString();
				}
			}
		});
		memberTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		
		// combine both tables and wrap them in a scrollPane
		JPanel bothTables = new JPanel();
		bothTables.setLayout(new GridBagLayout());
		bothTables.add(new JLabel(tr("Tags (empty value deletes tag)")), GBC.eol().fill(GBC.HORIZONTAL));
		bothTables.add(new JScrollPane(propertyTable), GBC.eop().fill(GBC.BOTH));
		bothTables.add(new JLabel(tr("Members")), GBC.eol().fill(GBC.HORIZONTAL));
		bothTables.add(new JScrollPane(memberTable), GBC.eol().fill(GBC.BOTH));
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,3));
		
		buttonPanel.add(createButton(marktr("Add Selected"),tr("Add all currently selected objects as members"), KeyEvent.VK_S, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSelected();
			}
		}));

		buttonPanel.add(createButton(marktr("Delete"),tr("Remove the selected member from this relation"), KeyEvent.VK_D, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = memberTable.getSelectedRow();
				RelationMember mem = new RelationMember();
				mem.role = memberTable.getValueAt(row, 0).toString();
				mem.member = (OsmPrimitive) memberTable.getValueAt(row, 1);
				clone.members.remove(mem);
				refreshTables();
			}
		}));
		bothTables.add(buttonPanel, GBC.eop().fill(GBC.HORIZONTAL));

		tabPane.add(bothTables, "Basic");
		
		refreshTables();
		
		setSize(new Dimension(400, 500));
		setLocationRelativeTo(Main.parent);
	}
	
	private void refreshTables() {
		
		// re-load property data
		
		propertyData.setRowCount(0);
		for (Entry<String, String> e : clone.entrySet()) {
			propertyData.addRow(new Object[]{e.getKey(), e.getValue()});
		}
		propertyData.addRow(new Object[]{"", ""});
		
		// re-load membership data
		
		memberData.setRowCount(0);
		for (RelationMember em : clone.members) {
			memberData.addRow(new Object[]{em.role, em.member});
		}
	}
	
	private JButton createButton(String name, String tooltip, int mnemonic, ActionListener actionListener) {
		JButton b = new JButton(tr(name), ImageProvider.get("dialogs", name.toLowerCase()));
		b.setActionCommand(name);
		b.addActionListener(actionListener);
		b.setToolTipText(tooltip);
		b.setMnemonic(mnemonic);
		b.putClientProperty("help", "Dialog/Properties/"+name);
		return b;
	}
	
	private void addSelected() {
		for (OsmPrimitive p : Main.ds.getSelected()) {
			RelationMember em = new RelationMember();
			em.member = p;
			em.role = "";
			clone.members.add(em);
		}
		refreshTables();
	}
}
