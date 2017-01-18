// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.print;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.print.attribute.standard.OrientationRequested;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit test of {@link PrintDialog} class.
 */
public class PrintDialogTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    /**
     * Unit test of {@link PrintDialog#unmarshallPrintSetting}
     * @throws ReflectiveOperationException if an error occurs
     */
    @Test
    public void testUnmarshallPrintSetting() throws ReflectiveOperationException {
        assertEquals(OrientationRequested.PORTRAIT, PrintDialog.unmarshallPrintSetting(Arrays.asList(
                "javax.print.attribute.standard.OrientationRequested",
                "javax.print.attribute.standard.OrientationRequested",
                "javax.print.attribute.EnumSyntax",
                "3")));
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/13302">#13302</a>
     * @throws ReflectiveOperationException if an error occurs
     */
    @Test
    @Ignore("not fixed yet")
    public void testTicket13302() throws ReflectiveOperationException {
        assertEquals(OrientationRequested.PORTRAIT, PrintDialog.unmarshallPrintSetting(Arrays.asList(
                "javax.print.attribute.standard.MediaSizeName",
                "sun.print.CustomMediaSizeName",
                "javax.print.attribute.EnumSyntax",
                "82")));
    }
}
