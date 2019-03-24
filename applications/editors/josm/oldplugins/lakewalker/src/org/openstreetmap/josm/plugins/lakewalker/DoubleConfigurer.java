// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

/**
 * A Configurer for Double values
 */
public class DoubleConfigurer extends StringConfigurer {

    public DoubleConfigurer() {
        super();
    }

    public DoubleConfigurer(String key, String name) {
        this(key, name, 0d);
    }

    public DoubleConfigurer(String key, String name, Double val) {
        super(key, name, val == null ? null : val.toString());
    }

    @Override
    public void setValue(String s) {
        Double d = null;
        try {
            d = Double.valueOf(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        if (d != null)
            setValue(d);
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
        if (value == null || value.equals("")) {
            return null;
        }
        return value.toString();
    }
}
