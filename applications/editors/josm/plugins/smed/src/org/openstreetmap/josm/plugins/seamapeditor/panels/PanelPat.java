// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor.panels;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openstreetmap.josm.plugins.seamapeditor.SmedAction;
import org.openstreetmap.josm.plugins.seamapeditor.messages.Messages;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Ent;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Pat;

public class PanelPat extends JPanel {

    private SmedAction dlg;
    private Ent ent;
    public PanelCol panelCol;

    private ButtonGroup patButtons = new ButtonGroup();
    public JRadioButton noneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NoneButton.png")));
    public JRadioButton horizButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/HorizontalButton.png")));
    public JRadioButton vertButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/VerticalButton.png")));
    public JRadioButton diagButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DiagonalButton.png")));
    public JRadioButton squareButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SquaredButton.png")));
    public JRadioButton borderButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BorderButton.png")));
    public EnumMap<Pat, JRadioButton> patterns = new EnumMap<>(Pat.class);
    private ActionListener alPat = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Pat pat : patterns.keySet()) {
                JRadioButton button = patterns.get(pat);
                if (button.isSelected()) {
                    SmedAction.panelMain.mark.setPattern(ent, pat);
                    button.setBorderPainted(true);
                } else {
                    button.setBorderPainted(false);
                }
            }
            switch (SmedAction.panelMain.mark.getPattern(ent)) {
            case NOPAT:
                panelCol.trimStack(1);
                break;
            case HSTRP:
            case VSTRP:
            case DIAG:
                break;
            case SQUARED:
                panelCol.trimStack(4);
                break;
            case BORDER:
            case CROSS:
                panelCol.trimStack(2);
                break;
            default:
                break;
            }
        }
    };

    public PanelPat(SmedAction dia, Ent entity) {
        dlg = dia;
        ent = entity;
        setLayout(null);
        panelCol = new PanelCol(dlg, ent);
        panelCol.setBounds(new Rectangle(0, 0, 72, 160));
        add(panelCol);
        add(getPatButton(noneButton, 76, 0, 27, 27, "NoPat", Pat.NOPAT));
        add(getPatButton(horizButton, 76, 26, 27, 27, "HorizPat", Pat.HSTRP));
        add(getPatButton(vertButton, 76, 52, 27, 27, "VertPat", Pat.VSTRP));
        add(getPatButton(diagButton, 76, 78, 27, 27, "DiagPat", Pat.DIAG));
        add(getPatButton(squareButton, 76, 104, 27, 27, "SquarePat", Pat.SQUARED));
        add(getPatButton(borderButton, 76, 130, 27, 27, "BorderPat", Pat.BORDER));

    }

    public void syncPanel() {
        for (Pat pat : patterns.keySet()) {
            JRadioButton button = patterns.get(pat);
            if (SmedAction.panelMain.mark.getPattern(ent) == pat) {
                button.setBorderPainted(true);
            } else {
                button.setBorderPainted(false);
            }
        }
        panelCol.syncPanel();
    }

    private JRadioButton getPatButton(JRadioButton button, int x, int y, int w, int h, String tip, Pat pat) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alPat);
        patButtons.add(button);
        patterns.put(pat, button);
        return button;
    }

}
