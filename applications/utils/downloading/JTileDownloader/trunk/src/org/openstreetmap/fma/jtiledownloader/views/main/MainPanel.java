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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.openstreetmap.fma.jtiledownloader.TileListExporter;
import org.openstreetmap.fma.jtiledownloader.TileServerList;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileServer;
import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSquare;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class MainPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    private TileListSquare _tileListSquare = new TileListSquare();

    private static final String COMPONENT_OUTPUT_ZOOM_LEVEL = "outputZoomLevel";
    private static final String COMPONENT_PASTE_URL = "pasteURL";
    private static final String COMPONENT_RADIUS = "radius";

    private static final String COMMAND_SELECTOUTPUTFOLDER = "selectOutputFolder";
    private static final String COMMAND_DOWNLOAD = "download";
    private static final String COMMAND_EXPORT = "export";

    JLabel _labelPasteUrl = new JLabel("Paste URL:");
    JTextField _textPasteUrl = new JTextField();

    JLabel _labelLatitude = new JLabel("Latitude:");
    JTextField _textLatitude = new JTextField();
    JLabel _labelLongitude = new JLabel("Longitude:");
    JTextField _textLongitude = new JTextField();
    JLabel _labelRadius = new JLabel("Radius (km):");
    JTextField _textRadius = new JTextField();

    JLabel _labelOutputZoomLevel = new JLabel("Output Zoom Level:");
    JComboBox _comboOutputZoomLevel = new JComboBox();
    JComboBox _comboTileServer = new JComboBox();

    JLabel _labelAltTileServer = new JLabel("Alt. Tileserver:");
    JTextField _textAltTileServer = new JTextField();

    JLabel _labelOutputFolder = new JLabel("Outputfolder:");
    JTextField _textOutputFolder = new JTextField();
    JButton _buttonSelectOutputFolder = new JButton("...");

    JLabel _labelNumberOfTiles = new JLabel("Number Tiles:");
    JTextField _textNumberOfTiles = new JTextField("---");

    JProgressBar _progressBar = new JProgressBar();
    JButton _buttonDownload = new JButton("Download Tiles");
    JButton _buttonExport = new JButton("Export Tilelist");

    private final JTileDownloaderMainView _mainView;
    private final DownloadConfigurationUrlSquare _downloadTemplate;

    private TileServer[] _tileServers;

    /**
     * @param downloadTemplate 
     * 
     */
    public MainPanel(DownloadConfigurationUrlSquare downloadTemplate, JTileDownloaderMainView mainView)
    {
        super();

        _downloadTemplate = downloadTemplate;
        _mainView = mainView;

        _tileServers = new TileServerList().getTileServerList();

        createMainPanel();
        initializeMainPanel();

    }

    /**
     * 
     */
    private void initializeMainPanel()
    {
        _buttonSelectOutputFolder.addActionListener(new MainViewActionListener());
        _buttonSelectOutputFolder.setActionCommand(COMMAND_SELECTOUTPUTFOLDER);
        _buttonSelectOutputFolder.setPreferredSize(new Dimension(25, 19));

        _buttonDownload.addActionListener(new MainViewActionListener());
        _buttonDownload.setActionCommand(COMMAND_DOWNLOAD);

        _buttonExport.addActionListener(new MainViewActionListener());
        _buttonExport.setActionCommand(COMMAND_EXPORT);

        _textPasteUrl.setPreferredSize(new Dimension(330, 20));
        _textPasteUrl.addFocusListener(new MainViewFocusListener());
        _textPasteUrl.setName(COMPONENT_PASTE_URL);
        _textPasteUrl.setText(_downloadTemplate.getPasteUrl());

        _textLatitude.setEditable(false);
        _textLatitude.setFocusable(false);
        _textLongitude.setEditable(false);
        _textLongitude.setFocusable(false);

        _textRadius.setText("" + _downloadTemplate.getRadius());
        _textRadius.setName(COMPONENT_RADIUS);
        _textRadius.addFocusListener(new MainViewFocusListener());
        _tileListSquare.setRadius(Integer.parseInt(_textRadius.getText()) * 1000);

        _comboOutputZoomLevel.setName(COMPONENT_OUTPUT_ZOOM_LEVEL);
        for (int outputZoomLevel = 0; outputZoomLevel <= 18; outputZoomLevel++)
        {
            _comboOutputZoomLevel.addItem("" + outputZoomLevel);
        }
        _comboOutputZoomLevel.setSelectedItem("" + _downloadTemplate.getOutputZoomLevel());
        _comboOutputZoomLevel.addFocusListener(new MainViewFocusListener());
        _comboOutputZoomLevel.addItemListener(new MainViewItemListener());

        _tileListSquare.setDownloadZoomLevel(Integer.parseInt("" + _comboOutputZoomLevel.getSelectedItem()));

        int foundTileServerIndex = -1;

        for (int index = 0; index < _tileServers.length; index++)
        {
            _comboTileServer.addItem(_tileServers[index].getTileServerName());
            if (_tileServers[index].getTileServerUrl().equals(_downloadTemplate.getTileServer()))
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
            _textAltTileServer.setText(_downloadTemplate.getTileServer());
        }
        _tileListSquare.setTileServerBaseUrl("" + _downloadTemplate.getTileServer());

        _textOutputFolder.setText(_downloadTemplate.getOutputLocation());

        _textNumberOfTiles.setEditable(false);
        _textNumberOfTiles.setFocusable(false);

        _progressBar.setPreferredSize(new Dimension(300, 20));

        parsePasteUrl();
        _tileListSquare.calculateTileValuesXY();
        updateNumberOfTiles();
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
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_labelOutputZoomLevel, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_comboOutputZoomLevel, constraints);

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

        add(_progressBar, constraints);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        add(_buttonDownload, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(_buttonExport, constraints);

        constraints.weighty = 1.0;
        add(new JPanel(), constraints);

        //        return panel;
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

    private void updateNumberOfTiles()
    {
        long numberOfTiles = 0;
        numberOfTiles = getNumberOfTilesToDownload();
        _textNumberOfTiles.setText("" + numberOfTiles);
    }

    /**
     * @return
     */
    private int getNumberOfTilesToDownload()
    {
        return Integer.parseInt("" + (Math.abs(_tileListSquare.getXBottomRight() - _tileListSquare.getXTopLeft()) + 1) * (Math.abs(_tileListSquare.getYBottomRight() - _tileListSquare.getYTopLeft()) + 1));
    }

    /**
     * 
     */
    private void updateTileListSquare()
    {
        _tileListSquare.calculateTileValuesXY();
        updateNumberOfTiles();

        if (_textAltTileServer.getText() != null && _textAltTileServer.getText().length() > 0)
        {
            _tileListSquare.setTileServerBaseUrl(_textAltTileServer.getText());
        }
        else
        {
            _tileListSquare.setTileServerBaseUrl("" + getTileServer());
        }
    }

    /**
     * Getter for progressBar
     * @return the progressBar
     */
    protected final JProgressBar getProgressBar()
    {
        return _progressBar;
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

            if (componentName.equalsIgnoreCase(COMPONENT_PASTE_URL))
            {
                parsePasteUrl();
                _tileListSquare.calculateTileValuesXY();
                updateNumberOfTiles();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_OUTPUT_ZOOM_LEVEL))
            {
                _tileListSquare.setDownloadZoomLevel(Integer.parseInt("" + _comboOutputZoomLevel.getSelectedItem()));
                _tileListSquare.calculateTileValuesXY();
                updateNumberOfTiles();
            }
            else if (componentName.equalsIgnoreCase(COMPONENT_RADIUS))
            {
                _tileListSquare.setRadius(Integer.parseInt("" + _textRadius.getText()) * 1000);
                _tileListSquare.calculateTileValuesXY();
                updateNumberOfTiles();
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
                _tileListSquare.setDownloadZoomLevel(Integer.parseInt("" + _comboOutputZoomLevel.getSelectedItem()));
                _tileListSquare.calculateTileValuesXY();
                updateNumberOfTiles();
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
                if (_textPasteUrl.getText() == null || _textPasteUrl.getText().length() == 0)
                {
                    JOptionPane.showMessageDialog(_mainView, "Please enter a URL in the input field Paste URL!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                updateTileListSquare();

                _mainView.updateConfigs();
                getButtonDownload().setEnabled(false);
                getButtonExport().setEnabled(false);

                _mainView.setTileListDownloader(_mainView.createTileListDownloader(_textOutputFolder.getText(), _tileListSquare));

                getProgressBar().setMinimum(0);
                getProgressBar().setMaximum(getNumberOfTilesToDownload());
                getProgressBar().setStringPainted(true);
                getProgressBar().setString("Starting download ...");

                _mainView.getTileListDownloader().setListener(_mainView);
                _mainView.getTileListDownloader().start();
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_EXPORT))
            {
                updateTileListSquare();

                TileListExporter tle = new TileListExporter(_textOutputFolder.getText(), _tileListSquare.getFileListToDownload());
                tle.doExport();
                JOptionPane.showMessageDialog(_mainView, "Exported Tilelist to " + _textOutputFolder.getText() + File.separator + "export.txt", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (actionCommand.equalsIgnoreCase(COMPONENT_OUTPUT_ZOOM_LEVEL))
            {
                _tileListSquare.setDownloadZoomLevel(Integer.parseInt("" + _comboOutputZoomLevel.getSelectedItem()));
                _tileListSquare.calculateTileValuesXY();
                updateNumberOfTiles();
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
    public String getAltTileServer()
    {
        return _textAltTileServer.getText().trim();
    }

    /**
     * @return
     */
    public String getTileServer()
    {
        return _tileServers[_comboTileServer.getSelectedIndex()].getTileServerUrl();
    }
}
