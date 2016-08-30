// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.Utils;
import org.wikipedia.WikipediaApp;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaEntry implements Comparable<WikipediaEntry> {

    public final String lang;
    public final String article;
    public final String label;
    public final LatLon coordinate;
    private Boolean wiwosmStatus;

    public WikipediaEntry(String lang, String article) {
        this(lang, article, null, null);
    }

    public WikipediaEntry(String lang, String article, String label, LatLon coordinate) {
        this.lang = lang;
        this.article = article;
        this.label = label;
        this.coordinate = coordinate;
    }

    static WikipediaEntry parseFromUrl(String url) {
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
        return new WikipediaEntry(m.group(2), m.group(3));
    }

    public static WikipediaEntry parseTag(String key, String value) {
        if (value == null) {
            return null;
        } else if (value.startsWith("http")) {
            //wikipedia=http...
            return WikipediaEntry.parseFromUrl(value);
        } else if (value.contains(":")) {
            //wikipedia=[lang]:[article]
            //wikipedia:[lang]=[lang]:[article]
            final String[] item = Utils.decodeUrl(value).split(":", 2);
            final String article = item[1].replace("_", " ");
            return new WikipediaEntry(item[0], article);
        } else if (key.startsWith("wikipedia:")) {
            //wikipedia:[lang]=[lang]:[article]
            //wikipedia:[lang]=[article]
            final String lang = key.split(":", 2)[1];
            final String[] item = Utils.decodeUrl(value).split(":", 2);
            final String article = item[item.length == 2 ? 1 : 0].replace("_", " ");
            return new WikipediaEntry(lang, article);
        } else {
            return null;
        }
    }

    public Tag createWikipediaTag() {
        return new Tag("wikipedia", lang + ":" + article);
    }

    public void setWiwosmStatus(Boolean wiwosmStatus) {
        this.wiwosmStatus = wiwosmStatus;
    }

    public Boolean getWiwosmStatus() {
        return wiwosmStatus;
    }

    public String getBrowserUrl() {
        return WikipediaApp.getSiteUrl(lang) + "/wiki/" + Utils.encodeUrl(article.replace(" ", "_"));
    }

    public String getLabelText() {
        return article;
    }

    @Override
    public String toString() {
        return article;
    }

    @Override
    public int compareTo(WikipediaEntry o) {
        return Comparator
                .<WikipediaEntry, String>comparing(x -> x.label, AlphanumComparator.getInstance())
                .thenComparing(x -> x.article, AlphanumComparator.getInstance())
                .compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WikipediaEntry that = (WikipediaEntry) o;
        return Objects.equals(lang, that.lang) &&
                Objects.equals(article, that.article);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lang, article);
    }
}
