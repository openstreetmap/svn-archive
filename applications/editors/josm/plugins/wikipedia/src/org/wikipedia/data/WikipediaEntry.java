// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.Utils;
import org.wikipedia.WikipediaApp;

import java.util.Comparator;

public class WikipediaEntry implements Comparable<WikipediaEntry> {

    public final String label;
    public final String wikipediaLang, wikipediaArticle;
    public final LatLon coordinate;
    private Boolean wiwosmStatus;

    public WikipediaEntry(String wikipediaLang, String wikipediaArticle) {
        this(wikipediaLang, wikipediaArticle, null, null);
    }

    public WikipediaEntry(String wikipediaLang, String wikipediaArticle, String label, LatLon coordinate) {
        this.label = label;
        this.wikipediaLang = wikipediaLang;
        this.wikipediaArticle = wikipediaArticle;
        this.coordinate = coordinate;
    }

    public Tag createWikipediaTag() {
        return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
    }

    public void setWiwosmStatus(Boolean wiwosmStatus) {
        this.wiwosmStatus = wiwosmStatus;
    }

    public Boolean getWiwosmStatus() {
        return wiwosmStatus;
    }

    public String getBrowserUrl() {
        return WikipediaApp.getSiteUrl(wikipediaLang) + "/wiki/" + Utils.encodeUrl(wikipediaArticle.replace(" ", "_"));
    }

    public String getLabelText() {
        return wikipediaArticle;
    }

    @Override
    public String toString() {
        return wikipediaArticle;
    }

    @Override
    public int compareTo(WikipediaEntry o) {
        return Comparator
                .<WikipediaEntry, String>comparing(x -> x.label, AlphanumComparator.getInstance())
                .thenComparing(x -> x.wikipediaArticle, AlphanumComparator.getInstance())
                .compare(this, o);
    }
}
