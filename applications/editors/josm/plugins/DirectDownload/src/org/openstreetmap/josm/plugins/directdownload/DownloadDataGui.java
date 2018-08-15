// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;

public class DownloadDataGui extends ExtendedDialog {

    private NamedResultTableModel model;
    private NamedResultTableColumnModel columnmodel;
    private JTable tblSearchResults;

    public DownloadDataGui() {
        // Initalizes ExtendedDialog
        super(MainApplication.getMainFrame(),
          tr("Download Track"),
          new String[] {tr("Download Track"), tr("Cancel")},
          true
          );

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        model = new NamedResultTableModel(selectionModel);
        columnmodel = new NamedResultTableColumnModel();
        tblSearchResults = new JTable(model, columnmodel);
        tblSearchResults.setSelectionModel(selectionModel);
        tblSearchResults.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tblSearchResults);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

    model.setData(new UserTrackReader().getTrackList());

    setContent(panel);
    setupDialog();
    }

    static class NamedResultTableModel extends DefaultTableModel {
        private ArrayList<UserTrack> data;
        private ListSelectionModel selectionModel;

        NamedResultTableModel(ListSelectionModel selectionModel) {
            data = new ArrayList<>();
            this.selectionModel = selectionModel;
        }

        @Override
        public int getRowCount() {
            if (data == null) return 0;
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (data == null) return null;
            return data.get(row);
        }

        public void setData(List<UserTrack> data) {
            if (data == null) {
                this.data.clear();
            } else {
                this.data = new ArrayList<>(data);
            }
            fireTableDataChanged();
        }

        public ArrayList<UserTrack> getDataArrayList() {
            return data;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public UserTrack getSelectedUserTrack() {
            if (selectionModel.getMinSelectionIndex() < 0)
                return null;
            return data.get(selectionModel.getMinSelectionIndex());
        }
    }

    public ArrayList<UserTrack> getSelectedUserTracks() {
        ArrayList<UserTrack> DataArray = model.getDataArrayList();
        int[] selected = tblSearchResults.getSelectedRows();
        ArrayList<UserTrack> selectedTracks = new ArrayList<>(selected.length);
        for (int i = 0; i < selected.length; i++) {
            selectedTracks.add(DataArray.get(selected[i]));
        }
        return selectedTracks;
    }

    public UserTrack getSelectedUserTrack() {
        return model.getSelectedUserTrack();
    }

    static class NamedResultTableColumnModel extends DefaultTableColumnModel {
        protected void createColumns() {
            TableColumn col = null;
            NamedResultCellRenderer renderer = new NamedResultCellRenderer();

            // column 0 - DateTime
            col = new TableColumn(0);
            col.setHeaderValue(tr("Date"));
            col.setResizable(true);
            col.setPreferredWidth(150);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 1 - Filename
            col = new TableColumn(1);
            col.setHeaderValue(tr("Filename"));
            col.setResizable(true);
            col.setPreferredWidth(200);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 2 - Description
            col = new TableColumn(2);
            col.setHeaderValue(tr("Description"));
            col.setResizable(true);
            col.setPreferredWidth(450);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 3 - tags

            col = new TableColumn(3);
            col.setHeaderValue(tr("Tags"));
            col.setResizable(true);
            col.setPreferredWidth(100);
            col.setCellRenderer(renderer);
            addColumn(col);

        }

        NamedResultTableColumnModel() {
            createColumns();
        }
    }

    static class NamedResultCellRenderer extends JLabel implements TableCellRenderer {

        NamedResultCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        protected void reset() {
            setText("");
            setIcon(null);
        }

        protected void renderColor(boolean selected) {
            if (selected) {
                setForeground(UIManager.getColor("Table.selectionForeground"));
                setBackground(UIManager.getColor("Table.selectionBackground"));
            } else {
                setForeground(UIManager.getColor("Table.foreground"));
                setBackground(UIManager.getColor("Table.background"));
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            reset();
            renderColor(isSelected);

            if (value == null) return this;
            UserTrack sr = (UserTrack) value;
            switch(column) {
            case 0:
                setText(sr.datetime);
                break;
            case 1:
                setText(sr.filename);
                break;
            case 2:
                setText(sr.description);
                break;

            case 3:
                setText(sr.tags);
                break;

            }
            return this;
        }
    }
}
