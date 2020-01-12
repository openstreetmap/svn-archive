// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link ToulouseModule} class.
 */
public class ToulouseModuleTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    @Test
    public void testHandlersConstruction() {
        ToulouseModule module = new ToulouseModule(null);
        assertFalse(module.getHandlers().isEmpty());
        assertEquals(module.getHandlers().size(), module.getNewlyInstanciatedHandlers().size());
    }
}
