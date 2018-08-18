/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package org.openstreetmap.josm.plugins.colorscheme;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.preferences.ColorInfo;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.display.ColorPreference;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.GBC;

public class ColorSchemePreference implements SubPreferenceSetting {
    private static final String PREF_KEY_SCHEMES_PREFIX = "colorschemes.";
    private static final String PREF_KEY_SCHEMES_NAMES = PREF_KEY_SCHEMES_PREFIX + "names";
    public static final String PREF_KEY_COLOR_PREFIX = "color.";
    private JList<String> schemesList;
    private DefaultListModel<String> listModel;
    private List<String>colorKeys;
    private ColorPreference colorPreference;

    /**
     * Default Constructor
     */
    public ColorSchemePreference() {
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Map<String, String> colorMap = Preferences.main().getAllPrefix(PREF_KEY_COLOR_PREFIX);
        colorKeys = new ArrayList<>(colorMap.keySet());
        Collections.sort(colorKeys);
        listModel = new DefaultListModel<>();
        schemesList = new JList<>(listModel);
        String schemes = Config.getPref().get(PREF_KEY_SCHEMES_NAMES);
        StringTokenizer st = new StringTokenizer(schemes, ";");
        String schemeName;
        while (st.hasMoreTokens()) {
            schemeName = st.nextToken();
            listModel.addElement(schemeName);
        }

        JButton useScheme = new JButton(tr("Use"));
        useScheme.addActionListener(e -> {
            if (schemesList.getSelectedIndex() == -1)
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Please select a scheme to use."));
            else {
                String schemeName1 = (String) listModel.get(schemesList.getSelectedIndex());
                getColorPreference(gui).setColors(getColorMap(schemeName1));
            }
        });
        JButton addScheme = new JButton(tr("Add"));
        addScheme.addActionListener(e -> {
            String schemeName1 = JOptionPane.showInputDialog(MainApplication.getMainFrame(), tr("Color Scheme"));
            if (schemeName1 == null)
                return;
            schemeName1 = schemeName1.replaceAll("\\.", "_");
            setColorScheme(schemeName1, getColorPreference(gui).getColors());
            listModel.addElement(schemeName1);
            saveSchemeNamesToPref();
        });

        JButton deleteScheme = new JButton(tr("Delete"));
        deleteScheme.addActionListener(e -> {
            if (schemesList.getSelectedIndex() == -1)
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Please select the scheme to delete."));
            else {
                String schemeName1 = (String) listModel.get(schemesList.getSelectedIndex());
                removeColorSchemeFromPreferences(schemeName1);
                listModel.remove(schemesList.getSelectedIndex());
                saveSchemeNamesToPref();
            }
        });
        schemesList.setVisibleRowCount(3);

        useScheme.setToolTipText(tr("Use the selected scheme from the list."));
        addScheme.setToolTipText(tr("Use the current colors as a new color scheme."));
        deleteScheme.setToolTipText(tr("Delete the selected scheme from the list."));

        panel.add(new JLabel(tr("Color Schemes")), GBC.eol().insets(0,5,0,0));
        panel.add(new JScrollPane(schemesList), GBC.eol().fill(GBC.BOTH));
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        panel.add(buttonPanel, GBC.eol().fill(GBC.HORIZONTAL));
        buttonPanel.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
        buttonPanel.add(useScheme, GBC.std().insets(0,5,5,0));
        buttonPanel.add(addScheme, GBC.std().insets(0,5,5,0));
        buttonPanel.add(deleteScheme, GBC.std().insets(0,5,5,0));

        JScrollPane scrollpane = new JScrollPane(panel);
        scrollpane.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
        gui.getDisplayPreference().getTabPane().addTab(tr("Color Schemes"), scrollpane);
    }

    @Override
    public TabPreferenceSetting getTabPreferenceSetting(final PreferenceTabbedPane gui) {
        return gui.getDisplayPreference();
    }

    /**
     * Saves the names of the schemes to the preferences.
     */
    public void saveSchemeNamesToPref() {
        if (schemesList.getModel().getSize() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < schemesList.getModel().getSize(); ++i)
                sb.append(";"+schemesList.getModel().getElementAt(i));
            Config.getPref().put(PREF_KEY_SCHEMES_NAMES, sb.toString().substring(1));
        } else
            Config.getPref().put(PREF_KEY_SCHEMES_NAMES, null);
    }

    @Override
    public boolean ok() {
        return false;// nothing to do
    }

    @Override
    public boolean isExpert() {
        return false;
    }

    /**
     * Remove all color entries for the given scheme from the preferences.
     * @param schemeName the name of the scheme.
     */
    public void removeColorSchemeFromPreferences(String schemeName) {
        // delete color entries for scheme in preferences:
        Map<String, String> colors = Preferences.main().getAllPrefix(PREF_KEY_SCHEMES_PREFIX + schemeName + ".");
        for(String key : colors.keySet()) {
            Config.getPref().put(key, null);
        }
    }

    /**
     * Copy all color entries from the given map to entries in preferences with the scheme name.
     * @param schemeName the name of the scheme.
     * @param colorMap the map containing the color key (without prefix) and the html color values.
     */
    public void setColorScheme(String schemeName, Map<String, ColorInfo> colorMap) {
        String key;
        for(String colorKey : colorMap.keySet()) {
            key = PREF_KEY_SCHEMES_PREFIX + schemeName + "." + PREF_KEY_COLOR_PREFIX + colorKey;
            Config.getPref().put(key, ColorHelper.color2html(colorMap.get(colorKey).getValue()));
        }
    }

    /**
     * Reads all colors for a scheme and returns them in a map (key = color key without prefix,
     * value = html color code).
     * @param schemeName the name of the scheme.
     * @return color map for the given scheme name 
     */
    public Map<String, ColorInfo> getColorMap(String schemeName) {
        String colorKey;
        String prefix = PREF_KEY_SCHEMES_PREFIX + schemeName + "." + PREF_KEY_COLOR_PREFIX;
        Map<String, ColorInfo> colorMap = new HashMap<>();
        for(String schemeColorKey : Preferences.main().getAllPrefix(prefix).keySet()) {
            colorKey = schemeColorKey.substring(prefix.length());
            colorMap.put(colorKey, ColorInfo.fromPref(Arrays.asList(
                    // FIXME: does not work, corrupts the color table ? See #16110
                    Config.getPref().get(schemeColorKey), "", "", ""), false));
        }
        return colorMap;
    }

    public ColorPreference getColorPreference(PreferenceTabbedPane gui) {
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
