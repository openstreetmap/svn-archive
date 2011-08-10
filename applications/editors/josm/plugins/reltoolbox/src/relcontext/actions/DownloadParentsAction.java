package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadReferrersTask;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.dialogs.relation.DownloadRelationMemberTask;
import org.openstreetmap.josm.gui.dialogs.relation.DownloadRelationTask;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * Downloads parent relations for this relation and all parent objects for its members.
 * 
 * @author Zverik
 */
public class DownloadParentsAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public DownloadParentsAction( ChosenRelation rel ) {
        super(tr("Download referrers"));
        putValue(SMALL_ICON, ImageProvider.get("downloadreferrers"));
        putValue(SHORT_DESCRIPTION, tr("Download referrers for the chosen relation and its members."));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null && Main.map.mapView.getEditLayer() != null);
    }

    public void actionPerformed( ActionEvent e ) {
        Relation relation = rel.get();
        if( relation == null ) return;
        List<OsmPrimitive> objects = new ArrayList<OsmPrimitive>();
        objects.add(relation);
        objects.addAll(relation.getMemberPrimitives());
        Main.worker.submit(new DownloadReferrersTask(Main.map.mapView.getEditLayer(), objects));
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null && Main.map.mapView.getEditLayer() != null);
    }

    protected void downloadMembers( Relation rel ) {
        if( !rel.isNew() ) {
            Main.worker.submit(new DownloadRelationTask(Collections.singletonList(rel), Main.map.mapView.getEditLayer()));
        }
    }

    protected void downloadIncomplete( Relation rel ) {
        if( rel.isNew() ) return;
        Set<OsmPrimitive> ret = new HashSet<OsmPrimitive>();
        ret.addAll(rel.getIncompleteMembers());
        if( ret.isEmpty() ) return;
        Main.worker.submit(new DownloadRelationMemberTask(Collections.singletonList(rel), ret, Main.map.mapView.getEditLayer()));
    }
}
