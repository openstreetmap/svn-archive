package org.openstreetmap.josm.plugins.taggingpresettester;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.tagging.TaggingPreset;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Fires up the tagging preset tester
 * @author Immanuel.Scholz
 */
public class TaggingPresetTesterAction extends JosmAction {

    public TaggingPresetTesterAction() {
        super(tr("Tagging Preset Tester"), "tagging-preset-tester",
        tr("Open the tagging preset test tool for previewing tagging preset dialogs."),
        Shortcut.registerShortcut("tools:taggingresettester",
        tr("Tool: {0}", tr("Tagging Preset Tester")),
        KeyEvent.VK_T, Shortcut.GROUPS_ALT1+Shortcut.GROUP_DIRECT3), true);
        Main.main.menu.helpMenu.addSeparator();
        MainMenu.add(Main.main.menu.helpMenu, this);
    }

    public TaggingPresetTesterAction(PluginInformation info) {
        this();
    }

    public void actionPerformed(ActionEvent e) {
        Collection<String> coll = TaggingPreset.getPresetSources();

        if (coll.size() == 0) {
            JOptionPane.showMessageDialog(Main.parent, tr("You have to specify tagging preset sources in the preferences first."));
            return;
        }

        String[] taggingPresetSources = new String [coll.size()];
        coll.toArray(taggingPresetSources);
        new TaggingPresetTester(taggingPresetSources);
    }
}
