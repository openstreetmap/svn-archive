/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009-2011, Sven Strickroth <email@cs-ware.de>
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;

public class OptionsPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    private JCheckBox _chkWaitAfterNrTiles = new JCheckBox("Wait <n> sec after downloading <m> tiles?");
    private JLabel _labelWaitSeconds = new JLabel("Seconds <n> to wait:");
    private JTextField _textWaitSeconds = new JTextField();
    private JLabel _labelWaitNrTiles = new JLabel("Wait after <m> tiles:");
    private JTextField _textWaitNrTiles = new JTextField();

    private JLabel _labelMinimumAgeInDays = new JLabel("Minimum age in days:");
    private JTextField _textMinimumAgeInDays = new JTextField();

    private JLabel _labelDownloadThreads = new JLabel("Download Threads");
    private JSlider _sliderDownloadThreads = new JSlider(1, 4);

    private JCheckBox _chkOverwriteExistingFiles = new JCheckBox("Overwrite existing files");

    private JCheckBox _slippyMapNoDownload = new JCheckBox("Do not download new tiles");
    private JCheckBox _slippyMapSaveTiles = new JCheckBox("Save downloaded tiles");
    
    private JLabel _labelTileSortingPolicy = new JLabel("Downloading order");
    private JComboBox _comboTileSortingPolicy = new JComboBox();

    /**
     * 
     */
    public OptionsPanel()
    {
        super();

        createOptionsPanel();
        initializeOptionsPanel();

        _labelDownloadThreads.setText("Download Threads: " + _sliderDownloadThreads.getValue());
        _sliderDownloadThreads.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                _labelDownloadThreads.setText("Download Threads: " + _sliderDownloadThreads.getValue());
            }
        });
    }

    /**
     */
    private void createOptionsPanel()
    {
        setLayout(new GridBagLayout());

        GridBagConstraints outerConstraints = new GridBagConstraints();
        outerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        outerConstraints.weightx = 1.0;
        outerConstraints.fill = GridBagConstraints.HORIZONTAL;
        outerConstraints.insets = new Insets(10, 5, 0, 5);

        GridBagConstraints innerConstraints = new GridBagConstraints();
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        innerConstraints.weightx = 1.0;
        innerConstraints.fill = GridBagConstraints.HORIZONTAL;
        innerConstraints.insets = new Insets(5, 5, 0, 5);


        JPanel panelWaitOptions = new JPanel();
        panelWaitOptions.setLayout(new GridBagLayout());
        Border borderWaitOptions = new TitledBorder("Wait Options");
        panelWaitOptions.setBorder(borderWaitOptions);

        panelWaitOptions.add(_chkWaitAfterNrTiles, innerConstraints);

        innerConstraints.gridwidth = GridBagConstraints.RELATIVE;
        panelWaitOptions.add(_labelWaitSeconds, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelWaitOptions.add(_textWaitSeconds, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.RELATIVE;
        panelWaitOptions.add(_labelWaitNrTiles, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelWaitOptions.add(_textWaitNrTiles, innerConstraints);

        add(panelWaitOptions, outerConstraints);


        JPanel slippyMapOptions = new JPanel();
        slippyMapOptions.setLayout(new GridBagLayout());
        Border borderSlippyMapOptions = new TitledBorder("SlippyMap Options");
        slippyMapOptions.setBorder(borderSlippyMapOptions);

        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        slippyMapOptions.add(_slippyMapNoDownload, innerConstraints);
        slippyMapOptions.add(_slippyMapSaveTiles, innerConstraints);

        add(slippyMapOptions, outerConstraints);


        JPanel otherOptions = new JPanel();
        otherOptions.setLayout(new GridBagLayout());
        Border borderOtherOptions = new TitledBorder("Other Options");
        otherOptions.setBorder(borderOtherOptions);

        innerConstraints.gridwidth = GridBagConstraints.RELATIVE;
        otherOptions.add(_labelMinimumAgeInDays, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_textMinimumAgeInDays, innerConstraints);

        innerConstraints.gridwidth = GridBagConstraints.RELATIVE;
        otherOptions.add(_labelTileSortingPolicy, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_comboTileSortingPolicy, innerConstraints);

        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_chkOverwriteExistingFiles, innerConstraints);

        innerConstraints.gridwidth = GridBagConstraints.RELATIVE;
        otherOptions.add(_labelDownloadThreads, innerConstraints);
        innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_sliderDownloadThreads, innerConstraints);
        add(otherOptions, outerConstraints);

        outerConstraints.weighty = 1.0;
    }

    /**
     * 
     */
    private void initializeOptionsPanel()
    {
        _chkWaitAfterNrTiles.setSelected(AppConfiguration.getInstance().isWaitingAfterNrOfTiles());
        _textWaitSeconds.setText(String.valueOf(AppConfiguration.getInstance().getWaitSeconds()));
        _textWaitNrTiles.setText(String.valueOf(AppConfiguration.getInstance().getWaitNrTiles()));

        _textMinimumAgeInDays.setText(String.valueOf(AppConfiguration.getInstance().getMinimumAgeInDays()));

        _sliderDownloadThreads.setValue(AppConfiguration.getInstance().getDownloadThreads());
        _chkOverwriteExistingFiles.setSelected(AppConfiguration.getInstance().isOverwriteExistingFiles());

        _slippyMapNoDownload.setSelected(AppConfiguration.getInstance().isSlippyMap_NoDownload());
        _slippyMapSaveTiles.setSelected(AppConfiguration.getInstance().isSlippyMap_SaveTiles());
        
        _comboTileSortingPolicy.addItem("Does not matter");
        _comboTileSortingPolicy.addItem("Sequential");
        _comboTileSortingPolicy.addItem("By quad tiles");
        _comboTileSortingPolicy.setSelectedIndex(AppConfiguration.getInstance().getTileSortingPolicy());
    }

    /**
     * @return overwrite existing files
     */
    public boolean isOverwriteExistingFiles()
    {
        return _chkOverwriteExistingFiles.isSelected();
    }

    /**
     * @return wait after download a number of tiles
     */
    public boolean isWaitAfterNumberOfTiles()
    {
        return _chkWaitAfterNrTiles.isSelected();
    }

    /**
     * @return wait x seconds after download a number of tiles
     */
    public int getWaitSeconds()
    {
        return Integer.parseInt(_textWaitSeconds.getText());
    }

    /**
     * @return the number of tiles to wait after
     */
    public int getWaitNrTiles()
    {
        return Integer.parseInt(_textWaitNrTiles.getText());
    }

    /**
     * @return minimum age in days before trying to redownload
     */
    public int getMinimumAgeInDays()
    {
        return Integer.parseInt(_textMinimumAgeInDays.getText());
    }

    /**
     * @return the slippyMapNoDownload
     */
    public boolean isSlippyMapNoDownload()
    {
        return _slippyMapNoDownload.isSelected();
    }

    /**
     * @return the slippyMapSaveTiles
     */
    public boolean isSlippyMapSaveTiles()
    {
        return _slippyMapSaveTiles.isSelected();
    }

    /**
     * @return downloadThreads
     */
    public int getDownloadThreads()
    {
        return _sliderDownloadThreads.getValue();
    }
    
    public int getTileSortingPolicy() {
        return _comboTileSortingPolicy.getSelectedIndex();
    }
}
