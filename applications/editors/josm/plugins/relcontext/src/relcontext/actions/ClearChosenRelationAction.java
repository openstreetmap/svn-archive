package relcontext.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class ClearChosenRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public ClearChosenRelationAction( ChosenRelation rel ) {
        super();
//        putValue(Action.NAME, "X");
        putValue(Action.SMALL_ICON, ImageProvider.get("relcontext", "clear"));
        putValue(Action.SHORT_DESCRIPTION, tr("Clear chosen relation"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    public void actionPerformed( ActionEvent e ) {
        rel.clear();
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
