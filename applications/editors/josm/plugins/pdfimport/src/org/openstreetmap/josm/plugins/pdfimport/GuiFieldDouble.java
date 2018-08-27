// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pdfimport;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;

public class GuiFieldDouble extends GuiFieldString {

    private double value;
    public CheckDouble checker;

    public GuiFieldDouble() {
        super();
        addFocusListener(checker = new CheckDouble());
        setValue(0.0);
    }

    @SuppressWarnings("unused")
    private GuiFieldDouble(String text) {
        super(text);
        addFocusListener(checker = new CheckDouble());
    }

    public GuiFieldDouble(double v) {
        super();
        addFocusListener(checker = new CheckDouble());
        setValue(v);
    }

    public void setValue(double v) {
        super.setText(Double.toString(v));
        this.checker.check(this);
    }

    public double getValue() throws NumberFormatException {
        if (!dataValid) throw new NumberFormatException();
        return value;
    }

    public class CheckDouble implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            check((GuiFieldDouble) event.getSource());
        }

        public void check(GuiFieldDouble field) {
            try {
                value = Double.parseDouble(field.getText());
                dataValid =true;
                field.setBorder(defaultBorder);
            } catch (NumberFormatException e) {
                field.setBorder(BorderFactory.createLineBorder(Color.red));
                value = Double.NaN;
                dataValid = false;
            }
        }
    }
}
