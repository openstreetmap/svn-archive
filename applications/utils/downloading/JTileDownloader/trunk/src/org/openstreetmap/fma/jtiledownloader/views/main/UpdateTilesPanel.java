package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.UpdateTileList;
import org.openstreetmap.fma.jtiledownloader.datatypes.YDirectory;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSimple;
import org.openstreetmap.fma.jtiledownloader.views.errortilelist.ErrorTileListView;
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
public class UpdateTilesPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    private JTable _updateTilesTable;

    JLabel _labelFolder = new JLabel("Folder:");
    //    JTextField _textFolder = new JTextField();
    JLabel _textFolder = new JLabel();
    //    JButton _buttonSelectFolder = new JButton("...");
    JLabel _labelTileServer = new JLabel("TileServer:");
    JLabel _textTileServer = new JLabel();

    JButton _buttonSearch = new JButton("Search");
    public static final String UPDATE = "Update";
    public static final String STOP = "Stop";
    JButton _buttonUpdate = new JButton(UPDATE);

    public static final String COMMAND_SELECT_FOLDER = "selectFolder";
    public static final String COMMAND_SEARCH = "search";
    public static final String COMMAND_UPDATE = "update";

    private static final String[] COL_HEADS = new String[] { "Zoom Level", "Number of Tiles" };
    private static final int[] COL_SIZE = new int[] { 100, 290 };

    private DefaultTableColumnModel _cm;

    private JScrollPane _scrollPane;

    private Vector _updateList;

    private String _tileServer = "";
    private String _folder = "";

    private final JTileDownloaderMainView _mainView;
    private TileListDownloader _tileListDownloader;

    /**
     * 
     */
    public UpdateTilesPanel(JTileDownloaderMainView mainView)
    {
        super();
        _mainView = mainView;

        createPanel();
        initialize();
    }

    /**
     * 
     */
    private void createPanel()
    {

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 5);
        constraints.weightx = 1.0;
        setLayout(new GridBagLayout());

        JPanel panelFolder = new JPanel();
        panelFolder.setLayout(new GridBagLayout());
        GridBagConstraints constraintsFolder = new GridBagConstraints();
        constraintsFolder.insets = new Insets(5, 5, 0, 0);
        constraintsFolder.gridwidth = GridBagConstraints.RELATIVE;
        constraintsFolder.weightx = 1;
        constraintsFolder.fill = GridBagConstraints.HORIZONTAL;
        panelFolder.add(_labelFolder, constraintsFolder);
        constraintsFolder.gridwidth = GridBagConstraints.REMAINDER;
        panelFolder.add(_textFolder, constraintsFolder);
        constraintsFolder.gridwidth = GridBagConstraints.RELATIVE;
        panelFolder.add(_labelTileServer, constraintsFolder);
        constraintsFolder.gridwidth = GridBagConstraints.REMAINDER;
        panelFolder.add(_textTileServer, constraintsFolder);
        //        constraintsFolder.weightx = 0.01;
        constraintsFolder.insets = new Insets(5, 0, 0, 5);
        //        panelFolder.add(_buttonSelectFolder, constraintsFolder);
        add(panelFolder, constraints);

        JPanel panelUpdate = new JPanel();
        panelUpdate.setLayout(new GridBagLayout());
        GridBagConstraints constraintsUpdate = new GridBagConstraints();
        constraintsUpdate.insets = new Insets(5, 5, 5, 5);

        constraintsUpdate.gridwidth = GridBagConstraints.REMAINDER;
        constraintsUpdate.weightx = 1;
        constraintsUpdate.fill = GridBagConstraints.HORIZONTAL;

        initTable();
        _scrollPane = new JScrollPane(_updateTilesTable);
        _scrollPane.setPreferredSize(new Dimension(250, 220));
        _scrollPane.getViewport().add(_updateTilesTable, BorderLayout.CENTER);
        panelUpdate.add(_scrollPane, constraintsUpdate);

        constraintsUpdate.gridwidth = GridBagConstraints.RELATIVE;
        panelUpdate.add(_buttonSearch, constraintsUpdate);
        constraintsUpdate.gridwidth = GridBagConstraints.REMAINDER;
        panelUpdate.add(_buttonUpdate, constraintsUpdate);
        add(panelUpdate, constraints);

    }

    /**
     * 
     */
    private void initialize()
    {
        _textFolder.setText(getFolder());

        //        _buttonSelectFolder.addActionListener(new MyActionListener());
        //        _buttonSelectFolder.setActionCommand(COMMAND_SELECT_FOLDER);
        //        _buttonSelectFolder.setPreferredSize(new Dimension(25, 19));

        _buttonSearch.addActionListener(new MyActionListener());
        _buttonSearch.setActionCommand(COMMAND_SEARCH);
        _buttonSearch.setPreferredSize(new Dimension(100, 25));

        _buttonUpdate.addActionListener(new MyActionListener());
        _buttonUpdate.setActionCommand(COMMAND_UPDATE);
        _buttonUpdate.setPreferredSize(new Dimension(100, 25));

    }

    /**
     * 
     */
    private void initTable()
    {
        _cm = new DefaultTableColumnModel();
        for (int i = 0; i < COL_HEADS.length; ++i)
        {
            TableColumn col = new TableColumn(i, COL_SIZE[i]);
            col.setHeaderValue(COL_HEADS[i]);
            _cm.addColumn(col);
        }

        TableModel tm = new UpdateTilesTableModel(null);

        _updateTilesTable = new JTable(tm, _cm);
        _updateTilesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        //        _updateTilesTable.getModel().addTableModelListener(this);

    }

    class MyActionListener
        implements ActionListener
    {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         * {@inheritDoc}
         */
        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            System.out.println("button pressed -> " + actionCommand);

            if (actionCommand.equalsIgnoreCase(COMMAND_SELECT_FOLDER))
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File(_textFolder.getText()));
                if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Select"))
                {
                    File dir = chooser.getSelectedFile();
                    System.out.println(dir.getAbsolutePath());
                    _textFolder.setText(dir.getAbsolutePath());
                }

            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_SEARCH))
            {
                getButtonSearch().setEnabled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                doSearch();
                setCursor(Cursor.getDefaultCursor());
                getButtonSearch().setEnabled(true);
            }
            else if (actionCommand.equalsIgnoreCase(COMMAND_UPDATE))
            {
                doUpdate();
            }
        }

        /**
         * 
         */
        private void doUpdate()
        {
            int[] selectedRows = _updateTilesTable.getSelectedRows();
            if (selectedRows == null || selectedRows.length == 0)
            {
                JOptionPane.showMessageDialog(getMainView(), "Please select zoom level(s) to be updated!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TileListSimple updateList = new TileListSimple();
            for (int index = 0; index < selectedRows.length; index++)
            {
                String zoomLevel = (String) _updateTilesTable.getValueAt(selectedRows[index], 0);
                System.out.println("selected zoom level " + zoomLevel);

                for (int indexTileList = 0; indexTileList < _updateList.size(); indexTileList++)
                {
                    UpdateTileList updateTileList = (UpdateTileList) _updateList.elementAt(indexTileList);
                    if (updateTileList.getZoomLevel().equals(zoomLevel))
                    {
                        System.out.println("found updateTileList for zoom level " + zoomLevel);
                        Vector directory = updateTileList.getYDirectory();
                        for (int indexDirectoryY = 0; indexDirectoryY < directory.size(); indexDirectoryY++)
                        {
                            YDirectory yDir = (YDirectory) directory.elementAt(indexDirectoryY);
                            String[] tiles = yDir.getTiles();
                            for (int indexTiles = 0; indexTiles < tiles.length; indexTiles++)
                            {
                                updateList.addTile(getTileServer() + zoomLevel + "/" + yDir.getName() + "/" + tiles[indexTiles]);
                            }
                        }

                    }
                }
            }

            System.out.println("folder:" + getFolder());
            System.out.println("tileServer:" + getTileServer());

            // design problem: AppConfiguration doesn't provide the real current config
            setTileListDownloader(new TileListDownloader(getFolder(), updateList));

            ProgressBar pg = new ProgressBar(1, getTileListDownloader());
        }

        /**
         * 
         */
        private void doSearch()
        {
            File file = new File(getFolder());

            if (file == null)
            {
                return;
            }

            if (!file.isDirectory())
            {
                return;
            }

            _updateList = new Vector();

            File[] zoomLevels = file.listFiles();

            if (zoomLevels == null || zoomLevels.length == 0)
            {
                JOptionPane.showMessageDialog(getMainView(), "No files found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int zoomLevelCount = zoomLevels.length;
            for (int indexZoomLevel = 0; indexZoomLevel < zoomLevelCount; indexZoomLevel++)
            {
                File zoomLevel = zoomLevels[indexZoomLevel];

                if (zoomLevel != null)
                {
                    UpdateTileList tileList = new UpdateTileList();
                    tileList.setZoomLevel(zoomLevel.getName());

                    File[] yDirs = zoomLevel.listFiles();
                    if (yDirs != null)
                    {
                        YDirectory yDirectory;
                        for (int indexY = 0; indexY < yDirs.length; indexY++)
                        {
                            String[] strTiles;
                            File yDir = yDirs[indexY];
                            yDirectory = new YDirectory();
                            yDirectory.setName(yDir.getName());
                            File[] tiles = yDir.listFiles();
                            if (tiles != null)
                            {
                                strTiles = new String[tiles.length];
                                for (int tileIndex = 0; tileIndex < tiles.length; tileIndex++)
                                {
                                    File tile = tiles[tileIndex];
                                    strTiles[tileIndex] = tile.getName();
                                    String subPath = zoomLevel.getName() + File.separator + yDir.getName() + File.separator + tile.getName();
                                    System.out.println("found tile to update: '" + subPath + "'");
                                }
                                yDirectory.setTiles(strTiles);
                            }
                            tileList.addYDirectory(yDirectory);
                        }
                    }

                    _updateList.add(tileList);
                }
            }

            updateTable(_updateList);
        }

        /**
         * @param updateList
         */
        private void updateTable(Vector updateList)
        {
            if (_updateList != null)
            {
                for (int index = 0; index < _updateList.size(); index++)
                {
                    UpdateTileList list = (UpdateTileList) _updateList.elementAt(index);
                    System.out.println("zoom level = " + list.getZoomLevel() + " count = " + list.getFileCount());
                }

                TableModel tm = new UpdateTilesTableModel(_updateList);
                _updateTilesTable.setModel(tm);
            }

        }

    }

    //    /**
    //     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
    //     * {@inheritDoc}
    //     */
    //    public void tableChanged(TableModelEvent e)
    //    {
    //    }

    /**
     * Getter for tileServer
     * @return the tileServer
     */
    public final String getTileServer()
    {
        return _tileServer;
    }

    /**
     * Setter for tileServer
     * @param tileServer the tileServer to set
     */
    public final void setTileServer(String tileServer)
    {
        _tileServer = tileServer.trim();
        _textTileServer.setText(_tileServer);
    }

    public String getFolder()
    {
        return _folder;
    }

    /**
     * Setter for folder
     * @param folder the folder to set
     */
    public final void setFolder(String folder)
    {
        _folder = folder.trim();
        _textFolder.setText(_folder);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int, java.util.Vector)
     * {@inheritDoc}
     */
    public void downloadComplete(int errorCount, Vector errorTileList)
    {
        getButtonSearch().setEnabled(true);
        getButtonUpdate().setText(UPDATE);
        getButtonUpdate().setActionCommand(COMMAND_UPDATE);

        getTileListDownloader().setListener(null);
        setTileListDownloader(null);

        if (errorTileList != null && errorTileList.size() > 0)
        {
            // TODO: show List of failed tiles
            ErrorTileListView view = new ErrorTileListView(errorTileList);
            view.setVisible(true);
            int exitCode = view.getExitCode();
            view = null;

            if (exitCode == ErrorTileListView.CODE_RETRY)
            {
                TileListSimple tiles = new TileListSimple();
                for (Enumeration enumeration = errorTileList.elements(); enumeration.hasMoreElements();)
                {
                    TileDownloadError tde = (TileDownloadError) enumeration.nextElement();
                    tiles.addTile(tde.getTile());
                }

                setTileListDownloader(getMainView().createTileListDownloader(getFolder(), tiles));

                getTileListDownloader().start();

            }

        }

    }

    public UpdateTilesPanel getInstance()
    {
        return this;
    }

    /**
     * Getter for mainView
     * @return the mainView
     */
    public JTileDownloaderMainView getMainView()
    {
        return _mainView;
    }

    /**
     * Setter for tileListDownloader
     * @param tileListDownloader the tileListDownloader to set
     */
    public void setTileListDownloader(TileListDownloader tileListDownloader)
    {
        _tileListDownloader = tileListDownloader;
    }

    /**
     * Getter for tileListDownloader
     * @return the tileListDownloader
     */
    public TileListDownloader getTileListDownloader()
    {
        return _tileListDownloader;
    }

    /**
     * Getter for buttonSearch
     * @return the buttonSearch
     */
    public final JButton getButtonSearch()
    {
        return _buttonSearch;
    }

    /**
     * Getter for buttonUpdate
     * @return the buttonUpdate
     */
    public final JButton getButtonUpdate()
    {
        return _buttonUpdate;
    }
}
