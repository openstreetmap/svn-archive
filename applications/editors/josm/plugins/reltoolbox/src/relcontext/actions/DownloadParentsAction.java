// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.actions.downloadtasks.DownloadReferrersTask;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
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

    public DownloadParentsAction(ChosenRelation rel) {
        super(tr("Download referrers"));
        putValue(SMALL_ICON, ImageProvider.get("download"));
        putValue(SHORT_DESCRIPTION, tr("Download referrers for the chosen relation and its members."));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(rel.get() != null && MainApplication.getLayerManager().getEditLayer() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Relation relation = rel.get();
        if (relation == null) return;
        List<OsmPrimitive> objects = new ArrayList<>();
        objects.add(relation);
        objects.addAll(relation.getMemberPrimitives());
        MainApplication.worker.submit(
                new DownloadReferrersTask(MainApplication.getLayerManager().getEditLayer(), objects));
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(newRelation != null && MainApplication.getLayerManager().getEditLayer() != null);
    }

    protected void downloadMembers(Relation rel) {
        if (!rel.isNew()) {
            MainApplication.worker.submit(
                    new DownloadRelationTask(Collections.singletonList(rel), MainApplication.getLayerManager().getEditLayer()));
        }
    }

    protected void downloadIncomplete(Relation rel) {
        if (rel.isNew()) return;
        Set<OsmPrimitive> ret = new HashSet<>();
        ret.addAll(rel.getIncompleteMembers());
        if (ret.isEmpty()) return;
        MainApplication.worker.submit(
                new DownloadRelationMemberTask(Collections.singletonList(rel), ret, MainApplication.getLayerManager().getEditLayer()));
    }
}
