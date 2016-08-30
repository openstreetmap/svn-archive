// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WikipediaEntryTest {

    @Test
    public void testGetBrowserUrl() {
        final WikipediaEntry entry = new WikipediaEntry("de", "Sternheim & Emanuel");
        assertThat(entry.getBrowserUrl(), is("https://de.wikipedia.org/wiki/Sternheim_%26_Emanuel"));
    }

}