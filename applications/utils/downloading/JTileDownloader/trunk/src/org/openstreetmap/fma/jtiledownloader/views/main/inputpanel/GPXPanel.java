/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 *
 * based on UrlSquarePanel by:
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of jTileDownloader.
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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationGPX;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonGPX;
import org.openstreetmap.fma.jtiledownloader.views.main.MainPanel;

/**
 * 
 */
public class GPXPanel
    extends InputPanel
{
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT_GPX_FILE = "gpxFile";

    private TileListCommonGPX _tileList = new TileListCommonGPX();

    private JLabel _labelGPXFile = new JLabel("GPX File:");
    private JTextField _textGPXFile = new JTextField();
    private JButton _selectFileButton = new JButton("Select file...");

    private JLabel _labelSliderCorridor = new JLabel("Corridor in km (0 will use the bounding rectangle of GPX file)");
    private JSlider _sliderCorridor = new JSlider(0, 30, 0);

    private DownloadConfigurationGPX _downloadConfig;

    /**
     * 
     */
    public GPXPanel(MainPanel mainPanel)
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
        _downloadConfig = new DownloadConfigurationGPX();
        _downloadConfig.loadFromFile();

        _textGPXFile.setText(_downloadConfig.getGpxFile());
        _sliderCorridor.setValue(_downloadConfig.getCorridor());

        setCommonValues(_downloadConfig);
    }

    @Override
    public void saveConfig()
    {
        if (_downloadConfig == null)
        {
            return;
        }

        _downloadConfig.setGpxFile(_textGPXFile.getText());
        _downloadConfig.setCorridor(_sliderCorridor.getValue());
        super.saveCommonConfig(_downloadConfig);
        _downloadConfig.saveToFile();
    }

    /**
     * 
     */
    private void initializePanel()
    {
        _textGPXFile.setPreferredSize(new Dimension(330, 20));
        _textGPXFile.addFocusListener(new MyFocusListener());
        _textGPXFile.setName(COMPONENT_GPX_FILE);

        _selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f)
                    {
                        if (f.getName().endsWith(".gpx") || f.isDirectory())
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }

                    @Override
                    public String getDescription()
                    {
                        return "GPX-File";
                    }

                });
                chooser.setCurrentDirectory(new File(_textGPXFile.getText()));
                if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Select"))
                {
                    File dir = chooser.getSelectedFile();
                    _textGPXFile.setText(dir.getAbsolutePath());
                    updateAll();
                }
            }
        });

        _sliderCorridor.setMinorTickSpacing(1);
        _sliderCorridor.setMajorTickSpacing(5);
        _sliderCorridor.setPaintTicks(true);
        _sliderCorridor.setSnapToTicks(true);
        _sliderCorridor.setPaintLabels(true);
        _sliderCorridor.addChangeListener(new ChangeListener() {
            /**
             * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting())
                {
                    updateAll();
                }
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
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        add(_labelGPXFile, constraints);
        add(_textGPXFile, constraints);
        add(_selectFileButton, constraints);
        add(_labelSliderCorridor, constraints);
        add(_sliderCorridor, constraints);
    }

    /**
     * @return number of tiles
     */
    @Override
    public int getNumberOfTilesToDownload()
    {
        return _tileList.getTileListToDownload().size();
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

            if (componentName.equalsIgnoreCase(COMPONENT_GPX_FILE))
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
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        _tileList.setDownloadZoomLevels(getDownloadZoomLevel());
        _tileList.updateList(_textGPXFile.getText(), _sliderCorridor.getValue());
        updateNumberOfTiles();

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
        return "GPX File";
    }
}
