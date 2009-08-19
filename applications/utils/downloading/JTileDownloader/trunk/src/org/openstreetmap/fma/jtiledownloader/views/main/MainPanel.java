package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.TileListExporter;
import org.openstreetmap.fma.jtiledownloader.TileProviderList;
import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxLatLonPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxXYPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.GPXPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.UrlSquarePanel;
import org.openstreetmap.fma.jtiledownloader.views.progressbar.ProgressBar;

/**
 * Copyright 2008, Friedrich Maier 
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
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
public class MainPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    public static final String COMPONENT_OUTPUT_ZOOM_LEVEL = "outputZoomLevel";
    public static final String COMPONENT_OUTPUT_ZOOM_LEVEL_TEXT = "outputZoomLevelText";

    public static final String COMMAND_SELECTOUTPUTFOLDER = "selectOutputFolder";
    public static final String COMMAND_DOWNLOAD = "download";
    public static final String COMMAND_EXPORT = "export";

    private JLabel _labelOutputZoomLevel = new JLabel("Output Zoom Level:");
    private JComboBox _comboOutputZoomLevel = new JComboBox();
    private JLabel _labelOutputZoomLevels = new JLabel("Output Zoom Levels (ex. 12,13,14) :");
    private JTextField _textOutputZoomLevels = new JTextField();
    private JComboBox _comboTileServer = new JComboBox();

    private JLabel _labelAltTileServer = new JLabel("Alt. Tileserver:");
    private JTextField _textAltTileServer = new JTextField();

    private JLabel _labelOutputFolder = new JLabel("Outputfolder:");
    private JTextField _textOutputFolder = new JTextField();
    private JButton _buttonSelectOutputFolder = new JButton("...");

    private JLabel _labelNumberOfTiles = new JLabel("Number Tiles:");
    private JTextField _textNumberOfTiles = new JTextField("---");

    public static final String DOWNLOAD_TILES = "Download Tiles";

    private JButton _buttonDownload = new JButton(DOWNLOAD_TILES);
    private JButton _buttonExport = new JButton("Export Tilelist");

    private final JTileDownloaderMainView _mainView;

    private TileProviderIf[] _tileProviders;
    private int _selectedInputPanel = 0; // HACK to get save of latest setting work

    private JTabbedPane _inputTabbedPane;

    private List<InputPanel> inputPanels;

    /**
     * @param i 
     * @param downloadTemplate 
     * 
     */
    public MainPanel(JTileDownloaderMainView mainView, int tabIndex)
    {
        super();

        _mainView = mainView;

        _tileProviders = new TileProviderList().getTileProviderList();

        registerInputPanels();

        createMainPanel();
        initializeMainPanel();
        if (tabIndex >= 0 && tabIndex < _inputTabbedPane.getTabCount())
        {
            _inputTabbedPane.setSelectedIndex(tabIndex);
            _selectedInputPanel = tabIndex;
        }
        getInputPanel().loadConfig();
    }

    /**
     * Register all input panels
     */
    private void registerInputPanels()
    {
        inputPanels = new LinkedList<InputPanel>();

        // TODO: use reflections here
        inputPanels.add(new UrlSquarePanel(this));
        inputPanels.add(new BBoxLatLonPanel(this));
        inputPanels.add(new BBoxXYPanel(this));
        inputPanels.add(new GPXPanel(this));
    }

    /**
     * 
     */
    private void initializeMainPanel()
    {
        _buttonSelectOutputFolder.setActionCommand(COMMAND_SELECTOUTPUTFOLDER);
        _buttonSelectOutputFolder.setPreferredSize(new Dimension(25, 19));

        _buttonDownload.setActionCommand(COMMAND_DOWNLOAD);

        _buttonExport.setActionCommand(COMMAND_EXPORT);

        _comboOutputZoomLevel.setName(COMPONENT_OUTPUT_ZOOM_LEVEL);
        for (int outputZoomLevel = 0; outputZoomLevel <= 18; outputZoomLevel++)
        {
            _comboOutputZoomLevel.addItem("" + outputZoomLevel);
        }
        _textOutputZoomLevels.setName(COMPONENT_OUTPUT_ZOOM_LEVEL_TEXT);
        //initializeOutputZoomLevel(getInputPanel().getDownloadZoomLevel());

        for (int index = 0; index < _tileProviders.length; index++)
        {
            _comboTileServer.addItem(_tileProviders[index].getName());
        }
        //initializeTileServer("");
        //String url = getInputPanel().getTileServerBaseUrl();
        //initializeTileServer(url);
        //_textOutputFolder.setText(getInputPanel().getOutputLocation());//_downloadTemplate.getOutputLocation());

        _textNumberOfTiles.setEditable(false);
        _textNumberOfTiles.setFocusable(false);
        _textNumberOfTiles.setHorizontalAlignment(JTextField.RIGHT);

        // set all listeners
        _buttonSelectOutputFolder.addActionListener(new MainViewActionListener());
        _buttonDownload.addActionListener(new MainViewActionListener());
        _buttonExport.addActionListener(new MainViewActionListener());

        _comboOutputZoomLevel.addFocusListener(new MainViewFocusListener());
        _comboOutputZoomLevel.addItemListener(new MainViewItemListener());
        _textOutputZoomLevels.addFocusListener(new MainViewFocusListener());
    }

    /**
     * @param tileServer
     */
    public void initializeTileServer(String tileServer)
    {
        TileProviderIf loaded = Util.getTileProvider(tileServer);
        int foundTileServerIndex = -1;
        for (int index = 0; index < _tileProviders.length; index++)
        {
            if (_tileProviders[index].getName().equals(loaded.getName())) //_downloadTemplate.getTileServer()))
            {
                foundTileServerIndex = index;
            }
        }

        if (foundTileServerIndex > -1)
        {
            _comboTileServer.setSelectedIndex(foundTileServerIndex);
            _textAltTileServer.setText("");
        }
        else
        {
            _textAltTileServer.setText(tileServer); //_downloadTemplate.getTileServer());
        }
    }

    public void initializeOutputZoomLevel(int[] zoomLevels)
    {
        if (zoomLevels == null || zoomLevels.length == 0)
        {
            _textOutputZoomLevels.setText("");
            _comboOutputZoomLevel.setSelectedItem("12");
            return;
        }

        if (zoomLevels.length == 1)
        {
            _textOutputZoomLevels.setText("");
            _comboOutputZoomLevel.setSelectedItem("" + zoomLevels[0]);
            return;
        }

        String textZoomLevels = "";
        for (int index = 0; index < zoomLevels.length; index++)
        {
            if (index > 0)
            {
                textZoomLevels += ",";
            }
            textZoomLevels += zoomLevels[index];
        }
        _textOutputZoomLevels.setText(textZoomLevels);

    }

    /**
     * @return
     */
    private void createMainPanel()
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        //constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        setLayout(new GridBagLayout());

        _inputTabbedPane = new JTabbedPane();

        for (InputPanel inputPanel : inputPanels)
        {
            _inputTabbedPane.addTab(inputPanel.getInputName(), inputPanel);
        }
        add(_inputTabbedPane, constraints);
        _inputTabbedPane.addChangeListener(new InputTabListener());

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelOutputZoomLevel, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_comboOutputZoomLevel, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelOutputZoomLevels, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textOutputZoomLevels, constraints);

        add(_comboTileServer, constraints);
        add(_labelAltTileServer, constraints);
        add(_textAltTileServer, constraints);

        JPanel panelOutputFolder = new JPanel();
        panelOutputFolder.setLayout(new GridBagLayout());
        GridBagConstraints constraintsOutputFolder = new GridBagConstraints();
        constraintsOutputFolder.gridwidth = GridBagConstraints.REMAINDER;
        constraintsOutputFolder.weightx = 0.99;
        constraintsOutputFolder.fill = GridBagConstraints.HORIZONTAL;
        panelOutputFolder.add(_labelOutputFolder, constraintsOutputFolder);
        constraintsOutputFolder.gridwidth = GridBagConstraints.RELATIVE;
        panelOutputFolder.add(_textOutputFolder, constraintsOutputFolder);
        constraintsOutputFolder.gridwidth = GridBagConstraints.REMAINDER;
        constraintsOutputFolder.weightx = 0.01;
        panelOutputFolder.add(_buttonSelectOutputFolder, constraintsOutputFolder);
        add(panelOutputFolder, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelNumberOfTiles, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_textNumberOfTiles, constraints);

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_buttonDownload, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_buttonExport, constraints);

        constraints.weighty = 1.0;
        add(new JPanel(), constraints);

        //        return panel;
    }

    private void valuesChanged()
    {
        getInputPanel().updateAll();
    }

    /**
     * @return
     */
    public String getOutputZoomLevelString()
    {
        if (_textOutputZoomLevels.getText().isEmpty())
        {
            return _comboOutputZoomLevel.getSelectedItem().toString();
        }
        else
        {
            return _textOutputZoomLevels.getText();
        }
    }

    private class MainViewFocusListener
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

            if (componentName.equalsIgnoreCase(COMPONENT_OUTPUT_ZOOM_LEVEL))
            {
                valuesChanged();
            }
            if (componentName.equalsIgnoreCase(COMPONENT_OUTPUT_ZOOM_LEVEL_TEXT))
            {
                valuesChanged();
            }

        }

    }

    private class MainViewItemListener
        implements ItemListener
    {

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         * {@inheritDoc}
         */
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == _comboOutputZoomLevel)
            {
                valuesChanged();
            }

        }
    }

    private class MainViewActionListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            System.out.println("button pressed -> " + actionCommand);

            if (actionCommand.equalsIgnoreCase(COMMAND_DOWNLOAD))
            {
                if (!getInputPanel().isDownloadOkay())
                {
                    return;
                }
                getInputPanel().saveConfig();
                valuesChanged();

                //_mainView.updateActualDownloadConfig();
                _mainView.updateAppConfig();

                if (getInputPanel().getTileList().getTileListToDownload().size() > 0)
                {
                    TileListDownloader tld = new TileListDownloader(_textOutputFolder.getText(), getInputPanel().getTileList(), getSelectedTileProvider());
                    ProgressBar pg = new ProgressBar(getInputPanel().getNumberOfTilesToDownload(), tld);
                }
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_EXPORT))
            {
                valuesChanged();

                TileListExporter tle = new TileListExporter(_textOutputFolder.getText(), getInputPanel().getTileList().getTileListToDownload(), getSelectedTileProvider());
                tle.doExport();
                JOptionPane.showMessageDialog(_mainView, "Exported Tilelist to " + _textOutputFolder.getText() + File.separator + "export.txt", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (actionCommand.equalsIgnoreCase(COMPONENT_OUTPUT_ZOOM_LEVEL))
            {
                valuesChanged();
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_SELECTOUTPUTFOLDER))
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File(_textOutputFolder.getText()));
                if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Select"))
                {
                    File dir = chooser.getSelectedFile();
                    System.out.println(dir.getAbsolutePath());
                    _textOutputFolder.setText(dir.getAbsolutePath());
                }
            }

        }
    }

    /**
     * @return
     */
    public String getOutputfolder()
    {
        return _textOutputFolder.getText().trim();
    }

    /**
     * Returns the selected tile server
     * @return selected tile server
     */
    public TileProviderIf getSelectedTileProvider()
    {
        TileProviderIf provider = getTileProvider();
        if (!_textAltTileServer.getText().trim().isEmpty())
        {
            provider = new GenericTileProvider(_textAltTileServer.getText().trim());
        }
        return provider;
    }

    /**
     * @return
     */
    private TileProviderIf getTileProvider()
    {
        return _tileProviders[_comboTileServer.getSelectedIndex()];
    }

    /**
     * Getter for input panels
     * @return a inputpanel
     */
    private final InputPanel getInputPanel()
    {
        return inputPanels.get(_selectedInputPanel);
    }

    /**
     * Sets the number of tiles to download
     */
    public void setNumberOfTiles(int numberOfTiles)
    {
        _textNumberOfTiles.setText(String.valueOf(numberOfTiles));
    }

    class TabChangeListener
        implements ChangeListener
    {

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         * {@inheritDoc}
         */
        public void stateChanged(ChangeEvent e)
        {

        }
    }

    private class InputTabListener
        implements ChangeListener
    {

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         * {@inheritDoc}
         */
        public void stateChanged(ChangeEvent evt)
        {
            JTabbedPane pane = (JTabbedPane) evt.getSource();

            getInputPanel().saveConfig();

            //select new panel & load config
            AppConfiguration.getInstance().setInputPanelIndex(pane.getSelectedIndex());
            _selectedInputPanel = pane.getSelectedIndex();
            getInputPanel().loadConfig();
            valuesChanged();
        }
    }

    /**
     * @param downloadConfig
     */
    public void setOutputFolder(String outputFolder)
    {
        _textOutputFolder.setText(outputFolder);
    }

    /**
     * Saves all Download configs
     */
    public void updateActualDownloadConfig()
    {
        getInputPanel().saveConfig();
    }
}
