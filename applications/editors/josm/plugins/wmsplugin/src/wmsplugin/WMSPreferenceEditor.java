package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;


import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

public class WMSPreferenceEditor implements PreferenceSetting {
	
	private Map<String,String> orig;
	private DefaultTableModel model;
	private int highestIdUsed = 0;
	private HashMap<Integer, WMSInfo> oldValues = new HashMap<Integer, WMSInfo>();
	
	public void addGui(final PreferenceDialog gui) {
		JPanel p = gui.createPreferenceTab("wms", tr("WMS Plugin Preferences"), tr("Modify list of WMS servers displayed in the WMS plugin menu"));
		
		model = new DefaultTableModel(new String[]{"#", "Menu Name", "WMS URL"}, 0) {
			@Override public boolean isCellEditable(int row, int column) {
				return column != 0;
			}
		};
		final JTable list = new JTable(model);
		list.getColumnModel().removeColumn(list.getColumnModel().getColumn(0));
		JScrollPane scroll = new JScrollPane(list);
		p.add(scroll, GBC.eol().fill(GBC.BOTH));
		scroll.setPreferredSize(new Dimension(400,200));
		
		for (WMSInfo i : WMSPlugin.wmsList) {
			oldValues.put(i.prefid, i);
			model.addRow(new String[]{Integer.toString(i.prefid), i.name, i.url});
			if (i.prefid > highestIdUsed) highestIdUsed = i.prefid;
		}
		
		JButton add = new JButton(tr("Add"));
		p.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		p.add(add, GBC.std().insets(0,5,0,0));
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel(new GridBagLayout());
				p.add(new JLabel("Menu Name"), GBC.std().insets(0,0,5,0));
				JTextField key = new JTextField(10);
				JTextField value = new JTextField(10);
				p.add(key, GBC.eop().insets(5,0,0,0).fill(GBC.HORIZONTAL));
				p.add(new JLabel("WMS URL"), GBC.std().insets(0,0,5,0));
				p.add(value, GBC.eol().insets(5,0,0,0).fill(GBC.HORIZONTAL));
				int answer = JOptionPane.showConfirmDialog(gui, p, tr("Enter a menu name and WMS URL"), JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					highestIdUsed++;
					model.addRow(new String[]{Integer.toString(highestIdUsed), key.getText(), value.getText()});
				}
			}
		});
				
		JButton delete = new JButton(tr("Delete"));
		p.add(delete, GBC.std().insets(0,5,0,0));
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(gui, tr("Please select the row to delete."));
					return;
				}
				while (list.getSelectedRow() != -1)
					model.removeRow(list.getSelectedRow());
			}
		});
	}
	
	public void ok() {
		boolean change = false;
		for (int i = 0; i < model.getRowCount(); ++i) {
			int id = Integer.parseInt(model.getValueAt(i, 0).toString());
			String name = model.getValueAt(i,1).toString();
			String url = model.getValueAt(i,2).toString();
			
			WMSInfo origValue = oldValues.get(id);
			if (origValue == null) {
				new WMSInfo(name, url, id).save();
				change = true;
			} else {
				if (origValue.name.equals(name) && origValue.url.equals(url)) {
					// no change
				} else {
					origValue.name = name; 
					origValue.url = url;
					origValue.save();
					change = true;
				}
				oldValues.remove(id);
			}
		}
		
		// josm doesn't seem to give us an option to delete preferences, 
		// we can only overwrite them with empty strings...
		for (WMSInfo i : oldValues.values()) {
			i.url = ""; 
			i.name = "";
			i.save();
			change = true;
		}
		
		if (change) WMSPlugin.refreshMenu();
	}
	
    /**
     * Updates a server URL in the preferences dialog. Used by other plugins.
     * 
     * @param server The server name
     * @param url The server URL
     */
    public void setServerUrl(String server, String url)
    {
        for (int i = 0; i < model.getRowCount(); i++) 
        {
            String name = model.getValueAt(i,1).toString();
            if( name.equals(server) )
            {
                model.setValueAt(url, i, 2);
                return;
            }
        }        

        highestIdUsed++;
        model.addRow(new String[]{Integer.toString(highestIdUsed), server, url});
    }
    
    /**
     * Gets a server URL in the preferences dialog. Used by other plugins.
     * 
     * @param server The server name
     * @return The server URL
     */
    public String getServerUrl(String server)
    {
        for (int i = 0; i < model.getRowCount(); i++) 
        {
            String name = model.getValueAt(i,1).toString();
            if( name.equals(server) )
            {
                String url = model.getValueAt(i,2).toString();
                return url;
            }
        }
        
        return null;
    }
}

