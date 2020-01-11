// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.HttpClient;

/**
 * Integration tests of {@link ToulouseModule} class.
 */
public class ToulouseModuleTestIT {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().timeout(30_000);

    @Test
    public void testUrlValidity() throws IOException {
        Map<String, Integer> errors = new TreeMap<>();
        for (AbstractDataSetHandler handler : new ToulouseModule(null).getNewlyInstanciatedHandlers()) {
            int code = HttpClient.create(handler.getLocalPortalURL()).connect().getResponseCode();
            if (code >= 400) {
                errors.put(handler.getLocalPortalURL().toExternalForm(), code);
            }
        }
        assertTrue(errors.toString(), errors.isEmpty());
    }
}
