package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import javax.swing.JList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.StatusListener;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Match;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.proposal.Proposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalListPainter;
import org.openstreetmap.josm.tools.ImageProvider;

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
            boolean shouldDraw = false;
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
        proposalList.setCellRenderer(new ProposalListPainter());

        // Init the "match" combobox.
        matchesComboBox.setModel(new MatchesComboBoxModel());
        matchesComboBox.setRenderer(new MatchesComboBoxPainter());

        if (primitive.get("addr:alternatenumber") != null) {
            alternateNumberEdit.setText(primitive.get("addr:alternatenumber"));
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
            
            if (updateMatchesTimer.isRunning()) {
                updateMatchesTimer.stop();
                updateMatches();
            }

            proposalContainer.applyAll();

            Main.ds.setSelected((Node) null); // TODO: This is an ugly hack.
            Main.ds.setSelected(proposalContainer.getTarget());
            
            Reasoner r = CzechAddressPlugin.getReasoner();
            Match    m = (Match) matchesComboBox.getSelectedItem();
            if (m != null) {
                r.overwriteMatch(m.elem, m.prim);
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
        
        if (proposalContainer.getTarget().deleted) {
            setVisible(false);
        }
        
        OsmPrimitive prim = this.proposalContainer.getTarget();
        
        Map<String,String> backup = prim.keys; 
        prim.keys = null;
        prim.put("addr:alternatenumber", alternateNumberEdit.getText());
        
        Reasoner r = CzechAddressPlugin.getReasoner();
        NotNullList<Match> matches = r.getMatchesForPrimitive(prim);

        prim.keys = backup;
        
        // TODO: Here we should sort matches according to their quality.

        MatchesComboBoxModel matchesModel =
                ((MatchesComboBoxModel) matchesComboBox.getModel());

        // Fill the combobox with suitable houses.
        matchesModel.setMatches(matches);
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

        // When reasoning is done, dialog gets enabled and new proposals are added.
        } else if (message == MESSAGE_MATCHES_CHANGED) {
            updateMatches();
            mainPanel.setEnabled(true);
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
        ensureConsistencyButton = new javax.swing.JButton();

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

        ensureConsistencyButton.setIcon(ImageProvider.get("actions", "refresh-small.png"));
        ensureConsistencyButton.setText("");
        ensureConsistencyButton.setToolTipText("Provede nové přiřazení prvků mapy na elementy databáze.\nTouto volbou se zruší všechny manuálně vyřešené konflikty."); // NOI18N
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
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(locationEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeLocationButton))
                    .addComponent(alternateNumberEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(matchesComboBox, 0, 208, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ensureConsistencyButton))))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
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
                    .addComponent(matchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ensureConsistencyButton))
                .addContainerGap(179, Short.MAX_VALUE))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(92, 92, 92)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)))
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
        Match match = (Match) matchesComboBox.getSelectedItem();

        proposalContainer.setProposals(match.elem.getDiff(
                                                proposalContainer.getTarget()));
    }//GEN-LAST:event_matchChanged

    private void ensureConsistencyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ensureConsistencyButtonActionPerformed
        CzechAddressPlugin.getReasoner().ensureConsistency();
    }//GEN-LAST:event_ensureConsistencyButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alternateNumberEdit;
    private javax.swing.JButton changeLocationButton;
    private javax.swing.JButton ensureConsistencyButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField locationEdit;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox matchesComboBox;
    private javax.swing.JList proposalList;
    // End of variables declaration//GEN-END:variables

    /**
     * Painter for adding icons to the {@code matchesComboBox}.
     */
    private class MatchesComboBoxPainter extends DefaultListCellRenderer {

        ImageIcon envelopeNormIcon = ImageProvider.get("envelope-closed-small.png");
        ImageIcon envelopeStarIcon = ImageProvider.get("envelope-closed-star-small.png");
        ImageIcon envelopeExclIcon = ImageProvider.get("envelope-closed-exclamation-small.png");

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            Reasoner r = CzechAddressPlugin.getReasoner();
            setIcon(null);

            if (value instanceof Match) {
                Match match = (Match) value;

                setText(AddressElement.getName(match.elem));

                if (match.elem instanceof House) {
                    setIcon(envelopeStarIcon);
                    if ( r.getConflicts(match.elem) != null )
                        setIcon(envelopeExclIcon);
                    else if ( r.translate(match.elem) != null)
                        setIcon(envelopeNormIcon);
                }
            }

            return c;
        }
    }

    /**
     * Container for all Houses, which match the given 'alternatenumber'.
     */
    private class MatchesComboBoxModel extends HalfCookedComboBoxModel {

        private List<Match> matches = null;
        int selectedIndex = -1;

        public void setMatches(List<Match> matches) {
            this.matches = matches;
            selectedIndex = -1;
            notifyAllListeners();
        }

        public void setSelectedItem(Object anItem) {
            if (matches == null) return;
            selectedIndex = matches.indexOf(anItem);
        }

        public Object getSelectedItem() {
            return getElementAt(selectedIndex);
        }

        public int getSize() {
            if (matches == null) return 0;
            return matches.size();
        }

        public Object getElementAt(int index) {
            if (matches == null) return null;
            if ((index < 0) || (index >= matches.size())) return null;
            return matches.get(index);
        }
    }
}
