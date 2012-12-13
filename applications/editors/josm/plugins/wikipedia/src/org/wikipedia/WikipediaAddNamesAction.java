// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.io.remotecontrol.AddTagsDialog;

public class WikipediaAddNamesAction extends JosmAction {

    public WikipediaAddNamesAction() {
        super(tr("Add names from Wikipedia"), "dialogs/wikipedia",
                tr("Fetches interwiki links from Wikipedia in order to add several name tags"),
                null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] parts = getWikipediaValue().split(":", 2);
        List<String[]> tags = new ArrayList<String[]>();
        for (WikipediaApp.WikipediaLangArticle i : WikipediaApp.getInterwikiArticles(parts[0], parts[1])) {
            if (useWikipediaLangArticle(i)) {
                tags.add(new String[]{"name:" + i.lang, i.article});
            }
        }
        new AddTagsDialog(tags.toArray(new String[tags.size()][]));
    }

    protected boolean useWikipediaLangArticle(WikipediaApp.WikipediaLangArticle i) {
        return (!Main.pref.getBoolean("wikipedia.filter-iso-languages", true)
                || Arrays.asList(Locale.getISOLanguages()).contains(i.lang))
                && (!Main.pref.getBoolean("wikipedia.filter-same-names", true)
                || !i.article.equals(getCurrentDataSet().getSelected().iterator().next().get("name")));

    }

    protected String getWikipediaValue() {
        if (getCurrentDataSet() == null || getCurrentDataSet().getSelected() == null || getCurrentDataSet().getSelected().size() != 1) {
            return null;
        } else {
            return getCurrentDataSet().getSelected().iterator().next().get("wikipedia");
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getWikipediaValue() != null);
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        updateEnabledState();
    }

}
