// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.http2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openstreetmap.josm.plugins.http2.Http2Client.Http2Response;

/**
 * Unit test of {@link Http2Client}
 */
public class Http2ClientTest {

    /**
     * Unit test of {@link Http2Response#parseDate}
     */
    @Test
    public void testParseDate() {
        assertEquals(786297600000L, Http2Response.parseDate("Thu, 01 Dec 1994 16:00:00 GMT"));
        assertEquals(783459811000L, Http2Response.parseDate("Sat, 29 Oct 1994 19:43:31 GMT"));
        assertEquals(784903526000L, Http2Response.parseDate("Tue, 15 Nov 1994 12:45:26 GMT"));
        // == RFC 2616 ==
        // All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT), without exception.
        assertEquals(0L, Http2Response.parseDate("Thu, 01 Jan 1970 01:00:00 CET"));
        // == RFC 2616 ==
        // HTTP/1.1 clients and caches MUST treat other invalid date formats,
        // especially including the value "0", as in the past (i.e., "already
        // expired").
        assertEquals(0L, Http2Response.parseDate("0"));
        assertEquals(0L, Http2Response.parseDate("foo-bar"));
    }
}
