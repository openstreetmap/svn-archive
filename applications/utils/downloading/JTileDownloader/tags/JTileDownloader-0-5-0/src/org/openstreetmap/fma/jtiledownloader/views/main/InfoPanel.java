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

package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.TileProviderList;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;

public class InfoPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    private JTextArea _textInfo = new JTextArea();

    /**
     * 
     */
    public InfoPanel()
    {
        super();

        createPanel();
        initializePanel();

    }

    /**
     */
    private void createPanel()
    {

        JScrollPane scrollPane = new JScrollPane(_textInfo);

        //        GridBagConstraints constraints = new GridBagConstraints();
        //        //        constraints.gridwidth = GridBagConstraints.REMAINDER;
        //        constraints.fill = GridBagConstraints.BOTH;
        //        constraints.insets = new Insets(5, 5, 0, 5);
        //        constraints.weightx = 1;
        //        constraints.weighty = 1;

        //                setLayout(new GridBagLayout());
        setLayout(new BorderLayout());

        //        add(scrollPane, constraints);
        add(scrollPane, BorderLayout.CENTER);

    }

    /**
     * 
     */
    private void initializePanel()
    {
        _textInfo.setEditable(false);
        _textInfo.setLineWrap(true);
        _textInfo.setWrapStyleWord(true);
        _textInfo.setRows(10);

        _textInfo.setText("JTileDownloader Version " + Constants.VERSION + "\n");
        _textInfo.append("----------------------------------------\n");
        _textInfo.append("JTileDownloader  Copyright (C) 2008  Friedrich Maier\n");
        _textInfo.append("This program comes with ABSOLUTELY NO WARRANTY.\n");
        _textInfo.append("This is free software, and you are welcome to redistribute\n");
        _textInfo.append("it under certain conditions\n");
        _textInfo.append("See file COPYING.txt and README.txt for details.\n");
        _textInfo.append("GPLv3 see <http://www.gnu.org/licenses/>\n");
        _textInfo.append("----------------------------------------\n");
        _textInfo.append("Project Homepage:\n");
        _textInfo.append("http://wiki.openstreetmap.org/index.php/JTileDownloader\n");
        _textInfo.append("----------------------------------------\n");
        _textInfo.append("\n");
        _textInfo.append("Please always make sure that you agree and stick to the policies of the tile-providers before downloading!");
        _textInfo.append("\n");
        _textInfo.append("Downloaded data (tiles) based http://www.openstreetmap.org");
        _textInfo.append("see http://wiki.openstreetmap.org/index.php/OpenStreetMap_License\n");
        _textInfo.append("\n");
        _textInfo.append("Plase take also a look at the 'Tile usage policy'\nhttp://wiki.openstreetmap.org/index.php/Tile_usage_policy\n");
        _textInfo.append("\n");
        _textInfo.append("----------------------------------------\n");
        _textInfo.append("Predefined tile servers are:\n");
        TileProviderIf[] tileServerList = new TileProviderList().getTileProviderList();
        for (TileProviderIf element : tileServerList)
        {
            _textInfo.append(element.getName() + "\n");
            _textInfo.append(element.getTileServerUrl() + "\n");
        }

        _textInfo.append("----------------------------------------\n");
        _textInfo.append("Source code of this program is available at:\n");
        _textInfo.append("http://svn.openstreetmap.org/applications/utils/downloading/JTileDownloader/\n");

        _textInfo.setCaretPosition(0);

    }
}
