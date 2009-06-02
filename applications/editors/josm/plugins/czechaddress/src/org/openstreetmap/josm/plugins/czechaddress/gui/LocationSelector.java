package org.openstreetmap.josm.plugins.czechaddress.gui;

import org.openstreetmap.josm.plugins.czechaddress.*;
import java.awt.Component;
import java.util.ArrayList;

import java.awt.event.ItemListener;


import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Suburb;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ViToCi;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Region;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;

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
        obecComboBox.setRenderer(new AddressElementRenderer());
        castObceComboBox.setRenderer(new AddressElementRenderer());

        oblastComboBox.setModel(new DefaultComboBoxModel(
                Database.getInstance().regions.toArray()));

        autodetectLocation();
        oblastComboBoxItemStateChanged(null);
    }

    /**
     * @deprecated 
     */
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
            Bounds bounds = visitor.getBounds();
            LatLon max = bounds.max;
            LatLon min = bounds.min;
            center = new LatLon(
                    (max.getX() + min.getX()) / 2,
                    (max.getY() + min.getY()) / 2);

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

            double currLen = multiplicator * (node.coor.distance(center));


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
                            obecComboBox.setSelectedItem(obec);
                            for (Suburb castObce : obec.getSuburbs()) {
                                if (castObce.getName().toUpperCase().equals(bestFit.get("name").toUpperCase())) {
                                    castObceComboBox.setSelectedItem(castObce);
                                    break;
                                }
                            }
                            break;
                        }
                    } else {
                        for (Suburb castObce : obec.getSuburbs()) {
                            if (castObce.getName().toUpperCase().equals(bestFit.get("name").toUpperCase())) {
                                oblastComboBox.setSelectedItem(oblast);
                                obecComboBox.setSelectedItem(obec);
                                castObceComboBox.setSelectedItem(castObce);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        oblastComboBox = new javax.swing.JComboBox();
        castObceComboBox = new javax.swing.JComboBox();
        obecComboBox = new javax.swing.JComboBox();
        obecLabel = new javax.swing.JLabel();
        castObceLabel = new javax.swing.JLabel();
        oblastLabel = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        setTitle("V\u00fdb\u011br um\u00edst\u011bn\u00ed");
        setModal(true);
        setName("locationSelector");
        oblastComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                oblastComboBoxItemStateChanged(evt);
            }
        });

        castObceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                castObceComboBoxItemStateChanged(evt);
            }
        });

        obecComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                obecComboBoxItemStateChanged(evt);
            }
        });

        obecLabel.setText("Obec:");

        castObceLabel.setText("\u010c\u00e1st obce:");

        oblastLabel.setText("Oblast:");

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
                    .addComponent(castObceComboBox, 0, 340, Short.MAX_VALUE)
                    .addComponent(obecComboBox, 0, 340, Short.MAX_VALUE)
                    .addComponent(oblastComboBox, 0, 340, Short.MAX_VALUE)))
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
                    .addComponent(obecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(castObceLabel)
                    .addComponent(castObceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        obecComboBox.setModel(new DefaultComboBoxModel(oblast.getViToCis().toArray()));
        obecComboBox.setEnabled(obecComboBox.getModel().getSize() > 1);

        obecComboBoxItemStateChanged(null);
    }//GEN-LAST:event_oblastComboBoxItemStateChanged

    private void obecComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_obecComboBoxItemStateChanged

        ViToCi obec = (ViToCi) obecComboBox.getSelectedItem();
        if (obec == null) return;

        castObceComboBox.setModel(new DefaultComboBoxModel(obec.getSuburbs().toArray()));
        castObceComboBox.setEnabled(castObceComboBox.getModel().getSize() > 1);

        castObceComboBoxItemStateChanged(null);
    }//GEN-LAST:event_obecComboBoxItemStateChanged

        private void castObceComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_castObceComboBoxItemStateChanged

        /*if (castObceComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) castObceComboBox.getSelectedItem());

        else if (obecComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) obecComboBox.getSelectedItem());

        else if (oblastComboBox.getSelectedItem() != null)
            checkSetSelected((ElementWithStreets) oblastComboBox.getSelectedItem());*/

        if (castObceComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) castObceComboBox.getSelectedItem());

        else if (obecComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) obecComboBox.getSelectedItem());

        else if (oblastComboBox.getSelectedItem() != null)
            selectedElement = ((ElementWithStreets) oblastComboBox.getSelectedItem());
        
        }//GEN-LAST:event_castObceComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox castObceComboBox;
    private javax.swing.JLabel castObceLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox obecComboBox;
    private javax.swing.JLabel obecLabel;
    private javax.swing.JComboBox oblastComboBox;
    private javax.swing.JLabel oblastLabel;
    // End of variables declaration//GEN-END:variables

    private class AddressElementRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof AddressElement && !(value instanceof Region))
                setText(((AddressElement) value).getName());

            return c;
        }
    }
}
