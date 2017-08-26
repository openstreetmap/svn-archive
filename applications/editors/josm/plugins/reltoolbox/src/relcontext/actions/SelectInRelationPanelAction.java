// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class SelectInRelationPanelAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public SelectInRelationPanelAction(ChosenRelation rel) {
        super();
        putValue(NAME, tr("Select in relation list"));
        putValue(SHORT_DESCRIPTION, tr("Select relation in relation list."));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "relationlist"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (rel.get() != null) {
            MainApplication.getMap().relationListDialog.selectRelation(rel.get());
            MainApplication.getMap().relationListDialog.unfurlDialog();
        }
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(newRelation != null);
    }
}
