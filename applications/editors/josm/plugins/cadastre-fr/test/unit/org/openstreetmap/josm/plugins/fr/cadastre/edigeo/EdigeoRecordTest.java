// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoRecord.Format;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoRecord.Nature;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test of {@link EdigeoRecord}.
 */
public class EdigeoRecordTest {

    /**
     * Unit test of {@link EdigeoRecord#EdigeoRecord}.
     */
    @Test
    public void testEdigeoRecord() {
        EdigeoRecord r = new EdigeoRecord("SCPCP27:TEST01;SeSD;OBJ;PARCELLE_id");
        assertEquals("SCP", r.name);
        assertEquals(Nature.COMPOSED, r.nature);
        assertEquals(Format.DESCRIPTOR_REF, r.format);
        assertEquals(27, r.length);
        assertEquals(Arrays.asList("TEST01", "SeSD", "OBJ", "PARCELLE_id"), r.values);
    }

    /**
     * Unit test of methods {@link EdigeoRecord#equals} and {@link EdigeoRecord#hashCode}.
     */
    @Test
    public void testEqualsContract() {
        TestUtils.assumeWorkingEqualsVerifier();
        EqualsVerifier.forClass(EdigeoRecord.class).usingGetClass()
            .verify();
    }
}
