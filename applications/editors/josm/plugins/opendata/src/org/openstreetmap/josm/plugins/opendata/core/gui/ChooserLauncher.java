// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.gui;

import java.awt.Component;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;

public final class ChooserLauncher implements Runnable {

    private Projection proj = null;
    private final ProgressMonitor progressMonitor;

    private ChooserLauncher(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    @Override
    public void run() {
        Component parent = progressMonitor == null ? Main.parent : progressMonitor.getWindowParent();
        ProjectionChooser dialog = (ProjectionChooser) new ProjectionChooser(parent).showDialog();
        if (dialog.getValue() == 1) {
            proj = dialog.getProjection();
        }
    }
    
    public static Projection askForProjection(ProgressMonitor pm) {
        ChooserLauncher launcher = new ChooserLauncher(pm);
        GuiHelper.runInEDTAndWait(launcher);
        if (launcher.proj == null) {
            return null; // User clicked Cancel
        }
        return launcher.proj;
    }
}
