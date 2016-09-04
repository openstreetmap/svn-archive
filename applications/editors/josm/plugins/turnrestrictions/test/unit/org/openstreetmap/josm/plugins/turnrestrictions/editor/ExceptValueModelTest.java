// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExceptValueModelTest {

    @Test
    public void testConstructors() {
        new ExceptValueModel();
        new ExceptValueModel(null);
        new ExceptValueModel("");
        new ExceptValueModel("  ");
        new ExceptValueModel("hgv");
        new ExceptValueModel("hgv;psv");
        new ExceptValueModel("non_standard");
    }

    @Test
    public void testSetValue() {
        ExceptValueModel evm;

        // null value allowed - means no vehicle exceptions
        evm = new ExceptValueModel();
        evm.setValue(null);
        assertEquals("", evm.getValue());
        assertTrue(evm.isStandard());

        // empty string allowed - means no vehicle expections
        evm = new ExceptValueModel();
        evm.setValue("");
        assertEquals("", evm.getValue());
        assertTrue(evm.isStandard());

        // a single standard vehicle exeption
        evm = new ExceptValueModel();
        evm.setValue("hgv");
        assertEquals("hgv", evm.getValue());
        assertTrue(evm.isVehicleException("hgv"));
        assertFalse(evm.isVehicleException("psv"));
        assertTrue(evm.isStandard());

        // two standard vehicle exceptions
        evm = new ExceptValueModel();
        evm.setValue("hgv;psv");
        assertEquals("hgv;psv", evm.getValue());
        assertTrue(evm.isVehicleException("hgv"));
        assertTrue(evm.isVehicleException("psv"));
        assertTrue(evm.isStandard());

        // white space and lowercase/uppercase mix allowed. Should be normalized
        // by the except value model
        evm = new ExceptValueModel();
        evm.setValue(" hGv ; PsV  ");
        assertEquals("hgv;psv", evm.getValue());
        assertTrue(evm.isVehicleException("hgv"));
        assertTrue(evm.isVehicleException("psv"));
        assertTrue(evm.isStandard());

        // non standard value allowed
        evm = new ExceptValueModel();
        evm.setValue("Non Standard");
        assertEquals("Non Standard", evm.getValue());
        assertFalse(evm.isVehicleException("hgv"));
        assertFalse(evm.isVehicleException("psv"));
        assertFalse(evm.isStandard());
    }
}
