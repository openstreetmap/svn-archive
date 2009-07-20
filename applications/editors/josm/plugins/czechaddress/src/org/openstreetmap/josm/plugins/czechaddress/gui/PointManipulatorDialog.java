package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.Preferences;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;
import org.openstreetmap.josm.plugins.czechaddress.StatusListener;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.HalfCookedComboBoxModel;
import org.openstreetmap.josm.plugins.czechaddress.gui.utils.UniversalListRenderer;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.proposal.AddKeyValueProposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;

/**
 * Dialog for adding/editing an address of a single primitive.
 *
 * <p><b>TODO:</b> This dialog does not dispose and disconnect from
 * message handling system after being closed. Reproduce: Create a node
 * using this dialog, delete it and update reasoner.</p>
 *
 * @author radomir.cernoch@gmail.com
 */
public class PointManipulatorDialog extends ExtendedDialog implements StatusListener {

    private Timer  updateMatchesTimer = null;
    private Action updateMatchesAction;
    private ProposalContainer proposalContainer;

    /**
     * Creates and shows the dialog.
     * @param primitive the primitive, which should be edited by this dialog
     */
    public PointManipulatorDialog(OsmPrimitive primitive) {

        super(Main.parent, "Adresní bod",
                            new String[] { "OK", "Zrušit" }, true);
        initComponents();

        // Create action for delaying the database query...
        updateMatchesAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateMatches();
            }
        };

        // Register for plugin-wide messages.
        CzechAddressPlugin.addStatusListener(this);
        updateLocation();

        // A the beginning there are no proposals.
        proposalContainer = new ProposalContainer(primitive);
        proposalList.setModel(proposalContainer);
        proposalList.setCellRenderer(new UniversalListRenderer());

        // Init the "match" combobox.
        matchesComboBox.setModel(new MatchesComboBoxModel());
        matchesComboBox.setRenderer(new UniversalListRenderer());

        if (primitive.get(PrimUtils.KEY_ADDR_CP) != null) {
            alternateNumberEdit.setText(primitive.get(PrimUtils.KEY_ADDR_CP));
            updateMatches();
        }

        // And finalize initializing the form.
        setupDialog(mainPanel, new String[] { "ok.png", "cancel.png" });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(false);

        // TODO: Why does it always crash if the modality is set in constructor?
        setModal(false);
    }

    @Override
    protected void buttonAction(ActionEvent evt) {
        super.buttonAction(evt);
        if (getValue() == 1) {

            if (updateMatchesTimer != null && updateMatchesTimer.isRunning()) {
                updateMatchesTimer.stop();
                updateMatches();
            }

            proposalContainer.applyAll();

            Main.main.getCurrentDataSet().setSelected((Node) null); // TODO: This is an ugly hack.
            Main.main.getCurrentDataSet().setSelected(proposalContainer.getTarget());

            AddressElement elem = (AddressElement) matchesComboBox.getSelectedItem();
            if (elem != null) {
                Reasoner r = Reasoner.getInstance();
                synchronized (r) {
                    r.openTransaction();
                    r.doOverwrite(proposalContainer.getTarget(), elem);
                    r.closeTransaction();
                }
            }
        }

        CzechAddressPlugin.removeStatusListener(this);
    }

    /**
     * Updates the dialog after a location has changed.
     */
    public void updateLocation() {
        locationEdit.setText(CzechAddressPlugin.getLocation().toString());
    }

    /**
     * Updates the combobox with houses that match the current input.
     */
    public void updateMatches() {

        if (proposalContainer.getTarget().deleted)
            setVisible(false);
        OsmPrimitive prim = this.proposalContainer.getTarget();
        Reasoner r = Reasoner.getInstance();
        List<AddressElement> elems = new NotNullList<AddressElement>();

        synchronized (r) {
            Map<String,String> backup = prim.keys;
            r.openTransaction();
            for (AddressElement elem : r.getCandidates(prim))
                r.unOverwrite(prim, elem);
            prim.keys = null;
            prim.put(PrimUtils.KEY_ADDR_CP, alternateNumberEdit.getText());
            r.update(prim);
            elems.addAll(r.getCandidates(prim));
            prim.keys = backup;
            r.update(prim);
            r.closeTransaction();
        }

        MatchesComboBoxModel matchesModel =
                ((MatchesComboBoxModel) matchesComboBox.getModel());

        // Fill the combobox with suitable houses.
        matchesModel.setElements(elems);
        if (matchesModel.getSize() > 0) {
            matchesComboBox.setSelectedIndex(0);
            return;
        }

        // If there are no suitable houses, invent one!
        House fakeHouse = new House(alternateNumberEdit.getText(), null);
        fakeHouse.setParent(CzechAddressPlugin.getLocation());
        proposalContainer.setProposals(
                fakeHouse.getDiff(proposalContainer.getTarget()));
    }

    public void pluginStatusChanged(int message) {

        // If location changes, we block the dialog until reasoning is done.
        if (message == MESSAGE_LOCATION_CHANGED) {
            updateLocation();
            mainPanel.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     *
     * <p><b>WARNING:</b> Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.</p>
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        alternateNumberEdit = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        locationEdit = new javax.swing.JTextField();
        changeLocationButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        proposalList = new javax.swing.JList();
        matchesComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        jLabel4.setText("jLabel4");

        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setText("Číslo popisné:");

        alternateNumberEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                PointManipulatorDialog.this.keyReleased(evt);
            }
        });

        jLabel5.setText("Místo:");

        locationEdit.setEditable(false);
        locationEdit.setFocusable(false);

        changeLocationButton.setText("Změnit");
        changeLocationButton.setFocusable(false);
        changeLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLocationButtonActionPerformed(evt);
            }
        });

        proposalList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                proposalListKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(proposalList);

        matchesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        matchesComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                matchChanged(evt);
            }
        });

        jLabel6.setText("Zaznam v databazi:");

        statusLabel.setText(" ");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(locationEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeLocationButton))
                    .addComponent(alternateNumberEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(matchesComboBox, 0, 293, Short.MAX_VALUE)))
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(changeLocationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alternateNumberEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(matchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel))
        );

        getContentPane().add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void changeLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLocationButtonActionPerformed
        CzechAddressPlugin.changeLocation();
    }//GEN-LAST:event_changeLocationButtonActionPerformed

    private void keyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyReleased
        if (updateMatchesTimer != null)
            updateMatchesTimer.stop();

        updateMatchesTimer = new Timer(300, updateMatchesAction);
        updateMatchesTimer.setRepeats(false);
        updateMatchesTimer.start();
    }//GEN-LAST:event_keyReleased

    private void proposalListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_proposalListKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            for (Object o : proposalList.getSelectedValues())
                proposalContainer.removeProposal((Proposal) o);
        }
    }//GEN-LAST:event_proposalListKeyReleased

    private void matchChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_matchChanged

        if (matchesComboBox.getSelectedItem() == null) return;
        AddressElement selectedElement = (AddressElement) matchesComboBox.getSelectedItem();
        proposalContainer.setProposals(selectedElement.getDiff(proposalContainer.getTarget()));

        Preferences p = Preferences.getInstance();
        if (p.addNewTag && proposalContainer.getTarget().keySet().size() == 0)
            proposalContainer.addProposal(new AddKeyValueProposal(
                                               p.addNewTagKey, p.addNewTagValue));

    }//GEN-LAST:event_matchChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alternateNumberEdit;
    private javax.swing.JButton changeLocationButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField locationEdit;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox matchesComboBox;
    private javax.swing.JList proposalList;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Container for all Houses, which match the given 'alternatenumber'.
     */
    private class MatchesComboBoxModel extends HalfCookedComboBoxModel {

        private List<AddressElement> matches = null;
        AddressElement selected = null;

        public void setElements(List<AddressElement> elements) {
            this.matches = elements;
            selected = null;
            notifyAllListeners();
        }

        public void setSelectedItem(Object anItem) {
            if (matches == null) return;
            selected = (AddressElement) anItem;
            if (Reasoner.getInstance().translate(selected) != proposalContainer.getTarget())
                statusLabel.setText("Vybraná adresa už v mapě existuje."+
                                    " Potvrzením vznikne konflikt.");
            else
                statusLabel.setText(" ");
        }

        public Object getSelectedItem() {
            return selected;
        }

        public int getSize() {
            if (matches == null) return 0;
            return matches.size();
        }

        public Object getElementAt(int index) {
            if (matches == null) return null;
            if (index >= matches.size()) return null;
            return matches.get(index);
        }
    }
}
