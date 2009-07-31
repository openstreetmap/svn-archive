package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Region;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ViToCi;

/**
 * Dialog for selecting the current loaction.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class LocationSelector extends ExtendedDialog {

    protected ElementWithStreets selectedElement;
    protected ArrayList<ItemListener> listeners = new ArrayList<ItemListener>();


    public static ElementWithStreets selectLocation() {
        LocationSelector ls = new LocationSelector();
        ls.setVisible(true);

        if (ls.getValue() == 1)
            return ls.selectedElement;
        else
            return null;
    }

    private LocationSelector() {
        super(Main.parent, "Výběr umístění",
                            new String[] { "OK", "Zrušit"}, true);

        initComponents();
        setupDialog(mainPanel, new String[] { "ok.png", "cancel.png"});

        oblastComboBox.setRenderer(new AddressElementRenderer());
        vitociComboBox.setRenderer(new AddressElementRenderer());
        suburbComboBox.setRenderer(new SuburbRenderer());

        oblastComboBox.setModel(new DefaultComboBoxModel(
                Database.getInstance().regions.toArray()));

        try {
            autodetectLocation();
        } catch (Exception e) {}
        oblastComboBoxItemStateChanged(null);
    }

    /**
     * Hardly ever working method for autodetecting the current location.
     *
     * @deprecated
     */
    @Deprecated
	private void autodetectLocation() {
        boolean assertions = false;
        assert  assertions = true;

        OsmPrimitive bestFit = null;
        double bestLen = 0;

        // TODO: center se počítá jako střed stažené oblasti. Měl by to však
        // být střed obrazovky... Jen vědět, jak získat souřadnici středu
        // obrazovky.

        BoundingXYVisitor visitor = new BoundingXYVisitor();
        for (OsmPrimitive op : Main.ds.allPrimitives()) {
            if (op instanceof Node) {
                ((Node) op).visit(visitor);
            } else if (op instanceof Way) {
                ((Way) op).visit(visitor);
            }
        }

        LatLon center;

        try {
            center = Main.proj.eastNorth2latlon(visitor.getBounds().getCenter());
        } catch (Exception e) {
            System.err.println("AUTO: No bounds to determine autolocation.");
            return;
        }

        if (assertions)
            System.out.println("AUTO: Center is " + center);

        for (OsmPrimitive op : Main.ds.allPrimitives()) {

            if (!(op instanceof Node)) {
                continue;
            }
            Node node = (Node) op;

            double multiplicator = 5;
            if (new String("city").equals(op.get("place"))) {
                multiplicator = 2.8;
            } else if (new String("town").equals(op.get("place"))) {
                multiplicator = 2.3;
            } else if (new String("village").equals(op.get("place"))) {
                multiplicator = 2;
            } else if (new String("suburb").equals(op.get("place"))) {
                multiplicator = 1;
            } else {
                continue;
            }

            double currLen = multiplicator * (node.getCoor().distance(center));


            if ((bestFit == null) || (currLen < bestLen)) {
                bestFit = op;
                bestLen = currLen;
            }
        }

        if (bestFit != null) {

            if (assertions)
                System.out.println("AUTO: Best fit " + bestFit.getName()
                                 + "\t " + bestFit.get("name"));

            for (Region oblast : Database.getInstance().regions) {
                for (ViToCi obec : oblast.getViToCis()) {
                    if (!bestFit.get("place").equals("suburb")) {
                        if (obec.getName().toUpperCase().equals(bestFit.get("name").toUpperCase())) {
                            oblastComboBox.setSelectedItem(oblast);
                            vitociComboBox.setSelectedItem(obec);
                            for (Suburb castObce : obec.getSuburbs()) {
                                if (castObce.getName().toUpperCase().equals(bestFit.get("name").toUpperCase())) {
                                    suburbComboBox.setSelectedItem(castObce);
                                    break;
                                }
                            }
                            break;
                        }
                    } else {
                        for (Suburb castObce : obec.getSuburbs()) {
                            if (castObce.getName().toUpperCase().equals(bestFit.get("name").toUpperCase())) {
                                oblastComboBox.setSelectedItem(oblast);
                                vitociComboBox.setSelectedItem(obec);
                                suburbComboBox.setSelectedItem(castObce);
                                break;
                            }
                        }
                    }
                }
            }
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
        oblastComboBox = new javax.swing.JComboBox();
        suburbComboBox = new javax.swing.JComboBox();
        vitociComboBox = new javax.swing.JComboBox();
        obecLabel = new javax.swing.JLabel();
        castObceLabel = new javax.swing.JLabel();
        oblastLabel = new javax.swing.JLabel();

        setTitle("Výběr umístění");
        setModal(true);
        setName("locationSelector"); // NOI18N
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        oblastComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                oblastComboBoxItemStateChanged(evt);
            }
        });

        suburbComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                suburbComboBoxItemStateChanged(evt);
            }
        });

        vitociComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                vitociComboBoxItemStateChanged(evt);
            }
        });

        obecLabel.setText("Obec:");

        castObceLabel.setText("Část obce:");

        oblastLabel.setText("ORP:");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(oblastLabel)
                    .addComponent(obecLabel)
                    .addComponent(castObceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(suburbComboBox, 0, 341, Short.MAX_VALUE)
                    .addComponent(vitociComboBox, 0, 341, Short.MAX_VALUE)
                    .addComponent(oblastComboBox, 0, 341, Short.MAX_VALUE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oblastLabel)
                    .addComponent(oblastComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(obecLabel)
                    .addComponent(vitociComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(castObceLabel)
                    .addComponent(suburbComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Notifies all listeners about
     *
     *
    private void checkSetSelected(ElementWithStreets elem) {

        if (elem == null) return;

        ItemEvent event = new ItemEvent(this,
                   ItemEvent.DESELECTED, selectedElement, ItemEvent.DESELECTED);

        for (ItemListener i : listeners)
            i.itemStateChanged(event);

        selectedElement = elem;

        event = new ItemEvent(this,
                       ItemEvent.SELECTED, selectedElement, ItemEvent.SELECTED);

        for (ItemListener i : listeners)
                i.itemStateChanged(event);
    }*/

    private void oblastComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_oblastComboBoxItemStateChanged

        Region oblast = (Region) oblastComboBox.getSelectedItem();
        if (oblast == null) return;

        vitociComboBox.setModel(new DefaultComboBoxModel(oblast.getViToCis().toArray()));
        vitociComboBox.setEnabled(vitociComboBox.getModel().getSize() > 1);
        vitociComboBoxItemStateChanged(null);
    }//GEN-LAST:event_oblastComboBoxItemStateChanged

    private void vitociComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_vitociComboBoxItemStateChanged

        ViToCi obec = (ViToCi) vitociComboBox.getSelectedItem();
        if (obec == null) return;

        if (obec.getSuburbs().size() > 0) {
            Object[] suburbs = new Object[obec.getSuburbs().size() + 1];
            for (int i=0; i<obec.getSuburbs().size(); i++)
                suburbs[i] = obec.getSuburbs().get(i);
            suburbs[obec.getSuburbs().size()] = obec;
            suburbComboBox.setModel(new DefaultComboBoxModel(suburbs));
        } else
            suburbComboBox.setModel(new DefaultComboBoxModel());

        suburbComboBox.setEnabled(suburbComboBox.getModel().getSize() > 1);
        suburbComboBoxItemStateChanged(null);
    }//GEN-LAST:event_vitociComboBoxItemStateChanged

        private void suburbComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_suburbComboBoxItemStateChanged

        /*if (castObceComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) castObceComboBox.getSelectedItem());

        else if (obecComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) obecComboBox.getSelectedItem());

        else if (oblastComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) oblastComboBox.getSelectedItem());*/

        if (suburbComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) suburbComboBox.getSelectedItem());

        else if (vitociComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) vitociComboBox.getSelectedItem());

        else if (oblastComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) oblastComboBox.getSelectedItem());

        }//GEN-LAST:event_suburbComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel castObceLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel obecLabel;
    private javax.swing.JComboBox oblastComboBox;
    private javax.swing.JLabel oblastLabel;
    private javax.swing.JComboBox suburbComboBox;
    private javax.swing.JComboBox vitociComboBox;
    // End of variables declaration//GEN-END:variables

    private class AddressElementRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index,
                                                      isSelected, cellHasFocus);

            if (value instanceof AddressElement && !(value instanceof Region))
                setText(((AddressElement) value).getName());

            return c;
        }
    }

    private class SuburbRenderer extends AddressElementRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                          int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value,
                                               index, isSelected, cellHasFocus);

            if (value instanceof ViToCi) {
                setFont(getFont().deriveFont(Font.BOLD));
//                setText(((ViToCi) value).getName() + ", všechny části [experimentální]");
                setText("všechny části obce [experimentální]");
            } else
                setFont(getFont().deriveFont(Font.PLAIN));

            return c;
        }
    }
}
