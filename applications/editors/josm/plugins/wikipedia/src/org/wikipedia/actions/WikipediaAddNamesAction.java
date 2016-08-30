// License: GPL. For details, see LICENSE file.
package org.wikipedia.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.io.remotecontrol.AddTagsDialog;
import org.wikipedia.WikipediaApp;
import org.wikipedia.data.WikipediaEntry;

public class WikipediaAddNamesAction extends JosmAction {

    public WikipediaAddNamesAction() {
        super(tr("Add names from Wikipedia"), "dialogs/wikipedia",
                tr("Fetches interwiki links from Wikipedia in order to add several name tags"),
                null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final WikipediaEntry wp = WikipediaEntry.parseTag("wikipedia", getWikipediaValue());
        List<String[]> tags = new ArrayList<>();
        WikipediaApp.getInterwikiArticles(wp.lang, wp.article).stream()
                .filter(this::useWikipediaLangArticle)
                .map(i -> new String[]{"name:" + i.lang, i.article})
                .forEach(tags::add);
        if (Main.isDebugEnabled()) {
            Main.debug(tags.toString());
        }
        AddTagsDialog.addTags(tags.toArray(new String[tags.size()][]), "Wikipedia", getLayerManager().getEditDataSet().getSelected());
    }

    private boolean useWikipediaLangArticle(WikipediaEntry i) {
        return (!Main.pref.getBoolean("wikipedia.filter-iso-languages", true)
                || Arrays.asList(Locale.getISOLanguages()).contains(i.lang))
                && (!Main.pref.getBoolean("wikipedia.filter-same-names", true)
                || !i.article.equals(getLayerManager().getEditDataSet().getSelected().iterator().next().get("name")));
    }

    private String getWikipediaValue() {
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds == null || ds.getSelected() == null || ds.getSelected().size() != 1) {
            return null;
        } else {
            return ds.getSelected().iterator().next().get("wikipedia");
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
