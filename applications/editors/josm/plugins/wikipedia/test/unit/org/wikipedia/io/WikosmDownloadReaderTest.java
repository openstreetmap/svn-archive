// License: GPL. For details, see LICENSE file.
package org.wikipedia.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests of {@link WikosmDownloadReader} class.
 */
public class WikosmDownloadReaderTest {

    /**
     * Base test environment is enough
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules().preferences();

    /**
     * Tests evaluating the extended query feature {@code date}.
     */
    @Test
    public void testIdParsing() throws UnsupportedEncodingException {
        String json = String.join("\n",
                "{\"results\":{\"bindings\":[",
                "{\"a\":{\"type\": \"uri\", \"value\": \"https://www.openstreetmap.org/node/12345\"}},",
                "{\"a\":{\"type\": \"uri\", \"value\": \"https://www.openstreetmap.org/way/1234512345\"}},",
                "{\"a\":{\"type\": \"uri\", \"value\": \"https://www.openstreetmap.org/relation/98765\"}}",
                "]}}"
        );

        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8.name()));
        List<PrimitiveId> actual = WikosmDownloadReader.getPrimitiveIds(stream);

        List<PrimitiveId> expected = Arrays.asList(new PrimitiveId[]{
                new SimplePrimitiveId(12345, OsmPrimitiveType.NODE),
                new SimplePrimitiveId(1234512345, OsmPrimitiveType.WAY),
                new SimplePrimitiveId(98765, OsmPrimitiveType.RELATION),
        });

        assertThat(actual, is(expected));
    }
}
