// License: GPL. For details, see LICENSE file.
package reverter;

import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.gui.MainApplication;

public class ReverterUploadHook implements UploadHook {
    String pluginString;
    public ReverterUploadHook(ReverterPlugin plugin) {
        pluginString = "reverter_plugin/" + plugin.getPluginInformation().version;
    }

    @Override
    public boolean checkUpload(APIDataSet apiDataSet) {
        if (!ReverterPlugin.reverterUsed) return true;
        boolean hasRevertions = false;
        for (Command cmd : UndoRedoHandler.getInstance().commands) {
            if (cmd instanceof RevertChangesetCommand) {
                hasRevertions = true;
                break;
            }
        }

        if (hasRevertions) {
            MainApplication.getLayerManager().getEditDataSet().addChangeSetTag("created_by", "reverter");
        }
        return true;
    }

}
