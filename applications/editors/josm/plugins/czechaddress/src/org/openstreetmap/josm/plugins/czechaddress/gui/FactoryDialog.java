package org.openstreetmap.josm.plugins.czechaddress.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.openstreetmap.josm.plugins.czechaddress.StringUtils;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithHouses;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.HalfCookedComboBoxModel;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.HalfCookedListModel;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.ReasonerListener;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * A dockable dialog for controlling the "one click" address node creation.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class FactoryDialog extends ToggleDialog
        implements SelectionChangedListener, StatusListener, ReasonerListener {


    private static FactoryDialog singleton = null;
    public  static FactoryDialog getInstance() {
        if (singleton == null)
            singleton = new FactoryDialog();
        return singleton;
    }

    private HouseListModel  houseModel  = new HouseListModel();
    private StreetListModel streetModel = new StreetListModel();

    private FactoryDialog() {

        super( "Továrna na adresy",
               "envelope-scrollbar.png",
               "Umožňuje rychlé vytváření adresních bodů „jedním kliknutím.“",
                Shortcut.registerShortcut("subwindow:addressfactory","Přepnout: Továrna na adresy",
                    KeyEvent.VK_T, Shortcut.ALT),
                200);

        // Hack the ToggleDialog to allow using NetBeans form editor
        LayoutManager originalManager = getLayout();
        initComponents();
        setLayout(originalManager);
        createLayout(mainPanel, false, null);

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

    @Override
	public void pluginStatusChanged(int message) {

        if (message == MESSAGE_DATABASE_LOADED) {
            relocateButton.setEnabled(true);
            return;
        }

        if (message == MESSAGE_LOCATION_CHANGED) {
            DataSet.addSelectionListener(this);

            streetModel.setParent(CzechAddressPlugin.getLocation());
            relocateButton.setText("Změnit místo");
            streetComboBox.setEnabled(true);
            houseList.setEnabled(true);
            keepOddityCheckBox.setEnabled(true);
            return;
        }
    }

    public void setSelectedHouse(House house) {

        for (int i=0; i<streetModel.getSize(); i++)
            if (streetModel.getElementAt(i) == house.getParent()) {
                streetComboBox.setSelectedIndex(i);
                streetComboBox.repaint();
                break;
            }

        for (int i=0; i<houseModel.getSize(); i++)
            if (houseModel.getHouseAt(i) == house) {
                houseList.setSelectedIndex(i);
                houseList.ensureIndexIsVisible(i);
                break;
            }
    }

    public House getSelectedHouse() {
        if (houseList.getSelectedValue() instanceof House)
            return (House) houseList.getSelectedValue();
        return null;
    }

    public boolean existsAvailableHouse() {
        Reasoner r = Reasoner.getInstance();

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

        index++; // Initial kick to do at least one move.
        House current;
        while ( (current = houseModel.getHouseAt(index))  != null
             && Reasoner.getInstance().translate(current) != null)
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

            } while ((oldNum + newNum) % 2 == 1 &&
                     houseList.getSelectedIndex() != 0);

        // If anything goes wrong, we can silently ignore the errors.
        // The selected item just does not get updated...
        } catch (Exception exp) {}
    }

    public void selectNextUnmatchedHouseByCheckBox() {
        if (keepOddityCheckBox.isSelected())
            selectNextUnmatchedHouseMaintainOddity();
        else
            selectNextUnmatchedHouse();
    }

    public boolean selectionListenerActivated = true;
    @Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {

        if (!selectionListenerActivated) return;
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

            int currQuality = currHouse.getQ(selectedPrim);

            if (currQuality > bestQuality) {
                bestQuality = currQuality;
                bestHouse = currHouse;
            }
        }

        if (bestHouse == null) return;
        houseList.setSelectedValue(bestHouse, true);
        houseList.ensureIndexIsVisible(houseList.getSelectedIndex());
        houseModel.notifyAllListeners();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        houseList = new javax.swing.JList();
        keepOddityCheckBox = new javax.swing.JCheckBox();
        relocateButton = new javax.swing.JButton();
        streetComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridLayout(1, 0));

        houseList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            @Override
			public int getSize() { return strings.length; }
            @Override
			public Object getElementAt(int i) { return strings[i]; }
        });
        houseList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        houseList.setEnabled(false);
        houseList.setFocusable(false);
        houseList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
                houseListClicked(evt);
            }
        });
        jScrollPane1.setViewportView(houseList);

        keepOddityCheckBox.setSelected(true);
        keepOddityCheckBox.setText("Zachovávat sudost / lichost");
        keepOddityCheckBox.setEnabled(false);

        relocateButton.setText("Inicializovat");
        relocateButton.setEnabled(false);
        relocateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                relocateButtonActionPerformed(evt);
            }
        });

        streetComboBox.setModel(streetModel);
        streetComboBox.setEnabled(false);
        streetComboBox.setFocusable(false);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(streetComboBox, 0, 199, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relocateButton))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(keepOddityCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(relocateButton))
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
            Reasoner r = Reasoner.getInstance();

            if (r.translate(getSelectedHouse()) != null)
                MapUtils.zoomTo(r.translate(getSelectedHouse()));
            else
                ConflictResolver.getInstance().focusElement(getSelectedHouse());
        }
    }//GEN-LAST:event_houseListClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList houseList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox keepOddityCheckBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton relocateButton;
    private javax.swing.JComboBox streetComboBox;
    // End of variables declaration//GEN-END:variables

    @Override
	public void elementChanged(AddressElement elem) {
        houseModel.notifyAllListeners();
    }
    @Override
	public void primitiveChanged(OsmPrimitive prim) {}
    @Override
	public void resonerReseted() {}

