// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * A Configurer that returns a String from among a list of possible values
 */
public class StringEnumConfigurer extends Configurer {
    protected String[] validValues;
    protected String[] transValues;
    protected JComboBox<String> box;
    protected Box panel;
    protected String tooltipText = "";

    public StringEnumConfigurer(String key, String name, String[] validValues) {
        super(key, name);
        this.validValues = validValues;
        transValues = new String[validValues.length];
        for (int i = 0; i < validValues.length; ++i) {
            transValues[i] = tr(validValues[i]);
        }
    }

    public StringEnumConfigurer(String[] validValues) {
        this(null, "", validValues);
    }

    public void setToolTipText(String s) {
        tooltipText = s;
    }

    @Override
    public Component getControls() {
        if (panel == null) {
            panel = Box.createHorizontalBox();
            panel.add(new JLabel(name));
            box = new JComboBox<>(transValues);
            box.setToolTipText(tooltipText);
            box.setMaximumSize(new Dimension(box.getMaximumSize().width, box.getPreferredSize().height));
            setValue(value);
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    noUpdate = true;
                    setValue(box.getSelectedIndex());
                    noUpdate = false;
                }
            });
            panel.add(box);
        }
        return panel;
    }

    @Override
    public void setValue(Object o) {
        if (o == null)
            o = 0;
        super.setValue(o);
        if (!noUpdate && box != null)
            box.setSelectedIndex((Integer) o);
    }

    @Override
    public void setValue(String s) {
        Integer n = 0;
        for (int i = 0; i < transValues.length; ++i) {
            if (transValues[i].equals(s) || validValues[i].equals(s)) {
                n = i;
                break;
            }
        }
        setValue(n);
    }

    @Override
    public String getValueString() {
        return validValues[(Integer) value];
    }
}
