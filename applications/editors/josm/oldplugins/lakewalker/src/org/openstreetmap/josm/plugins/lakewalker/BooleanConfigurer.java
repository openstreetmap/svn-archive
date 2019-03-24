// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

import java.awt.Component;

import javax.swing.JCheckBox;

/**
 * Configurer for Boolean values
 */
public class BooleanConfigurer extends Configurer {
    private JCheckBox box;

    public BooleanConfigurer() {
        this(false);
    }

    public BooleanConfigurer(boolean val) {
        this(null, "", val);
    }

    public BooleanConfigurer(String key, String name, Boolean val) {
        super(key, name, val);
    }

    public BooleanConfigurer(String key, String name, boolean val) {
        super(key, name, val ? Boolean.TRUE : Boolean.FALSE);
    }

    public BooleanConfigurer(String key, String name) {
        this(key, name, Boolean.FALSE);
    }

    @Override
    public String getValueString() {
        return booleanValue().toString();
    }

    @Override
    public void setValue(Object o) {
        super.setValue(o);
        if (box != null
                && !o.equals(box.isSelected())) {
            box.setSelected(booleanValue().booleanValue());
        }
    }

    @Override
    public void setValue(String s) {
        setValue(Boolean.valueOf(s));
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        if (box != null) {
            box.setText(s);
        }
    }

    @Override
    public Component getControls() {
        if (box == null) {
            box = new javax.swing.JCheckBox(getName());
            box.setSelected(booleanValue().booleanValue());
            box.addItemListener(new java.awt.event.ItemListener() {
                @Override
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    setValue(box.isSelected());
                }
            });
        }
        return box;
    }

    public Boolean booleanValue() {
        return (Boolean) value;
    }
}
