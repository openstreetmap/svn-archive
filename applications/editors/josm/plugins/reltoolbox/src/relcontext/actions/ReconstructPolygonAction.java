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

/**
 * Make a single polygon out of the multipolygon relation. The relation must have only outer members.
 * @author Zverik
 */
public class ReconstructPolygonAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public ReconstructPolygonAction( ChosenRelation rel ) {
        super(tr("Reconstruct polygon"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
	putValue(LONG_DESCRIPTION, "Reconstruct polygon from multipolygon relation");
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    public void actionPerformed( ActionEvent e ) {
        Relation r = rel.get();
        rel.clear();
//        Main.main.undoRedo.add(new DeleteCommand(r));
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null); //todo
    }
}
