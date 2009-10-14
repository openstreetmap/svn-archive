/* Copyright (c) 2008, Henrik Niehaus
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
package org.openstreetmap.josm.plugins.osb;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.josm.Main;

public class OsbDownloadLoop extends Thread {

    private static OsbDownloadLoop instance;

    private long countdown = TimeUnit.SECONDS.toMillis(1);

    private boolean downloadDone = false;

    private final int INTERVAL = 100;

    private OsbPlugin plugin;

    private Point2D lastCenter;

    public OsbDownloadLoop() {
        setName(tr("OpenStreetBugs download loop"));
        start();
    }

    public static synchronized OsbDownloadLoop getInstance() {
        if(instance == null) {
            instance = new OsbDownloadLoop();
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            while(true) {
                countdown -= INTERVAL;

                // if the center of the map has changed, the user has dragged or
                // zoomed the map
                if(Main.map != null && Main.map.mapView != null) {
                    Point2D currentCenter = Main.map.mapView.getCenter();
                    if(currentCenter != null && !currentCenter.equals(lastCenter)) {
                        resetCountdown();
                        lastCenter = currentCenter;
                    }
                }

                // auto download if configured
                if( Main.pref.getBoolean(ConfigKeys.OSB_AUTO_DOWNLOAD) && 
                        plugin != null && plugin.getDialog() != null && plugin.getDialog().isDialogShowing() ) {
                    if(countdown < 0) {
                        if(!downloadDone) {
                            if(plugin != null) {
                                plugin.updateData();
                                downloadDone = true;
                            }
                        } else {
                            countdown = -1;
                        }
                    }
                }

                Thread.sleep(INTERVAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resetCountdown() {
        downloadDone = false;
        countdown = TimeUnit.SECONDS.toMillis(1);
    }

    public void setPlugin(OsbPlugin plugin) {
        this.plugin = plugin;
    }
}
