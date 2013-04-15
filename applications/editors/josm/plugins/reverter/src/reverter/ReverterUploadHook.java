package reverter;

import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.gui.io.UploadDialog;

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

        UploadDialog ud = UploadDialog.getUploadDialog();
        Map<String, String> tags = ud.getDefaultChangesetTags();
        String created_by = tags.get("created_by");
        if (created_by == null || "".equals(created_by)) {
            if (hasRevertions) {
                tags.put("created_by", Version.getInstance().getAgentString() + ";" + pluginString);
                ud.setDefaultChangesetTags(tags);
            }
            return true;
        }
        if (hasRevertions) {
            if (!created_by.contains(pluginString)) {
                tags.put("created_by", created_by + ";" + pluginString);
            }
        } else {
            if (created_by.contains(";" + pluginString)) {
                tags.put("created_by", created_by.replace(";" + pluginString, ""));
            }
        }
        ud.setDefaultChangesetTags(tags);
        return true;
    }

}
