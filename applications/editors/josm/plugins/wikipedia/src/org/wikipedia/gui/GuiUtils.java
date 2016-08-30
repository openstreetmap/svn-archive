// License: GPL. See LICENSE file for details./*
package org.wikipedia.gui;

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
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

public class GuiUtils {

    public static final String PREF_OVERWRITE = "wikipedia.overwrite-tag";

    public static boolean confirmOverwrite(final String key, final String newValue, final Collection<OsmPrimitive> primitives) {
        final SortedSet<String> existingValues = primitives.stream()
                .map(x -> x.get(key))
                .filter(x -> x != null && !newValue.equals(x))
                .collect(Collectors.toCollection(() -> new TreeSet<>(AlphanumComparator.getInstance())));

        if (existingValues.isEmpty()) {
            return true;
        }
        final Boolean r = GuiHelper.runInEDTAndWaitAndReturn(() ->
                ConditionalOptionPaneUtil.showConfirmationDialog(PREF_OVERWRITE, Main.parent,
                        trn(
                                "Overwrite ''{0}'' tag {1} from {2} with new value ''{3}''?",
                                "Overwrite ''{0}'' tags {1} from {2} with new value ''{3}''?", existingValues.size(),
                                key, Utils.joinAsHtmlUnorderedList(existingValues),
                                DefaultNameFormatter.getInstance().formatAsHtmlUnorderedList(primitives, 10), newValue),
                        tr("Overwrite key"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_OPTION));
        return Boolean.TRUE.equals(r);
    }
}
