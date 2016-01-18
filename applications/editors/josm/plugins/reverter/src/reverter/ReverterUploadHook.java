package reverter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.APIDataSet;

public class ReverterUploadHook implements UploadHook {
    String pluginString;
    public ReverterUploadHook(ReverterPlugin plugin) {
        pluginString = "reverter_plugin/" + plugin.getPluginInformation().version;
    }
    @Override
    public boolean checkUpload(APIDataSet apiDataSet) {
        if (!ReverterPlugin.reverterUsed) return true;
        boolean hasRevertions = false;
        for (Command cmd : Main.main.undoRedo.commands) {
            if (cmd instanceof RevertChangesetCommand) {
                hasRevertions = true;
                break;
            }
        }

        if (hasRevertions) {
            Main.main.getCurrentDataSet().addChangeSetTag("created_by", "reverter");
        }
        return true;
    }

}
