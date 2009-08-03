package wmsplugin;

import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.gui.OptionPaneUtil;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

public class WMSPreferenceEditor implements PreferenceSetting {
    private Map<String,String> orig;
    private DefaultTableModel model;
    private JComboBox browser;
    private HashMap<Integer, WMSInfo> oldValues = new HashMap<Integer, WMSInfo>();

    public void addGui(final PreferenceDialog gui) {
        JPanel p = gui.createPreferenceTab("wms", tr("WMS Plugin Preferences"), tr("Modify list of WMS servers displayed in the WMS plugin menu"));

        model = new DefaultTableModel(new String[]{tr("Menu Name"), tr("WMS URL")}, 0);
        final JTable list = new JTable(model);
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll, GBC.eol().fill(GBC.BOTH));
        scroll.setPreferredSize(new Dimension(200,200));

        for (WMSInfo i : WMSPlugin.wmsList) {
            oldValues.put(i.prefid, i);
            model.addRow(new String[]{i.name, i.url});
        }

        final DefaultTableModel modeldef = new DefaultTableModel(
        new String[]{tr("Menu Name (Default)"), tr("WMS URL (Default)")}, 0);
        final JTable listdef = new JTable(modeldef){
        	@Override
            public boolean isCellEditable(int row,int column){return false;}
        };;
        JScrollPane scrolldef = new JScrollPane(listdef);
        p.add(scrolldef, GBC.eol().insets(0,5,0,0).fill(GBC.BOTH));
        scrolldef.setPreferredSize(new Dimension(200,200));

        for (Map.Entry<String,String> i : WMSPlugin.wmsListDefault.entrySet()) {
            modeldef.addRow(new String[]{i.getKey(), i.getValue()});
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton add = new JButton(tr("Add"));
        buttonPanel.add(add, GBC.std().insets(0,5,0,0));
        add.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JPanel p = new JPanel(new GridBagLayout());
                p.add(new JLabel(tr("Menu Name")), GBC.std().insets(0,0,5,0));
                JTextField key = new JTextField(40);
                JTextField value = new JTextField(40);
                p.add(key, GBC.eop().insets(5,0,0,0).fill(GBC.HORIZONTAL));
                p.add(new JLabel(tr("WMS URL")), GBC.std().insets(0,0,5,0));
                p.add(value, GBC.eol().insets(5,0,0,0).fill(GBC.HORIZONTAL));
                int answer = OptionPaneUtil.showConfirmationDialog(
                		gui, p, 
                		tr("Enter a menu name and WMS URL"), 
                		JOptionPane.OK_CANCEL_OPTION,
                		JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.OK_OPTION) {
                    model.addRow(new String[]{key.getText(), value.getText()});
                }
            }
        });

        JButton delete = new JButton(tr("Delete"));
        buttonPanel.add(delete, GBC.std().insets(0,5,0,0));
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

        JButton copy = new JButton(tr("Copy Default"));
        buttonPanel.add(copy, GBC.std().insets(0,5,0,0));
        copy.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Integer line = listdef.getSelectedRow();
                if (line == -1)
                    OptionPaneUtil.showMessageDialog(
                    		gui, 
                    		tr("Please select the row to copy."),
                    		tr("Information"),
                    		JOptionPane.INFORMATION_MESSAGE
                    		);
                else
                {
                    model.addRow(new String[]{modeldef.getValueAt(line, 0).toString(),
                    modeldef.getValueAt(line, 1).toString()});
                }
            }
        });

        p.add(buttonPanel);
        p.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
        browser = new JComboBox(new String[]{
        "webkit-image {0}",
        "gnome-web-photo --mode=photo --format=png {0} /dev/stdout",
        "gnome-web-photo-fixed {0}",
        "webkit-image-gtk {0}"});
        browser.setEditable(true);
        browser.setSelectedItem(Main.pref.get("wmsplugin.browser", "webkit-image {0}"));
        p.add(new JLabel(tr("Downloader:")), GBC.eol().fill(GBC.HORIZONTAL));
        p.add(browser, GBC.eol().fill(GBC.HORIZONTAL));
    }

    public boolean ok() {
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

        Main.pref.put("wmsplugin.browser", browser.getEditor().getItem().toString());
        return false;
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

