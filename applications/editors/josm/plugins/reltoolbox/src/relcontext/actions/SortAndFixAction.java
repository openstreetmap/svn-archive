package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;
import relcontext.relationfix.AssociatedStreetFixer;
import relcontext.relationfix.BoundaryFixer;
import relcontext.relationfix.MultipolygonFixer;
import relcontext.relationfix.NothingFixer;
import relcontext.relationfix.RelationFixer;

public class SortAndFixAction extends AbstractAction implements ChosenRelationListener {
	private static final long serialVersionUID = 1L;
	private ChosenRelation rel;
    private List<RelationFixer> fixers;

    public SortAndFixAction( ChosenRelation rel ) {
        super();
//        putValue(Action.NAME, "AZ");
        putValue(Action.SMALL_ICON, ImageProvider.get("data", "warning"));
        putValue(Action.SHORT_DESCRIPTION, tr("Fix roles of the chosen relation members"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);

        // construct all available fixers
        fixers = new ArrayList<RelationFixer>();
        //should be before multipolygon as takes special case of multipolygon relation - boundary
        fixers.add(new BoundaryFixer()); // boundary, multipolygon, boundary=administrative
        fixers.add(new MultipolygonFixer()); // multipolygon
        fixers.add(new AssociatedStreetFixer()); //associatedStreet

        for(RelationFixer fix : fixers) {
            fix.setFixAction(this);
        }
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Command c = fixRelation(rel.get());
        if( c != null )
            Main.main.undoRedo.add(c);
    }

    @Override
    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null && needsFixing( newRelation));
    }

    public boolean needsFixing( Relation rel ) {
        return !isIncomplete(rel) && !getFixer(rel).isRelationGood(rel);
    }

    private RelationFixer getFixer( Relation rel ) {
    	for(RelationFixer fixer : fixers)
    		if (fixer.isFixerApplicable(rel))
    			return fixer;
    	return new NothingFixer();
    }

    public Command fixRelation( Relation rel ) {
        return getFixer(rel).fixRelation(rel);
    }

    protected static boolean isIncomplete( Relation r ) {
        if( r == null || r.isIncomplete() || r.isDeleted() )
            return true;
        for( RelationMember m : r.getMembers())
            if( m.getMember().isIncomplete() )
                return true;
        return false;
    }

}
