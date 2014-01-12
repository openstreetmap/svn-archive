package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class SelectInRelationPanelAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public SelectInRelationPanelAction( ChosenRelation rel ) {
        super();
        putValue(NAME, tr("Select in relation list"));
        putValue(SHORT_DESCRIPTION, tr("Select relation in relation list."));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "relationlist"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null);
    }

    public void actionPerformed( ActionEvent e ) {
        if( rel.get() != null ) {
            Main.map.relationListDialog.selectRelation(rel.get());
            Main.map.relationListDialog.unfurlDialog();
        }
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
