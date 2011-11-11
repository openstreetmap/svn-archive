/*
 * Copyright 2009, Friedrich Maier
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

package org.openstreetmap.fma.jtiledownloader.config;

import java.util.Properties;

import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.datatypes.DownloadJob;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonGPX;

public class DownloadConfigurationGPX
    extends DownloadConfiguration
{

    private String _gpxFile = "";
    private int _corridor = 0;

    private static final String GPX_FILE = "GpxFile";
    private static final String CORRIDOR = "Corridor";

    public static final String ID = "GPX";

    @Override
    public void save(Properties prop)
    {
        setTemplateProperty(prop, TYPE, ID);

        setTemplateProperty(prop, GPX_FILE, _gpxFile);
        setTemplateProperty(prop, CORRIDOR, String.valueOf(_corridor));
    }

    @Override
    public void load(Properties prop)
    {
        _gpxFile = prop.getProperty(GPX_FILE, "");
        _corridor = Integer.parseInt(prop.getProperty(CORRIDOR, "0"));
    }

    /**
     * Getter for gpxFile
     * @return the gpxFile
     */
    public final String getGpxFile()
    {
        return _gpxFile;
    }

    /**
     * Setter for gpxFile
     * @param gpxFile the gpxFile to set
     */
    public final void setGpxFile(String gpxFile)
    {
        _gpxFile = gpxFile;
    }

    /**
     * Getter for corridor
     * @return the corridor
     */
    public final int getCorridor()
    {
        return _corridor;
    }

    /**
     * Setter for corridor
     * @param corridor the corridor to set
     */
    public final void setCorridor(int corridor)
    {
        _corridor = corridor;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration#getType()
     */
    @Override
    public String getType()
    {
        return ID;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration#getTileList(DownloadJob)
     */
    @Override
    public TileList getTileList(DownloadJob downloadJob)
    {
        TileListCommonGPX tileList = new TileListCommonGPX();

        tileList.setDownloadZoomLevels(Util.getOutputZoomLevelArray(downloadJob.getTileProvider(), downloadJob.getOutputZoomLevels()));

        String gpxFile = getGpxFile();
        int corridor = getCorridor();

        tileList.updateList(gpxFile, corridor);

        return tileList;
    }

}
