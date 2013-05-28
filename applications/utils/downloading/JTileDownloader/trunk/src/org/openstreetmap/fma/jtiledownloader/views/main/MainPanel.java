/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.TileListExporter;
import org.openstreetmap.fma.jtiledownloader.TileProviderList;
import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.DownloadJob;
import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxLatLonPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxXYPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.GPXPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.InputPanel;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.UrlSquarePanel;
import org.openstreetmap.fma.jtiledownloader.views.progressbar.ProgressBar;

public class MainPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(MainPanel.class.getName());

    public static final String COMPONENT_OUTPUT_ZOOM_LEVEL = "outputZoomLevel";
    public static final String COMPONENT_OUTPUT_ZOOM_LEVEL_TEXT = "outputZoomLevelText";

    public static final String COMMAND_SELECTOUTPUTFOLDER = "selectOutputFolder";
    public static final String COMMAND_DOWNLOAD = "download";
    public static final String COMMAND_LOADJOB = "loadjob";
    public static final String COMMAND_SAVEJOB = "savejob";
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

    private JButton _buttonDownload = new JButton("Download Tiles");
    private JButton _buttonSaveJob = new JButton("Save Job");
    private JButton _buttonLoadJob = new JButton("Load Job");
    private JButton _buttonExport = new JButton("Export Tilelist");

    private final JTileDownloaderMainView _mainView;

    private TileProviderIf[] _tileProviders;

    private JTabbedPane _inputTabbedPane;

    private List<InputPanel> inputPanels;

    /**
     * @param mainView reference to mainView
     * @param tabIndex tab to select at start up
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
        }
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
        _buttonSaveJob.setActionCommand(COMMAND_SAVEJOB);
        _buttonLoadJob.setActionCommand(COMMAND_LOADJOB);
        _buttonExport.setActionCommand(COMMAND_EXPORT);

        _comboOutputZoomLevel.setName(COMPONENT_OUTPUT_ZOOM_LEVEL);
        for (int outputZoomLevel = 0; outputZoomLevel <= 20; outputZoomLevel++)
        {
            _comboOutputZoomLevel.addItem(String.valueOf(outputZoomLevel));
        }
        _textOutputZoomLevels.setName(COMPONENT_OUTPUT_ZOOM_LEVEL_TEXT);

        for (TileProviderIf tileProvider : _tileProviders)
        {
            _comboTileServer.addItem(tileProvider.getName());
        }
        initializeTileServer(AppConfiguration.getInstance().getTileServer());
        initializeOutputZoomLevel(AppConfiguration.getInstance().getLastZoom());
        _textOutputFolder.setText(AppConfiguration.getInstance().getOutputFolder());

        _textNumberOfTiles.setEditable(false);
        _textNumberOfTiles.setFocusable(false);
        _textNumberOfTiles.setHorizontalAlignment(SwingConstants.RIGHT);

        // set all listeners
        _buttonSelectOutputFolder.addActionListener(new MainViewActionListener());
        _buttonDownload.addActionListener(new MainViewActionListener());
        _buttonSaveJob.addActionListener(new MainViewActionListener());
        _buttonLoadJob.addActionListener(new MainViewActionListener());
        _buttonExport.addActionListener(new MainViewActionListener());

        _comboOutputZoomLevel.addFocusListener(new MainViewFocusListener());
        _comboOutputZoomLevel.addItemListener(new MainViewItemListener());
        _textOutputZoomLevels.addFocusListener(new MainViewFocusListener());
    }

    /**
     * @param tileServer
     */
    private void initializeTileServer(String tileServer)
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

    private void initializeOutputZoomLevel(String zoomLevelsString)
    {
        int[] zoomLevels = Util.getOutputZoomLevelArray(getSelectedTileProvider(), zoomLevelsString);
        if (zoomLevels == null || zoomLevels.length == 0)
        {
            _textOutputZoomLevels.setText("");
            _comboOutputZoomLevel.setSelectedItem("12");
            return;
        }

        if (zoomLevels.length == 1)
        {
            _textOutputZoomLevels.setText("");
            _comboOutputZoomLevel.setSelectedItem(Integer.toString(zoomLevels[0]));
            return;
        }

        String textZoomLevels = Integer.toString(zoomLevels[0]);
        int rangeSize = 1;
        for (int index = 1; index < zoomLevels.length; index++)
        {
            if( zoomLevels[index] == zoomLevels[index-1] + 1 )
                rangeSize++;
            else {
                if( rangeSize > 1 ) {
                    textZoomLevels += rangeSize > 2 ? "-" : ",";
                    textZoomLevels += zoomLevels[index-1];
                }
                textZoomLevels += ",";
                textZoomLevels += zoomLevels[index];
                rangeSize = 1;
            }
        }
        if( rangeSize > 1 ) {
            textZoomLevels += rangeSize > 2 ? "-" : ",";
            textZoomLevels += zoomLevels[zoomLevels.length-1];
        }
        _textOutputZoomLevels.setText(textZoomLevels);

    }

    /**
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

        _comboTileServer.addItemListener(new MainViewItemListener());
        add(_comboTileServer, constraints);
        add(_labelAltTileServer, constraints);
        _textAltTileServer.addFocusListener(new MainViewFocusListener());
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

        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_buttonSaveJob, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_buttonLoadJob, constraints);

        constraints.weighty = 1.0;
        add(new JPanel(), constraints);

        //        return panel;
    }

    private void valuesChanged()
    {
        getInputPanel().updateAll();
    }

    /**
     * @return entered/selected zoomlevels
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
         */
        public void focusGained(FocusEvent focusevent)
        {

        }

        /**
         * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
         */
        public void focusLost(FocusEvent focusevent)
        {
            valuesChanged();
        }

    }

    private class MainViewItemListener
        implements ItemListener
    {

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            valuesChanged();
        }
    }

    private class MainViewActionListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            log.fine("button pressed -> " + actionCommand);

            if (actionCommand.equalsIgnoreCase(COMMAND_DOWNLOAD))
            {
                if (!getInputPanel().isDownloadOkay())
                {
                    return;
                }
                valuesChanged();

                if (getInputPanel().getTileList().getTileListToDownload().size() > 0)
                {
                    TileListDownloader tld = new TileListDownloader(_textOutputFolder.getText(), getInputPanel().getTileList(), getSelectedTileProvider());
                    new ProgressBar(getInputPanel().getNumberOfTilesToDownload(), tld).setVisible(true);
                }
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_LOADJOB))
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f)
                    {
                        if (f.getName().endsWith(".xml") || f.isDirectory())
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
                        return "XML-File";
                    }

                });
                if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Open config"))
                {
                    DownloadJob job = new DownloadJob(chooser.getSelectedFile().getAbsolutePath());

                    for (InputPanel inputPanel : inputPanels)
                    {
                        if (inputPanel.getJobType().equals(job.getType()))
                        {
                            inputPanel.loadConfig(job);
                        }
                    }

                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(_mainView, "Also load general settings?", "JTileDownloader", JOptionPane.YES_NO_OPTION))
                    {
                        _textOutputFolder.setText(job.getOutputLocation());
                        initializeOutputZoomLevel(job.getOutputZoomLevels());
                        initializeTileServer(job.getTileServer());
                    }

                    valuesChanged();
                    JOptionPane.showMessageDialog(_mainView, "Loaded.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_SAVEJOB))
            {
                if (!getInputPanel().isDownloadOkay())
                {
                    return;
                }
                valuesChanged();

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f)
                    {
                        if (f.getName().endsWith(".xml") || f.isDirectory())
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
                        return "XML-File";
                    }

                });
                if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Save"))
                {
                    File dir = chooser.getSelectedFile();
                    // add default extension if missing
                    if (!dir.getName().endsWith(".xml") && !chooser.accept(dir))
                    {
                        dir = new File(dir.getAbsolutePath() + ".xml");
                    }
                    if (dir.exists())
                    {
                        JOptionPane.showMessageDialog(_mainView, "File exists. Aborting...", "Info", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    DownloadJob job = new DownloadJob();
                    job.setOutputLocation(_textOutputFolder.getText());
                    job.setOutputZoomLevels(getOutputZoomLevelString());
                    job.setTileServer(getTileProvider().getName());
                    getInputPanel().saveConfig(job);
                    job.saveToFile(dir.getAbsolutePath());
                    JOptionPane.showMessageDialog(_mainView, "Saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_EXPORT))
            {
                if (!getInputPanel().isDownloadOkay())
                {
                    return;
                }
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
                    log.fine(dir.getAbsolutePath());
                    _textOutputFolder.setText(dir.getAbsolutePath());
                }
            }

        }
    }

    /**
     * @return output folder string
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
     * @return selected tile provider
     */
    private TileProviderIf getTileProvider()
    {
        return _comboTileServer.getSelectedIndex() < 0 || _comboTileServer.getSelectedIndex() >= _tileProviders.length
                ? null : _tileProviders[_comboTileServer.getSelectedIndex()];
    }

    /**
     * Getter for input panels
     * @return a inputpanel
     */
    private InputPanel getInputPanel()
    {
        return inputPanels.get(_inputTabbedPane.getSelectedIndex());
    }

    /**
     * Sets the number of tiles to download
     * @param numberOfTiles 
     */
    public void setNumberOfTiles(int numberOfTiles)
    {
        _textNumberOfTiles.setText(String.valueOf(numberOfTiles));
    }

    private class InputTabListener
        implements ChangeListener
    {

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent evt)
        {
            JTabbedPane pane = (JTabbedPane) evt.getSource();

            //select new panel & load config
            AppConfiguration.getInstance().setInputPanelIndex(pane.getSelectedIndex());
            valuesChanged();
        }
    }

    /**
     * @param outputFolder
     */
    public void setOutputFolder(String outputFolder)
    {
        _textOutputFolder.setText(outputFolder);
    }

    /**
     * Saves all Download configs
     */
    public void saveAllConfigOptions()
    {
        for (InputPanel inputPanel : inputPanels)
        {
            inputPanel.saveConfig(AppConfiguration.getInstance());
        }
    }
}
