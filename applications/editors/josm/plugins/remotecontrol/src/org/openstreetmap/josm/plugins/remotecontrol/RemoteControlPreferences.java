package org.openstreetmap.josm.plugins.remotecontrol;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.remotecontrol.handler.AddNodeHandler;
import org.openstreetmap.josm.plugins.remotecontrol.handler.ImportHandler;
import org.openstreetmap.josm.plugins.remotecontrol.handler.LoadAndZoomHandler;
import org.openstreetmap.josm.plugins.remotecontrol.handler.VersionHandler;
import org.openstreetmap.josm.tools.GBC;

/**
 * Preference settings for the Remote Control plugin
 *
 * @author Frederik Ramm
 */
public class RemoteControlPreferences implements PreferenceSetting
{
    private JCheckBox permissionLoadData = new JCheckBox(tr("load data from API"));
    private JCheckBox permissionImportData = new JCheckBox(tr("import data from URL"));
    private JCheckBox permissionCreateObjects = new JCheckBox(tr("create new objects"));
    private JCheckBox permissionChangeSelection = new JCheckBox(tr("change the selection"));
    private JCheckBox permissionChangeViewport = new JCheckBox(tr("change the viewport"));
    private JCheckBox permissionReadProtocolversion = new JCheckBox(tr("read protocol version"));
    private JCheckBox alwaysAskUserConfirm = new JCheckBox(tr("confirm all Remote Control actions manually"));

    public void addGui(final PreferenceTabbedPane gui)
    {
        String description = tr("A plugin that allows JOSM to be controlled from other applications.");
        JPanel remote = gui.createPreferenceTab("remotecontrol.gif", tr("Remote Control"), tr("Settings for the Remote Control plugin."));
        remote.add(new JLabel("<html>"+tr("The Remote Control plugin will always listen on port 8111 on localhost." +
                "The port is not variable because it is referenced by external applications talking to the plugin.") + "</html>"), GBC.eol().insets(0,5,0,10).fill(GBC.HORIZONTAL));

        JPanel perms = new JPanel();
        perms.setLayout(new GridBagLayout());
        perms.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), tr("Permitted actions")));
        perms.add(permissionLoadData, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionImportData, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionChangeSelection, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionChangeViewport, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionCreateObjects, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionReadProtocolversion, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        remote.add(perms, GBC.eol().fill(GBC.HORIZONTAL));

        remote.add(alwaysAskUserConfirm, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        remote.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

        permissionLoadData.setSelected(Main.pref.getBoolean(LoadAndZoomHandler.loadDataPermissionKey, LoadAndZoomHandler.loadDataPermissionDefault));
        permissionImportData.setSelected(Main.pref.getBoolean(ImportHandler.permissionKey, ImportHandler.permissionDefault));
        permissionChangeSelection.setSelected(Main.pref.getBoolean(LoadAndZoomHandler.changeSelectionPermissionKey, LoadAndZoomHandler.changeSelectionPermissionDefault));
        permissionChangeViewport.setSelected(Main.pref.getBoolean(LoadAndZoomHandler.changeViewportPermissionKey, LoadAndZoomHandler.changeViewportPermissionDefault));
        permissionCreateObjects.setSelected(Main.pref.getBoolean(AddNodeHandler.permissionKey, AddNodeHandler.permissionDefault));
        permissionReadProtocolversion.setSelected(Main.pref.getBoolean(VersionHandler.permissionKey, VersionHandler.permissionDefault));
        alwaysAskUserConfirm.setSelected(Main.pref.getBoolean(RequestHandler.globalConfirmationKey, RequestHandler.globalConfirmationDefault));

    }

    public boolean ok() {
        Main.pref.put(LoadAndZoomHandler.loadDataPermissionKey, permissionLoadData.isSelected());
        Main.pref.put(ImportHandler.permissionKey, permissionImportData.isSelected());
        Main.pref.put(LoadAndZoomHandler.changeSelectionPermissionKey, permissionChangeSelection.isSelected());
        Main.pref.put(LoadAndZoomHandler.changeViewportPermissionKey, permissionChangeViewport.isSelected());
        Main.pref.put(AddNodeHandler.permissionKey, permissionCreateObjects.isSelected());
        Main.pref.put(VersionHandler.permissionKey, permissionReadProtocolversion.isSelected());
        Main.pref.put(RequestHandler.globalConfirmationKey, alwaysAskUserConfirm.isSelected());
        // FIXME confirm return value - really no restart needed?
        return false /* no restart needed */;
    }
}
