package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * Opens an editor for chosen relation.
 * 
 * @author Zverik
 */
public class EditChosenRelationAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public EditChosenRelationAction( ChosenRelation rel ) {
        super();
//        putValue(NAME, "E");
        putValue(SMALL_ICON, ImageProvider.get("dialogs/mappaint", "pencil"));
        putValue(SHORT_DESCRIPTION, tr("Open relation editor for the chosen relation"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    public void actionPerformed( ActionEvent e ) {
        Relation relation = rel.get();
        if( relation == null ) return;
        RelationEditor.getEditor(Main.map.mapView.getEditLayer(), relation, null).setVisible(true);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
