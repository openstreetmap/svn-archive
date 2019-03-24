// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.lakewalker;

import java.awt.Component;
import java.beans.PropertyChangeListener;

/**
 * A property editor class.  Wraps an Object value and provides
 * methods for saving and restoring the Object from a String.  Also
 * includes a {@link java.awt.Component} that can be placed into a
 * property editing window, allowing the user to change the value
 * interactively.
 * */
public abstract class Configurer {
  // FIXME: maybe parameterize this so that value can have the right type in subclasses?
  public static final String NAME_PROPERTY = "Configurer.name";

  /** A String the uniquely identifies this property */
  protected String key;
  /** A String that provides a short description of the property to the user */
  protected String name;
  /** The value */
  protected Object value;
  protected java.beans.PropertyChangeSupport changeSupport;
  /** When noUpdate is true, setting the value programmatically will not
   * result in an update of the GUI Component */
  protected boolean noUpdate = false;
  /** When frozen is true, setting the value programmatically will not
   * result in a PropertyChangeEvent being fired */
  protected boolean frozen = false;

  public Configurer(String key, String name) {
    this(key, name, null);
  }

  public Configurer(String key, String name, Object val) {
    this.key = key;
    this.name = name;
    changeSupport = new java.beans.PropertyChangeSupport(this);
    setValue(val);
  }

  /**
   * Unique identifier
   * @return Unique identifier
   */
  public String getKey() {
    return key;
  }

  /**
   * Plain English description of the Object
   * @return Plain English description of the Object
   */
  public String getName() {
    return name;
  }

  public void setName(String s) {
    String oldName = name;
    name = s;
    if (!frozen) {
      changeSupport.firePropertyChange(NAME_PROPERTY, oldName, name);
    }
  }

  /**
   * The Object value
   * May be null if the Object has not been initialized
   * @return The Object value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @return a String representation of the Object value
   */
  public abstract String getValueString();

  /**
   * Set the Object value
   * @param o the Object value
   */
  public void setValue(Object o) {
    Object oldValue = getValue();
    value = o;
    if (!frozen) {
      changeSupport.firePropertyChange(key, oldValue, value);
    }
  }

  /**
   * If true, then don't fire PropertyChangeEvents when the value is reset
   * @param val true to disable firing PropertyChangeEvents when the value is reset
   */
  public void setFrozen(boolean val) {
    frozen = val;
  }

  public boolean isFrozen() {
    return frozen;
  }

  /**
   * Fire a PropertyChangeEvent as if the value had been set from null
   */
  public void fireUpdate() {
    changeSupport.firePropertyChange(key, null, value);
  }

  /**
   * Set the Object value from a String
   * @param s Object value as String
   */
  public abstract void setValue(String s);

  /**
   * GUI interface for setting the option in an editing window
   * @return GUI interface for setting the option in an editing window
   */
  public abstract Component getControls();

  /**
   * Add a listener to be notified when the Object state changes
   * @param l listener to add
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    changeSupport.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    changeSupport.removePropertyChangeListener(l);
  }
}
