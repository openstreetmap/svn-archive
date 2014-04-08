package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Arrays;
import java.util.Collection;
import org.openstreetmap.josm.actions.search.PushbackTokenizer;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.actions.search.SearchCompiler.SimpleMatchFactory;

public class UtilsSimpleMatchFactory implements SimpleMatchFactory {
    
    private static Collection<String> keywords = Arrays.asList("usedinways", "usedinrelations");

    @Override
    public Collection<String> getKeywords() {
        return keywords;
    }

    @Override
    public SearchCompiler.Match get(String keyword, PushbackTokenizer tokenizer) throws SearchCompiler.ParseError {
        if ("usedinways".equals(keyword)) {
            return new UsedInWaysMatch(tokenizer);
        } else 
        if ("usedinrelations".equals(keyword)) {
            return new UsedInRelationsMatch(tokenizer);
        } else 
            return null; 
    };
    
}
