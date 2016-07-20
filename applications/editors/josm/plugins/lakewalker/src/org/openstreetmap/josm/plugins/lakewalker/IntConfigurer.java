// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

/**
 * A Configurer for Integer values
 */
public class IntConfigurer extends StringConfigurer {

    public IntConfigurer() {
        super();
    }

    public IntConfigurer(String key, String name) {
        this(key, name, 0);
    }

    public IntConfigurer(String key, String name, Integer val) {
        super(key, name);
        if (val != null) {
            setValue(val);
        }
    }

    @Override
    public void setValue(String s) {
        Integer i = null;
        try {
            i = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            i = null;
        }
        if (i != null) {
            setValue(i);
        }
    }

    public int getIntValue(int defaultValue) {
        if (getValue() instanceof Integer) {
            return ((Integer) getValue()).intValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public void setValue(Object o) {
        if (!noUpdate && nameField != null && o != null) {
            nameField.setText(o.toString());
        }
        super.setValue(o);
    }

    @Override
    public String getValueString() {
        return value == null ? null : value.toString();
    }
}
