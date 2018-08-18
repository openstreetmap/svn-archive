// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 */
public class AlignWaysWhatsNewPanel extends JPanel {

    private static final long serialVersionUID = 3691600157957492583L;

    public AlignWaysWhatsNewPanel() {
        initComponents();
    }

    private void initComponents() {

        lblWhatsNew = new JLabel();
        icnLogo = new JLabel();
        jSeparator1 = new JSeparator();
        newItem1 = new JLabel();
        btnHelpItem1 = new JButton();
        newItem2 = new JLabel();

        lblWhatsNew.setText("<html><div style=\"font-family: sans-serif; font-weight: bold; font-style: italic;\">"+
                            "<span style=\"font-size: large;\"><span style=\"font-size: x-large;\">"
                            + tr("What''s new...")
                            + "</span></div></html>");

        icnLogo.setIcon(new ImageIcon(getClass().getResource("/images/wndialog/alignways64.png"))); // NOI18N

        newItem1.setText("<html><div style=\"font-family: sans-serif;\"><ul style=\"margin-left: 20px;\"><li>"
                         + tr("Added <b>angle preserving</b> aligning mode")
                         + "</li></ul></div></html>");

        btnHelpItem1.setIcon(new ImageIcon(getClass().getResource("/images/wndialog/extlink10.png"))); // NOI18N
        btnHelpItem1.setText("More Info");
        btnHelpItem1.setToolTipText("Preserving angle aligning");
        btnHelpItem1.setBorder(null);
        btnHelpItem1.setBorderPainted(false);
        btnHelpItem1.setFocusPainted(false);
        btnHelpItem1.setFocusable(false);
        btnHelpItem1.setIconTextGap(6);
        btnHelpItem1.setOpaque(false);
        btnHelpItem1.setPreferredSize(new Dimension(69, 25));
        btnHelpItem1.addActionListener(evt -> btnHelpItem1ActionPerformed(evt));

        newItem2.setText("<html><div style=\"font-family: sans-serif;\"><ul style=\"margin-left: 20px;\"><li>"
                         + tr("Various improvements and bugfixes")
                         + "</li></ul></div></html>");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblWhatsNew, GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(icnLogo))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addComponent(newItem2, GroupLayout.Alignment.LEADING)
                            .addComponent(newItem1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnHelpItem1, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(icnLogo)
                    .addComponent(lblWhatsNew, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(newItem1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHelpItem1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newItem2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }

    private void btnHelpItem1ActionPerformed(ActionEvent evt) {
       openURI();
    }

    private JButton btnHelpItem1;
    private JLabel icnLogo;
    private JSeparator jSeparator1;
    private JLabel lblWhatsNew;
    private JLabel newItem1;
    private JLabel newItem2;

    private void openURI() {
        if (Desktop.isDesktopSupported()) {
          try {
            URI uri = new URI("https://wiki.openstreetmap.org/wiki/JOSM/Plugins/AlignWayS#Preserving_angles");
            Desktop.getDesktop().browse(uri);
          } catch (URISyntaxException ex) {
              Logger.getLogger(AlignWaysWhatsNewPanel.class.getName()).log(Level.SEVERE, null, ex);
          } catch (IOException e) {
              JOptionPane.showMessageDialog(this, e, tr("Errr..."), JOptionPane.WARNING_MESSAGE);
          }
        } else {
             JOptionPane.showMessageDialog(this, tr("Browser not supported."), tr("Errr..."), JOptionPane.WARNING_MESSAGE);
        }
    }
}
