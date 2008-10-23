package org.openstreetmap.fma.jtiledownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */
public class TileListExporter
{
    private Vector _tilesToDownload;
    private final String _downloadPathBase;

    /**
     * @param tilesToDownload
     */
    public TileListExporter(String downloadPathBase, Vector tilesToDownload)
    {
        super();
        _downloadPathBase = downloadPathBase;
        _tilesToDownload = tilesToDownload;
    }

    public void doExport()
    {

        String exportFile = _downloadPathBase + File.separator + "export.txt";

        //check directories

        File testDir = new File(_downloadPathBase);
        if (!testDir.exists())
        {
            log("directory " + testDir.getPath() + " does not exist, so create it");
            testDir.mkdirs();
        }

        // check if export file exists
        File exportFileTest = new File(exportFile);
        if (exportFileTest.exists())
        {
            exportFileTest.delete();
        }

        int count = 0;
        for (Enumeration enumeration = _tilesToDownload.elements(); enumeration.hasMoreElements();)
        {
            String tileToDownload = (String) enumeration.nextElement();
            doSingleExport(tileToDownload, exportFile);
            count++;
        }

    }

    private boolean doSingleExport(String tileToDownload, String exportFile)
    {
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(exportFile, true);
            for (int i = 0; i < tileToDownload.length(); i++)
            {
                outputStream.write((byte) tileToDownload.charAt(i));
            }
            outputStream.write((byte) "\r".charAt(0));
            outputStream.write((byte) "\n".charAt(0));
            outputStream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        System.out.println("added url " + tileToDownload + " to export file " + exportFile);

        return true;
    }

    /**
     * method to write to System.out
     * 
     * @param msg message to log
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }

}
