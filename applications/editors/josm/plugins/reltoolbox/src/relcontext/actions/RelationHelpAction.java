// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.properties.HelpAction;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class RelationHelpAction extends AbstractAction implements ChosenRelationListener {
    private final ChosenRelation rel;

    public RelationHelpAction(ChosenRelation rel) {
        putValue(NAME, tr("Open relation wiki page"));
        putValue(SHORT_DESCRIPTION, tr("Launch browser with wiki help for selected object"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "search"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(newRelation != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (rel.get() == null)
            return;
        try {
            MainApplication.worker.execute(() -> HelpAction.displayRelationHelp(rel.get()));
        } catch (Exception e1) {
            Logging.error(e1);
        }
    }
}
