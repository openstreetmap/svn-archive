// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Utils;
import org.wikipedia.WikipediaApp;

import java.util.Comparator;
import java.util.Optional;

public class WikidataEntry extends WikipediaEntry {

    public final String label;
    public final String description;

    public WikidataEntry(String id, String label, LatLon coordinate, String description) {
        super("wikidata", id, coordinate);
        this.label = label;
        this.description = description;
        ensureValidWikidataId(id);
    }

    @Override
    public Tag createWikipediaTag() {
        return new Tag("wikidata", article);
    }

    @Override
    public String getLabelText() {
        final String descriptionInParen = description == null ? "" : (" (" + description + ")");
        return getLabelText(label, article + descriptionInParen);
    }

    public static String getLabelText(String bold, String gray) {
        return Utils.escapeReservedCharactersHTML(bold) + " <span color='gray'>" + Utils.escapeReservedCharactersHTML(gray) + "</span>";
    }

    @Override
    public String getSearchText() {
        return Optional.ofNullable(label).orElse(article);
    }

    private static void ensureValidWikidataId(String id) {
        CheckParameterUtil.ensureThat(WikipediaApp.WIKIDATA_PATTERN.matcher(id).matches(), "Invalid Wikidata ID given: " + id);
    }

    @Override
    public int compareTo(WikipediaEntry o) {
        if (o instanceof WikidataEntry) {
            return Comparator
                    .<WikidataEntry, String>comparing(x -> x.label, AlphanumComparator.getInstance())
                    .thenComparing(x -> x.article, AlphanumComparator.getInstance())
                    .compare(this, ((WikidataEntry) o));
        } else {
            return super.compareTo(o);
        }
    }
}
