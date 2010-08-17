package org.openstreetmap.josm.plugins.remotecontrol.handler;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.net.URLDecoder;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
import org.openstreetmap.josm.plugins.remotecontrol.PermissionPref;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandler;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandlerBadRequestException;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandlerErrorException;

/**
 * Handler for import request
 */
public class ImportHandler extends RequestHandler {

	public static final String command = "/import";

	@Override
	protected void handleRequest() throws RequestHandlerErrorException {
        try {
            DownloadTask osmTask = new DownloadOsmTask();
            osmTask.loadUrl(false, URLDecoder.decode(args.get("url"), "UTF-8"), null);
        } catch (Exception ex) {
            System.out.println("RemoteControl: Error parsing import remote control request:");
            ex.printStackTrace();
            throw new RequestHandlerErrorException();
        }
	}

	@Override
	protected String[] getMandatoryParams()
	{
		return new String[] { "url" };
	}
	
	@Override
	public String getPermissionMessage() {
		return tr("Remote Control has been asked to import data from the following URL:") +
        "<br>" + request;
	}

	@Override
	public PermissionPref getPermissionPref()
	{
		return new PermissionPref("remotecontrol.permission.import",
				"RemoteControl: import forbidden by preferences");
	}
}
