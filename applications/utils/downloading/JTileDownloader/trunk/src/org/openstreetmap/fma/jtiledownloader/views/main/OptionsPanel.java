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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;

public class OptionsPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    JCheckBox _chkWaitAfterNrTiles = new JCheckBox("Wait <n> sec after downloading <m> tiles?");
    JLabel _labelWaitSeconds = new JLabel("Seconds <n> to wait:");
    JTextField _textWaitSeconds = new JTextField();
    JLabel _labelWaitNrTiles = new JLabel("Wait after <m> tiles:");
    JTextField _textWaitNrTiles = new JTextField();

    JLabel _labelMinimumAgeInDays = new JLabel("Minimum age in days:");
    JTextField _textMinimumAgeInDays = new JTextField();

    JCheckBox _chkShowTilePreview = new JCheckBox("Show TilePreview");

    JCheckBox _chkOverwriteExistingFiles = new JCheckBox("Overwrite existing files");

    /**
     * 
     */
    public OptionsPanel()
    {
        super();

        createOptionsPanel();
        initializeOptionsPanel();
    }

    /**
     * @return
     */
    private void createOptionsPanel()
    {
        setLayout(new GridBagLayout());

        JPanel panelWaitOptions = new JPanel();

        GridBagConstraints constraintsWaitOptions = new GridBagConstraints();
        constraintsWaitOptions.gridwidth = GridBagConstraints.REMAINDER;
        constraintsWaitOptions.weightx = 1.0;
        constraintsWaitOptions.fill = GridBagConstraints.HORIZONTAL;
        constraintsWaitOptions.insets = new Insets(5, 5, 0, 5);

        panelWaitOptions.setLayout(new GridBagLayout());
        Border borderWaitOptions = new TitledBorder("Wait Options");
        panelWaitOptions.setBorder(borderWaitOptions);

        panelWaitOptions.add(_chkWaitAfterNrTiles, constraintsWaitOptions);

        constraintsWaitOptions.gridwidth = GridBagConstraints.RELATIVE;
        panelWaitOptions.add(_labelWaitSeconds, constraintsWaitOptions);
        constraintsWaitOptions.gridwidth = GridBagConstraints.REMAINDER;
        panelWaitOptions.add(_textWaitSeconds, constraintsWaitOptions);
        constraintsWaitOptions.gridwidth = GridBagConstraints.RELATIVE;
        panelWaitOptions.add(_labelWaitNrTiles, constraintsWaitOptions);
        constraintsWaitOptions.gridwidth = GridBagConstraints.REMAINDER;
        panelWaitOptions.add(_textWaitNrTiles, constraintsWaitOptions);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 5, 0, 5);
        add(panelWaitOptions, constraints);

        GridBagConstraints constraintsOtherOptions = new GridBagConstraints();
        constraintsOtherOptions.gridwidth = GridBagConstraints.REMAINDER;
        constraintsOtherOptions.weightx = 1.0;
        constraintsOtherOptions.fill = GridBagConstraints.HORIZONTAL;
        constraintsOtherOptions.insets = new Insets(5, 5, 0, 5);

        JPanel otherOptions = new JPanel();
        otherOptions.setLayout(new GridBagLayout());
        Border borderOtherOptions = new TitledBorder("Other Options");
        otherOptions.setBorder(borderOtherOptions);

        constraintsWaitOptions.gridwidth = GridBagConstraints.RELATIVE;
        otherOptions.add(_labelMinimumAgeInDays, constraintsWaitOptions);
        constraintsWaitOptions.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_textMinimumAgeInDays, constraintsWaitOptions);

        otherOptions.add(_chkShowTilePreview, constraintsOtherOptions);
        constraintsOtherOptions.gridwidth = GridBagConstraints.RELATIVE;
        otherOptions.add(_chkOverwriteExistingFiles, constraintsOtherOptions);

        add(otherOptions, constraints);

        constraints.weighty = 1.0;
    }

    /**
     * 
     */
    private void initializeOptionsPanel()
    {
        _chkWaitAfterNrTiles.setSelected(AppConfiguration.getInstance().getWaitAfterNrTiles());
        _textWaitSeconds.setText("" + AppConfiguration.getInstance().getWaitSeconds());
        _textWaitNrTiles.setText("" + AppConfiguration.getInstance().getWaitNrTiles());

        _textMinimumAgeInDays.setText("" + AppConfiguration.getInstance().getMinimumAgeInDays());

        _chkShowTilePreview.setSelected(AppConfiguration.getInstance().isShowTilePreview());
        _chkOverwriteExistingFiles.setSelected(AppConfiguration.getInstance().isOverwriteExistingFiles());

    }

    /**
     * @return
     */
    public boolean isShowTilePreview()
    {
        return _chkShowTilePreview.isSelected();
    }

    /**
     * @return
     */
    public boolean isOverwriteExistingFiles()
    {
        return _chkOverwriteExistingFiles.isSelected();
    }

    /**
     * @return
     */
    public boolean isWaitAfterNumberOfTiles()
    {
        return _chkWaitAfterNrTiles.isSelected();
    }

    /**
     * @return
     */
    public int getWaitSeconds()
    {
        return Integer.parseInt(_textWaitSeconds.getText());
    }

    /**
     * @return
     */
    public int getWaitNrTiles()
    {
        return Integer.parseInt(_textWaitNrTiles.getText());
    }

    /**
     * @return
     */
    public int getMinimumAgeInDays()
    {
        return Integer.parseInt(_textMinimumAgeInDays.getText());
    }

}
