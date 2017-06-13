package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AlignInCircleAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/*
 */
public class SplitRoundaboutAction extends JosmAction {

	private static final String actionName = "Split Roundabout";

	private static final long serialVersionUID = 8912249304286025356L;

	public SplitRoundaboutAction() {
		super(actionName, "icons/splitroundabout", actionName, null, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Way roundabout = (Way) getLayerManager().getEditDataSet().getSelected().iterator().next();

		//download the bbox around the roundabout
		DownloadOsmTask task = new DownloadOsmTask();
        task.setZoomAfterDownload(true);
        BBox rbbox = roundabout.getBBox();
        double latOffset = (rbbox.getTopLeftLat() - rbbox.getBottomRightLat()) / 10;
        double lonOffset = (rbbox.getBottomRightLon() - rbbox.getTopLeftLon()) / 10;
        Bounds area = new Bounds(
        		rbbox.getBottomRightLat() - latOffset,
        		rbbox.getTopLeftLon() - lonOffset,
        		rbbox.getTopLeftLat() + latOffset,
        		rbbox.getBottomRightLon() + lonOffset);
        Future<?> future = task.download(false, area, null);
        Main.worker.submit(() -> {
        	try {
				future.get();
				continueAfterDownload(roundabout);
			} catch (InterruptedException | ExecutionException e1) {
			 	Main.error(e1);
				return;
			}
        });
	}

	private void continueAfterDownload(Way roundabout)
	{
		//make the roundabout round, if requested
		int result = JOptionPane.showOptionDialog(Main.parent,
				tr("Do you want to make the roundabout round?"), tr("Roundabout round"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);

		if(result == JOptionPane.YES_OPTION) {
			new AlignInCircleAction().actionPerformed(null);
		}

		//save the position of the roundabout inside each relation
		Map<Relation, Integer> savedPositions = new HashMap<>();
		List <OsmPrimitive> referrers = roundabout.getReferrers();
		referrers.removeIf(r -> r.getType() != OsmPrimitiveType.RELATION
				|| !RouteUtils.isTwoDirectionRoute((Relation) r));
		for(OsmPrimitive currPrim : referrers) {
			Relation curr = (Relation) currPrim;
			for(int j = 0; j < curr.getMembersCount(); j++) {
				if(curr.getMember(j).getUniqueId() == roundabout.getUniqueId()) {
					savedPositions.put(curr, j);
					curr.removeMember(j);
					break;
				}
			}
		}

        //split the roundabout
		List<Node> splitNodes = roundabout.getNodes();
		splitNodes.removeIf(n -> n.getParentWays().size() != 2);
		getLayerManager().getEditDataSet().setSelected(splitNodes);
		new SplitWayAction().actionPerformed(null);
		Collection<OsmPrimitive> splittedWays = getLayerManager().getEditDataSet().getSelected();

        //update the relations
		savedPositions.forEach((r, i) -> {
			Way previous = r.getMember(i-1).getWay();
			Way next = r.getMember(i).getWay();
			if(splitNodes.contains(previous.lastNode()) && splitNodes.contains(next.firstNode())) {
				//lucky case: the ways were in order and the whole left by
				//removing the roundabout can be easily filled with the splitted
				//segment(s) that has as the first and last node the two just checked

				List<Way> parents = previous.lastNode().getParentWays();
				parents.removeIf(w -> !w.firstNode().equals(previous.lastNode()));

				Way curr = parents.get(0);
				int j = 0;

				while(!curr.lastNode().equals(next.firstNode())) {
					r.addMember(i + j++, new RelationMember(null, curr));
					parents = curr.lastNode().getParentWays();
					parents.remove(curr);
					parents.removeIf(w -> !splittedWays.contains(w));
					curr = parents.get(0);
				}
				r.addMember(i + j++, new RelationMember(null, curr));
			}
		});
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
        setEnabled(false);
		if (selection == null || selection.size() != 1)
            return;
		OsmPrimitive selected = selection.iterator().next();
	    if(selected.getType() != OsmPrimitiveType.WAY)
        	return;
        if(((Way)selected).isClosed() && selected.hasTag("junction", "roundabout")) {
        	setEnabled(true);
        	return;
        }
	}
}
