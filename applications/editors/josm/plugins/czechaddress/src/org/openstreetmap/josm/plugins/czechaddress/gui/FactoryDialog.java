package org.openstreetmap.josm.plugins.czechaddress.gui;

import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.MapUtils;
import org.openstreetmap.josm.plugins.czechaddress.StatusListener;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithHouses;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * A dockable dialog for controlling the "one click" address node creation.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class FactoryDialog extends ToggleDialog
        implements SelectionChangedListener, StatusListener {

    HouseListModel  houseModel  = new HouseListModel();
    StreetListModel streetModel = new StreetListModel();
    
    public FactoryDialog() {

        super( "Továrna na adresy",
               "envelope-scrollbar.png",
               "Umožňuje rychlé vytváření adresních bodů „jedním kliknutím.“",
                Shortcut.registerShortcut(
                    "subwindow:addressfactory",
                    "Přepnout: Továrna na adresy",
                    KeyEvent.VK_T, Shortcut.GROUP_LAYER),
                200);

        // Hack the ToggleDialog to allow using NetBeans form editor
        LayoutManager originalManager = getLayout();
        initComponents();
        setLayout(originalManager);
        add(mainPanel);

        // Register to all messages
        CzechAddressPlugin.addStatusListener(this);

        // And init all listeners to data model
        streetModel.notifyAllListeners();
        houseModel.notifyAllListeners();

        streetComboBox.setModel(streetModel);
        streetComboBox.setRenderer(new StreetListRenderer());
        houseList.setModel(houseModel);
        houseList.setCellRenderer(new HouseListRenderer());
    }

    public void pluginStatusChanged(int message) {

        if (message == MESSAGE_DATABASE_LOADED) {
            relocateButton.setEnabled(true);
            return;
        }
        
        if (message == MESSAGE_LOCATION_CHANGED) {
            DataSet.selListeners.add(this);

            streetModel.setParent(CzechAddressPlugin.getLocation());
            relocateButton.setText("Změnit místo");
            streetComboBox.setEnabled(true);
            houseList.setEnabled(true);
            keepOddityCheckBox.setEnabled(true);
            return;
        }
        
        if (message == MESSAGE_REASONER_REASONED) {
            ensureConsistencyButton.setEnabled(true);
        }
        
        if (message == MESSAGE_MATCHES_CHANGED || message == MESSAGE_CONFLICT_CHANGED) {
            houseModel.notifyAllListeners();
            return;
        }
    }

    public House getSelectedHouse() {
        if (houseList.getSelectedValue() instanceof House)
            return (House) houseList.getSelectedValue();
        else
            return null;
    }

    public boolean existsAvailableHouse() {
        Reasoner r = CzechAddressPlugin.reasoner;

        int i = houseList.getSelectedIndex();
        while (i < houseModel.getSize()) {
            if (r.translate((House) houseModel.getElementAt(i)) == null)
                return true;
            i++;
        }

        i = 0;
        while (i < houseList.getSelectedIndex()) {
            if (r.translate((House) houseModel.getElementAt(i)) == null)
                return true;
            i++;
        }

        return false;
    }

    public void selectNextUnmatchedHouse() {

        int index = houseList.getSelectedIndex();

        index++;
        Object current;
        while ( (current = houseModel.getElementAt(index)) != null
              && CzechAddressPlugin.reasoner.translate((House) current) != null)
            index++;

        if (index >= houseModel.getSize())
            index = 0;

        houseList.setSelectedIndex(index);
        houseList.ensureIndexIsVisible(index);
    }

    public void selectNextUnmatchedHouseMaintainOddity() {

        if (getSelectedHouse().getCO() == null) {
            selectNextUnmatchedHouse();
            return;
        }

        String oldStr = StringUtils.extractNumber(getSelectedHouse().getCO());

        try {
            int oldNum = -1, newNum = -1;
            do {
                selectNextUnmatchedHouse();
                    
                String newStr = StringUtils.extractNumber(getSelectedHouse().getCO());

                if (oldNum == -1)
                    oldNum = Integer.valueOf(oldStr);
                newNum = Integer.valueOf(newStr);

            } while ( (oldNum + newNum) % 2 == 1 &&
                      houseList.getSelectedIndex() != 0 );
            
        } catch (Exception exp) {}
    }

    public void selectNextUnmatchedHouseByCheckBox() {
        if (keepOddityCheckBox.isSelected())
            selectNextUnmatchedHouseMaintainOddity();
        else
            selectNextUnmatchedHouse();
    }

    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {

        if (newSelection.size() != 1) return;

        OsmPrimitive selectedPrim = (OsmPrimitive) newSelection.toArray()[0];

        String streetName;

        if ((streetName = selectedPrim.get("addr:street")) == null) {
            if (selectedPrim.get("highway") == null)
                return;
            else
                if ((streetName = selectedPrim.get("name")) == null)
                    return;
        }

        Street selectedStreet = null;
        for (Street street : CzechAddressPlugin.getLocation().getStreets())
            if (street.getName().toUpperCase().equals(streetName.toUpperCase())) {
                selectedStreet = street;
                break;
            }

        if (selectedStreet == null) return;
        streetComboBox.setSelectedItem(selectedStreet);
        streetModel.notifyAllListeners();

        int bestQuality = -5;
        House bestHouse = null;
        for (House currHouse : selectedStreet.getHouses()) {

            int currQuality = currHouse.getMatchQuality(selectedPrim);

            if (currQuality > bestQuality) {
                bestQuality = currQuality;
                bestHouse = currHouse;
            }
        }

        if (bestHouse == null) return;
        houseList.setSelectedValue(bestHouse, true);
        houseModel.notifyAllListeners();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        houseList = new javax.swing.JList();
        keepOddityCheckBox = new javax.swing.JCheckBox();
        relocateButton = new javax.swing.JButton();
        streetComboBox = new javax.swing.JComboBox();
        ensureConsistencyButton = new javax.swing.JButton();

        setLayout(new java.awt.GridLayout(1, 0));

        houseList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        houseList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        houseList.setEnabled(false);
        houseList.setFocusable(false);
        houseList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                houseListClicked(evt);
            }
        });

        jScrollPane1.setViewportView(houseList);

        keepOddityCheckBox.setSelected(true);
        keepOddityCheckBox.setText("Zachov\u00e1vat sudost / lichost");
        keepOddityCheckBox.setEnabled(false);

        relocateButton.setText("Inicializovat");
        relocateButton.setEnabled(false);
        relocateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relocateButtonActionPerformed(evt);
            }
        });

        streetComboBox.setModel(streetModel);
        streetComboBox.setEnabled(false);
        streetComboBox.setFocusable(false);

        ensureConsistencyButton.setIcon(ImageProvider.get("actions", "refresh-small.png"));
        ensureConsistencyButton.setText("");
        ensureConsistencyButton.setToolTipText("Provede nov\u00e9 p\u0159i\u0159azen\u00ed prvk\u016f mapy na elementy datab\u00e1ze.\nTouto volbou se zru\u0161\u00ed v\u0161echny manu\u00e1ln\u011b vy\u0159e\u0161en\u00e9 konflikty.");
        ensureConsistencyButton.setEnabled(false);
        ensureConsistencyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ensureConsistencyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(streetComboBox, 0, 50, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relocateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ensureConsistencyButton))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(keepOddityCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(relocateButton)
                    .addComponent(ensureConsistencyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepOddityCheckBox))
        );
        add(mainPanel);

    }// </editor-fold>//GEN-END:initComponents

    private void relocateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relocateButtonActionPerformed
        CzechAddressPlugin.changeLocation();
    }//GEN-LAST:event_relocateButtonActionPerformed

    private void houseListClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_houseListClicked
        if (evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1) {
            Reasoner r = CzechAddressPlugin.reasoner;

            if (r.translate(getSelectedHouse()) != null) {
                MapUtils.zoomTo(r.translate(getSelectedHouse()));
                return;
            }

            // TODO: The following code does not work... for some reason.
            /*List<Match> conflicts = r.getConflictsForElement(getSelectegetConflicts if (conflicts != null) {
                List<OsmPrimitive> toZoom
                        = new ArrayList<OsmPrimitive>(conflicts.size());
                for (Match conflict : conflicts)
                    toZoom.add(conflict.prim);

                MapUtils.zoomToMany(toZoom);
                return;
            }*/
        }
    }//GEN-LAST:event_houseListClicked

    private void ensureConsistencyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ensureConsistencyButtonActionPerformed
        CzechAddressPlugin.reasoner.ensureConsistency();
}//GEN-LAST:event_ensureConsistencyButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ensureConsistencyButton;
    private javax.swing.JList houseList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox keepOddityCheckBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton relocateButton;
    private javax.swing.JComboBox streetComboBox;
    // End of variables declaration//GEN-END:variables

    private class StreetListRenderer extends DefaultListCellRenderer {

        Font plainFont = null;
        Font boldFont = null;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (plainFont == null) plainFont = getFont().deriveFont(Font.PLAIN);
            if (boldFont  == null)  boldFont = getFont().deriveFont(Font.BOLD);

            if (value instanceof Street) {
                setFont(plainFont);
                setText(((Street) value).getName());
                
            } else {
                setFont(boldFont);
                if (value instanceof ElementWithStreets) setText("[domy bez ulice]");
                if (value instanceof AllStreetProvider)  setText("[všechny domy]");
                if (value instanceof FreeStreetProvider) setText("[nepřiřazené domy]");

            }

            return c;
        }
    }

    private class HouseListRenderer extends DefaultListCellRenderer {

        Font plainFont = null;
        Font boldFont = null;

        ImageIcon envelopeNormIcon = ImageProvider.get("envelope-closed-small.png");
        ImageIcon envelopeStarIcon = ImageProvider.get("envelope-closed-star-small.png");
        ImageIcon envelopeExclIcon = ImageProvider.get("envelope-closed-exclamation-small.png");

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (plainFont == null) plainFont = getFont().deriveFont(Font.PLAIN);
            if (boldFont == null) boldFont = getFont().deriveFont(Font.BOLD);

            Reasoner r = CzechAddressPlugin.reasoner;

            if (value instanceof House) {
                House house = (House) value;

                setIcon(envelopeNormIcon);
                setFont(plainFont);

                if ( r.conflicts(house) != null)
                    setIcon(envelopeExclIcon);
                else if ( r.translate(house) == null) {
                    setIcon(envelopeStarIcon);
                    setFont(boldFont);
                }

                setText(house.getName());
            }
            return c;
        }
    }

    private class AllStreetProvider extends ElementWithHouses {
        public AllStreetProvider() {
            super("Všechny domy");
        }

        @Override
        public void setHouses(List<House> houses) {
            this.houses = houses;
        }
    }

    private class FreeStreetProvider extends ElementWithHouses
                                     implements StatusListener {

        public FreeStreetProvider() {
            super("Nepřiřazené domy");
            CzechAddressPlugin.addStatusListener(this);
            rebuild();
        }

        @Override
        public void setHouses(List<House> houses) {
            this.houses = houses;
        }

        public void pluginStatusChanged(int message) {
            if (message == StatusListener.MESSAGE_MATCHES_CHANGED)
                rebuild();
        }

        public void rebuild() {
            Reasoner r = CzechAddressPlugin.reasoner;
            if (r == null) return;

            houses.clear();
            for (AddressElement house : r.getElementPool())
                if (r.translate(house) == null)
                    if (house instanceof House)
                        houses.add((House) house);
        }
    }

    private class StreetListModel extends HalfCookedComboBoxModel {

        private ElementWithHouses selected = null;
        private ElementWithStreets parent = null;

        private List<ElementWithHouses> metaElem
                = new ArrayList<ElementWithHouses>();

        public StreetListModel() {
            metaElem.add(null);
            metaElem.add(new AllStreetProvider());
            metaElem.add(new FreeStreetProvider());
        }



        public int getSize() {
            if (parent == null) return 0;
            return parent.getStreets().size() + metaElem.size();
        }

        public void setParent(ElementWithStreets parent) {
            if (parent == null) return;

            selected = parent;
            this.parent = parent;
            metaElem.set(0, parent);

            metaElem.get(1).setHouses(parent.getAllHouses());
            notifyAllListeners();
        }

        public Object getElementAt(int index) {

            if (parent == null) return null;
            if (index <  0) return null;

            if (index < metaElem.size())
                return metaElem.get(index);

            index -= metaElem.size();
            // Now the index points to the list of streets
            if (index < parent.getStreets().size())
                return parent.getStreets().get(index);

            return null;
        }

        public void setSelectedItem(Object anItem) {
            assert anItem instanceof ElementWithHouses;
            selected = (ElementWithHouses) anItem;
            houseModel.notifyAllListeners();
        }

        public Object getSelectedItem() {
            return selected;
        }
    }

    private class HouseListModel extends HalfCookedListModel {

        public int getSize() {
            if (streetComboBox.getSelectedItem() == null) return 0;
            ElementWithHouses selected
                    = (ElementWithHouses) streetComboBox.getSelectedItem();

            return selected.getHouses().size();
        }

        public House getHouseAt(int index) {
            if (streetComboBox.getSelectedItem() == null) return null;
            ElementWithHouses selected
                    = (ElementWithHouses) streetComboBox.getSelectedItem();

            if ((index < 0) || (index >= selected.getHouses().size())) return null;
            return selected.getHouses().get(index);
        }

        public Object getElementAt(int index) {
            return getHouseAt(index);
        }
    }

}
