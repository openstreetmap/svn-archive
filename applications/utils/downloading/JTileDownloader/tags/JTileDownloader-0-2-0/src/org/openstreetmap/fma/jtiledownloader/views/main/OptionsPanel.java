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

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class OptionsPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    JCheckBox _chkWaitAfterNrTiles = new JCheckBox("Wait <n> sec after downloading <m> tiles?");
    JLabel _labelWaitSeconds = new JLabel("Seconds <n> to wait:");
    JTextField _textWaitSeconds = new JTextField();
    JLabel _labelWaitNrTiles = new JLabel("Wait after <m> tiles:");
    JTextField _textWaitNrTiles = new JTextField();

    JCheckBox _chkShowTilePreview = new JCheckBox("Show TilePreview");
    JCheckBox _chkAutoCloseTilePreview = new JCheckBox("AutoClose TilePreview");

    private final AppConfiguration _appConfiguration;

    /**
     * 
     */
    public OptionsPanel(AppConfiguration appConfiguration)
    {
        super();
        _appConfiguration = appConfiguration;

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
        constraintsOtherOptions.gridwidth = GridBagConstraints.RELATIVE;
        constraintsOtherOptions.weightx = 1.0;
        constraintsOtherOptions.fill = GridBagConstraints.HORIZONTAL;
        constraintsOtherOptions.insets = new Insets(5, 5, 0, 5);
        constraintsOtherOptions.anchor = GridBagConstraints.WEST;

        JPanel otherOptions = new JPanel();
        Border borderOtherOptions = new TitledBorder("Other Options");
        otherOptions.setBorder(borderOtherOptions);
        constraintsOtherOptions.gridwidth = GridBagConstraints.REMAINDER;
        otherOptions.add(_chkShowTilePreview, constraintsOtherOptions);
        otherOptions.add(_chkAutoCloseTilePreview, constraintsOtherOptions);

        add(otherOptions, constraints);

        constraints.weighty = 1.0;
    }

    /**
     * 
     */
    private void initializeOptionsPanel()
    {
        _chkWaitAfterNrTiles.setSelected(_appConfiguration.getWaitAfterNrTiles());
        _textWaitSeconds.setText("" + _appConfiguration.getWaitSeconds());
        _textWaitNrTiles.setText("" + _appConfiguration.getWaitNrTiles());

        _chkShowTilePreview.setSelected(_appConfiguration.isShowTilePreview());
        _chkAutoCloseTilePreview.setSelected(_appConfiguration.isAutoCloseTilePreview());

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
    public boolean isAutoCloseTilePreview()
    {
        return _chkAutoCloseTilePreview.isSelected();
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

}
