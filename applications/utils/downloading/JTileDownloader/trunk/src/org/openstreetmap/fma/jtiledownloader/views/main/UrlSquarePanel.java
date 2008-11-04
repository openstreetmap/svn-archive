package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSquare;

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
public class UrlSquarePanel
    extends InputPanel
{
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT_PASTE_URL = "pasteURL";
    private static final String COMPONENT_RADIUS = "radius";

    private TileListSquare _tileListSquare = new TileListSquare();

    JLabel _labelPasteUrl = new JLabel("Paste URL:");
    JTextField _textPasteUrl = new JTextField();

    JLabel _labelLatitude = new JLabel("Latitude:");
    JTextField _textLatitude = new JTextField();
    JLabel _labelLongitude = new JLabel("Longitude:");
    JTextField _textLongitude = new JTextField();
    JLabel _labelRadius = new JLabel("Radius (km):");
    JTextField _textRadius = new JTextField();

    private final JTileDownloaderMainView _mainView;
    private DownloadConfigurationUrlSquare _downloadConfig;

    /**
     * 
     */
    public UrlSquarePanel(JTileDownloaderMainView mainView)
    {
        super();
        _mainView = mainView;

        createUrlSquarePanel();
        initializeUrlSquarePanel();
    }

    /**
     * 
     */
    public void loadConfig()
    {
        _downloadConfig = new DownloadConfigurationUrlSquare();
        _downloadConfig.loadFromFile();

        _textPasteUrl.setText(_downloadConfig.getPasteUrl());
        _textRadius.setText("" + _downloadConfig.getRadius());

        setOutputLocation(_downloadConfig.getOutputLocation());
        setDownloadZoomLevel(_downloadConfig.getOutputZoomLevel());
        setTileServerBaseUrl(_downloadConfig.getTileServer());
        _mainView.getMainPanel().initializeTileServer(getTileServerBaseUrl());
        _mainView.getMainPanel().initializeOutputZoomLevel(getDownloadZoomLevel());
        _mainView.getMainPanel()._textOutputFolder.setText(getOutputLocation());

        parsePasteUrl();

    }

    /**
     * 
     */
    private void initializeUrlSquarePanel()
    {
        _textPasteUrl.setPreferredSize(new Dimension(330, 20));
        _textPasteUrl.addFocusListener(new MyFocusListener());
        _textPasteUrl.setName(COMPONENT_PASTE_URL);

        _textLatitude.setEditable(false);
        _textLatitude.setFocusable(false);
        _textLongitude.setEditable(false);
        _textLongitude.setFocusable(false);

        _textRadius.setName(COMPONENT_RADIUS);
        _textRadius.addFocusListener(new MyFocusListener());
    }

    /**
     * 
     */
    private void createUrlSquarePanel()
    {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        add(_labelPasteUrl, constraints);
        add(_textPasteUrl, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelLatitude, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textLatitude, constraints);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelLongitude, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textLongitude, constraints);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelRadius, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textRadius, constraints);

    }

    /**
     * 
     */
    public void parsePasteUrl()
    {
        //String pasteUrl = "http://www.openstreetmap.org/?lat=48.256&lon=13.0434&zoom=12&layers=0B0FT";
        String url = _textPasteUrl.getText();
        if (url == null || url.length() == 0)
        {
            _textLatitude.setText("" + 0);
            _textLongitude.setText("" + 0);

            _tileListSquare.setLatitude(0);
            _tileListSquare.setLongitude(0);
            return;
        }

        int posLat = url.indexOf("lat=");
        String lat = url.substring(posLat);
        int posLon = url.indexOf("lon=");
        String lon = url.substring(posLon);

        int posAnd = lat.indexOf("&");
        lat = lat.substring(4, posAnd);
        posAnd = lon.indexOf("&");
        lon = lon.substring(4, posAnd);

        _textLatitude.setText(lat);
        _textLongitude.setText(lon);

        _tileListSquare.setLatitude(Double.parseDouble(lat));
        _tileListSquare.setLongitude(Double.parseDouble(lon));

    }

    /**
     * 
     */
    private void updateTileListSquare()
    {
        _tileListSquare.setDownloadZoomLevel(getDownloadZoomLevel());
        _tileListSquare.setTileServerBaseUrl(getTileServerBaseUrl());
        _tileListSquare.setRadius(Integer.parseInt("" + _textRadius.getText()) * 1000);
        _tileListSquare.calculateTileValuesXY();
        updateNumberOfTiles();
    }

    private void updateNumberOfTiles()
    {
        long numberOfTiles = 0;
        numberOfTiles = getNumberOfTilesToDownload();
        _mainView.getMainPanel()._textNumberOfTiles.setText("" + numberOfTiles);
    }

    public void saveConfig()
    {
        _downloadConfig.setOutputLocation(getOutputLocation());
        _downloadConfig.setOutputZoomLevel(getDownloadZoomLevel());
        _downloadConfig.setPasteUrl(getPasteUrl());
        _downloadConfig.setRadius(getRadius());
        _downloadConfig.setTileServer(getTileServerBaseUrl());
        _downloadConfig.saveToFile();
    }

    /**
     * @return
     */
    public String getPasteUrl()
    {
        return _textPasteUrl.getText().trim();
    }

    /**
     * @return
     */
    public int getRadius()
    {
        return Integer.parseInt(_textRadius.getText().trim());
    }

    /**
     * @return
     */
    public int getNumberOfTilesToDownload()
    {
        return Integer.parseInt("" + (Math.abs(_tileListSquare.getXBottomRight() - _tileListSquare.getXTopLeft()) + 1) * (Math.abs(_tileListSquare.getYBottomRight() - _tileListSquare.getYTopLeft()) + 1));
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

            if (componentName.equalsIgnoreCase(COMPONENT_PASTE_URL))
            {
                updateAll();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_RADIUS))
            {
                updateAll();
            }
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.InputPanel#updateAll()
     * {@inheritDoc}
     */
    public void updateAll()
    {
        updateTileListSquare();
        updateNumberOfTiles();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.InputPanel#getTileList()
     * {@inheritDoc}
     */
    public TileList getTileList()
    {
        return _tileListSquare;
    }

}
