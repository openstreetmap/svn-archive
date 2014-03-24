// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import javax.swing.JButton;

import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.imagery.ImageryPreference.ImageryProvidersPanel;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.EditEntriesAction;

/**
 *
 * @author Don-vip
 *
 */
public class XmlBoundsPreferenceSetting implements SubPreferenceSetting {

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        ImageryProvidersPanel ipp = gui.getImageryPreference().getProvidersPanel();
        EditEntriesAction action = new EditEntriesAction(ipp.defaultTable, ipp.defaultModel);
        ipp.middleToolbar.add(new JButton(action));
    }

    @Override
    public boolean ok() {
        return false;
    }

    @Override
    public boolean isExpert() {
        return false;
    }

    public TabPreferenceSetting getTabPreferenceSetting(final PreferenceTabbedPane gui) {
        return gui.getImageryPreference();
    }

}
