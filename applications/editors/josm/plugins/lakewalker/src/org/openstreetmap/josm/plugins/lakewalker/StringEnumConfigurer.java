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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * A Configurer that returns a String from among a list of possible values
 */
public class StringEnumConfigurer extends Configurer {
  protected String[] validValues;
  protected JComboBox box;
  protected Box panel;
  protected String tooltipText = "";
 
  public StringEnumConfigurer(String key, String name, String[] validValues) {
    super(key, name);
    this.validValues = validValues;
  }

  public StringEnumConfigurer(String[] validValues) {
    this(null, "", validValues);
  }
  
  public void setToolTipText(String s) {
    tooltipText = s;
  }
  public Component getControls() {
    if (panel == null) {
      panel = Box.createHorizontalBox();
      panel.add(new JLabel(name));
      box = new JComboBox(validValues);
      box.setToolTipText(tooltipText);
      box.setMaximumSize(new Dimension(box.getMaximumSize().width,box.getPreferredSize().height));
      if (isValidValue(getValue())) {
        box.setSelectedItem(getValue());
      }
      else if (validValues.length > 0) {
        box.setSelectedIndex(0);
      }
      box.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          noUpdate = true;
          setValue(box.getSelectedItem());
          noUpdate = false;
        }
      });
      panel.add(box);
    }
    return panel;
  }

  public boolean isValidValue(Object o) {
    for (int i = 0; i < validValues.length; ++i) {
      if (validValues[i].equals(o)) {
        return true;
      }
    }
    return false;
  }

  public String[] getValidValues() {
    return validValues;
  }

  public void setValidValues(String[] s) {
  	validValues = s;
  	if (box == null) {
  	  getControls();
  	}
	box.setModel(new DefaultComboBoxModel(validValues));
  }
  
  public void setValue(Object o) {
    if (validValues == null
        || isValidValue(o)) {
      super.setValue(o);
      if (!noUpdate && box != null) {
        box.setSelectedItem(o);
      }
    }
  }

  public String getValueString() {
    return box != null ? (String) box.getSelectedItem() : validValues[0];
  }

  public void setValue(String s) {
    setValue((Object) s);
  }

}
