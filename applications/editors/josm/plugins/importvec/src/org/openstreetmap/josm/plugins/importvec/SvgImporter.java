package org.openstreetmap.josm.plugins.importvec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * 
 */
public class SvgImporter extends FileImporter {

    public SvgImporter() {
        super(new ExtensionFileFilter("svg", "svg",tr("SVG files [ImportVec plugin] (*.svg)")));
    }

    @Override
    public boolean isBatchImporter() {
        return true;
    }
    

    @Override
    public void importData(List<File> files, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
        if (!Main.main.hasEditLayer()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Please open or create data layer before importing"));
            return;
        }
        ImportDialog dlg = new ImportDialog();
        if (dlg.getValue() != 1) return;
        dlg.saveSettings();
        Main.worker.submit(new SvgImportTask(files));
    }
}
