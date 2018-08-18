// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.MultipolygonBuilder;
import org.openstreetmap.josm.data.osm.MultipolygonBuilder.JoinedPolygon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * Make a single polygon out of the multipolygon relation. The relation must have only outer members.
 * @author Zverik
 */
public class ReconstructPolygonAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    private static final List<String> IRRELEVANT_KEYS = Arrays.asList(new String[] {
            "source", "created_by", "note"});

    public ReconstructPolygonAction(ChosenRelation rel) {
        super(tr("Reconstruct polygon"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "filter"));
        putValue(LONG_DESCRIPTION, "Reconstruct polygon from multipolygon relation");
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(isSuitableRelation(rel.get()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Relation r = rel.get();
        boolean relationReused = false;
        List<Way> ways = new ArrayList<>();
        boolean wont = false;
        for (RelationMember m : r.getMembers()) {
            if (m.isWay()) {
                ways.add(m.getWay());
            } else {
                wont = true;
            }
        }
        if (wont) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Multipolygon must consist only of ways"), tr("Reconstruct polygon"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        MultipolygonBuilder mpc = new MultipolygonBuilder();
        String error = mpc.makeFromWays(ways);
        if (error != null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), error);
            return;
        }

        rel.clear();
        List<OsmPrimitive> newSelection = new ArrayList<>();
        List<Command> commands = new ArrayList<>();
        Command relationDeleteCommand = DeleteCommand.delete(Collections.singleton(r), true, true);
        if (relationDeleteCommand == null)
            return;

        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        for (JoinedPolygon p : mpc.outerWays) {

            ArrayList<JoinedPolygon> myInnerWays = new ArrayList<>();
            for (JoinedPolygon i : mpc.innerWays) {
                // if the first point of any inner ring is contained in this
                // outer ring, then this inner ring belongs to us. This
                // assumption only works if multipolygons have valid geometries
                EastNorth en = i.ways.get(0).firstNode().getEastNorth();
                if (p.area.contains(en.east(), en.north())) {
                    myInnerWays.add(i);
                }
            }

            if (!myInnerWays.isEmpty()) {
                // this ring has inner rings, so we leave a multipolygon in
                // place and don't reconstruct the rings.
                Relation n = null;
                if (relationReused) {
                    n = new Relation();
                    n.setKeys(r.getKeys());
                } else {
                    n = new Relation(r);
                    n.setMembers(null);
                }
                for (Way w : p.ways) {
                    n.addMember(new RelationMember("outer", w));
                }
                for (JoinedPolygon i : myInnerWays) {
                    for (Way w : i.ways) {
                        n.addMember(new RelationMember("inner", w));
                    }
                }
                if (relationReused) {
                    commands.add(new AddCommand(ds, n));
                } else {
                    relationReused = true;
                    commands.add(new ChangeCommand(r, n));
                }
                newSelection.add(n);
                continue;
            }

            // move all tags from relation and common tags from ways
            // start with all tags from first way but only if area tags are present
            Map<String, String> tags = p.ways.get(0).getKeys();
            if (!p.ways.get(0).hasAreaTags()) {
                tags.clear();
            }
            List<OsmPrimitive> relations = p.ways.get(0).getReferrers();
            Set<String> noTags = new HashSet<>(r.keySet());
            for (int i = 1; i < p.ways.size(); i++) {
                Way w = p.ways.get(i);
                for (String key : w.keySet()) {
                    String value = w.get(key);
                    if (!noTags.contains(key) && tags.containsKey(key) && !tags.get(key).equals(value)) {
                        tags.remove(key);
                        noTags.add(key);
                    }
                }
                List<OsmPrimitive> referrers = w.getReferrers();
                for (Iterator<OsmPrimitive> ref1 = relations.iterator(); ref1.hasNext();) {
                    if (!referrers.contains(ref1.next())) {
                        ref1.remove();
                    }
                }
            }
            tags.putAll(r.getKeys());
            tags.remove("type");

            // then delete ways that are not relevant (do not take part in other relations of have strange tags)
            Way candidateWay = null;
            for (Way w : p.ways) {
                if (w.getReferrers().equals(relations)) {
                    // check tags that remain
                    Set<String> keys = new HashSet<>(w.keySet());
                    keys.removeAll(tags.keySet());
                    keys.removeAll(IRRELEVANT_KEYS);
                    if (keys.isEmpty()) {
                        if (candidateWay == null) {
                            candidateWay = w;
                        } else {
                            if (candidateWay.isNew() && !w.isNew()) {
                                // prefer ways that are already in the database
                                Way tmp = w;
                                w = candidateWay;
                                candidateWay = tmp;
                            }
                            commands.add(new DeleteCommand(w));
                        }
                    }
                }
            }

            // take the first way, put all nodes into it, making it a closed polygon
            Way result = candidateWay == null ? new Way() : new Way(candidateWay);
            result.setNodes(p.nodes);
            result.addNode(result.firstNode());
            result.setKeys(tags);
            newSelection.add(candidateWay == null ? result : candidateWay);
            commands.add(candidateWay == null ? new AddCommand(ds, result) : new ChangeCommand(candidateWay, result));
        }

        // only delete the relation if it hasn't been re-used
        if (!relationReused) {
            commands.add(relationDeleteCommand);
        }

        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Reconstruct polygons from relation {0}",
                r.getDisplayName(DefaultNameFormatter.getInstance())), commands));
        ds.setSelected(newSelection);
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        setEnabled(isSuitableRelation(newRelation));
    }

    private boolean isSuitableRelation(Relation newRelation) {
        if (newRelation == null || !"multipolygon".equals(newRelation.get("type")) || newRelation.getMembersCount() == 0)
            return false;
        else
            return true;
    }
}
