package org.openstreetmap.josm.plugins.trustosm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.plugins.trustosm.actions.GetMissingDataAction;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;

public class SigImporter extends FileImporter {

	public SigImporter() {
		super(new ExtensionFileFilter("txml,xml", "txml", tr("OSM Signature Files") + " (*.txml *.xml)"));
	}

	public SigImporter(ExtensionFileFilter filter) {
		super(filter);
	}

	@Override public void importData(File file, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
		try {
			FileInputStream in = new FileInputStream(file);
			importData(in, file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IOException(tr("File ''{0}'' does not exist.", file.getName()));
		}
	}

	protected void importData(InputStream in, File associatedFile) throws IllegalDataException {
		if (!Main.main.hasEditLayer()) {
			DataSet dataSet = new DataSet();
			final OsmDataLayer layer = new OsmDataLayer(dataSet, associatedFile.getName(), associatedFile);
			Main.main.addLayer(layer);
		}
		//		Set<OsmPrimitive> missingData = new HashSet<OsmPrimitive>();
		Map<String,TrustOsmPrimitive> trustitems = SigReader.parseSignatureXML(in, NullProgressMonitor.INSTANCE);

		/*
		int missingCount = missingData.size();
		int itemCount = trustitems.size();
		if (missingCount == 0) {
			JOptionPane.showMessageDialog(Main.parent, tr("{0} Signatures loaded. All referenced OSM objects found.",itemCount));
		} else {
			int n = JOptionPane.showOptionDialog(Main.parent, tr("{0} of {1} OSM objects are referenced but not there.\nDo you want to load them from OSM-Server?",missingCount,itemCount), tr("Load objects from server"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (n == JOptionPane.YES_OPTION) {
				Main.worker.submit(new DownloadSignedOsmDataTask(missingData, Main.main.getEditLayer()));
			}
		}
		 */
		TrustOSMplugin.signedItems.putAll(trustitems);
		new GetMissingDataAction().downloadMissing();
		//TrustOSMplugin.signedItems.putAll(TrustStoreHandler.loadSigsFromFile(in));

	}
}