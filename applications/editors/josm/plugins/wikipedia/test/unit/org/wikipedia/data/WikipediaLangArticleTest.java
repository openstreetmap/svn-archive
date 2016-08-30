// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class WikipediaLangArticleTest {

    @Test
    public void testParseFromUrl1() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseFromUrl("https://de.wikipedia.org/wiki/Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl2() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseFromUrl("http://de.wikipedia.org/wiki/%C3%96sterreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl3() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseFromUrl("http://de.wikipedia.org/wiki/Sternheim_%26_Emanuel");
        assertThat(actual.article, is("Sternheim_&_Emanuel"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromUrl4() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseFromUrl("//de.wikipedia.org/wiki/Reichstagsgeb%C3%A4ude");
        assertThat(actual.article, is("Reichstagsgebäude"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag0() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseTag("wikipedia", "Österreich");
        assertThat(actual, nullValue());
    }

    @Test
    public void testParseFromTag1() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseTag("wikipedia", "de:Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag2() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseTag("wikipedia:de", "Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag3() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseTag("wikipedia:de", "de:Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

    @Test
    public void testParseFromTag4() {
        final WikipediaLangArticle actual = WikipediaLangArticle.parseTag("wikipedia", "https://de.wikipedia.org/wiki/Österreich");
        assertThat(actual.article, is("Österreich"));
        assertThat(actual.lang, is("de"));
    }

}