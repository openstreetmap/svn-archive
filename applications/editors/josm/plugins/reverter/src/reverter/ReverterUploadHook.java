// License: GPL. For details, see LICENSE file.
package reverter;

import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Upload hook to add tag to new changeset.
 *
 */
public class ReverterUploadHook implements UploadHook {
    final String pluginString;

    /**
     * Create new {@link ReverterUploadHook}
     * @param info plugin information
     */
    public ReverterUploadHook(PluginInformation info) {
        pluginString = "reverter_plugin/" + info.version;
    }

    @Override
    public boolean checkUpload(APIDataSet apiDataSet) {
        if (ReverterPlugin.reverterUsed) {
            for (Command cmd : UndoRedoHandler.getInstance().getUndoCommands()) {
                if (isReverterCmd(cmd)) {
                    MainApplication.getLayerManager().getEditDataSet().addChangeSetTag("created_by", pluginString);
                    break;
                }
            }
        }
        return true;
    }

    private static boolean isReverterCmd(Command cmd) {
        if (cmd instanceof RevertChangesetCommand)
            return true;
        if (cmd instanceof SequenceCommand) {
            return ((SequenceCommand) cmd).getLastCommand() instanceof RevertChangesetCommand;
        }
        return false;
    }
}
