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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.UpdateTileList;
import org.openstreetmap.fma.jtiledownloader.datatypes.YDirectory;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSimple;
import org.openstreetmap.fma.jtiledownloader.views.progressbar.ProgressBar;

public class UpdateTilesPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(UpdateTilesPanel.class.getName());

    private JTable _updateTilesTable;

    private JLabel _labelFolder = new JLabel("Folder:");
    private JLabel _textFolder = new JLabel();
    private JLabel _labelTileServer = new JLabel("TileServer:");
    private JLabel _textTileServer = new JLabel();

    private JButton _buttonSearch = new JButton("Search");
    public static final String UPDATE = "Update";
    private JButton _buttonUpdate = new JButton(UPDATE);

    public static final String COMMAND_SEARCH = "search";
    public static final String COMMAND_UPDATE = "update";

    private static final String[] COL_HEADS = new String[] { "Zoom Level", "Number of Tiles" };
    private static final int[] COL_SIZE = new int[] { 100, 290 };

    private DefaultTableColumnModel _cm;

    private JScrollPane _scrollPane;

    private ArrayList<UpdateTileList> _updateList;

    private String _tileServer = "";
    private String _folder = "";

    private final MainPanel _mainPanel;
    private TileListDownloader _tileListDownloader;

    /**
     * @param mainPanel 
     */
    public UpdateTilesPanel(MainPanel mainPanel)
    {
        super();
        _mainPanel = mainPanel;

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
         */
        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            log.fine("button pressed -> " + actionCommand);

            if (actionCommand.equalsIgnoreCase(COMMAND_SEARCH))
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
                JOptionPane.showMessageDialog(_mainPanel, "Please select zoom level(s) to be updated!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TileListSimple updateList = new TileListSimple();
            for (int selectedRow : selectedRows)
            {
                int zoomLevel = (Integer) _updateTilesTable.getValueAt(selectedRow, 0);
                log.fine("selected zoom level " + zoomLevel);

                for (int indexTileList = 0; indexTileList < _updateList.size(); indexTileList++)
                {
                    UpdateTileList updateTileList = _updateList.get(indexTileList);
                    if (updateTileList.getZoomLevel() == zoomLevel)
                    {
                        log.fine("found updateTileList for zoom level " + zoomLevel);
                        ArrayList<YDirectory> directory = updateTileList.getYDirectory();
                        for (int indexDirectoryY = 0; indexDirectoryY < directory.size(); indexDirectoryY++)
                        {
                            YDirectory yDir = directory.get(indexDirectoryY);
                            Tile[] tiles = yDir.getTiles();
                            for (int indexTiles = 0; tiles != null && indexTiles < tiles.length; indexTiles++)
                            {
                                updateList.addTile(tiles[indexTiles]);
                            }
                        }
                    }
                }
            }

            log.fine("folder:" + getFolder());
            log.fine("tileServer:" + _tileServer);

            TileListDownloader tld = new TileListDownloader(getFolder(), updateList, _mainPanel.getSelectedTileProvider());

            new ProgressBar(1, tld).setVisible(true);
        }

        /**
         * 
         */
        private void doSearch()
        {
            File file = new File(getFolder());

            if (file == null || !file.isDirectory())
            {
                return;
            }

            _updateList = new ArrayList<UpdateTileList>();

            File[] zoomLevels = file.listFiles(new FileFilter() {
                public boolean accept(File pathname)
                {
                    if (pathname.isDirectory() == false)
                    {
                        return false;
                    }
                    try
                    {
                        if (Integer.parseInt(pathname.getName()) > 0)
                        {
                            return true;
                        }
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                    return false;
                }
            });

            if (zoomLevels == null || zoomLevels.length == 0)
            {
                JOptionPane.showMessageDialog(_mainPanel, "No zoom directories found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (File zoomLevel : zoomLevels)
            {
                if (zoomLevel != null)
                {
                    UpdateTileList tileList = new UpdateTileList();
                    tileList.setZoomLevel(Integer.parseInt(zoomLevel.getName()));

                    File[] yDirs = zoomLevel.listFiles(new FileFilter() {
                        public boolean accept(File pathname)
                        {
                            if (pathname.isDirectory() == false)
                            {
                                return false;
                            }
                            try
                            {
                                if (Integer.parseInt(pathname.getName()) > 0)
                                {
                                    return true;
                                }
                            }
                            catch (Exception e)
                            {
                                // ignore
                            }
                            return false;
                        }
                    });
                    if (yDirs != null)
                    {
                        YDirectory yDirectory;
                        for (File yDir : yDirs)
                        {
                            Tile[] theTiles;
                            yDirectory = new YDirectory();
                            yDirectory.setName(yDir.getName());
                            File[] tiles = yDir.listFiles(new FileFilter() {
                                public boolean accept(File pathname)
                                {
                                    if (pathname.isDirectory() == true)
                                    {
                                        return false;
                                    }
                                    try
                                    {
                                        if (pathname.getName().matches("[0-9]+\\.[a-z]+"))
                                        {
                                            return true;
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        // ignore
                                    }
                                    return false;
                                }
                            });
                            if (tiles != null)
                            {
                                theTiles = new Tile[tiles.length];
                                for (int tileIndex = 0; tileIndex < tiles.length; tileIndex++)
                                {
                                    File tile = tiles[tileIndex];
                                    theTiles[tileIndex] = new Tile(Integer.parseInt(yDir.getName()), Integer.parseInt(tile.getName().substring(0, tile.getName().lastIndexOf("."))), Integer.parseInt(zoomLevel.getName()));
                                    log.fine("found tile to update: '" + theTiles[tileIndex] + "'");
                                }
                                yDirectory.setTiles(theTiles);
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
        private void updateTable(ArrayList<UpdateTileList> updateList)
        {
            if (_updateList != null)
            {
                for (int index = 0; index < _updateList.size(); index++)
                {
                    UpdateTileList list = _updateList.get(index);
                    log.fine("zoom level = " + list.getZoomLevel() + " count = " + list.getFileCount());
                }

                TableModel tm = new UpdateTilesTableModel(_updateList);
                _updateTilesTable.setModel(tm);
            }

        }

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
