// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.relation.DownloadSelectedIncompleteMembersAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.DownloadRelationMemberTask;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;
import org.openstreetmap.josm.tools.Utils;

public class SortPTStopsAction extends JosmAction {

    private static final long serialVersionUID = 1714879296430852530L;
    private static final String ACTION_NAME = "Sort PT Stops";

    /**
     * Creates a new SortPTStopsAction
     */
    public SortPTStopsAction() {
        super(ACTION_NAME, "icons/sortptstops", ACTION_NAME, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Relation rel = (Relation) getLayerManager().getEditDataSet().getSelected().iterator().next();

        if (rel.hasIncompleteMembers()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(Main.parent,
                tr("The relation has incomplete members. Do you want to download them and continue with the sorting?"),
                tr("Incomplete Members"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null)) {

                List<Relation> incomplete = Collections.singletonList(rel);
                Future<?> future = Main.worker.submit(new DownloadRelationMemberTask(
                        incomplete,
                        DownloadSelectedIncompleteMembersAction.buildSetOfIncompleteMembers(incomplete),
                        Main.getLayerManager().getEditLayer()));

                    Main.worker.submit(() -> {
                        try {
                            future.get();
                            continueAfterDownload(rel);
                        } catch (InterruptedException | ExecutionException e1) {
                             Main.error(e1);
                            return;
                        }
                    });
            } else
                return;
        } else
            continueAfterDownload(rel);
    }

    private void continueAfterDownload(Relation rel) {
        List<RelationMember> members = rel.getMembers();

        for (int i = 0; i < members.size(); i++) {
            rel.removeMember(0);
        }

        members = new RelationSorter().sortMembers(members);

        List<RelationMember> stops = new ArrayList<>();
        List<RelationMember> wayMembers = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            RelationMember rm = members.get(i);
            if (PTStop.isPTPlatform(rm) || PTStop.isPTStopPosition(rm))
                stops.add(rm);
            else {
                wayMembers.add(rm);
                if (rm.getType() == OsmPrimitiveType.WAY)
                    ways.add(rm.getWay());
            }
        }

        Map<String, PTStop> stopsByName = new HashMap<>();
        List<PTStop> unnamed = new ArrayList<>();
        stops.forEach(rm -> {
            String name = getStopName(rm.getMember());
            if (name != null) {
                if (!stopsByName.containsKey(name))
                    stopsByName.put(name, new PTStop(rm));
                else
                    stopsByName.get(name).addStopElement(rm);
            } else {
                unnamed.add(new PTStop(rm));
            }
        });

        StopToWayAssigner assigner = new StopToWayAssigner(ways);
        List<PTStop> ptstops = new ArrayList<>(stopsByName.values());
        Map<Way, List<PTStop>> wayStop = new HashMap<>();
        ptstops.forEach(stop -> {
            Way way = assigner.get(stop);
            if (!wayStop.containsKey(way))
                wayStop.put(way, new ArrayList<PTStop>());
            wayStop.get(way).add(stop);
        });

        unnamed.forEach(stop -> {
            Way way = assigner.get(stop);
            if (!wayStop.containsKey(way))
                wayStop.put(way, new ArrayList<PTStop>());
            wayStop.get(way).add(stop);
        });

        wayMembers.forEach(wm -> {
            if (wm.getType() != OsmPrimitiveType.WAY)
                return;
            List<PTStop> stps = wayStop.get(wm.getWay());
            if (stps == null)
                return;
            stps.forEach(stop -> {
                if (stop != null) {
                    if (stop.getStopPositionRM() != null)
                        rel.addMember(stop.getStopPositionRM());
                    if (stop.getPlatformRM() != null)
                        rel.addMember(stop.getPlatformRM());
                }
            });
        });

        wayMembers.forEach(rel::addMember);
    }

    private static String getStopName(OsmPrimitive p) {
        for (Relation ref : Utils.filteredCollection(p.getReferrers(), Relation.class)) {
            if (ref.hasTag("type", "public_transport")
                    && ref.hasTag("public_transport", "stop_area")
                    && ref.getName() != null) {
                return ref.getName();
            }
        }
        return p.getName();
    }

    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        setEnabled(false);
        if (selection == null || selection.size() != 1)
            return;
        OsmPrimitive selected = selection.iterator().next();
        if (selected.getType() == OsmPrimitiveType.RELATION &&
                RouteUtils.isPTRoute((Relation) selected)) {
            setEnabled(true);
        }
    }
}
