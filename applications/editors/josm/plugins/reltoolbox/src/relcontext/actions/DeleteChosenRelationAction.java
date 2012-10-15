package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.Collections;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
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
        setEnabled(rel.get() != null);
    }

    public void actionPerformed( ActionEvent e ) {
        Relation r = rel.get();
        rel.clear();
        Command c = DeleteCommand.delete(Main.main.getEditLayer(), Collections.singleton(r), true, true);
        if( c != null )
            Main.main.undoRedo.add(c);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
