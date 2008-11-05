package org.openstreetmap.fma.jtiledownloader.views.main.inputpanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.views.main.JTileDownloaderMainView;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader.
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

/**
 * 
 */
public class BBoxLatLonPanel
    extends InputPanel
{
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT_MINLAT = "MIN_LAT";
    private static final String COMPONENT_MINLON = "MIN_LON";
    private static final String COMPONENT_MAXLAT = "MAX_LAT";
    private static final String COMPONENT_MAXLON = "MAX_LON";

    private TileListBBoxLatLon _tileList = new TileListBBoxLatLon();

    JLabel _labelMinLat = new JLabel("Min. Latitude:");
    JTextField _textMinLat = new JTextField();
    JLabel _labelMinLon = new JLabel("Min. Longitude:");
    JTextField _textMinLon = new JTextField();
    JLabel _labelMaxLat = new JLabel("Max. Latitude:");
    JTextField _textMaxLat = new JTextField();
    JLabel _labelMaxLon = new JLabel("Max. Longitude:");
    JTextField _textMaxLon = new JTextField();

    private DownloadConfigurationBBoxLatLon _downloadConfig;

    /**
     * 
     */
    public BBoxLatLonPanel(JTileDownloaderMainView mainView)
    {
        super(mainView);

        createPanel();
        initializePanel();
    }

    /**
     * 
     */
    public void loadConfig()
    {
        _downloadConfig = new DownloadConfigurationBBoxLatLon();
        _downloadConfig.loadFromFile();

        _textMinLat.setText("" + _downloadConfig.getMinLat());
        _textMinLon.setText("" + _downloadConfig.getMinLon());
        _textMaxLat.setText("" + _downloadConfig.getMaxLat());
        _textMaxLon.setText("" + _downloadConfig.getMaxLon());

        setCommonValues(_downloadConfig);
    }

    /**
     * 
     */
    private void initializePanel()
    {
        _textMinLat.setName(COMPONENT_MINLAT);
        _textMinLat.addFocusListener(new MyFocusListener());

        _textMinLon.setName(COMPONENT_MINLON);
        _textMinLon.addFocusListener(new MyFocusListener());

        _textMaxLat.setName(COMPONENT_MAXLAT);
        _textMaxLat.addFocusListener(new MyFocusListener());

        _textMaxLon.setName(COMPONENT_MAXLON);
        _textMaxLon.addFocusListener(new MyFocusListener());
    }

    /**
     * 
     */
    private void createPanel()
    {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMinLat, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMinLat, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMaxLat, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMaxLat, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMinLon, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMinLon, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelMaxLon, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textMaxLon, constraints);

    }

    /**
     * 
     */
    private void updateTileList()
    {
        _tileList.setDownloadZoomLevel(getDownloadZoomLevel());
        _tileList.setTileServerBaseUrl(getTileServerBaseUrl());
        _tileList.setMinLat(getMinLat());
        _tileList.setMinLon(getMinLon());
        _tileList.setMaxLat(getMaxLat());
        _tileList.setMaxLon(getMaxLon());
        _tileList.calculateTileValuesXY();
        updateNumberOfTiles();
    }

    public void saveConfig()
    {
        _downloadConfig.setOutputLocation(getOutputLocation());
        _downloadConfig.setOutputZoomLevel(getDownloadZoomLevel());
        _downloadConfig.setTileServer(getTileServerBaseUrl());
        _downloadConfig.setMinLat(getMinLat());
        _downloadConfig.setMinLon(getMinLon());
        _downloadConfig.setMaxLat(getMaxLat());
        _downloadConfig.setMaxLon(getMaxLon());
        _downloadConfig.saveToFile();
    }

    /**
     * @return
     */
    public double getMinLat()
    {
        String str = _textMinLat.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0.0;
        }
        return Double.parseDouble(str);
    }

    /**
     * @return
     */
    public double getMinLon()
    {
        String str = _textMinLon.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0.0;
        }
        return Double.parseDouble(str);
    }

    /**
     * @return
     */
    public double getMaxLat()
    {
        String str = _textMaxLat.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0.0;
        }
        return Double.parseDouble(str);
    }

    /**
     * @return
     */
    public double getMaxLon()
    {
        String str = _textMaxLon.getText().trim();
        if (str == null || str.length() == 0)
        {
            return 0.0;
        }
        return Double.parseDouble(str);
    }

    /**
     * @return
     */
    public int getNumberOfTilesToDownload()
    {
        return Integer.parseInt("" + (Math.abs(_tileList.getXBottomRight() - _tileList.getXTopLeft()) + 1) * (Math.abs(_tileList.getYBottomRight() - _tileList.getYTopLeft()) + 1));
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

            if (componentName.equalsIgnoreCase(COMPONENT_MINLAT))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MINLON))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MAXLAT))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_MAXLON))
            {
                updateAll();
            }
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#updateAll()
     * {@inheritDoc}
     */
    public void updateAll()
    {
        updateTileList();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getTileList()
     * {@inheritDoc}
     */
    public TileList getTileList()
    {
        return _tileList;
    }

}
