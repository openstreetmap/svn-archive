package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.datatypes.UpdateTileList;
import org.openstreetmap.fma.jtiledownloader.datatypes.YDirectory;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSimple;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class UpdateTilesPanel
    extends JPanel
    implements TableModelListener
{
    private static final long serialVersionUID = 1L;

    private JTable _updateTilesTable;

    JLabel _labelFolder = new JLabel("Folder:");
    JTextField _textFolder = new JTextField();
    JButton _buttonSelectFolder = new JButton("...");

    JButton _buttonSearch = new JButton("Search");
    JButton _buttonUpdate = new JButton("Update");

    private static final String COMMAND_SELECT_FOLDER = "selectFolder";
    private static final String COMMAND_SEARCH = "search";
    private static final String COMMAND_UPDATE = "update";

    private final String _folder;

    private static final String[] COL_HEADS = new String[] {"Zoom Lvl", "Number of Tiles" };
    private static final int[] COL_SIZE = new int[] {90, 300 };

    private DefaultTableColumnModel _cm;

    private JScrollPane _scrollPane;

    private Vector _updateList;

    /**
     * 
     */
    public UpdateTilesPanel(String folder)
    {
        super();
        _folder = folder;

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

        JPanel panelOutputFolder = new JPanel();
        panelOutputFolder.setLayout(new GridBagLayout());
        GridBagConstraints constraintsFolder = new GridBagConstraints();
        constraintsFolder.insets = new Insets(5, 5, 0, 0);
        constraintsFolder.gridwidth = GridBagConstraints.REMAINDER;
        constraintsFolder.weightx = 0.99;
        constraintsFolder.fill = GridBagConstraints.HORIZONTAL;
        panelOutputFolder.add(_labelFolder, constraintsFolder);
        constraintsFolder.gridwidth = GridBagConstraints.RELATIVE;
        panelOutputFolder.add(_textFolder, constraintsFolder);
        constraintsFolder.gridwidth = GridBagConstraints.REMAINDER;
        constraintsFolder.weightx = 0.01;
        constraintsFolder.insets = new Insets(5, 0, 0, 5);
        panelOutputFolder.add(_buttonSelectFolder, constraintsFolder);
        add(panelOutputFolder, constraints);

        JPanel panelUpdate = new JPanel();
        panelUpdate.setLayout(new GridBagLayout());
        GridBagConstraints constraintsUpdate = new GridBagConstraints();
        constraintsUpdate.insets = new Insets(5, 5, 5, 5);

        constraintsUpdate.gridwidth = GridBagConstraints.REMAINDER;
        constraintsUpdate.weightx = 1;
        constraintsUpdate.fill = GridBagConstraints.HORIZONTAL;

        initTable();
        _scrollPane = new JScrollPane(_updateTilesTable);
        _scrollPane.setPreferredSize(new Dimension(250, 250));
        _scrollPane.getViewport().add(_updateTilesTable, BorderLayout.CENTER);
        panelUpdate.add(_scrollPane, constraintsUpdate);

        //        panelUpdate.add(new JButton("Dummy"), constraintsUpdate);

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
        _textFolder.setText(_folder);

        _buttonSelectFolder.addActionListener(new MyActionListener());
        _buttonSelectFolder.setActionCommand(COMMAND_SELECT_FOLDER);
        _buttonSelectFolder.setPreferredSize(new Dimension(25, 19));

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
        _updateTilesTable.getModel().addTableModelListener(this);

    }

    public String getFolder()
    {
        return _textFolder.getText().trim();
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
                doSearch();
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
                                updateList.addTile(zoomLevel + "/" + yDir.getName() + "/" + tiles[indexTiles]);
                            }
                        }

                    }
                }
            }

            TileListDownloader tld = new TileListDownloader(getFolder(), updateList);
            //            tld.setWaitAfterTiles(getAppConfiguration().getWaitAfterNrTiles());
            //            tld.setWaitAfterTilesAmount(getAppConfiguration().getWaitNrTiles());
            //            tld.setWaitAfterTilesSeconds(getAppConfiguration().getWaitSeconds());
            tld.start();

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
            for (int indexZoomLevel = 0; indexZoomLevel < zoomLevels.length; indexZoomLevel++)
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

    /**
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     * {@inheritDoc}
     */
    public void tableChanged(TableModelEvent e)
    {
        //        e.fireTableDataChanged()

    }
}
