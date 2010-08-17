package org.openstreetmap.josm.plugins.remotecontrol;

import java.io.IOException;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**

 */
public class RemoteControlPlugin extends Plugin
{
    /** The HTTP server this plugin launches */
    static HttpServer server;

    /**
     * Creates the plugin, and starts the HTTP server
     */
    public RemoteControlPlugin(PluginInformation info)
    {
    	super(info);
    	/*
		System.out.println("constructor " + this.getClass().getName() + " (" + info.name +
				" v " + info.version + " stage " + info.stage + ")");
		*/
        restartServer();
    }

    @Override
    public PreferenceSetting getPreferenceSetting()
    {
        return new RemoteControlPreferences();
    }

    /**
     * Starts or restarts the HTTP server
     *
     */
    public void restartServer()
    {
        try
        {
            if (server != null)
                server.stopServer();

            int port = HttpServer.DEFAULT_PORT;
            server = new HttpServer(port);
            server.start();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Add external external request handler.
     * Can be used by other plug-ins that want to use remote control.
     *
     * @param handler The additional request handler.
     */
    public void addRequestHandler(String command, Class<? extends RequestHandler> handlerClass)
    {
        RequestProcessor.addRequestHandlerClass(command, handlerClass);
    }
    
}
