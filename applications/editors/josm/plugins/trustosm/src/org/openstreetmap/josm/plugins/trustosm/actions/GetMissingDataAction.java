package org.openstreetmap.josm.plugins.trustosm.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.gui.DownloadSignedOsmDataTask;
import org.openstreetmap.josm.tools.Shortcut;

public class GetMissingDataAction extends JosmAction {

	public GetMissingDataAction() {
		super(tr("Download OSM"),"getmissing",tr("Get all referenced but not actually present OSM objects from OSM server."),
				Shortcut.registerShortcut("gpg:download", tr("Download referenced osm objects..."), KeyEvent.VK_T, Shortcut.CTRL),true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (!isEnabled())
			return;
		downloadMissing();
	}

	public boolean downloadMissing() {
		Collection<OsmPrimitive> missingData = new HashSet<OsmPrimitive>();
		Map<String,TrustOsmPrimitive> trustitems = TrustOSMplugin.signedItems;
		getMissing(trustitems, missingData);

		int missingCount = missingData.size();
		int itemCount = trustitems.size();
		if (missingCount == 0) {
			JOptionPane.showMessageDialog(Main.parent, tr("{0} Signatures loaded. All referenced OSM objects found.",itemCount));
		} else {
			int n = JOptionPane.showOptionDialog(Main.parent, tr("{0} of {1} OSM objects are referenced but not there.\nDo you want to load them from OSM-Server?",missingCount,itemCount), tr("Load objects from server"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (n == JOptionPane.YES_OPTION) {
				Main.worker.submit(new DownloadSignedOsmDataTask(missingData, Main.main.getEditLayer()));
				return true;
			}
		}

		return false;
	}

	public void getMissing(Map<String,TrustOsmPrimitive> trustitems, Collection<OsmPrimitive> missingData) {
		Collection<OsmPrimitive> presentData = Main.main.getCurrentDataSet().allPrimitives();
		for (TrustOsmPrimitive t : trustitems.values()) {
			OsmPrimitive osm = t.getOsmPrimitive();
			if (!presentData.contains(osm))
				missingData.add(osm);
		}
	}


}
