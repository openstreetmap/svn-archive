// License: GPL. For details, see LICENSE file.
package org.wikipedia.gui;

import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.gui.preferences.PreferencesTestUtils;
import org.openstreetmap.josm.gui.preferences.server.ServerAccessPreference;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests of {@link WikosmServerPreference} class.
 */
public class WikosmServerPreferenceTest {

    /**
     * Setup tests
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules().preferences().platform();

    /**
     * Unit test of {@link WikosmServerPreference}.
     */
    @Test
    public void testWikosmServerPreference() {
        assertNotNull(new WikosmServerPreference.Factory().createPreferenceSetting());
    }

    /**
     * Unit test of {@link WikosmServerPreference#addGui}.
     */
    @Test
    public void testAddGui() {
        PreferencesTestUtils.doTestPreferenceSettingAddGui(new WikosmServerPreference.Factory(), ServerAccessPreference.class);
    }
}
