/*
 * $Id: DoubleConfigurer.java 5 2003-10-02 15:11:38 +0000 (Thu, 02 Oct 2003) rodneykinney $
 *
 * Copyright (c) 2000-2007 by Rodney Kinney, Brent Easton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package org.openstreetmap.josm.plugins.lakewalker;

/**
 * A Configurer for Double values
 */
public class DoubleConfigurer extends StringConfigurer {

    public DoubleConfigurer() {
        super();
    }

    public DoubleConfigurer(String key, String name) {
        this(key, name, new Double(0));
    }

    public DoubleConfigurer(String key, String name, Double val) {
        super(key, name, val == null ? null : val.toString());
    }

    public void setValue(String s) {
        Double d = null;
        try {
            d = Double.valueOf(s);
        }
        catch (NumberFormatException e) {
            d = null;
        }
        if (d != null)
            setValue(d);
    }

    public void setValue(Object o) {
        if (!noUpdate && nameField != null && o != null) {
            nameField.setText(o.toString());
        }
        super.setValue(o);
    }

    public String getValueString() {
        if (value == null || value.equals("")) {
            return null;
        }
        return value.toString();
    }
}
