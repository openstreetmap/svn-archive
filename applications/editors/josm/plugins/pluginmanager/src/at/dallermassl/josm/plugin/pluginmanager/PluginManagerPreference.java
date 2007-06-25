/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

/**
 * @author cdaller
 *
 */
public class PluginManagerPreference implements PreferenceSetting {
    private String PREF_KEY_REMOTE_SITE_PREFIX = "pluginmanager.site.";
    private String PREF_KEY_SITE_NAME_SUFFIX = ".name";
    private String PREF_KEY_SITE_URL_SUFFIX = ".url";
    private JList siteList;
    private DefaultListModel siteListModel;
    
    protected DefaultListModel createListModel() {
        Map<String, String> sites = Main.pref.getAllPrefix(PREF_KEY_REMOTE_SITE_PREFIX);
        if(sites.keySet().size() == 0) {
            // add default entry (for demonstration purpose)
            sites.put(PREF_KEY_REMOTE_SITE_PREFIX + "0"+PREF_KEY_SITE_URL_SUFFIX, 
                "http://www.tegmento.org/~cdaller/josm/");
        }
        int siteCount = 0;
        String name;
        String url;
        SiteDescription description;
        DefaultListModel listModel = new DefaultListModel();
        while((url = sites.get(PREF_KEY_REMOTE_SITE_PREFIX + siteCount + PREF_KEY_SITE_URL_SUFFIX)) != null) {
            name = sites.get(PREF_KEY_REMOTE_SITE_PREFIX + siteCount + PREF_KEY_SITE_NAME_SUFFIX);
            try {
                description = new SiteDescription(name, url);
                listModel.addElement(description);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            ++siteCount;
        }        
        return listModel;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.preferences.PreferenceSetting#addGui(org.openstreetmap.josm.gui.preferences.PreferenceDialog)
     */
    // only in 1.6 allowed @Override
    public void addGui(final PreferenceDialog gui) {
        
        siteListModel = createListModel();
        siteList = new JList(siteListModel);
        siteList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton addSite = new JButton(tr("Add Site"));
        addSite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String siteUrl = JOptionPane.showInputDialog(Main.parent, tr("Update Site Url"));
                if (siteUrl == null)
                    return;
                SiteDescription site;
                try {
                    if(!siteUrl.endsWith("/")) {
                        siteUrl = siteUrl + "/";
                    }
                  site = new SiteDescription(siteUrl);
                  siteListModel.addElement(site);
                } catch(MalformedURLException mue) {
                    JOptionPane.showMessageDialog(Main.parent, tr("Invalid Url"), tr("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                gui.requiresRestart = gui.requiresRestart || false;
            }
        });

        JButton deleteSite = new JButton(tr("Delete Site(s)"));
        deleteSite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (siteList.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select the site to delete."));
                else {
                    int[] selected = siteList.getSelectedIndices();
                    for (int i = selected.length - 1; i >=0; --i) {                        
                        siteListModel.removeElementAt(selected[i]);
                    }
                    gui.requiresRestart = gui.requiresRestart || false;
                }
            }
        });

        JButton checkSite = new JButton(tr("Check Site(s)"));
        checkSite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (siteList.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select the site(s) to check for updates."));
                else {
                    int[] selected = siteList.getSelectedIndices();
                    List<SiteDescription> descriptions = new ArrayList<SiteDescription>();
                    SiteDescription description;
                    for(int selectedIndex : selected) {
                        description = (SiteDescription)siteListModel.get(selectedIndex);
                        descriptions.add(description);
                        try {
                            description.loadFromUrl();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    PluginUpdateFrame frame = new PluginUpdateFrame(tr("Plugins"), descriptions);
                    frame.setVisible(true);
                    gui.requiresRestart = true;
                }
            }
        });

        siteList.setVisibleRowCount(3);

        //schemesList.setToolTipText(tr("The sources (url or filename) of annotation preset definition files. See http://josm.eigenheimstrasse.de/wiki/AnnotationPresets for help."));
        addSite.setToolTipText(tr("Add a new plugin site."));
        deleteSite.setToolTipText(tr("Delete the selected site(s) from the list."));
        checkSite.setToolTipText(tr("Check the selected site(s) for new plugins or updates."));

        gui.map.add(new JLabel(tr("Update Sites")), GBC.eol().insets(0,5,0,0));
        gui.map.add(new JScrollPane(siteList), GBC.eol().fill(GBC.BOTH));
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gui.map.add(buttonPanel, GBC.eol().fill(GBC.HORIZONTAL));
        buttonPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
        buttonPanel.add(addSite, GBC.std().insets(0,5,5,0));
        buttonPanel.add(deleteSite, GBC.std().insets(0,5,5,0));
        buttonPanel.add(checkSite, GBC.std().insets(0,5,5,0));
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.preferences.PreferenceSetting#ok()
     */
    // only in 1.6 allowed @Override
    public void ok() {
        // first remove all old entries:
        Map<String, String> keys = Main.pref.getAllPrefix(PREF_KEY_REMOTE_SITE_PREFIX);
        for(String key : keys.keySet()) {
            Main.pref.put(key, null);
        }
        // set all sites into prefs:
        SiteDescription desc;
        String key;
        for(int index = 0; index < siteListModel.getSize(); ++index) {
            desc = (SiteDescription) siteListModel.elementAt(index);
            if(desc.getName() != null) {
                key = PREF_KEY_REMOTE_SITE_PREFIX + index + PREF_KEY_SITE_NAME_SUFFIX;
                Main.pref.put(key, desc.getName());
            }
            key = PREF_KEY_REMOTE_SITE_PREFIX + index + PREF_KEY_SITE_URL_SUFFIX;
            try {
                Main.pref.put(key, desc.getUrl().toURI().toASCIIString());
            } catch (URISyntaxException e) {
            }
        }
    }

}
