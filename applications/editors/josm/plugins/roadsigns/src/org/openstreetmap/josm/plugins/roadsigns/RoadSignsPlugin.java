// License: GPL (v2 or later)
package org.openstreetmap.josm.plugins.roadsigns;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.xml.sax.SAXException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

public class RoadSignsPlugin extends Plugin {
    private static boolean presetsLoaded = false;
    public static List<Sign> signs;
    static List<String> iconDirs;

    public RoadSignsPlugin(PluginInformation info) {
        super(info);
        registerAction();
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
                    Shortcut.registerShortcut("plugin:roadsigns:dialog", tr("Roadsigns plugin: open dialog"), KeyEvent.VK_R, Shortcut.ALT), false);
        }

        public void actionPerformed(ActionEvent e) {
            loadSignPresets();
            RoadSignInputDialog input = new RoadSignInputDialog(signs);
            input.showDialog();
        }

    }

    protected static void loadSignPresets() {
        if (presetsLoaded)
            return;
        presetsLoaded=true;
        List<String> files = new ArrayList<String>(
                Main.pref.getCollection("plugin.roadsigns.sources",
                    Collections.<String>singletonList("resource://data/defaultroadsignpreset.xml")));
        iconDirs = new ArrayList<String>(
                Main.pref.getCollection("plugin.roadsigns.icon.sources", Collections.<String>emptySet()));

        if (Main.pref.getBoolean("plugin.roadsigns.use_default_icon_source", true)) {
            iconDirs.add("resource://images/");
        }

        for (String source : files) {
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
            } catch (SAXException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        Main.parent,
                        tr("Error parsing tagging preset from ''{0}'':\n", source)+ex.getMessage(),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
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
