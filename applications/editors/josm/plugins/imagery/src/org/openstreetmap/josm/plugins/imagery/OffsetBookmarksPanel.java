package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;


public class OffsetBookmarksPanel extends JPanel {
    List<OffsetBookmark> bookmarks = OffsetBookmark.allBookmarks;
    OffsetsBookmarksModel model = new OffsetsBookmarksModel();

    public OffsetBookmarksPanel(final PreferenceTabbedPane gui) {
        super(new GridBagLayout());
        final JTable list = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return model.getValueAt(rowAtPoint(p), columnAtPoint(p)).toString();
            }
        };
        JScrollPane scroll = new JScrollPane(list);
        add(scroll, GBC.eol().fill(GridBagConstraints.BOTH));
        scroll.setPreferredSize(new Dimension(200, 200));

        TableColumnModel mod = list.getColumnModel();
        mod.getColumn(0).setPreferredWidth(150);
        mod.getColumn(1).setPreferredWidth(200);
        mod.getColumn(2).setPreferredWidth(300);
        mod.getColumn(3).setPreferredWidth(150);
        mod.getColumn(4).setPreferredWidth(150);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton add = new JButton(tr("Add"));
        buttonPanel.add(add, GBC.std().insets(0, 5, 0, 0));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OffsetBookmark b = new OffsetBookmark(Main.proj,"","",0,0);
                model.addRow(b);
            }
        });

        JButton delete = new JButton(tr("Delete"));
        buttonPanel.add(delete, GBC.std().insets(0, 5, 0, 0));
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedRow() == -1)
                    JOptionPane.showMessageDialog(gui, tr("Please select the row to delete."));
                else {
                    Integer i;
                    while ((i = list.getSelectedRow()) != -1)
                        model.removeRow(i);
                }
            }
        });

        add(buttonPanel,GBC.eol());
    }

    /**
     * The table model for imagery offsets list
     */
    class OffsetsBookmarksModel extends DefaultTableModel {
        public OffsetsBookmarksModel() {
            setColumnIdentifiers(new String[] { tr("Projection"),  tr("Layer"), tr("Name"), tr("Easting"), tr("Northing"),});
        }

        public OffsetBookmark getRow(int row) {
            return bookmarks.get(row);
        }

        public void addRow(OffsetBookmark i) {
            bookmarks.add(i);
            int p = getRowCount() - 1;
            fireTableRowsInserted(p, p);
        }

        @Override
        public void removeRow(int i) {
            bookmarks.remove(getRow(i));
            fireTableRowsDeleted(i, i);
        }

        @Override
        public int getRowCount() {
            return bookmarks.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            OffsetBookmark info = bookmarks.get(row);
            switch (column) {
            case 0:
                return info.proj.toString();
            case 1:
                return info.layerName;
            case 2:
                return info.name;
            case 3:
                return info.dx;
            case 4:
                return info.dy;
            default:
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
            OffsetBookmark info = bookmarks.get(row);
            switch (column) {
            case 1:
                info.layerName = o.toString();
                break;
            case 2:
                info.name = o.toString();
                break;
            case 3:
                info.dx = Double.parseDouble((String) o);;
                break;
            case 4:
                info.dy = Double.parseDouble((String) o);;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= 1;
        }
    }
}
