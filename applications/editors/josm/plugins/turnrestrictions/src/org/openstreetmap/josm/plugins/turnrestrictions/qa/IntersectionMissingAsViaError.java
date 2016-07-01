// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler.BasicEditorFokusTargets;

/**
 * Issue when 'from' and 'to' intersect at node n and n isn't a via.
 *
 */
public class IntersectionMissingAsViaError extends Issue {
    private Way from;
    private Way to;
    private Node intersect;

    public IntersectionMissingAsViaError(IssuesModel parent, Way from, Way to, Node intersect) {
        super(parent, Severity.ERROR);
        this.from = from;
        this.to = to;
        this.intersect = intersect;
        actions.add(new SetVia());
        actions.add(new FixInEditorAction());
    }

    @Override
    public String getText() {
        // CHECKSTYLE.OFF: LineLength
        return tr("The <strong>from</strong>-way <span class=\"object-name\">{0}</span> and the <strong>to</strong>-way <span class=\"object-name\">{1}</span> "
               + "intersect at node <span class=\"object-name\">{2}</span> but this node isn''t a <strong>via</strong>-object.<br> "
               + "It is recommended to set it as unique <strong>via</strong>-object.",
               this.from.getDisplayName(DefaultNameFormatter.getInstance()),
               this.to.getDisplayName(DefaultNameFormatter.getInstance()),
               this.intersect.getDisplayName(DefaultNameFormatter.getInstance())
        );
        // CHECKSTYLE.ON: LineLength
    }

    class SetVia extends AbstractAction {
        SetVia() {
            putValue(NAME, tr("Set via-Object"));
            putValue(SHORT_DESCRIPTION, tr("Replaces the currently configured via-objects with the node at the intersection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getEditorModel().setVias(Collections.<OsmPrimitive>singletonList(intersect));
        }
    }

    class FixInEditorAction extends AbstractAction {
        FixInEditorAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to Basic Editor and manually fix the list of via-objects"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoBasicEditor(BasicEditorFokusTargets.VIA);
        }
    }
}
