// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.Logging;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests of {@link #SurveyorShowAction}
 */
public class SurveyorShowActionTest {

    /**
     * Setup rule
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules().preferences();

    @Test
    public void testCreateComponent() {
        Logging.clearLastErrorAndWarnings();
        SurveyorComponent comp = SurveyorShowAction.createComponent();
        assertNotNull(comp);
        List<String> errors = Logging.getLastErrorAndWarnings();
        assertTrue(errors.toString(), errors.isEmpty());
    }
}
