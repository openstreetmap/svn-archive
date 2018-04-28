// License: GPL. For details, see LICENSE file.
package org.wikipedia.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.server.ServerAccessPreference;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link SophoxServerPreference} class.
 */
public class SophoxServerPreferenceTest {

    /**
     * Setup tests
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules().preferences().platform();

    /**
     * Unit test of {@link SophoxServerPreference}.
     */
    @Test
    public void testSophoxServerPreference() {
        assertNotNull(new SophoxServerPreference.Factory().createPreferenceSetting());
    }

    /**
     * Unit test of {@link SophoxServerPreference#addGui}.
     */
    @Test
    public void testAddGui() {
        final SophoxServerPreference setting = new SophoxServerPreference.Factory().createPreferenceSetting();
        final PreferenceTabbedPane tabPane = new PreferenceTabbedPane();
        tabPane.buildGui();
        int tabs = tabPane.getSetting(ServerAccessPreference.class).getTabPane().getTabCount();
        setting.addGui(tabPane);

        assertEquals(tabs + 1, tabPane.getSetting(ServerAccessPreference.class).getTabPane().getTabCount());
        assertEquals(tabPane.getSetting(ServerAccessPreference.class), setting.getTabPreferenceSetting(tabPane));

        setting.ok();
    }
}
