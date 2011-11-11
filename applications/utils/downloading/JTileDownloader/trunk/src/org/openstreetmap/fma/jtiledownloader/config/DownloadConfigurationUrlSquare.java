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

package org.openstreetmap.fma.jtiledownloader.config;

import java.util.Properties;

import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.datatypes.DownloadJob;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListUrlSquare;

public class DownloadConfigurationUrlSquare
    extends DownloadConfiguration
{

    private String _pasteUrl = "";
    private int _radius = 5;

    private static final String PASTE_URL = "PasteUrl";
    private static final String RADIUS = "Radius";

    public static final String ID = "UrlSquare";

    @Override
    public void save(Properties prop)
    {
        setTemplateProperty(prop, TYPE, ID);

        setTemplateProperty(prop, PASTE_URL, _pasteUrl);
        setTemplateProperty(prop, RADIUS, String.valueOf(_radius));
    }

    @Override
    public void load(Properties prop)
    {
        _pasteUrl = prop.getProperty(PASTE_URL, "");
        _radius = Integer.parseInt(prop.getProperty(RADIUS, "5"));
    }

    /**
     * Getter for pasteUrl
     * @return the pasteUrl
     */
    public final String getPasteUrl()
    {
        return _pasteUrl;
    }

    /**
     * Setter for pasteUrl
     * @param pasteUrl the pasteUrl to set
     */
    public final void setPasteUrl(String pasteUrl)
    {
        _pasteUrl = pasteUrl;
    }

    /**
     * Getter for radius
     * @return the radius
     */
    public final int getRadius()
    {
        return _radius;
    }

    /**
     * Setter for radius
     * @param radius the radius to set
     */
    public final void setRadius(int radius)
    {
        _radius = radius;
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
        TileListUrlSquare tileList = new TileListUrlSquare();

        String url = getPasteUrl();
        if (url == null || url.length() == 0)
        {
            throw new RuntimeException("invalid URL");
        }

        tileList.setDownloadZoomLevels(Util.getOutputZoomLevelArray(downloadJob.getTileProvider(), downloadJob.getOutputZoomLevels()));

        Util.parsePasteUrl(url, tileList);
        tileList.setRadius(getRadius() * 1000);

        tileList.calculateTileValuesXY();

        return tileList;
    }
}
