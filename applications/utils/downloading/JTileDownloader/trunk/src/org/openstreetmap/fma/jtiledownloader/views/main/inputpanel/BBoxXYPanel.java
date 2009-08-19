/*
 * Copyright 2008, Friedrich Maier
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

package org.openstreetmap.fma.jtiledownloader.views.main.inputpanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationBBoxXY;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonBBox;
import org.openstreetmap.fma.jtiledownloader.views.main.MainPanel;

/**
 * 
 */
public class BBoxXYPanel
    extends InputPanel
{
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT_MINX = "MIN_X";
    private static final String COMPONENT_MINY = "MIN_Y";
    private static final String COMPONENT_MAXX = "MAX_X";
    private static final String COMPONENT_MAXY = "MAX_Y";

    private TileListCommonBBox _tileList = new TileListCommonBBox();

    private JLabel _labelMinX = new JLabel("Min. X:");
    private JTextField _textMinX = new JTextField();
    private JLabel _labelMinY = new JLabel("Min. Y:");
    private JTextField _textMinY = new JTextField();
    private JLabel _labelMaxX = new JLabel("Max. X:");
    private JTextField _textMaxX = new JTextField();
    private JLabel _labelMaxY = new JLabel("Max. Y:");
    private JTextField _textMaxY = new JTextField();

    private DownloadConfigurationBBoxXY _downloadConfig;

    /**
     * 
     */
    public BBoxXYPanel(MainPanel mainPanel)
    {
        super(mainPanel);

        createPanel();
        initializePanel();
    }

    /**
     * 
     */
    @Override
    public void loadConfig()
    {
        _downloadConfig = new DownloadConfigurationBBoxXY();
        _downloadConfig.loadFromFile();

        _textMinX.setText("" + _downloadConfig.getMinX());
        _textMinY.setText("" + _downloadConfig.getMinY());
        _textMaxX.setText("" + _downloadConfig.getMaxX());
        _textMaxY.setText("" + _downloadConfig.getMaxY());

        setCommonValues(_downloadConfig);
    }

    /**
     * 
     */
    private void initializePanel()
    {
        _textMinX.setName(COMPONENT_MINX);
        _textMinX.addFocusListener(new MyFocusListener());

        _textMinY.setName(COMPONENT_MINY);
        _textMinY.addFocusListener(new MyFocusListener());

        _textMaxX.setName(COMPONENT_MAXX);
        _textMaxX.addFocusListener(new MyFocusListener());

        _textMaxY.setName(COMPONENT_MAXY);
        _textMaxY.addFocusListener(new MyFocusListener());
    }

    /**
     * 
     */
    private void createPanel()
    {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        //        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMinX, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMinX, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMaxX, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMaxX, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMinY, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMinY, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMaxY, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMaxY, constraints);

    }

    /**
     * 
     */
    private void updateTileList()
    {
        _tileList.setDownloadZoomLevels(getDownloadZoomLevel());

        _tileList.initXTopLeft(getMinX());
        _tileList.initYTopLeft(getMinY());
        _tileList.initXBottomRight(getMaxX());
        _tileList.initYBottomRight(getMaxY());

        updateNumberOfTiles();
    }

    @Override
    public void saveConfig()
    {
        if (_downloadConfig == null)
        {
            return;
        }

        _downloadConfig.setMinX(getMinX());
        _downloadConfig.setMinY(getMinY());
        _downloadConfig.setMaxX(getMaxX());
        _downloadConfig.setMaxY(getMaxY());
        super.saveCommonConfig(_downloadConfig);
        _downloadConfig.saveToFile();
    }

    /**
     * @return min X
     */
    public int getMinX()
    {
        String str = _textMinX.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * @return min Y
     */
    public int getMinY()
    {
        String str = _textMinY.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * @return max X
     */
    public int getMaxX()
    {
        String str = _textMaxX.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * @return max Y
     */
    public int getMaxY()
    {
        String str = _textMaxY.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * @return number of tiles
     */
    @Override
    public int getNumberOfTilesToDownload()
    {
        return _tileList.getTileCount();
    }

    class MyFocusListener
        implements FocusListener
    {

        /**
         * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
         * {@inheritDoc}
         */
        public void focusGained(FocusEvent focusevent)
        {

        }

        /**
         * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
         * {@inheritDoc}
         */
        public void focusLost(FocusEvent focusevent)
        {
            String componentName = focusevent.getComponent().getName();
            System.out.println("focusLost: " + componentName);

            if (componentName.equalsIgnoreCase(COMPONENT_MINX))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MINY))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MAXX))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MAXY))
            {
                updateAll();
            }
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#updateAll()
     * {@inheritDoc}
     */
    @Override
    public void updateAll()
    {
        updateTileList();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getTileList()
     * {@inheritDoc}
     */
    @Override
    public TileList getTileList()
    {
        return _tileList;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getInputName()
     * {@inheritDoc}
     */
    @Override
    public String getInputName()
    {
        return "Bounding Box (X/Y)";
    }
}
