package org.openstreetmap.fma.jtiledownloader;

import java.util.HashMap;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openstreetmap.fma.jtiledownloader.cmdline.JTileDownloaderCommandLine;
import org.openstreetmap.fma.jtiledownloader.views.main.JTileDownloaderMainView;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class JTileDownloaderStart
{
    /**
     * 
     */
    private static final String CMDLINE_DL = "DL";

    public static void main(String[] args)
    {

        // get command line arguments
        HashMap arguments = new HashMap();
        if (args != null && args.length > 0)
        {
            for (int index = 0; index < args.length; index++)
            {
                String[] parts = args[index].split("=");
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
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    catch (Exception e)
                    {
                    }
                    new JTileDownloaderMainView();
                }
            });
        }

    }
}
