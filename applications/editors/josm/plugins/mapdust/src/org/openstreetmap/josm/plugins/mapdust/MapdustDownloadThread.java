/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust;


import java.awt.geom.Point2D;
import java.util.concurrent.TimeUnit;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;


/**
 * The MapDust download thread. Download the MapDust bugs in a given period of
 * time.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustDownloadThread extends Thread {

    /** The <code>MapdustDownloadThread</code> instance */
    private static MapdustDownloadThread instance;

    /** Specifies if the download was done or not */
    private boolean downloadDone = false;

    /** The interval */
    private final long INTERVAL = TimeUnit.MINUTES.toMillis(30);

    /** The <code>MapdustPlugin</code> object */
    private MapdustPlugin plugin;

    /** The <code>Point2D</code> object */
    private Point2D lastCenter;

    /**
     * Builds a <code>MapdustDownloadThread</code> object.
     */
    public MapdustDownloadThread() {
        setName("Downloading Mapdust data!");
        start();
    }

    /**
     *
     * @return A <code>MapdustDownloadThread</code> object
     */
    public static synchronized MapdustDownloadThread getInstance() {
        if (instance == null) {
            instance = new MapdustDownloadThread();
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // if the center of the map has changed, the user has dragged or
                // zoomed the map
                if (Main.map != null && Main.map.mapView != null) {
                    Point2D currentCenter = Main.map.mapView.getCenter();
                    if (currentCenter != null
                            && !currentCenter.equals(lastCenter)) {
                        downloadDone = false;
                        lastCenter = currentCenter;
                    }
                }
                String pluginState = Main.pref.get("mapdust.pluginState");
                boolean modify =
                        Boolean.parseBoolean(Main.pref.get("mapdust.modify"));
                if (!downloadDone) {
                    if (plugin != null
                            && pluginState.equals(MapdustPluginState.ONLINE
                                    .getValue()) && !modify) {
                        plugin.updateData();
                        downloadDone = true;
                    }
                }
                Thread.sleep(INTERVAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the <code>MapdustPlugin</code> object
     *
     * @return the plugin
     */
    public MapdustPlugin getPlugin() {
        return plugin;
    }

    /**
     * Sets the <code>MapdustPlugin</code> object
     *
     * @param plugin the plugin to set
     */
    public void setPlugin(MapdustPlugin plugin) {
        this.plugin = plugin;
    }

}
