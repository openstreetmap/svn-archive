// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pdfimport;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;


public class GuiFieldInteger extends GuiFieldString {

    private int value;
    public CheckInteger checker;

    public GuiFieldInteger() {
        super();
        addFocusListener(checker = new CheckInteger());
        setValue(0);
    }

    @SuppressWarnings("unused")
    private GuiFieldInteger(String text) {
        super(text);
        addFocusListener(checker = new CheckInteger());
    }

    public GuiFieldInteger(int v) {
        super();
        addFocusListener(checker = new CheckInteger());
        setValue(v);
    }

    public void setValue(int v) {
        super.setText(Integer.toString(v));
        value=v;
        this.checker.check(this);
    }

    public int getValue() throws NumberFormatException {
        if (!dataValid) throw new NumberFormatException();
        return value;
    }

    public class CheckInteger implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            check((GuiFieldInteger) event.getSource());
        }

        public void check(GuiFieldInteger field) {
            try {
                value = Integer.valueOf(field.getText());
                dataValid = true;
                field.setBorder(defaultBorder);
            } catch (NumberFormatException e) {
                field.setBorder(BorderFactory.createLineBorder(Color.red));
                dataValid = false;
            }
        }
    }
}