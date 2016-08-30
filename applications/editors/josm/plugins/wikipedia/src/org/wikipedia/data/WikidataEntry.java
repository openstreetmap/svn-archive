// License: GPL. For details, see LICENSE file.
package org.wikipedia.data;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Utils;
import org.wikipedia.WikipediaApp;

public class WikidataEntry extends WikipediaEntry {

    public final String description;

    public WikidataEntry(String id, String label, LatLon coordinate, String description) {
        super("wikidata", id, label, coordinate);
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

    private static void ensureValidWikidataId(String id) {
        CheckParameterUtil.ensureThat(WikipediaApp.WIKIDATA_PATTERN.matcher(id).matches(), "Invalid Wikidata ID given: " + id);
    }
}
