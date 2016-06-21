package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.io.DownloadPrimitivesWithReferrersTask;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class IncompleteMembersDownloadThread extends Thread {

	public IncompleteMembersDownloadThread() {
		super();

	}

	private void realRun() {
		// TODO
	}

	@Override
	public void run() {

		try {
			synchronized (this) {

				ArrayList<PrimitiveId> list = new ArrayList<>();

				// add all route relations that are of public_transport version
				// 2:
				Collection<Relation> allRelations = Main.getLayerManager().getEditDataSet().getRelations();
				for (Relation currentRelation : allRelations) {
					if (RouteUtils.isTwoDirectionRoute(currentRelation)) {
						list.add(currentRelation);
						for (RelationMember rm : currentRelation.getMembers()) {
							if (rm.hasRole("stop") || rm.hasRole("stop_entry_only") || rm.hasRole("stop_exit_only")
									|| rm.hasRole("platform") || rm.hasRole("platform_entry_only")
									|| rm.hasRole("platform_exit_only")) {
								List<OsmPrimitive> referrers = rm.getMember().getReferrers();
								for (OsmPrimitive referrer : referrers) {
									if (referrer.getType().equals(OsmPrimitiveType.RELATION)
											&& referrer.hasTag("public_transport", "stop_area")) {
										list.add(referrer);
										// TODO: this may never be executed
										// because the platform is an incomplete
										// member yet.
									}
								}
							}
						}
					}
				}

				// add all stop_positions:
				Collection<Node> allNodes = Main.getLayerManager().getEditDataSet().getNodes();
				for (Node currentNode : allNodes) {
					if (currentNode.hasTag("public_transport", "stop_position")) {
						List<OsmPrimitive> referrers = currentNode.getReferrers();
						boolean parentWayExists = false;
						for (OsmPrimitive referrer : referrers) {
							if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
								parentWayExists = true;
								break;
							}
						}
						if (!parentWayExists) {
							list.add(currentNode);

						}

					}
				}

				DownloadPrimitivesWithReferrersTask task = new DownloadPrimitivesWithReferrersTask(false, list, false,
						true, null, null);
				Thread t = new Thread(task);
				t.start();
				t.join();

			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}
}
