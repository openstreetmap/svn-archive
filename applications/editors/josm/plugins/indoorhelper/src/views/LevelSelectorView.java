// License: GPL. For details, see LICENSE file.
package views;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;

/**
 * This is the level selector toolbox of the indoorhelper plug-in.
 *
 * @author rebsc
 *
 */
public class LevelSelectorView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel infoBar;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel label1;
    private JLabel label2;
    private DisableShortcutsOnFocusGainedTextField field;

    public LevelSelectorView() {
        initComponents();
    }

    private void initComponents() {
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        infoBar = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        label1 = new JLabel();
        label2 = new JLabel();
        field = new DisableShortcutsOnFocusGainedTextField();

        //======== this ========
        setTitle(tr("Add a new level"));
        java.awt.Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========

        dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        dialogPane.setLayout(new BorderLayout());

        //======== infoBar ========

        //---- Label1 ----
        label1.setText(tr("<html> Please insert the new level-tag number you want to add.<br> "
                + " <i>Info</i>: <br> If the OK button got pressed you will switch to the drawing action.<br>"
                + "To finish the new object please press spacebar. The new level<br>will be tagged automatically. </html>"));
        infoBar.add(label1);
        dialogPane.add(infoBar, BorderLayout.NORTH);

        //======== contentPanel ========

        contentPanel.setLayout(new GridBagLayout());
        ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- Label2 ----
        label2.setText(tr("level number:"));
        contentPanel.add(label2, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 30), 0, 0));

        //---- Field ----
        field.setToolTipText(tr("Example: ''2'' or ''3''"));
        field.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {}

            @Override
            public void focusGained(FocusEvent e) {
                field.selectAll();
            }
        });
        contentPanel.add(field, new GridBagConstraints(3, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 0, 5, 200), 0, 0));

        dialogPane.add(contentPanel, BorderLayout.CENTER);

        //======== buttonBar ========

        buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttonBar.setLayout(new GridBagLayout());
        ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
        ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

        //---- okButton ----
        okButton.setText(tr("OK"));
        buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

        //---- Button ----
        cancelButton.setText(tr("Cancel"));
        buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        dialogPane.add(buttonBar, BorderLayout.SOUTH);

        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Getter for the level number field.
     *
     * @return the {@link String}
     */
    public String getLevelNumber() {
        return this.field.getText();
    }

    /**
     * Set the listener for the OK button.
     *
     * @param l the listener to set
     */
    public void setOkButtonListener(ActionListener l) {
        this.okButton.addActionListener(l);
    }

    /**
     * Set the listener for the Cancel button.
     *
     * @param l the listener to set
     */
    public void setCancelButtonListener(ActionListener l) {
        this.cancelButton.addActionListener(l);
    }

    /**
     * Set the listener for window {@link LevelSelectorView}
     *
     * @param l the listener to set
     */
    public void setSelectorWindowListener(WindowListener l) {
        this.addWindowListener(l);
    }
}
