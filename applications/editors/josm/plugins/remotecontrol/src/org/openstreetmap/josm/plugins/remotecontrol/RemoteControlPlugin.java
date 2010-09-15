package org.openstreetmap.josm.plugins.remotecontrol;

import java.io.IOException;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Base plugin for remote control operations.
 * This plugin contains operations that use JOSM core only.
 *
 * Other plugins can register additional operations by calling
 * @see addRequestHandler().
 * To allow API changes this plugin contains a @see getVersion() method.
 *
 * IMPORTANT! increment the minor version on compatible API extensions
 * and increment the major version and set minor to 0 on incompatible changes.
 */
public class RemoteControlPlugin extends Plugin
{
    /** API version
     * IMPORTANT! update the version number on API changes.
     */
    static final int apiMajorVersion = 1;
    static final int apiMinorVersion = 0;

    /**
     * RemoteControl HTTP protocol version. Change minor number for compatible
     * interface extensions. Change major number in case of incompatible
     * changes.
     */
    static final int protocolMajorVersion = 1;
    static final int protocolMinorVersion = 2;

    /** The HTTP server this plugin launches */
    static HttpServer server;

    /**
     * Returns an array of int values with major and minor API version
     * and major and minor HTTP protocol version.
     *
     * The function returns an int[4] instead of an object with fields
     * to avoid ClassNotFound errors with old versions of remotecontrol.
     *
     * @return array of integer version numbers:
     *    apiMajorVersion, apiMinorVersion, protocolMajorVersion, protocolMajorVersion
     */
    public int[] getVersion()
    {
        int versions[] = {apiMajorVersion, apiMinorVersion, protocolMajorVersion, protocolMajorVersion};
        return versions;
    }

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
