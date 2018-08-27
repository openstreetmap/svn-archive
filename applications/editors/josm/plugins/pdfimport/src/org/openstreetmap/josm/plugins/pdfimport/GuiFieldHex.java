/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;

/**
 * @author Nzara
 *
 */
public class GuiFieldHex extends GuiFieldString {

    private int value;
    public CheckHex checker;

    public GuiFieldHex() {
        super();
        addFocusListener(checker = new CheckHex());
        setValue(0);
    }

    @SuppressWarnings("unused")
    public GuiFieldHex(String text) {
        super(text);
        addFocusListener(checker = new CheckHex());
    }

    public GuiFieldHex(int v) {
        super();
        addFocusListener(checker = new CheckHex());
        setValue(v);
    }

    public void setValue(int v) {
        super.setText("#" + Integer.toHexString(v));
        value=v;
        this.checker.check(this);
    }

    public int getValue() throws NumberFormatException {
        if (!dataValid) throw new NumberFormatException();
        return value;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.checker.check(this);
    }

    public class CheckHex implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            check((GuiFieldHex) event.getSource());
        }

        public void check(GuiFieldHex field) {
            try {
                value = Integer.decode(field.getText());
//                value = Integer.parseUnsignedInt(field.getText().replace("#", ""), 16);
                dataValid = true;
                field.setBorder(defaultBorder);
            } catch (NumberFormatException e) {
                field.setBorder(BorderFactory.createLineBorder(Color.red));
                dataValid = false;
            }
        }
    }
}
