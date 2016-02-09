// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.Utils;

import javax.swing.JOptionPane;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

public class GuiUtils {

    static final String PREF_OVERWRITE = "wikipedia.overwrite-tag";

    protected static boolean confirmOverwrite(final String key, final String newValue, final Collection<OsmPrimitive> primitives) {
        final SortedSet<String> existingValues = new TreeSet<>(AlphanumComparator.getInstance());
        existingValues.addAll(Utils.transform(primitives, new Utils.Function<OsmPrimitive, String>() {
            @Override
            public String apply(OsmPrimitive x) {
                return x.get(key);
            }
        }));
        existingValues.remove(newValue);
        existingValues.remove(null);

        if (existingValues.isEmpty()) {
            return true;
        }
        final Boolean r = GuiHelper.runInEDTAndWaitAndReturn(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ConditionalOptionPaneUtil.showConfirmationDialog(PREF_OVERWRITE, Main.parent,
                        trn(
                                "Overwrite ''{0}'' tag {1} from {2} with new value ''{3}''?",
                                "Overwrite ''{0}'' tags {1} from {2} with new value ''{3}''?", existingValues.size(),
                                key, Utils.joinAsHtmlUnorderedList(existingValues),
                                DefaultNameFormatter.getInstance().formatAsHtmlUnorderedList(primitives, 10), newValue),
                        tr("Overwrite key"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_OPTION);
            }
        });
        return Boolean.TRUE.equals(r);
    }
}
