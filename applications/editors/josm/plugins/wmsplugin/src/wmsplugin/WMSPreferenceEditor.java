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
	private HashMap<Integer, WMSInfo> oldValues = new HashMap<Integer, WMSInfo>();
	
	public void addGui(final PreferenceDialog gui) {
		JPanel p = gui.createPreferenceTab("wms", tr("WMS Plugin Preferences"), tr("Modify list of WMS servers displayed in the WMS plugin menu"));
		
		model = new DefaultTableModel(new String[]{tr("Menu Name"), tr("WMS URL")}, 0);
		final JTable list = new JTable(model);
		JScrollPane scroll = new JScrollPane(list);
		p.add(scroll, GBC.eol().fill(GBC.BOTH));
		scroll.setPreferredSize(new Dimension(400,200));
		
		for (WMSInfo i : WMSPlugin.wmsList) {
			oldValues.put(i.prefid, i);
			model.addRow(new String[]{i.name, i.url});
		}
		
		JButton add = new JButton(tr("Add"));
		p.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		p.add(add, GBC.std().insets(0,5,0,0));
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel(new GridBagLayout());
				p.add(new JLabel(tr("Menu Name")), GBC.std().insets(0,0,5,0));
				JTextField key = new JTextField(10);
				JTextField value = new JTextField(10);
				p.add(key, GBC.eop().insets(5,0,0,0).fill(GBC.HORIZONTAL));
				p.add(new JLabel(tr("WMS URL")), GBC.std().insets(0,0,5,0));
				p.add(value, GBC.eol().insets(5,0,0,0).fill(GBC.HORIZONTAL));
				int answer = JOptionPane.showConfirmDialog(gui, p, tr("Enter a menu name and WMS URL"), JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					model.addRow(new String[]{key.getText(), value.getText()});
				}
			}
		});

		JButton delete = new JButton(tr("Delete"));
		p.add(delete, GBC.std().insets(0,5,0,0));
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedRow() == -1)
					JOptionPane.showMessageDialog(gui, tr("Please select the row to delete."));
				else
				{
					Integer i;
					while ((i = list.getSelectedRow()) != -1)
						model.removeRow(i);
				}
			}
		});
	}
	
	public void ok() {
		boolean change = false;
		for (int i = 0; i < model.getRowCount(); ++i) {
			String name = model.getValueAt(i,0).toString();
			String url = model.getValueAt(i,1).toString();

			WMSInfo origValue = oldValues.get(i);
			if (origValue == null)
			{
				new WMSInfo(name, url, i).save();
				change = true;
			}
			else
			{
				if (!origValue.name.equals(name) || !origValue.url.equals(url))
				{
					origValue.name = name; 
					origValue.url = url;
					origValue.save();
					change = true;
				}
				oldValues.remove(i);
			}
		}
		
		// using null values instead of empty string really deletes
		// the preferences entry
		for (WMSInfo i : oldValues.values())
		{
			i.url = null;
			i.name = null;
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
            if( server.equals(model.getValueAt(i,0).toString()) )
            {
                model.setValueAt(url, i, 1);
                return;
            }
        }
        model.addRow(new String[]{server, url});
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
            if( server.equals(model.getValueAt(i,0).toString()) )
            {
                return model.getValueAt(i,1).toString();
            }
        }
        return null;
    }
}

