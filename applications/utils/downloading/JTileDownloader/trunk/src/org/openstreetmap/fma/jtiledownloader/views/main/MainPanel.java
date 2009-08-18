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
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.TileListExporter;
import org.openstreetmap.fma.jtiledownloader.TileProviderList;
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

    JLabel _labelOutputZoomLevel = new JLabel("Output Zoom Level:");
    JComboBox _comboOutputZoomLevel = new JComboBox();
    JLabel _labelOutputZoomLevels = new JLabel("Output Zoom Levels (ex. 12,13,14) :");
    JTextField _textOutputZoomLevels = new JTextField();
    JComboBox _comboTileServer = new JComboBox();

    JLabel _labelAltTileServer = new JLabel("Alt. Tileserver:");
    JTextField _textAltTileServer = new JTextField();

    JLabel _labelOutputFolder = new JLabel("Outputfolder:");
    JTextField _textOutputFolder = new JTextField();
    JButton _buttonSelectOutputFolder = new JButton("...");

    JLabel _labelNumberOfTiles = new JLabel("Number Tiles:");
    JTextField _textNumberOfTiles = new JTextField("---");

    public static final String STOP_DOWNLOAD = "Stop Download";
    public static final String DOWNLOAD_TILES = "Download Tiles";

    JButton _buttonDownload = new JButton(DOWNLOAD_TILES);
    JButton _buttonExport = new JButton("Export Tilelist");

    private final JTileDownloaderMainView _mainView;

    private TileProviderIf[] _tileProviders;

    private UrlSquarePanel _urlSquarePanel;
    private BBoxLatLonPanel _bBoxLatLonPanel;
    private BBoxXYPanel _bBoxXYPanel;
    private GPXPanel _gpxPanel;

    private JTabbedPane _inputTabbedPane;

    /**
     * @param downloadTemplate 
     * 
     */
    public MainPanel(JTileDownloaderMainView mainView)
    {
        super();

        //_downloadTemplate = downloadTemplate;
        _mainView = mainView;

        _tileProviders = new TileProviderList().getTileProviderList();

        createMainPanel();
        initializeMainPanel();

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
        initializeOutputZoomLevel(getInputPanel().getDownloadZoomLevel());

        for (int index = 0; index < _tileProviders.length; index++)
        {
            _comboTileServer.addItem(_tileProviders[index].getName());
        }
        String url = getInputPanel().getTileServerBaseUrl();
        initializeTileServer(url);

        _textOutputFolder.setText(getInputPanel().getOutputLocation());//_downloadTemplate.getOutputLocation());

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
        int foundTileServerIndex = -1;
        for (int index = 0; index < _tileProviders.length; index++)
        {
            if (_tileProviders[index].getTileServerUrl().equals(tileServer)) //_downloadTemplate.getTileServer()))
            {
                foundTileServerIndex = index;
            }
        }

        if (foundTileServerIndex > -1)
        {
            _comboTileServer.setSelectedIndex(foundTileServerIndex);
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
        //        JPanel panel = new JPanel();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        //constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);

        setLayout(new GridBagLayout());

        _inputTabbedPane = new JTabbedPane();

        _urlSquarePanel = new UrlSquarePanel(_mainView);
        _inputTabbedPane.addTab(Constants.INPUT_TAB_TYPE[Constants.TYPE_URLSQUARE], _urlSquarePanel);
        _bBoxLatLonPanel = new BBoxLatLonPanel(_mainView);
        _inputTabbedPane.addTab(Constants.INPUT_TAB_TYPE[Constants.TYPE_BOUNDINGBOX_LATLON], _bBoxLatLonPanel);
        _bBoxXYPanel = new BBoxXYPanel(_mainView);
        _inputTabbedPane.addTab(Constants.INPUT_TAB_TYPE[Constants.TYPE_BOUNDINGBOX_XY], _bBoxXYPanel);
        _gpxPanel = new GPXPanel(_mainView);
        _inputTabbedPane.addTab(Constants.INPUT_TAB_TYPE[Constants.TYPE_GPX], _gpxPanel);

        add(_inputTabbedPane, constraints);
        _inputTabbedPane.addChangeListener(new InputTabListener(_mainView));

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

    /**
     * Getter for buttonDownload
     * @return the buttonDownload
     */
    protected final JButton getButtonExport()
    {
        return _buttonExport;
    }

    /**
     * Getter for buttonDownload
     * @return the buttonDownload
     */
    protected final JButton getButtonDownload()
    {
        return _buttonDownload;
    }

    public void valuesChanged()
    {
        getInputPanel().setDownloadZoomLevel(getOutputZoomLevelArray());

        String altTileServer = getAltTileServer();
        if (altTileServer == null || altTileServer.length() == 0)
        {
            getInputPanel().setTileServerBaseUrl(getTileProvider().getTileServerUrl());
        }
        else
        {
            getInputPanel().setTileServerBaseUrl(altTileServer);
        }
        getInputPanel().setOutputLocation(getOutputfolder());
        getInputPanel().updateAll();
    }

    /**
     * @param property
     * @return int[]
     */
    private int[] getOutputZoomLevelArray()
    {
        LinkedList<Integer> zoomLevels = new LinkedList<Integer>();
        if (_textOutputZoomLevels.getText().isEmpty())
        {
            try
            {
                int selectedZoom = Integer.parseInt(_comboOutputZoomLevel.getSelectedItem().toString());
                if (selectedZoom <= getSelectedTileProvider().getMaxZoom() && selectedZoom >= getSelectedTileProvider().getMinZoom())
                {
                    zoomLevels.add(selectedZoom);
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("could not parse");
            }
        }
        else
        {
            for (String zoomLevel : Arrays.asList(_textOutputZoomLevels.getText().split(",")))
            {
                int selectedZoom = Integer.parseInt(zoomLevel.trim());
                if (selectedZoom < getSelectedTileProvider().getMaxZoom() && selectedZoom > getSelectedTileProvider().getMinZoom())
                {
                    zoomLevels.add(selectedZoom);
                }
            }
        }
        int[] parsedLevels = new int[zoomLevels.size()];
        for (int i = 0; i < zoomLevels.size(); i++)
        {
            parsedLevels[i] = zoomLevels.get(i);
        }
        return parsedLevels;
    }

    /**
     * @return {@link UrlSquarePanel}
     */
    public final UrlSquarePanel getUrlSquarePanel()
    {
        return _urlSquarePanel;
    }

    /** {@link BBoxLatLonPanel}
     * @return
     */
    public final BBoxLatLonPanel getBBoxLatLonPanel()
    {
        return _bBoxLatLonPanel;
    }

    /** {@link BBoxXYPanel}
     * @return
     */
    public final BBoxXYPanel getBBoxXYPanel()
    {
        return _bBoxXYPanel;
    }

    public final GPXPanel getGPXPanel()
    {
        return _gpxPanel;
    }

    class MainViewFocusListener
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

    class MainViewItemListener
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

    class MainViewActionListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            System.out.println("button pressed -> " + actionCommand);

            if (actionCommand.equalsIgnoreCase(COMMAND_DOWNLOAD))
            {
                if (!preCheckDoDownload())
                {
                    return;
                }

                _mainView.updateActualDownloadConfig();
                _mainView.updateAppConfig();

                if (getInputPanel().getTileList().getTileListToDownload().size() > 0)
                {
                    TileListDownloader tld = new TileListDownloader(_textOutputFolder.getText(), getInputPanel().getTileList(), _mainView.getMainPanel().getSelectedTileProvider());
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

        /**
         * 
         */
        private boolean preCheckDoDownload()
        {
            switch (_mainView.getInputTabSelectedIndex())
            {
                case Constants.TYPE_URLSQUARE:
                    if (getUrlSquarePanel().getPasteUrl() == null || getUrlSquarePanel().getPasteUrl().length() == 0)
                    {
                        JOptionPane.showMessageDialog(_mainView, "Please enter a URL in the input field Paste URL!", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    break;

                default:
                    break;
            }

            valuesChanged();

            return true;
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
     * @return
     */
    public String getOutputZoomLevel()
    {
        String selectedItem = (String) _comboOutputZoomLevel.getSelectedItem();
        return selectedItem.trim();
    }

    /**
     * @return
     */
    public String getAltTileServer()
    {
        return _textAltTileServer.getText().trim();
    }

    /**
     * Returns the selected tile server
     * @return selected tile server
     */
    public TileProviderIf getSelectedTileProvider()
    {
        TileProviderIf provider = getTileProvider();
        if (!getAltTileServer().isEmpty())
        {
            provider = new GenericTileProvider(getAltTileServer());
        }
        return provider;
    }

    /**
     * @return
     */
    public TileProviderIf getTileProvider()
    {
        return _tileProviders[_comboTileServer.getSelectedIndex()];
    }

    /**
     * Getter for urlSquarePanel
     * @return the urlSquarePanel
     */
    public final InputPanel getInputPanel()
    {
        switch (_mainView.getInputTabSelectedIndex())
        {
            case Constants.TYPE_URLSQUARE:
                return getUrlSquarePanel();
            case Constants.TYPE_BOUNDINGBOX_LATLON:
                return getBBoxLatLonPanel();
            case Constants.TYPE_BOUNDINGBOX_XY:
                return getBBoxXYPanel();
            case Constants.TYPE_GPX:
                return getGPXPanel();

            default:
                return null;
        }
    }

    /**
     * Getter for textNumberOfTiles
     * @return the textNumberOfTiles
     */
    public final JTextField getTextNumberOfTiles()
    {
        return _textNumberOfTiles;
    }

    /**
     * Getter for textOutputFolder
     * @return the textOutputFolder
     */
    public final JTextField getTextOutputFolder()
    {
        return _textOutputFolder;
    }

    /**
     * Getter for inputTabbedPane
     * @return the inputTabbedPane
     */
    public final JTabbedPane getInputTabbedPane()
    {
        return _inputTabbedPane;
    }
}
