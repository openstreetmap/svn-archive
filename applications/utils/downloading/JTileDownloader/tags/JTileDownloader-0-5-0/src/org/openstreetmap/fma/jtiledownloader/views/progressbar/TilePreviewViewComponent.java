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

package org.openstreetmap.fma.jtiledownloader.views.progressbar;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JComponent;

public class TilePreviewViewComponent
    extends JComponent
{
    private static final long serialVersionUID = 1L;
    private Image image;

    /**
     * 
     */
    public TilePreviewViewComponent()
    {
        super();
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (image != null)
        {
            g.drawImage(image, 0, 0, this);
        }
    }

    public void setImage(String filePathName)
    {
        image = Toolkit.getDefaultToolkit().createImage(filePathName);
        if (image != null)
        {
            repaint();
        }
    }

    /**
     * @see javax.swing.JComponent#update(java.awt.Graphics)
     */
    @Override
    public void update(Graphics g)
    {
        paint(g);
    }

}
