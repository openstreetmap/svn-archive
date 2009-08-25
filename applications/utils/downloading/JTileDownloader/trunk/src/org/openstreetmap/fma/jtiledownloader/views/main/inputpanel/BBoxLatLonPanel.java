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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfigurationBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.views.main.MainPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.slippymap.SlippyMapChooser;
import org.openstreetmap.fma.jtiledownloader.views.main.slippymap.SlippyMapChooserWindow;

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

    private JLabel _labelMinLat = new JLabel("Min. Latitude:");
    private JTextField _textMinLat = new JTextField();
    private JLabel _labelMinLon = new JLabel("Min. Longitude:");
    private JTextField _textMinLon = new JTextField();
    private JLabel _labelMaxLat = new JLabel("Max. Latitude:");
    private JTextField _textMaxLat = new JTextField();
    private JLabel _labelMaxLon = new JLabel("Max. Longitude:");
    private JTextField _textMaxLon = new JTextField();
    private JButton _buttonSlippyMapChooser = new JButton("Slippy Map chooser");

    private DownloadConfigurationBBoxLatLon _downloadConfig;
    private SlippyMapChooser changeListener = null;

    /**
     * 
     */
    public BBoxLatLonPanel(MainPanel mainPanel)
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

        _buttonSlippyMapChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                SlippyMapChooserWindow smc = new SlippyMapChooserWindow((BBoxLatLonPanel) ((Component) arg0.getSource()).getParent());
                smc.setVisible(true);
            }
        });
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

        add(_buttonSlippyMapChooser, constraints);
    }

    public void setCoordinates(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude)
    {
        _textMaxLon.setText(String.valueOf(maxLongitude));
        _textMaxLat.setText(String.valueOf(maxLatitude));
        _textMinLon.setText(String.valueOf(minLongitude));
        _textMinLat.setText(String.valueOf(minLatitude));
        updateTileList();
    }

    /**
     * 
     */
    private void updateTileList()
    {
        _tileList.setDownloadZoomLevels(getDownloadZoomLevel());
        _tileList.setMinLat(getMinLat());
        _tileList.setMinLon(getMinLon());
        _tileList.setMaxLat(getMaxLat());
        _tileList.setMaxLon(getMaxLon());
        _tileList.calculateTileValuesXY();
        updateNumberOfTiles();
    }

    @Override
    public void saveConfig()
    {
        if (_downloadConfig == null)
        {
            return;
        }

        _downloadConfig.setMinLat(getMinLat());
        _downloadConfig.setMinLon(getMinLon());
        _downloadConfig.setMaxLat(getMaxLat());
        _downloadConfig.setMaxLon(getMaxLon());
        super.saveCommonConfig(_downloadConfig);
        _downloadConfig.saveToFile();
    }

    /**
     * @return min latitude
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
     * @return  min longitude
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
     * @return max latitude
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
     * @return max longitude
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
    @Override
    public void updateAll()
    {
        if (changeListener != null)
        {
            changeListener.boundingBoxChanged();
        }
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
        return "Bounding Box (Lat/Lon)";
    }

    /**
     * Setter for changeListener
     * @param changeListener the changeListener to set
     */
    public void setChangeListener(SlippyMapChooser changeListener)
    {
        this.changeListener = changeListener;
    }
}
