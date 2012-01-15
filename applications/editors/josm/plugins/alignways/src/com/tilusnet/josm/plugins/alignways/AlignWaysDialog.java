/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;

/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public class AlignWaysDialog extends ToggleDialog implements ActionListener {

    private static final long serialVersionUID = 2949349330750246969L;

    private final JLabel infoText;
    enum AligningModeOption {
        ALGN_OPT_KEEP_LENGTH,
        ALGN_OPT_KEEP_ANGLE
    }
    AligningModeOption awOpt;
    JPanel activateInfoPanel, modesPanel, dlgPane;


    public AlignWaysDialog(AlignWaysMode awMode) {
        super(tr("Align Way Segments: Modes"), "alignways_cfg", tr("Align Ways control panel"),
                null, 70);

        infoText = new JLabel();

        dlgPane = new JPanel();
        dlgPane.setLayout(new GridLayout(0, 1, 20, 20));


        // Create the panel that shows instruction when Align Ways mode is *not* active
        activateInfoPanel = new JPanel();
        activateInfoPanel.setLayout(new BoxLayout(activateInfoPanel, BoxLayout.PAGE_AXIS));
        activateInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel lbl1Pnl = new JPanel();
        lbl1Pnl.setLayout(new FlowLayout(FlowLayout.LEADING));
        JLabel lbl1 = new JLabel(tr("This panel activates in Align Ways mode:"));
        lbl1Pnl.add(lbl1);
        activateInfoPanel.add(lbl1Pnl);

        JPanel tglbtnPnl = new JPanel();
        tglbtnPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
        JToggleButton tglBtn = new JToggleButton(awMode);
        tglBtn.setPreferredSize(new Dimension(50, 50));
        tglBtn.setText(null);
        tglbtnPnl.add(tglBtn);
        activateInfoPanel.add(tglbtnPnl);


        // Create the Align Ways mode control panel for when Align Ways *is* active
        modesPanel = new JPanel();
        modesPanel.setLayout(new BoxLayout(modesPanel, BoxLayout.PAGE_AXIS));
        /*
		modesPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createTitledBorder(tr("Align with:")))
				);
         */
        modesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        modesPanel.setAlignmentX(LEFT_ALIGNMENT);

        JRadioButton btnKeepLength = new JRadioButton(tr("Length preserved"));
        btnKeepLength.setActionCommand("awOptKeepLen");
        btnKeepLength.addActionListener(this);

        JRadioButton btnKeepAngle = new JRadioButton(tr("Angle preserved"));
        btnKeepAngle.setActionCommand("awOptKeepAng");
        btnKeepAngle.addActionListener(this);

        ButtonGroup btnGrp = new ButtonGroup();
        btnGrp.add(btnKeepLength);
        btnGrp.add(btnKeepAngle);

        modesPanel.add(new JLabel(tr("Align with:")));
        modesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        modesPanel.add(btnKeepLength);
        modesPanel.add(btnKeepAngle);
        modesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        // modesPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        infoText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10) )
                );
        modesPanel.add(infoText);

        // Start inactivated - JOSM cannot start directly in awMode
        activate(false);

        createLayout(dlgPane, false, null);

        // Select length preserved mode by default
        btnKeepLength.doClick();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "awOptKeepLen") {
            awOpt = AligningModeOption.ALGN_OPT_KEEP_LENGTH;
            infoText.setText(tr("<html>Aligns the way segment to the reference so that its length is preserved.</html>"));
        } else if (e.getActionCommand() == "awOptKeepAng") {
            awOpt = AligningModeOption.ALGN_OPT_KEEP_ANGLE;
            infoText.setText(tr("<html>Aligns the way segment to the reference so that the angles of its adjacent segments are preserved.<br/>" +
                    "The length of the aligned segment is likely to change as result.</html>"));
        }
    }

    /**
     * @return the awOpt
     */
    public AligningModeOption getAwOpt() {
        return awOpt;
    }


    /**
     * @param action If set to true, the dialog will show the mode options, otherwise it will show some instructions
     */
    public void activate(boolean activeMode) {

        if (activeMode == true) {
            // we're in alignways mode
            activateInfoPanel.setVisible(false);
            modesPanel.setVisible(true);
            this.setPreferredSize(new Dimension(0, 200));

            dlgPane.remove(activateInfoPanel);
            dlgPane.add(modesPanel);
            dlgPane.validate();
        } else {
            // we're not in alignways mode
            activateInfoPanel.setVisible(true);
            modesPanel.setVisible(false);
            this.setPreferredSize(new Dimension(0, 70));

            dlgPane.remove(modesPanel);
            dlgPane.add(activateInfoPanel);
            dlgPane.validate();
        }

    }

    public JCheckBoxMenuItem getWindowMenuItem() {
        return windowMenuItem;
    }


}
