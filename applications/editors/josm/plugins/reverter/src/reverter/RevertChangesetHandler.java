package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.remotecontrol.PermissionPrefWithDefault;
import org.openstreetmap.josm.io.remotecontrol.handler.RequestHandler;

public class RevertChangesetHandler extends RequestHandler {
    public static final String command = "revert_changeset";
    public static final String permissionKey = "remotecontrol.permission.revert_changeset";
    public static final boolean permissionDefault = true;
    
    private int changesetId;

    @Override
    protected void handleRequest() throws RequestHandlerErrorException,
            RequestHandlerBadRequestException {
        try
        {
            Main.worker.submit(new RevertChangesetTask(changesetId, ChangesetReverter.RevertType.FULL, true));
        } catch (Exception ex) {
            System.out.println("RemoteControl: Error parsing revert_changeset remote control request:");
            ex.printStackTrace();
            throw new RequestHandlerErrorException();
        }

    }

    @Override
    public String[] getMandatoryParams() {
        return new String[] {"id"};
    }

    @Override
    public PermissionPrefWithDefault getPermissionPref() {
        return null;
    }

    @Override
    public String getPermissionMessage() {
        return tr("Remote Control has been asked to revert a changeset.");
    }

    @Override
    protected void validateRequest() throws RequestHandlerBadRequestException {
        try {
            changesetId = Integer.parseInt(args.get("id"));
        } catch (NumberFormatException e) {
            throw new RequestHandlerBadRequestException("NumberFormatException: "+e.getMessage());
        }
    }
}
