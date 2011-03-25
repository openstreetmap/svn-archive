package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class SelectRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public SelectRelationAction( ChosenRelation rel ) {
        super(tr("Select relation"));
        putValue(SHORT_DESCRIPTION, tr("Select relation in main selection."));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "select"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    public void actionPerformed( ActionEvent e ) {
        Main.map.mapView.getEditLayer().data.setSelected(rel.get() == null ? null : rel.get());
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
