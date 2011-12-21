package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.remotecontrol.handler.RequestHandler;

public class RevertChangesetHandler extends RequestHandler {
    public static final String command = "revert_changeset";
    public static final String permissionKey = "remotecontrol.permission.revert_changeset";
    public static final boolean permissionDefault = true;

    @Override
    protected void handleRequest() throws RequestHandlerErrorException,
            RequestHandlerBadRequestException {
        try
        {
            int changesetId = Integer.parseInt(args.get("id"));
            Main.worker.submit(new RevertChangesetTask(changesetId, ChangesetReverter.RevertType.FULL, true));
        } catch (Exception ex) {
            System.out.println("RemoteControl: Error parsing revert_changeset remote control request:");
            ex.printStackTrace();
            throw new RequestHandlerErrorException();
        }

    }

    @Override
    public String getPermissionMessage() {
        return tr("Remote Control has been asked to revert a changeset.");
    }
}
