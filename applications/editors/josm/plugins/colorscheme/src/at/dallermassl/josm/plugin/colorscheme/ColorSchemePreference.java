/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.colorscheme;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.ColorPreference;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

public class ColorSchemePreference implements PreferenceSetting {
    private static final String PREF_KEY_SCHEMES_PREFIX = "colorschemes.";
    private static final String PREF_KEY_SCHEMES_NAMES = PREF_KEY_SCHEMES_PREFIX + "names";
    public static final String PREF_KEY_COLOR_PREFIX = "color.";
    private JList schemesList;
    private DefaultListModel listModel;
    private List<String>colorKeys;
    private ColorPreference colorPreference;

    /**
     * Default Constructor
     */
    public ColorSchemePreference() {
    }


    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.preferences.PreferenceSetting#addGui(org.openstreetmap.josm.gui.preferences.PreferenceDialog)
     */
    public void addGui(final PreferenceDialog gui) {
        Map<String, String> colorMap = Main.pref.getAllPrefix(PREF_KEY_COLOR_PREFIX);
        colorKeys = new ArrayList<String>(colorMap.keySet());
        Collections.sort(colorKeys);
        listModel = new DefaultListModel();
        schemesList = new JList(listModel);
        String schemes = Main.pref.get(PREF_KEY_SCHEMES_NAMES);
        StringTokenizer st = new StringTokenizer(schemes, ";");
        String schemeName;
        while (st.hasMoreTokens()) {
            schemeName = st.nextToken();
            listModel.addElement(schemeName);
        }

        JButton useScheme = new JButton(tr("Use"));
        useScheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (schemesList.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select a scheme to use."));
                else {
                    String schemeName = (String) listModel.get(schemesList.getSelectedIndex());
                    getColorPreference(gui).setColorModel(getColorMap(schemeName));
                }
            }
        });
        JButton addScheme = new JButton(tr("Add"));
        addScheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String schemeName = JOptionPane.showInputDialog(Main.parent, tr("Color Scheme"));
                if (schemeName == null)
                    return;
                schemeName = schemeName.replaceAll("\\.", "_");
                setColorScheme(schemeName, getColorPreference(gui).getColorModel());
                listModel.addElement(schemeName);
                saveSchemeNamesToPref();
            }
        });

        JButton deleteScheme = new JButton(tr("Delete"));
        deleteScheme.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (schemesList.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(Main.parent, tr("Please select the scheme to delete."));
                else {
                    String schemeName = (String) listModel.get(schemesList.getSelectedIndex());
                    removeColorSchemeFromPreferences(schemeName);
                    listModel.remove(schemesList.getSelectedIndex());
                    saveSchemeNamesToPref();
                }
            }
        });
        schemesList.setVisibleRowCount(3);

        //schemesList.setToolTipText(tr("The sources (url or filename) of annotation preset definition files. See http://josm.eigenheimstrasse.de/wiki/AnnotationPresets for help."));
        useScheme.setToolTipText(tr("Use the selected scheme from the list."));
        addScheme.setToolTipText(tr("Use the current colors as a new color scheme."));
        deleteScheme.setToolTipText(tr("Delete the selected scheme from the list."));

        gui.map.add(new JLabel(tr("Color Schemes")), GBC.eol().insets(0,5,0,0));
        gui.map.add(new JScrollPane(schemesList), GBC.eol().fill(GBC.BOTH));
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gui.map.add(buttonPanel, GBC.eol().fill(GBC.HORIZONTAL));
        buttonPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
        buttonPanel.add(useScheme, GBC.std().insets(0,5,5,0));
        buttonPanel.add(addScheme, GBC.std().insets(0,5,5,0));
        buttonPanel.add(deleteScheme, GBC.std().insets(0,5,5,0));
    }

    /**
     * Saves the names of the schemes to the preferences.
     */
    public void saveSchemeNamesToPref() {
        if (schemesList.getModel().getSize() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < schemesList.getModel().getSize(); ++i)
                sb.append(";"+schemesList.getModel().getElementAt(i));
            Main.pref.put(PREF_KEY_SCHEMES_NAMES, sb.toString().substring(1));
        } else
            Main.pref.put(PREF_KEY_SCHEMES_NAMES, null);
    }

    public boolean ok() {
        return false;// nothing to do
    }

    /**
     * Remove all color entries for the given scheme from the preferences.
     * @param schemeName the name of the scheme.
     */
    public void removeColorSchemeFromPreferences(String schemeName) {
        // delete color entries for scheme in preferences:
        Map<String, String> colors = Main.pref.getAllPrefix(PREF_KEY_SCHEMES_PREFIX + schemeName + ".");
        for(String key : colors.keySet()) {
            Main.pref.put(key, null);
        }
    }

    /**
     * Copy all color entries from the given map to entries in preferences with the scheme name.
     * @param schemeName the name of the scheme.
     * @param the map containing the color key (without prefix) and the html color values.
     */
    public void setColorScheme(String schemeName, Map<String, String> colorMap) {
        String key;
        for(String colorKey : colorMap.keySet()) {
            key = PREF_KEY_SCHEMES_PREFIX + schemeName + "." + PREF_KEY_COLOR_PREFIX + colorKey;
            Main.pref.put(key, colorMap.get(colorKey));
        }
    }

    /**
     * Reads all colors for a scheme and returns them in a map (key = color key without prefix,
     * value = html color code).
     * @param schemeName the name of the scheme.
     */
    public Map<String, String> getColorMap(String schemeName) {
        String colorKey;
        String prefix = PREF_KEY_SCHEMES_PREFIX + schemeName + "." + PREF_KEY_COLOR_PREFIX;
        Map<String, String>colorMap = new HashMap<String, String>();
        for(String schemeColorKey : Main.pref.getAllPrefix(prefix).keySet()) {
            colorKey = schemeColorKey.substring(prefix.length());
            colorMap.put(colorKey, Main.pref.get(schemeColorKey));
        }
        return colorMap;
    }

    public ColorPreference getColorPreference(PreferenceDialog gui) {
        if(colorPreference == null) {
            for(PreferenceSetting setting : gui.getSettings()) {
                if(setting instanceof ColorPreference) {
                    colorPreference = (ColorPreference) setting;
                    break;
                }
            }
        }
        return colorPreference;
    }
}
