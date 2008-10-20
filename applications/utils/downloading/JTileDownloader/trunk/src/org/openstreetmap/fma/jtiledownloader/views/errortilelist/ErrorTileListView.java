package org.openstreetmap.fma.jtiledownloader.views.errortilelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class ErrorTileListView
    extends JDialog
{

    private static final String[] COL_HEADS = new String[] {"No", "Tile", "Error" };
    private static final int[] COL_SIZE = new int[] {30, 300, 80 };

    private static final int VIEW_SIZE_X = 550;
    private static final int VIEW_SIZE_Y = 480;
    private static final String RETRY = "RETRY";
    private static final String CLOSE = "CLOSE";
    private static final long serialVersionUID = 1L;
    private final Vector _errorTileList; // containing TileDownloadError

    private JTable _errorTable;
    private JButton _close;
    private JButton _retry;

    private int _exitCode = CODE_CLOSE;
    public static final int CODE_CLOSE = 0;
    public static final int CODE_RETRY = 1;

    /**
     * @param owner
     * @param title
     * @throws HeadlessException
     */
    public ErrorTileListView(Frame owner, Vector errorTileList)
        throws HeadlessException
    {
        super(owner, "ErrorTileListView", true);
        _errorTileList = errorTileList;

        setPreferredSize(new Dimension(VIEW_SIZE_X, VIEW_SIZE_Y));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initTable();

        _close = new JButton("Close");
        _close.setPreferredSize(new Dimension(100, 25));
        _close.setActionCommand(CLOSE);
        _close.addActionListener(new MyActionListener());

        _retry = new JButton("Retry");
        _retry.setPreferredSize(new Dimension(100, 25));
        _retry.setActionCommand(RETRY);
        _retry.addActionListener(new MyActionListener());

        initializeView();

    }

    /**
     * 
     */
    private void initTable()
    {
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        for (int i = 0; i < COL_HEADS.length; ++i)
        {
            TableColumn col = new TableColumn(i, COL_SIZE[i]);
            col.setHeaderValue(COL_HEADS[i]);
            cm.addColumn(col);
        }

        TableModel tm = new ErrorTileListViewTableModel(_errorTileList);

        _errorTable = new JTable(tm, cm);
        _errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    }

    /**
     * 
     */
    private void initializeView()
    {
        JScrollPane scrollPane = new JScrollPane(_errorTable);
        scrollPane.setPreferredSize(new Dimension(VIEW_SIZE_X - 20, VIEW_SIZE_Y - 90));
        scrollPane.getViewport().add(_errorTable, BorderLayout.CENTER);
        add(scrollPane);

        JPanel panelButtons = new JPanel();
        panelButtons.add(_retry);
        panelButtons.add(_close);

        setLayout(new FlowLayout());
        add(panelButtons);

        pack();
    }

    /**
     * Getter for errorTileList
     * @return the errorTileList
     */
    public Vector getErrorTileList()
    {
        return _errorTileList;
    }

    /**
     * Setter for exitCode
     * @param exitCode the exitCode to set
     */
    public void setExitCode(int exitCode)
    {
        _exitCode = exitCode;
    }

    /**
     * Getter for exitCode
     * @return the exitCode
     */
    public int getExitCode()
    {
        return _exitCode;
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

            if (actionCommand.equalsIgnoreCase(CLOSE))
            {
                setExitCode(CODE_CLOSE);
            }
            if (actionCommand.equalsIgnoreCase(RETRY))
            {
                setExitCode(CODE_RETRY);
            }
            _close.removeActionListener(this);
            _retry.removeActionListener(this);
            dispose();
        }
    }

}
