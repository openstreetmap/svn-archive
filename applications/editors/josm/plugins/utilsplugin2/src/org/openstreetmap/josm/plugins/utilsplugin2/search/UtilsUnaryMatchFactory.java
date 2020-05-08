// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Arrays;
import java.util.Collection;

import org.openstreetmap.josm.data.osm.search.PushbackTokenizer;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.search.SearchCompiler.CoreUnaryMatchFactory;
import org.openstreetmap.josm.data.osm.search.SearchParseError;

/**
 * Extension of the {@link CoreUnaryMatchFactory}.
 */
public class UtilsUnaryMatchFactory implements SearchCompiler.UnaryMatchFactory {

    private static Collection<String> keywords = Arrays.asList("inside",
            "intersecting", "allintersecting", "adjacent", "connected");

    @Override
    public SearchCompiler.UnaryMatch get(String keyword, SearchCompiler.Match matchOperand, PushbackTokenizer tokenizer)
            throws SearchParseError {
        if ("inside".equals(keyword)) {
            return new InsideMatch(matchOperand);
        } else if ("adjacent".equals(keyword)) {
            return new ConnectedMatch(matchOperand, false);
        } else if ("connected".equals(keyword)) {
            return new ConnectedMatch(matchOperand, true);
        } else if ("intersecting".equals(keyword)) {
            return new IntersectingMatch(matchOperand, false);
        } else if ("allintersecting".equals(keyword)) {
            return new IntersectingMatch(matchOperand, true);
        }
        return null;
    }

    @Override
    public Collection<String> getKeywords() {
        return keywords;
    }
}
