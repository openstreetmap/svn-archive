package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class DeleteChosenRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public DeleteChosenRelationAction( ChosenRelation rel ) {
        super(tr("Delete relation"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    public void actionPerformed( ActionEvent e ) {
        Relation r = rel.get();
        rel.clear();
        Main.main.undoRedo.add(new DeleteCommand(r));
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
