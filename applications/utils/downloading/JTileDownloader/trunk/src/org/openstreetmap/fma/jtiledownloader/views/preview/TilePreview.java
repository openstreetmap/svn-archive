package org.openstreetmap.fma.jtiledownloader.views.preview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

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
public class TilePreview
    extends JFrame
{
    private static final long serialVersionUID = 1L;

    private ViewComponent _component = new ViewComponent();

    public TilePreview()
    {
        super("TilePreview");

        addWindowListener(new TilePreviewWindowListener());

        Dimension size = new Dimension(256, 256);
        _component.setPreferredSize(size);
        getContentPane().add(_component);
        pack();
        setVisible(true);
    }

    /**
     * 
     */
    public void showImage(String filePathName)
    {
        _component.setImage(filePathName);
    }

    /**
     * @see javax.swing.JFrame#update(java.awt.Graphics)
     * {@inheritDoc}
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    class TilePreviewWindowListener
        extends WindowAdapter
    {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         * {@inheritDoc}
         */
        public void windowClosing(WindowEvent e)
        {
            System.out.println("WindowEvent windowClosing (TilePreview)");
            Window window = e.getWindow();
            window.dispose();
        }
    }

}
