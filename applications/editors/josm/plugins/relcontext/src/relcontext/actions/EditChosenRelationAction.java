package relcontext.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
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
        super("E");
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
