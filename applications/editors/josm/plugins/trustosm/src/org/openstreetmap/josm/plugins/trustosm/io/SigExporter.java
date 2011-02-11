package org.openstreetmap.josm.plugins.trustosm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.FileExporter;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class SigExporter extends FileExporter {

	public SigExporter(ExtensionFileFilter filter) {
		super(filter);
		// TODO Auto-generated constructor stub
	}

	public SigExporter() {
		super(new ExtensionFileFilter("txml,xml", "txml", tr("Signature Files") + " (*.txml *.xml)"));
	}

	@Override
	public void exportData(File file, Layer layer) throws IOException {
		CheckParameterUtil.ensureParameterNotNull(file, "file");

		String fn = file.getPath();
		if (fn.indexOf('.') == -1) {
			fn += ".tosm";
			file = new File(fn);
		}
		try {
			FileOutputStream fo = new FileOutputStream(file);
			new SigWriter(fo).write(TrustOSMplugin.signedItems.values());
			fo.flush();
			fo.close();
		} catch (IOException x) {
			x.printStackTrace();
			JOptionPane.showMessageDialog(Main.parent, tr("Error while exporting {0}:\n{1}", fn, x.getMessage()),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

}
