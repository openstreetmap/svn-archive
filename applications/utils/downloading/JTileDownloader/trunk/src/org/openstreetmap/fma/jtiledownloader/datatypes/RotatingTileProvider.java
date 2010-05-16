/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 *
 * Based on:
 * OsmTileSource.java from JMapViewer.
 *
 * This file is part of JTileDownloader.
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

package org.openstreetmap.fma.jtiledownloader.datatypes;

import java.text.MessageFormat;

/**
 * Rotating TileProvider
 */
public abstract class RotatingTileProvider
    extends GenericTileProvider
{

    private int serverNumber = -1;

    protected abstract String[] getSubDomains();

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileServerUrl()
     */
    @Override
    public String getTileServerUrl()
    {
        serverNumber = (serverNumber + 1) % getSubDomains().length;
        return MessageFormat.format(url, new Object[] { getSubDomains()[serverNumber] });
    }
}
