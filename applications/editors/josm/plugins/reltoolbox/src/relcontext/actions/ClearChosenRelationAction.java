// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class ClearChosenRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public ClearChosenRelationAction(ChosenRelation rel) {
        super();
        //        putValue(Action.NAME, "X");
        putValue(Action.SMALL_ICON, ImageProvider.get("relcontext", "clear"));
        putValue(Action.SHORT_DESCRIPTION, tr("Clear the chosen relation"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        rel.clear();
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(newRelation != null);
    }
}
