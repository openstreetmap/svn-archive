package org.wikipedia;

import org.junit.Test;
import org.wikipedia.WikipediaApp.WikipediaEntry;
import org.wikipedia.WikipediaApp.WikipediaLangArticle;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class WikipediaAppTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testPartitionList() {
        assertThat(
                WikipediaApp.partitionList(Arrays.asList(1, 2, 3, 4, 5), 2),
                is(Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(3, 4),
                        Arrays.asList(5)
                ))
        );
    }

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

    @Test
    public void testGetInterwikiArticles1() {
        final Collection<WikipediaLangArticle> iw = WikipediaApp.getInterwikiArticles("de", "Österreich");
        assertThat(iw, hasItem(new WikipediaLangArticle("en", "Austria")));
        assertThat(iw, hasItem(new WikipediaLangArticle("no", "Østerrike")));
        assertThat(iw, hasItem(new WikipediaLangArticle("ko", "오스트리아")));
    }

    @Test
    public void testGetInterwikiArticles2() {
        final Collection<WikipediaLangArticle> iw = WikipediaApp.getInterwikiArticles("en", "Ampersand");
        assertThat(iw, hasItem(new WikipediaLangArticle("fi", "&")));
    }

    @Test
    public void testGetBrowserUrl() {
        final WikipediaEntry entry = new WikipediaEntry("Sternheim & Emanuel", "de", "Sternheim & Emanuel");
        assertThat(entry.getBrowserUrl(), is("http://de.wikipedia.org/wiki/Sternheim_%26_Emanuel"));
    }

}
