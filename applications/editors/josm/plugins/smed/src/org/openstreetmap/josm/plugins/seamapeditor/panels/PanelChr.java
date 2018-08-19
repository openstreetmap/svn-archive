// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EnumMap;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.plugins.seamapeditor.SmedAction;
import org.openstreetmap.josm.plugins.seamapeditor.messages.Messages;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Att;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Chr;
import org.openstreetmap.josm.plugins.seamapeditor.seamarks.SeaMark.Col;

public class PanelChr extends JPanel {

    private SmedAction dlg;
    public JLabel col1Label = new JLabel();
    public JLabel col2Label = new JLabel();
    public JLabel charLabel = new JLabel();
    public JTextField charBox = new JTextField();
    public JToggleButton noneButton = newJToggleButton("/images/NoCharButton.png");
    public JToggleButton fixedButton = newJToggleButton("/images/FixedButton.png");
    public JToggleButton flashButton = newJToggleButton("/images/FlashButton.png");
    public JToggleButton longFlashButton = newJToggleButton("/images/LongFlashButton.png");
    public JToggleButton quickButton = newJToggleButton("/images/QuickButton.png");
    public JToggleButton veryQuickButton = newJToggleButton("/images/VeryQuickButton.png");
    public JToggleButton ultraQuickButton = newJToggleButton("/images/UltraQuickButton.png");
    public JToggleButton interruptedQuickButton = newJToggleButton("/images/InterruptedQuickButton.png");
    public JToggleButton interruptedVeryQuickButton = newJToggleButton("/images/InterruptedVeryQuickButton.png");
    public JToggleButton interruptedUltraQuickButton = newJToggleButton("/images/InterruptedUltraQuickButton.png");
    public JToggleButton isophasedButton = newJToggleButton("/images/IsophasedButton.png");
    public JToggleButton occultingButton = newJToggleButton("/images/OccultingButton.png");
    public JToggleButton morseButton = newJToggleButton("/images/MorseButton.png");
    public JToggleButton alternatingButton = newJToggleButton("/images/AlternatingButton.png");
    private EnumMap<Chr, JToggleButton> buttons = new EnumMap<>(Chr.class);
    private ActionListener alCharButton = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton source = (JToggleButton) e.getSource();
            EnumSet<Chr> combo = EnumSet.noneOf(Chr.class);
            for (Chr chr : buttons.keySet()) {
                JToggleButton button = buttons.get(chr);
                if (button.isSelected()) {
                    combo.add(chr);
                    button.setBorderPainted(true);
                } else {
                    combo.remove(chr);
                    button.setBorderPainted(false);
                }
            }
            if (SeaMark.ChrMAP.containsKey(combo)) {
                charBox.setText(SeaMark.ChrMAP.get(combo));
            } else {
                for (Chr chr : buttons.keySet()) {
                    JToggleButton button = buttons.get(chr);
                    if (button == source) {
                        charBox.setText(SeaMark.ChrMAP.get(EnumSet.of(chr)));
                        button.setSelected(true);
                        button.setBorderPainted(true);
                    } else {
                        button.setSelected(false);
                        button.setBorderPainted(false);
                    }
                }
            }
            String str = charBox.getText();
            SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, str);
            if (!str.contains("Al")) {
                col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.COL, 0)));
                SmedAction.panelMain.mark.setLightAtt(Att.ALT, 0, Col.UNKCOL);
            } else {
                col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.ALT, 0)));
            }
        }
    };

    private FocusListener flCharBox = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e) {
            String str = charBox.getText();
            SmedAction.panelMain.mark.setLightAtt(Att.CHR, 0, str);
            EnumSet<Chr> set = EnumSet.noneOf(Chr.class);
            for (EnumSet<Chr> map : SeaMark.ChrMAP.keySet()) {
                if (str.equals(SeaMark.ChrMAP.get(map))) {
                    set = map;
                    break;
                }
            }
            for (Chr chr : buttons.keySet()) {
                JToggleButton button = buttons.get(chr);
                if (set.contains(chr)) {
                    button.setSelected(true);
                    button.setBorderPainted(true);
                } else {
                    button.setSelected(false);
                    button.setBorderPainted(false);
                }
            }
            if (!str.contains("Al")) {
                col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.COL, 0)));
                SmedAction.panelMain.mark.setLightAtt(Att.ALT, 0, Col.UNKCOL);
            } else {
                col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.ALT, 0)));
            }
        }
    };

    public PanelChr(SmedAction dia) {
        dlg = dia;
        setLayout(null);
        add(getChrButton(noneButton, 0, 0, 44, 16, Messages.getString("NoChar"), Chr.UNKCHR));
        add(getChrButton(fixedButton, 0, 16, 44, 16, Messages.getString("FChar"), Chr.FIXED));
        add(getChrButton(flashButton, 0, 32, 44, 16, Messages.getString("FlChar"), Chr.FLASH));
        add(getChrButton(longFlashButton, 0, 48, 44, 16, Messages.getString("LFlChar"), Chr.LFLASH));
        add(getChrButton(quickButton, 0, 64, 44, 16, Messages.getString("QChar"), Chr.QUICK));
        add(getChrButton(veryQuickButton, 0, 80, 44, 16, Messages.getString("VQChar"), Chr.VQUICK));
        add(getChrButton(ultraQuickButton, 0, 96, 44, 16, Messages.getString("UQChar"), Chr.UQUICK));
        add(getChrButton(alternatingButton, 44, 0, 44, 16, Messages.getString("AlChar"), Chr.ALTERNATING));
        add(getChrButton(isophasedButton, 44, 16, 44, 16, Messages.getString("IsoChar"), Chr.ISOPHASED));
        add(getChrButton(occultingButton, 44, 32, 44, 16, Messages.getString("OcChar"), Chr.OCCULTING));
        add(getChrButton(morseButton, 44, 48, 44, 16, Messages.getString("MoChar"), Chr.MORSE));
        add(getChrButton(interruptedQuickButton, 44, 64, 44, 16, Messages.getString("IQChar"), Chr.IQUICK));
        add(getChrButton(interruptedVeryQuickButton, 44, 80, 44, 16, Messages.getString("IVQChar"), Chr.IVQUICK));
        add(getChrButton(interruptedUltraQuickButton, 44, 96, 44, 16, Messages.getString("IUQChar"), Chr.IUQUICK));
        charLabel.setBounds(new Rectangle(0, 113, 88, 20));
        charLabel.setHorizontalAlignment(SwingConstants.CENTER);
        charLabel.setText(Messages.getString("Character"));
        add(charLabel);
        col1Label.setBounds(new Rectangle(10, 135, 10, 20));
        col1Label.setOpaque(true);
        add(col1Label);
        col2Label.setBounds(new Rectangle(70, 135, 10, 20));
        col2Label.setOpaque(true);
        add(col2Label);
        charBox.setBounds(new Rectangle(20, 135, 50, 20));
        charBox.setHorizontalAlignment(SwingConstants.CENTER);
        add(charBox);
        charBox.addFocusListener(flCharBox);
    }

    private static JToggleButton newJToggleButton(String buttonIcon) {
        return new JToggleButton(new ImageIcon(PanelChr.class.getResource(buttonIcon)));
    }

    public void syncPanel() {
        String str = (String) SmedAction.panelMain.mark.getLightAtt(Att.CHR, 0);
        charBox.setText(str);
        EnumSet<Chr> set = EnumSet.noneOf(Chr.class);
        for (EnumSet<Chr> map : SeaMark.ChrMAP.keySet()) {
            if (dlg.node != null && str.equals(SeaMark.ChrMAP.get(map))) {
                set = map;
                break;
            }
        }
        if (!str.contains("Al")) {
            col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.COL, 0)));
        } else {
            col2Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.ALT, 0)));
        }
        col1Label.setBackground(SeaMark.ColMAP.get(SmedAction.panelMain.mark.getLightAtt(Att.COL, 0)));
        for (Chr chr : buttons.keySet()) {
            JToggleButton button = buttons.get(chr);
            if (set.contains(chr)) {
                button.setSelected(true);
                button.setBorderPainted(true);
            } else {
                button.setSelected(false);
                button.setBorderPainted(false);
            }
        }
    }

    private JToggleButton getChrButton(JToggleButton button, int x, int y, int w, int h, String tip, Chr chr) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setBorderPainted(false);
        button.setToolTipText(tr(tip));
        button.addActionListener(alCharButton);
        buttons.put(chr, button);
        return button;
    }

}
