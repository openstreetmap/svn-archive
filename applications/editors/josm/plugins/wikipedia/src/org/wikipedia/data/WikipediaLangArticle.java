package org.wikipedia.data;

import org.openstreetmap.josm.tools.Utils;
import org.wikipedia.WikipediaApp;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaLangArticle {

    public final String lang, article;

    public WikipediaLangArticle(String lang, String article) {
        this.lang = lang;
        this.article = article;
    }

    public static WikipediaLangArticle parseFromUrl(String url) {
        if (url == null) {
            return null;
        }
        // decode URL for nicer value
        url = Utils.decodeUrl(url);
        // extract Wikipedia language and
        final Matcher m = Pattern.compile("(https?:)?//(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
        if (!m.matches()) {
            return null;
        }
        return new WikipediaLangArticle(m.group(2), m.group(3));
    }

    public static WikipediaLangArticle parseTag(String key, String value) {
        if (value == null) {
            return null;
        } else if (value.startsWith("http")) {
            //wikipedia=http...
            return parseFromUrl(value);
        } else if (value.contains(":")) {
            //wikipedia=[lang]:[article]
            //wikipedia:[lang]=[lang]:[article]
            final String[] item = Utils.decodeUrl(value).split(":", 2);
            final String article = item[1].replace("_", " ");
            return new WikipediaLangArticle(item[0], article);
        } else if (key.startsWith("wikipedia:")) {
            //wikipedia:[lang]=[lang]:[article]
            //wikipedia:[lang]=[article]
            final String lang = key.split(":", 2)[1];
            final String[] item = Utils.decodeUrl(value).split(":", 2);
            final String article = item[item.length == 2 ? 1 : 0].replace("_", " ");
            return new WikipediaLangArticle(lang, article);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return lang + ":" + article;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WikipediaLangArticle that = (WikipediaLangArticle) o;
        return Objects.equals(lang, that.lang) &&
                Objects.equals(article, that.article);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lang, article);
    }
}
