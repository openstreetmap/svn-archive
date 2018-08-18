package org.openstreetmap.josm.plugins.importvec;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;

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
        if (MainApplication.getLayerManager().getEditLayer() == null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Please open or create data layer before importing"));
            return;
        }
        ImportDialog dlg = new ImportDialog();
        if (dlg.getValue() != 1) return;
        dlg.saveSettings();
        MainApplication.worker.submit(new SvgImportTask(files));
    }
}
