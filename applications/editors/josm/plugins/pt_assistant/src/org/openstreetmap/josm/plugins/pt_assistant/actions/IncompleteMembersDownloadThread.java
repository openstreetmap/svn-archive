package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.util.ArrayList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.io.DownloadPrimitivesWithReferrersTask;

public class IncompleteMembersDownloadThread extends Thread {

	private Relation relation;

	public IncompleteMembersDownloadThread(Relation r) {
		super();
		relation = r;
	}

	@Override
	public void run() {

		 try {
		synchronized (this) {

			ArrayList<PrimitiveId> list = new ArrayList<>();
			for (RelationMember rm : relation.getMembers()) {
				list.add(rm);
			}

			DownloadPrimitivesWithReferrersTask task = new DownloadPrimitivesWithReferrersTask(false, list, false, true,
					null, null);
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
