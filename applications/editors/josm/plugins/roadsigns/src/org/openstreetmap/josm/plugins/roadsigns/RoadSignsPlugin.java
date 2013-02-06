// License: GPL (v2 or later)
package org.openstreetmap.josm.plugins.roadsigns;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.xml.sax.SAXException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Preferences.pref;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.roadsigns.RoadSignInputDialog.SettingsPanel;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

public class RoadSignsPlugin extends Plugin {
    static PresetMetaData selectedPreset;
    public static List<Sign> signs;
    public static List<String> iconDirs;

    public static RoadSignsPlugin plugin;

    public final static PresetMetaData PRESET_DE = new PresetMetaData("DE", tr("Germany"), "resource://data/roadsignpresetDE.xml", "resource://images/DE/");
    public final static PresetMetaData PRESET_PL = new PresetMetaData("PL", tr("Poland"), "resource://data/roadsignpresetPL.xml", "resource://images/PL/");
    public final static PresetMetaData PRESET_SK = new PresetMetaData("SK", tr("Slovakia"), "resource://data/roadsignpresetSK.xml", "resource://images/SK/");
    public final static Collection<PresetMetaData> DEFAULT_PRESETS = Arrays.asList(PRESET_DE, PRESET_PL, PRESET_SK);

    public RoadSignsPlugin(PluginInformation info) {
        super(info);
        plugin = this;
        registerAction();
    }

    public static File pluginDir() {
        File dir = new File(plugin.getPluginDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void registerAction() {
        JButton btn = new JButton(new RoadSignAction());
        btn.setText(null);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(18,18));
        PropertiesDialog.pluginHook.add(btn);
        PropertiesDialog.pluginHook.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    }

    private static class RoadSignAction extends JosmAction {
        public RoadSignAction() {
            super(tr("Roadsign tagging"), "pref/roadsigns-small", tr("Add tags by clicking on road signs"),
                    Shortcut.registerShortcut("plugin:roadsigns:dialog", tr("Roadsigns plugin: open dialog"), KeyEvent.VK_Q, Shortcut.ALT_SHIFT), false);
        }

        public void actionPerformed(ActionEvent e) {
            String code = Main.pref.get("plugin.roadsigns.preset.selection", null);
            if (code == null) {
                ExtendedDialog ed = new ExtendedDialog(Main.parent, tr("Settings"), new String[] { tr("Ok"), tr("Cancel") });
                ed.setButtonIcons(new String[] {"ok", "cancel"});
                SettingsPanel settings = new SettingsPanel(true, null);
                ed.setContent(settings);
                ed.showDialog();
                if (ed.getValue() != 1) return;
                try {
                    settings.apply();
                } catch (IOException ex) {
                    return;
                }
            }
            try {
                loadSignPreset();
            } catch (IOException ex) {
                return;
            }
            RoadSignInputDialog input = new RoadSignInputDialog();
            input.showDialog();
        }

    }

    public static class PresetMetaData {
        @pref public String code;
        @pref public String display_name;
        @pref public String preset_path;
        @pref public String icon_path;

        public PresetMetaData() {
        }

        public PresetMetaData(String country_code, String display_name, String preset_path, String icons_path) {
            this.code = country_code;
            this.display_name = display_name;
            this.preset_path = preset_path;
            this.icon_path = icons_path;
        }

        @Override
        public String toString() {
            return display_name;
        }
    }

    public static void setSelectedPreset(PresetMetaData preset) throws IOException {
        Main.pref.put("plugin.roadsigns.preset.selection", preset.code);
        loadSignPreset();
    }

    public static List<PresetMetaData> getAvailablePresetsMetaData() {

        List<PresetMetaData> presetsData = Main.pref.getListOfStructs("plugin.roadsigns.presets", DEFAULT_PRESETS, PresetMetaData.class);

        String customFile = Main.pref.get("plugin.roadsigns.sources", null);
        if (customFile == null) {
            // for legacy reasons, try both string and collection preference type
            Collection<String> customFiles = Main.pref.getCollection("plugin.roadsigns.sources", null);
            if (customFiles != null && !customFiles.isEmpty()) {
                customFile = customFiles.iterator().next();
            }
        }

        if (customFile != null) {
            // first check, if custom file preference has changed. If yes,
            // change the current preset selection to custom directly
            String lastCustomFile = Main.pref.get("plugin.roadsigns.sources.last", null);
            if (!Utils.equal(customFile, lastCustomFile)) {
                Main.pref.put("plugin.roadsigns.sources.last", customFile);
                Main.pref.put("plugin.roadsigns.preset.selection", "custom");
            }

            String customIconDirsStr = Main.pref.get("plugin.roadsigns.icon.sources", null);
            Collection<String> customIconDirs = null;
            if (customIconDirsStr != null) {
                customIconDirs = new ArrayList<String>(Arrays.asList(customIconDirsStr.split(",")));
            } else {
                customIconDirs = Main.pref.getCollection("plugin.roadsigns.icon.sources", null);
            }
            if (customIconDirs != null) {
                customIconDirs = new ArrayList<String>(customIconDirs);
            } else {
                customIconDirs = new ArrayList<String>();
            }
            // add icon directory relative to preset file
            if (!customFile.startsWith("resource:")) {
                String parentDir = null;
                try {
                    URL url = new URL(customFile);
                    parentDir = url.getPath();
                } catch (MalformedURLException ex) {
                    File f = new File(customFile);
                    parentDir = f.getParent();
                }
                if (parentDir != null && !parentDir.isEmpty()) {
                    customIconDirs.add(parentDir);
                }
            }
            if (Main.pref.getBoolean("plugin.roadsigns.use_default_icon_source", true)) {
                customIconDirs.add("resource://images/");
            }
            PresetMetaData custom = new PresetMetaData("custom", tr("custom"), customFile, Utils.join(",", customIconDirs));
            presetsData.add(custom);
        } else {
            Main.pref.put("plugin.roadsigns.sources.last", null);
        }

        return presetsData;
    }

    protected static void loadSignPreset() throws IOException {
        List<PresetMetaData> presetsData =  getAvailablePresetsMetaData();
        String code = Main.pref.get("plugin.roadsigns.preset.selection", null);

        for (PresetMetaData data : presetsData) {
            if (data.code.equals(code)) {
                selectedPreset = data;
                break;
            }
        }
        if (selectedPreset == null) {
            if (!presetsData.isEmpty()) {
                selectedPreset = presetsData.get(0);
            } else {
                selectedPreset = PRESET_DE;
            }
        }
        iconDirs = Arrays.asList(selectedPreset.icon_path.split(","));
        String source = selectedPreset.preset_path;

        try {
            InputStream in = getInputStream(source);
            RoadSignsReader reader = new RoadSignsReader(in);
            signs = reader.parse();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Could not read tagging preset source: ''{0}''",source),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );
            throw ex;
        } catch (SAXException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Error parsing tagging preset from ''{0}'':\n", source)+ex.getMessage(),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );
            throw new IOException(ex);
        }
    }

    /**
     * Returns an inputstream from urls, files and classloaders, depending on the name.
     */
    public static InputStream getInputStream(String source) throws IOException {
        InputStream in = null;
        if (source.startsWith("http://") || source.startsWith("ftp://")) {
            in = new MirroredInputStream(source);
        } else if (source.startsWith("file:")) {
            in = new URL(source).openStream();
        } else if (source.startsWith("resource://")) {
            in = RoadSignsPlugin.class.getResourceAsStream(source.substring("resource:/".length()));
        } else {
            in = new FileInputStream(source);
        }
        return in;
    }
}
