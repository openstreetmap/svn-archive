package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.wms.AddWMSLayerPanel;
import org.openstreetmap.josm.tools.GBC;

public class ImageryProvidersPanel extends JPanel {
    final ImageryLayerTableModel model;
    private final ImageryLayerInfo layerInfo;

    public ImageryProvidersPanel(final PreferenceTabbedPane gui, ImageryLayerInfo layerInfo) {
        super(new GridBagLayout());
        this.layerInfo = layerInfo;
        this.model = new ImageryLayerTableModel();

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

        final ImageryDefaultLayerTableModel modeldef = new ImageryDefaultLayerTableModel();
        final JTable listdef = new JTable(modeldef) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return (String) modeldef.getValueAt(rowAtPoint(p), columnAtPoint(p));
            }
        };
        JScrollPane scrolldef = new JScrollPane(listdef);
        // scrolldef is added after the buttons so it's clearer the buttons
        // control the top list and not the default one
        scrolldef.setPreferredSize(new Dimension(200, 200));

        TableColumnModel mod = listdef.getColumnModel();
        mod.getColumn(1).setPreferredWidth(800);
        mod.getColumn(0).setPreferredWidth(200);
        mod = list.getColumnModel();
        mod.getColumn(2).setPreferredWidth(50);
        mod.getColumn(1).setPreferredWidth(800);
        mod.getColumn(0).setPreferredWidth(200);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton add = new JButton(tr("Add"));
        buttonPanel.add(add, GBC.std().insets(0, 5, 0, 0));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddWMSLayerPanel p = new AddWMSLayerPanel();
                int answer = JOptionPane.showConfirmDialog(
                        gui, p,
                        tr("Add Imagery URL"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                    model.addRow(new ImageryInfo(p.getUrlName(), p.getUrl()));
                }
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

        JButton copy = new JButton(tr("Copy Selected Default(s)"));
        buttonPanel.add(copy, GBC.std().insets(0, 5, 0, 0));
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] lines = listdef.getSelectedRows();
                if (lines.length == 0) {
                    JOptionPane.showMessageDialog(
                            gui,
                            tr("Please select at least one row to copy."),
                            tr("Information"),
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                outer: for (int i = 0; i < lines.length; i++) {
                    ImageryInfo info = modeldef.getRow(lines[i]);

                    // Check if an entry with exactly the same values already
                    // exists
                    for (int j = 0; j < model.getRowCount(); j++) {
                        if (info.equalsBaseValues(model.getRow(j))) {
                            // Select the already existing row so the user has
                            // some feedback in case an entry exists
                            list.getSelectionModel().setSelectionInterval(j, j);
                            list.scrollRectToVisible(list.getCellRect(j, 0, true));
                            continue outer;
                        }
                    }

                    if (info.eulaAcceptanceRequired != null) {
                        if (!confirmEulaAcceptance(gui, info.eulaAcceptanceRequired))
                            continue outer;
                    }

                    model.addRow(new ImageryInfo(info));
                    int lastLine = model.getRowCount() - 1;
                    list.getSelectionModel().setSelectionInterval(lastLine, lastLine);
                    list.scrollRectToVisible(list.getCellRect(lastLine, 0, true));
                }
            }
        });

        add(buttonPanel);
        add(Box.createHorizontalGlue(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
        // Add default item list
        add(scrolldef, GBC.eol().insets(0, 5, 0, 0).fill(GridBagConstraints.BOTH));
    }

    /**
     * The table model for imagery layer list
     */
    class ImageryLayerTableModel extends DefaultTableModel {
        public ImageryLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name"), tr("Imagery URL"), trc("layer", "Zoom") });
        }

        public ImageryInfo getRow(int row) {
            return layerInfo.layers.get(row);
        }

        public void addRow(ImageryInfo i) {
            layerInfo.add(i);
            int p = getRowCount() - 1;
            fireTableRowsInserted(p, p);
        }

        @Override
        public void removeRow(int i) {
            layerInfo.remove(getRow(i));
            fireTableRowsDeleted(i, i);
        }

        @Override
        public int getRowCount() {
            return layerInfo.layers.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            ImageryInfo info = layerInfo.layers.get(row);
            switch (column) {
            case 0:
                return info.name;
            case 1:
                return info.getFullURL();
            case 2:
                return (info.imageryType == ImageryType.WMS) ? (info.pixelPerDegree == 0.0 ? "" : info.pixelPerDegree)
                                                             : (info.maxZoom == 0 ? "" : info.maxZoom);
            default:
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
            ImageryInfo info = layerInfo.layers.get(row);
            switch (column) {
            case 0:
                info.name = (String) o;
                break;
            case 1:
                info.setURL((String)o);
                break;
            case 2:
                info.pixelPerDegree = 0;
                info.maxZoom = 0;
                try {
                    if(info.imageryType == ImageryType.WMS)
                        info.pixelPerDegree = Double.parseDouble((String) o);
                    else
                        info.maxZoom = Integer.parseInt((String) o);
                } catch (NumberFormatException e) {
                }
                break;
            default:
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    }

    /**
     * The table model for the default imagery layer list
     */
    class ImageryDefaultLayerTableModel extends DefaultTableModel {
        public ImageryDefaultLayerTableModel() {
            setColumnIdentifiers(new String[] { tr("Menu Name (Default)"), tr("Imagery URL (Default)") });
        }

        public ImageryInfo getRow(int row) {
            return layerInfo.defaultLayers.get(row);
        }

        @Override
        public int getRowCount() {
            return layerInfo.defaultLayers.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            ImageryInfo info = layerInfo.defaultLayers.get(row);
            switch (column) {
            case 0:
                return info.name;
            case 1:
                return info.getFullURL();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private boolean confirmEulaAcceptance(PreferenceTabbedPane gui, String eulaUrl) {
        URL url = null;
        try {
            url = new URL(eulaUrl.replaceAll("\\{lang\\}", Locale.getDefault().toString()));
            JEditorPane htmlPane = null;
            try {
                htmlPane = new JEditorPane(url);
            } catch (IOException e1) {
                // give a second chance with a default Locale 'en'
                try {
                    url = new URL(eulaUrl.replaceAll("\\{lang\\}", "en"));
                    htmlPane = new JEditorPane(url);
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(gui ,tr("EULA license URL not available: {0}", eulaUrl));
                    return false;
                }
            }
            Box box = Box.createVerticalBox();
            htmlPane.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(htmlPane);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            box.add(scrollPane);
            int option = JOptionPane.showConfirmDialog(Main.parent, box, tr("Please abort if you are not sure"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                return true;
            }
        } catch (MalformedURLException e2) {
            JOptionPane.showMessageDialog(gui ,tr("Malformed URL for the EULA licence: {0}", eulaUrl));
        }
        return false;
    }
}
