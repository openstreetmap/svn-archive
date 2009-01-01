/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

/**
 * @author cdaller
 *
 */
public class InstallPanel extends JPanel implements ListSelectionListener {
    private JTable table;
    private PluginTableModel pluginModel;
    private JScrollPane scrollpane;
    private JTextArea infoBox;

    /**
     * @param pluginUpdateFrame
     * @param descriptions
     */
    public InstallPanel(PluginUpdateFrame pluginUpdateFrame, List<SiteDescription> descriptions) {
        super(new BorderLayout(12,12));
        setBorder(new javax.swing.border.EmptyBorder(12,12,12,12));
        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

        /* Setup the table */
        table = new JTable(pluginModel = new PluginTableModel(descriptions));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setRowHeight(table.getRowHeight() + 2);
        table.setPreferredScrollableViewportSize(new Dimension(500,200));
        table.getSelectionModel().addListSelectionListener(this);

        TableColumn col1 = table.getColumnModel().getColumn(0);
        TableColumn col2 = table.getColumnModel().getColumn(1);
        TableColumn col3 = table.getColumnModel().getColumn(2);
//        TableColumn col4 = table.getColumnModel().getColumn(3);
//        TableColumn col5 = table.getColumnModel().getColumn(4);

        col1.setPreferredWidth(30);
        col1.setMinWidth(30);
        col1.setMaxWidth(30);
        col1.setResizable(false);

        col2.setPreferredWidth(180);
        col3.setPreferredWidth(130);
//        col4.setPreferredWidth(70);
//        col5.setPreferredWidth(70);


        scrollpane = new JScrollPane(table);
        scrollpane.getViewport().setBackground(table.getBackground());
        split.setTopComponent(scrollpane);

        /* Create description */
        JScrollPane infoPane = new JScrollPane(infoBox = new JTextArea());
        infoPane.setPreferredSize(new Dimension(500,100));
        split.setBottomComponent(infoPane);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                split.setDividerLocation(0.75);
            }
        });
        add(BorderLayout.CENTER,split);
    }

    /**
     *
     */
    public void install() {
        System.out.println("Installing selected plugins");
        pluginModel.install();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        String text = "";
        if (table.getSelectedRowCount() == 1)
        {
            PluginDescription plugin = pluginModel.getPlugins().get(table.getSelectedRow());
            text = plugin.getDescription();
        }
        infoBox.setText(text);
        infoBox.setCaretPosition(0);

    }

}
