package org.openstreetmap.josm.plugins.remotecontrol;

import java.io.IOException;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

/**

 */
public class RemoteControlPlugin extends Plugin
{
    /** The HTTP server this plugin launches */
    static HttpServer server;

    /**
     * Creates the plugin, and starts the HTTP server
     */
    public RemoteControlPlugin()
    {
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
}
