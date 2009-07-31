package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Region;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ViToCi;
import org.openstreetmap.josm.plugins.czechaddress.gui.databaseeditors.EditorFactory;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.HalfCookedTreeModel;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalTreeRenderer;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Capitalizator;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Editing the database of {@link AddressElement}s.
 *
 * <p>Currently allows to display the current location + its children and
 * automatic street renaming.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class ManagerDialog extends ExtendedDialog {

    RenameModel<Street> streetModel = new RenameModel<Street>();

    public ManagerDialog() {
        super(Main.parent, "Inspektor databáze", new String[] {}, true);
        initComponents();

        dbTree.setModel(new DatabaseModel());
        dbTree.setCellRenderer(new UniversalTreeRenderer());

        dbEditButton.setIcon(ImageProvider.get("actions", "edit.png"));

        Capitalizator cap = new Capitalizator(
                                Main.ds.allPrimitives(),
                                CzechAddressPlugin.getLocation().getStreets());

        for (Street capStreet : cap.getCapitalised()) {
            assert cap.translate(capStreet).get("name") != null : capStreet;

            String elemName = capStreet.getName();
            String primName = cap.translate(capStreet).get("name");

            if (!elemName.equals(primName)) {
                streetModel.elems.add(capStreet);
                streetModel.names.add(primName);
            }
        }

        renameTable.setModel(streetModel);
        renameTable.setDefaultRenderer( AddressElement.class,
                                        new AddressElementRenderer());
        renameTable.setDefaultRenderer( String.class,
                                        new AddressElementRenderer());

        // And finalize initializing the form.
        setupDialog(mainPanel, new String[] {});
    }

    public int countAutomaticRenameProposals() {
        return streetModel.getRowCount();
    }

    private class DatabaseModel extends HalfCookedTreeModel {

        @Override
        public Object getRoot() {
            return CzechAddressPlugin.getLocation();
        }

        public Object getChild(Object parent, int index) {

            if (parent instanceof House)
                return null;

            if (parent instanceof Street)
                return ((Street) parent).getHouses().get(index);

            if (parent instanceof Suburb) {
                Suburb suburb = (Suburb) parent;

                if (index< suburb.getHouses().size())
                    return suburb.getHouses().get(index);
                else
                    index -= suburb.getHouses().size();

                if (index< suburb.getStreets().size())
                    return suburb.getStreets().get(index);
                else
                    return null;
            }

            if (parent instanceof ViToCi) {
                ViToCi vitoci = (ViToCi) parent;

                if (index< vitoci.getHouses().size())
                    return vitoci.getHouses().get(index);
                else
                    index -= vitoci.getHouses().size();

                if (index< vitoci.getStreets().size())
                    return vitoci.getStreets().get(index);
                else
                    index -= vitoci.getStreets().size();

                if (index< vitoci.getSuburbs().size())
                    return vitoci.getSuburbs().get(index);
                else
                    return null;
            }

            if (parent instanceof Region) {
                Region region = (Region) parent;

                if (index< region.getHouses().size())
                    return region.getHouses().get(index);
                else
                    index -= region.getHouses().size();

                if (index< region.getStreets().size())
                    return region.getStreets().get(index);
                else
                    index -= region.getStreets().size();

                if (index< region.getViToCis().size())
                    return region.getViToCis().get(index);
                else
                    return null;
            }

            if (parent instanceof Database)
                return ((Database) parent).regions.get(index);

            return null;
        }

        public int getChildCount(Object parent) {

            if (parent instanceof House)
                return 0;

            if (parent instanceof Street)
                return ((Street) parent).getHouses().size();

            if (parent instanceof Suburb)
                return ((Suburb) parent).getHouses().size() +
                       ((Suburb) parent).getStreets().size();

            if (parent instanceof ViToCi)
                return ((ViToCi) parent).getHouses().size() +
                       ((ViToCi) parent).getStreets().size() +
                       ((ViToCi) parent).getSuburbs().size();

            if (parent instanceof Region)
                return ((Region) parent).getHouses().size() +
                       ((Region) parent).getStreets().size() +
                       ((Region) parent).getViToCis().size();

            if (parent instanceof Database)
                return ((Database) parent).regions.size();

            return 0;
        }

        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        streetScrollPane = new javax.swing.JScrollPane();
        renameTable = new javax.swing.JTable();
        renamerButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dbTree = new javax.swing.JTree();
        dbEditButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        renameTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Původní název", "Návrh z mapy"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        renameTable.setColumnSelectionAllowed(true);
        streetScrollPane.setViewportView(renameTable);

        renamerButton.setText("Použít navržené změny");
        renamerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renamerButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(253, Short.MAX_VALUE)
                .addComponent(renamerButton)
                .addContainerGap())
            .addComponent(streetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(streetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renamerButton)
                .addContainerGap())
        );

        tabbedPane.addTab("Návrhy na přejmenování", jPanel1);

        dbTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                dbTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(dbTree);

        dbEditButton.setText("Upravit");
        dbEditButton.setEnabled(false);
        dbEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(356, Short.MAX_VALUE)
                .addComponent(dbEditButton)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbEditButton)
                .addContainerGap())
        );

        tabbedPane.addTab("Inspektor databáze", jPanel2);

        mainPanel.add(tabbedPane);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void renamerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renamerButtonActionPerformed
        assert streetModel.elems.size() == streetModel.names.size();
        Reasoner r = Reasoner.getInstance();

        synchronized (r) {
            r.openTransaction();
            for(int i=0; i<streetModel.elems.size(); i++) {
                streetModel.elems.get(i).setName(streetModel.names.get(i));
                r.update(streetModel.elems.get(i));
            }
            r.closeTransaction();
        }

        streetModel.elems.clear();
        streetModel.names.clear();
        jPanel1.setVisible(false);
    }//GEN-LAST:event_renamerButtonActionPerformed

    private AddressElement dbTreeValue = null;

    private void dbTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_dbTreeValueChanged
        try {
            dbTreeValue = (AddressElement) dbTree.getSelectionPath().getLastPathComponent();
        } catch (NullPointerException except) {
            dbTreeValue = null;
            System.err.println("Strange exception has occured."+
                " If you find a way to reproduce it, please report a bug!");
            except.printStackTrace();
        }
        dbEditButton.setEnabled( EditorFactory.isEditable(dbTreeValue) );
    }//GEN-LAST:event_dbTreeValueChanged

    private void dbEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbEditButtonActionPerformed
        if (EditorFactory.isEditable(dbTreeValue)) {
            if (EditorFactory.edit(dbTreeValue))
                dbTree.repaint();
        } else
            dbEditButton.setEnabled(false);
    }//GEN-LAST:event_dbEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dbEditButton;
    private javax.swing.JTree dbTree;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTable renameTable;
    private javax.swing.JButton renamerButton;
    private javax.swing.JScrollPane streetScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private class AddressElementRenderer extends DefaultTableCellRenderer {

        public AddressElementRenderer() {}

        @Override
        protected void setValue(Object value) {
            super.setValue(value);

            if (value instanceof AddressElement)
                setText(((AddressElement) value).getName() );
        }
    }

    private class RenameModel<Element> implements TableModel {

        List<Element> elems = new ArrayList<Element>();
        List<String>  names = new ArrayList<String>();

        public int getRowCount() {
            assert elems.size() == names.size();
            return elems.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Původní název";
            if (columnIndex == 1) return "Navržený název";
            assert false : columnIndex;
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return AddressElement.class;
            if (columnIndex == 1) return String.class;
            assert false : columnIndex;
            return null;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) return elems.get(rowIndex);
            if (columnIndex == 1) return names.get(rowIndex);
            assert false : columnIndex;
            return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 1;
            names.set(rowIndex, (String) aValue);
        }

        public void addTableModelListener(TableModelListener l) {

        }

        public void removeTableModelListener(TableModelListener l) {

        }
    }
}
