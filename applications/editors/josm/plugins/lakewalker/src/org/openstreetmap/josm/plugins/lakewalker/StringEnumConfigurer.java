/*
 * $Id: StringEnumConfigurer.java 2472 2007-10-01 04:10:19 +0000 (Mon, 01 Oct 2007) rodneykinney $
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
/*
 * Created by IntelliJ IDEA.
 * User: rkinney
 * Date: Jul 20, 2002
 * Time: 3:52:36 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
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
    protected JComboBox box;
    protected Box panel;
    protected String tooltipText = "";

    public StringEnumConfigurer(String key, String name, String[] validValues) {
        super(key, name);
        this.validValues = validValues;
        transValues = new String[validValues.length];
        for(int i = 0; i < validValues.length; ++i)
            transValues[i] = tr(validValues[i]);
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
            box = new JComboBox(transValues);
            box.setToolTipText(tooltipText);
            box.setMaximumSize(new Dimension(box.getMaximumSize().width,box.getPreferredSize().height));
            setValue(value);
            box.addActionListener(new ActionListener() {
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
        if(o == null)
            o = 0;
        super.setValue(o);
        if(!noUpdate && box != null)
            box.setSelectedIndex((Integer)o);
    }

    @Override
	public void setValue(String s) {
        Integer n = 0;
        for (int i = 0; i < transValues.length; ++i)
        {
            if (transValues[i].equals(s) || validValues[i].equals(s)){
                n = i;
                break;
            }
        }
        setValue(n);
    }

    @Override
	public String getValueString() {
        return validValues[(Integer)value];
    }
}
