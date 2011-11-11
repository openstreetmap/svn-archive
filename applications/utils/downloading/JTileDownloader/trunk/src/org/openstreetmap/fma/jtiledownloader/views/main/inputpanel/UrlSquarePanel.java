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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationSaverIf;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListUrlSquare;
import org.openstreetmap.fma.jtiledownloader.views.main.MainPanel;

/**
 * 
 */
public class UrlSquarePanel
    extends InputPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(UrlSquarePanel.class.getName());

    private static final String COMPONENT_PASTE_URL = "pasteURL";
    private static final String COMPONENT_RADIUS = "radius";

    private TileListUrlSquare _tileList = new TileListUrlSquare();

    private JLabel _labelPasteUrl = new JLabel("Paste URL:");
    private JTextField _textPasteUrl = new JTextField();

    private JLabel _labelLatitude = new JLabel("Latitude:");
    private JTextField _textLatitude = new JTextField();
    private JLabel _labelLongitude = new JLabel("Longitude:");
    private JTextField _textLongitude = new JTextField();
    private JLabel _labelRadius = new JLabel("Radius (km):");
    private JTextField _textRadius = new JTextField();

    private DownloadConfigurationUrlSquare _downloadConfig;

    /**
     * @param mainPanel 
     */
    public UrlSquarePanel(MainPanel mainPanel)
    {
        super(mainPanel);

        createPanel();
        initializePanel();

        loadConfig(AppConfiguration.getInstance());
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getJobType()
     */
    @Override
    public String getJobType()
    {
        return DownloadConfigurationUrlSquare.ID;
    }

    @Override
    public final void loadConfig(DownloadConfigurationSaverIf configurationSaver)
    {
        _downloadConfig = new DownloadConfigurationUrlSquare();
        configurationSaver.loadDownloadConfig(_downloadConfig);

        _textPasteUrl.setText(_downloadConfig.getPasteUrl());
        _textRadius.setText(String.valueOf(_downloadConfig.getRadius()));

        parsePasteUrl();
    }

    /**
     * 
     */
    private void initializePanel()
    {
        _textPasteUrl.setPreferredSize(new Dimension(330, 20));
        _textPasteUrl.addFocusListener(new MyFocusListener());
        _textPasteUrl.getDocument().addDocumentListener(new MyDocumentListener());
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
    private void createPanel()
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
        Util.parsePasteUrl(_textPasteUrl.getText(), _tileList);

        _textLatitude.setText(String.valueOf(_tileList.getLatitude()));
        _textLongitude.setText(String.valueOf(_tileList.getLongitude()));
    }

    /**
     * 
     */
    private void updateTileListSquare()
    {
        parsePasteUrl();
        try
        {
            _tileList.setDownloadZoomLevels(getDownloadZoomLevel());
            _tileList.setRadius(Integer.parseInt(_textRadius.getText()) * 1000);
            _tileList.calculateTileValuesXY();
            updateNumberOfTiles();
        }
        catch (NumberFormatException e)
        {
            return;
        }
    }

    @Override
    public void saveConfig(DownloadConfigurationSaverIf configurationSave)
    {
        if (_downloadConfig == null)
        {
            return;
        }

        _downloadConfig.setPasteUrl(getPasteUrl());
        _downloadConfig.setRadius(getRadius());

        configurationSave.saveDownloadConfig(_downloadConfig);
    }

    /**
     * @return pasted URL
     */
    public String getPasteUrl()
    {
        return _textPasteUrl.getText().trim();
    }

    /**
     * @return radius
     */
    public int getRadius()
    {
        return Integer.parseInt(_textRadius.getText().trim());
    }

    /**
     * @return number of tiles to download
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
         */
        public void focusGained(FocusEvent focusevent)
        {

        }

        /**
         * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
         */
        public void focusLost(FocusEvent focusevent)
        {
            String componentName = focusevent.getComponent().getName();
            log.fine("focusLost: " + componentName);

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
    
    class MyDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            if( e.getLength() > 0 && _textPasteUrl.isShowing() )
                updateAll();
        }

        public void removeUpdate(DocumentEvent e) {
            if( e.getLength() > 0 && _textPasteUrl.isShowing() )
                updateAll();
        }

        public void changedUpdate(DocumentEvent e) { }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#updateAll()
     */
    @Override
    public void updateAll()
    {
        updateTileListSquare();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getTileList()
     */
    @Override
    public TileList getTileList()
    {
        return _tileList;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#getInputName()
     */
    @Override
    public String getInputName()
    {
        return "Paste URL (Square)";
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel#isDownloadOkay()
     */
    @Override
    public boolean isDownloadOkay()
    {
        if (getPasteUrl() == null || getPasteUrl().length() == 0)
        {
            JOptionPane.showMessageDialog(this, "Please enter a URL in the input field Paste URL!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        else
        {
            return true;
        }
    }
}
