// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class WikipediaEntryTest {

    @Test
    public void testParseFromUrl1() {
        final WikipediaEntry actual = WikipediaEntry.parseFromUrl("https://de.wikipedia.org/wiki/Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl2() {
        final WikipediaEntry actual = WikipediaEntry.parseFromUrl("http://de.wikipedia.org/wiki/%C3%96sterreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl3() {
        final WikipediaEntry actual = WikipediaEntry.parseFromUrl("http://de.wikipedia.org/wiki/Sternheim_%26_Emanuel");
        assertThat(actual.article, is("Sternheim_&_Emanuel"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl4() {
        final WikipediaEntry actual = WikipediaEntry.parseFromUrl("//de.wikipedia.org/wiki/Reichstagsgeb%C3%A4ude");
        assertThat(actual.article, is("Reichstagsgebäude"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag0() {
        final WikipediaEntry actual = WikipediaEntry.parseTag("wikipedia", "Österreich");
        assertThat(actual, nullValue());
    }

    @Test
    public void testParseFromTag1() {
        final WikipediaEntry actual = WikipediaEntry.parseTag("wikipedia", "de:Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag2() {
        final WikipediaEntry actual = WikipediaEntry.parseTag("wikipedia:de", "Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag3() {
        final WikipediaEntry actual = WikipediaEntry.parseTag("wikipedia:de", "de:Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag4() {
        final WikipediaEntry actual = WikipediaEntry.parseTag("wikipedia", "https://de.wikipedia.org/wiki/Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testGetBrowserUrl() {
        final WikipediaEntry entry = new WikipediaEntry("de", "Sternheim & Emanuel");
        assertThat(entry.getBrowserUrl(), is("https://de.wikipedia.org/wiki/Sternheim_%26_Emanuel"));
    }

}