//==============================================================================

    private class StreetListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Street) {
                setFont(getFont().deriveFont(Font.PLAIN));
                setText(((Street) value).getName());

            } else if (value instanceof ElementWithHouses) {
                setFont(getFont().deriveFont(Font.BOLD));
                setText("[" + ((ElementWithHouses) value).getName() + "]");
            }

            return c;
        }
    }

//==============================================================================

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

            if (value instanceof House) {
                House house = (House) value;

                setIcon(envelopeNormIcon);
                setFont(plainFont);

                if (Reasoner.getInstance().inConflict(house))
                    setIcon(envelopeExclIcon);

                else if (Reasoner.getInstance().translate(house) == null) {
                    setIcon(envelopeStarIcon);
                    setFont(boldFont);
                }

                setText(house.getName());
            }
            return c;
        }
    }

//==============================================================================

    private class AllStreetProvider extends ElementWithHouses {
        public AllStreetProvider() {
            super("všechny domy");
        }

        @Override
        public void setHouses(List<House> houses) {
            this.houses = houses;
        }
    }

    private class FreeStreetProvider extends ElementWithHouses
                                     implements ReasonerListener {

        public FreeStreetProvider() {
            super("nepřiřazené domy");
            Reasoner.getInstance().addListener(this);
        }

        @Override
		public void resonerReseted() { houses.clear(); }
        @Override
		public void primitiveChanged(OsmPrimitive prim) {}
        @Override
		public void elementChanged(AddressElement elem) {
            if (!(elem instanceof House)) return;
            House house = (House) elem;
            int index = Collections.binarySearch(houses, house);

            if (Reasoner.getInstance().translate(house) != null) {
                if (index >= 0) houses.remove(index);
            } else {
                if (index < 0)  houses.add(-index-1, house);
            }

            houseModel.notifyAllListeners();
        }
    }

//==============================================================================

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

        @Override
		public int getSize() {
            if (parent == null) return 0;
            return parent.getStreets().size() + metaElem.size();
        }

        public void setParent(ElementWithStreets parent) {
            if (parent == null) return;

            this.selected = parent;
            this.parent   = parent;

            metaElem.set(0, parent);
            metaElem.get(1).setHouses(parent.getAllHouses());
            notifyAllListeners();
        }

        @Override
		public Object getElementAt(int index) {
            if (parent == null) return null;

            if (index < metaElem.size())
                return metaElem.get(index);

            index -= metaElem.size();

            if (index < parent.getStreets().size())
                return parent.getStreets().get(index);

            return null;
        }

        @Override
		public void setSelectedItem(Object anItem) {
            assert anItem instanceof ElementWithHouses;
            selected = (ElementWithHouses) anItem;
            houseModel.notifyAllListeners();
        }

        @Override
		public Object getSelectedItem() {
            return selected;
        }
    }

//==============================================================================

    private class HouseListModel extends HalfCookedListModel
                                 implements ReasonerListener {

        public HouseListModel() {
            Reasoner.getInstance().addListener(this);
        }

        @Override
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

            if ((index < 0) || (index >= selected.getHouses().size()))
                return null;
            return selected.getHouses().get(index);
        }

        @Override
		public Object getElementAt(int index) {
            return getHouseAt(index);
        }

        @Override
		public void primitiveChanged(OsmPrimitive prim) {}
        @Override
		public void elementChanged(AddressElement elem) {
            notifyAllListeners();
        }

        @Override
		public void resonerReseted() {
            notifyAllListeners();
        }
    }
}
