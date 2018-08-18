// License: GPL. For details, see LICENSE file.
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
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.StructUtils;
import org.openstreetmap.josm.data.StructUtils.StructEntry;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.roadsigns.RoadSignInputDialog.SettingsPanel;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;
import org.xml.sax.SAXException;

public class RoadSignsPlugin extends Plugin {
    static PresetMetaData selectedPreset;
    public static List<Sign> signs;
    public static List<String> iconDirs;

    public static RoadSignsPlugin plugin;

    public static final PresetMetaData PRESET_BE = new PresetMetaData(
            "BE", tr("Belgium"), "resource://data/roadsignpresetBE.xml", "resource://images/BE/");
    public static final PresetMetaData PRESET_ES = new PresetMetaData(
            "ES", tr("Spain"), "resource://data/roadsignpresetES.xml", "resource://images/ES/");
    public static final PresetMetaData PRESET_DE = new PresetMetaData(
            "DE", tr("Germany"), "resource://data/roadsignpresetDE.xml", "resource://images/DE/");
    public static final PresetMetaData PRESET_PL = new PresetMetaData(
            "PL", tr("Poland"), "resource://data/roadsignpresetPL.xml", "resource://images/PL/");
    public static final PresetMetaData PRESET_SK = new PresetMetaData(
            "SK", tr("Slovakia"), "resource://data/roadsignpresetSK.xml", "resource://images/SK/");
    public static final Collection<PresetMetaData> DEFAULT_PRESETS = Arrays.asList(PRESET_BE, PRESET_ES, PRESET_DE, PRESET_PL, PRESET_SK);

    public RoadSignsPlugin(PluginInformation info) {
        super(info);
        plugin = this;
        registerAction();
    }

    public static File pluginDir() {
        File dir = plugin.getPluginDirs().getUserDataDirectory(false);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void registerAction() {
        JButton btn = new JButton(new RoadSignAction());
        btn.setText(null);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(18, 18));
        PropertiesDialog.pluginHook.add(btn);
        PropertiesDialog.pluginHook.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        PropertiesDialog.pluginHook.revalidate();
    }

    private static class RoadSignAction extends JosmAction {
        RoadSignAction() {
            super(tr("Roadsign tagging"), "pref/roadsigns-small", tr("Add tags by clicking on road signs"),
                    Shortcut.registerShortcut("plugin:roadsigns:dialog", tr("Roadsigns plugin: open dialog"),
                            KeyEvent.VK_Q, Shortcut.ALT_SHIFT), false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String code = Config.getPref().get("plugin.roadsigns.preset.selection", null);
            if (code == null) {
                ExtendedDialog ed = new ExtendedDialog(MainApplication.getMainFrame(), tr("Settings"), new String[] {tr("Ok"), tr("Cancel")});
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
        @StructEntry public String code;
        @StructEntry public String display_name;
        @StructEntry public String preset_path;
        @StructEntry public String icon_path;

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
        Config.getPref().put("plugin.roadsigns.preset.selection", preset.code);
        loadSignPreset();
    }

    public static List<PresetMetaData> getAvailablePresetsMetaData() {

        List<PresetMetaData> presetsData = StructUtils.getListOfStructs(
                Config.getPref(), "plugin.roadsigns.presets", DEFAULT_PRESETS, PresetMetaData.class);

        String customFile = Config.getPref().get("plugin.roadsigns.sources", null);
        if (customFile == null) {
            // for legacy reasons, try both string and collection preference type
            List<String> customFiles = Config.getPref().getList("plugin.roadsigns.sources", null);
            if (customFiles != null && !customFiles.isEmpty()) {
                customFile = customFiles.iterator().next();
            }
        }

        if (customFile != null) {
            // first check, if custom file preference has changed. If yes,
            // change the current preset selection to custom directly
            String lastCustomFile = Config.getPref().get("plugin.roadsigns.sources.last", null);
            if (!Objects.equals(customFile, lastCustomFile)) {
                Config.getPref().put("plugin.roadsigns.sources.last", customFile);
                Config.getPref().put("plugin.roadsigns.preset.selection", "custom");
            }

            String customIconDirsStr = Config.getPref().get("plugin.roadsigns.icon.sources", null);
            List<String> customIconDirs = null;
            if (customIconDirsStr != null) {
                customIconDirs = new ArrayList<>(Arrays.asList(customIconDirsStr.split(",")));
            } else {
                customIconDirs = Config.getPref().getList("plugin.roadsigns.icon.sources", null);
            }
            if (customIconDirs != null) {
                customIconDirs = new ArrayList<>(customIconDirs);
            } else {
                customIconDirs = new ArrayList<>();
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
            if (Config.getPref().getBoolean("plugin.roadsigns.use_default_icon_source", true)) {
                customIconDirs.add("resource://images/");
            }
            PresetMetaData custom = new PresetMetaData("custom", tr("custom"), customFile, Utils.join(",", customIconDirs));
            presetsData.add(custom);
        } else {
            Config.getPref().put("plugin.roadsigns.sources.last", null);
        }

        return presetsData;
    }

    protected static void loadSignPreset() throws IOException {
        List<PresetMetaData> presetsData = getAvailablePresetsMetaData();
        String code = Config.getPref().get("plugin.roadsigns.preset.selection", null);

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
                    MainApplication.getMainFrame(),
                    tr("Could not read tagging preset source: ''{0}''", source),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );
            throw ex;
        } catch (SAXException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    MainApplication.getMainFrame(),
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
    @SuppressWarnings("resource")
    public static InputStream getInputStream(String source) throws IOException {
        InputStream in = null;
        if (source.startsWith("http://") || source.startsWith("https://") || source.startsWith("ftp://")) {
            in = new CachedFile(source).getInputStream();
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
