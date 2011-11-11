/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.logging.Logger;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileComparatorFactory;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;

public class TileListExporter
{
    private static final Logger log = Logger.getLogger(TileListExporter.class.getName());
    private ArrayList<Tile> _tilesToDownload;
    private final String _downloadPathBase;
    private TileProviderIf _tileProvider;

    /**
     * @param downloadPathBase 
     * @param tilesToDownload
     * @param tileProvider 
     */
    public TileListExporter(String downloadPathBase, ArrayList<Tile> tilesToDownload, TileProviderIf tileProvider)
    {
        super();
        _downloadPathBase = downloadPathBase;
        _tilesToDownload = tilesToDownload;
        _tileProvider = tileProvider;
    }

    public void doExport()
    {
        int tileSortingPolicy = AppConfiguration.getInstance().getTileSortingPolicy();
        if (tileSortingPolicy > 0) {
            Collections.sort(_tilesToDownload, TileComparatorFactory.getComparator(tileSortingPolicy));
        }

        String exportFile = _downloadPathBase + File.separator + "export.txt";

        //check directories

        File testDir = new File(_downloadPathBase);
        if (!testDir.exists())
        {
            log.info("Creating directory " + testDir.getPath());
            testDir.mkdirs();
        }

        // check if export file exists
        File exportFileTest = new File(exportFile);
        if (exportFileTest.exists())
        {
            exportFileTest.delete();
        }

        BufferedWriter fileWriter;
        try
        {
            fileWriter = new BufferedWriter(new FileWriter(exportFile));

            int count = 0;
            for (Tile tileToDownload : _tilesToDownload)
            {
                doSingleExport(tileToDownload, fileWriter);
                count++;
            }

            fileWriter.close();
        }
        catch (IOException e)
        {
            log.warning("Failed to save tile: " + e.getLocalizedMessage());
        }
    }

    private void doSingleExport(Tile tileToDownload, BufferedWriter fileWriter) throws IOException
    {
        fileWriter.write(_tileProvider.getTileUrl(tileToDownload));
        fileWriter.newLine();
        log.fine("added url " + tileToDownload);
    }

}
