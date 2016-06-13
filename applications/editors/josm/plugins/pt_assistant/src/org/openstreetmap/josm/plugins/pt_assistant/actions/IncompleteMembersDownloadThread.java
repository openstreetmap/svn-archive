package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.util.ArrayList;

import org.openstreetmap.josm.actions.DownloadPrimitiveAction;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;

public class IncompleteMembersDownloadThread extends Thread {

	private Relation relation;

	public IncompleteMembersDownloadThread(Relation r) {
		super();
		relation = r;
	}

	@Override
	public void run() {

			synchronized (this) {

				ArrayList<PrimitiveId> list = new ArrayList<>();
				list.add(relation);
				DownloadPrimitiveAction.processItems(false, list, false, true);

			
				notify();

			}

	}
}
