/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader;

import java.util.HashMap;

import java.util.logging.LogManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openstreetmap.fma.jtiledownloader.cmdline.JTileDownloaderCommandLine;
import org.openstreetmap.fma.jtiledownloader.views.main.JTileDownloaderMainView;

public class JTileDownloaderStart
{
    /**
     * 
     */
    private static final String CMDLINE_DL = "DL";

    public static void main(String[] args)
    {
        try {
            LogManager.getLogManager().readConfiguration(JTileDownloaderStart.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch( Exception e ) { /* oh well... */ }
        // get command line arguments
        HashMap<String, String> arguments = new HashMap<String, String>();
        if (args != null && args.length > 0)
        {
            for (String arg : args)
            {
                String[] parts = arg.split("=");
                if (parts != null && parts.length == 2)
                {
                    String key = parts[0].toUpperCase();
                    String value = parts[1];
                    arguments.put(key, value);
                }
            }
        }

        if (arguments.containsKey(CMDLINE_DL))
        {
            JTileDownloaderCommandLine tileDownloaderCommandLine = new JTileDownloaderCommandLine(arguments);
            tileDownloaderCommandLine.start();
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    catch (Exception e)
                    {
                    }
                    new JTileDownloaderMainView().setVisible(true);
                }
            });
        }

    }
}
