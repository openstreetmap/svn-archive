/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;

import org.openstreetmap.josm.tools.ImageProvider;

/**
 * this class represents a button as it is described in the xml file.
 * @author cdaller
 *
 */
public class ButtonDescription {
    private String label;
    private String hotkey;
    private String iconName;
    private ButtonType type;
    private List<SurveyorActionDescription> actions;
    private GpsDataSource gpsDataSource;

    /**
     * Default Constructor
     */
    public ButtonDescription() {
        super();
    }
    /**
     * @param hotkey
     * @param actions a list of actions to be performed.
     * @param type if <code>null</code> {@link ButtonType#SINGLE} is used.
     */
    public ButtonDescription(String label, String hotkey, String iconName, String buttonAction, ButtonType type) {
        this(label, hotkey, iconName, createFromOneElement(new SurveyorActionDescription(buttonAction)), type);
    }

    /**
     * @param hotkey
     * @param actions a list of actions to be performed.
     * @param type if <code>null</code> {@link ButtonType#SINGLE} is used.
     */
    public ButtonDescription(String label, String hotkey, String iconName, SurveyorActionDescription actionDescription, ButtonType type) {
        this(label, hotkey, iconName, createFromOneElement(actionDescription), type);
    }

    /**
     * Helper method to create a list from one element.
     * @param buttonActionClassName the action's class name.
     * @return a list holding one ButtonActionDescription element.
     */
    private static List<SurveyorActionDescription> createFromOneElement(SurveyorActionDescription actionDescription) {
        List<SurveyorActionDescription> list = new ArrayList<SurveyorActionDescription>();
        list.add(actionDescription);
        return list;
    }

    /**
     * @param hotkey
     * @param actions a list of actions to be performed.
     * @param type if <code>null</code> {@link ButtonType#SINGLE} is used.
     */
    public ButtonDescription(String label, String hotkey, String iconName, List<SurveyorActionDescription> actions, ButtonType type) {
        super();
        this.label = label;
        this.hotkey = hotkey;
        this.iconName = iconName;
        if(type == null) {
            this.type = ButtonType.SINGLE;
        } else {
            this.type = type;
        }
        this.actions = actions;
    }

    /**
     * @return the actions
     */
    public List<SurveyorActionDescription> getActions() {
        return this.actions;
    }
    /**
     * @param actions the actions to set
     */
    public void setActions(List<SurveyorActionDescription> actions) {
        this.actions = actions;
    }
    /**
     * @return the hotkey
     */
    public String getHotkey() {
        return this.hotkey;
    }
    /**
     * @param hotkey the hotkey to set
     */
    public void setHotkey(String hotkey) {
        this.hotkey = hotkey;
    }
    /**
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the type
     */
    public ButtonType getButtonType() {
        return this.type;
    }

    /**
     * Set the button type as a string.
     * @param type the type of the button
     * @see ButtonType
     */
    public void setType(String type) {
        try {
        this.type = ButtonType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown button type '" + type + "' given. Allowed values are " + Arrays.toString(ButtonType.values()));
        }
    }
    /**
     * @param type the type to set
     */
    public void setButtonType(ButtonType type) {
        this.type = type;
    }

    /**
     * Sets the name of the icon.
     * @param icon
     */
    public void setIcon(String icon) {
        this.iconName = icon;
    }

    /**
     * Creates the button.
     * @return the button.
     */
    public JComponent createComponent() {

        String actionName = tr(getLabel()) + " (" + hotkey + ")";

        Icon icon = ImageProvider.getIfAvailable(null,iconName);
        if (icon == null)
            icon = ImageProvider.getIfAvailable("markers",iconName);
        if (icon == null)
            icon = ImageProvider.getIfAvailable("symbols",iconName);
        if (icon == null)
            icon = ImageProvider.getIfAvailable("nodes",iconName);

        MetaAction action = new MetaAction(actionName, icon);
        action.setActions(actions);
        action.setGpsDataSource(gpsDataSource);

        AbstractButton button;
        if(type == ButtonType.TOGGLE) {
            button = new JToggleButton(action);
            connectActionAndButton(action, button);
        } else {
            button = new JButton(action);
        }
        button.setActionCommand(label);

        // connect component keyboard map with buttons:
        ActionMap actionMap = new ActionMapUIResource();
        actionMap.put(actionName, action);

        InputMap keyMap = new ComponentInputMap(button);
        keyMap.put(KeyStroke.getKeyStroke(hotkey), actionName);

        SwingUtilities.replaceUIActionMap(button, actionMap);
        SwingUtilities.replaceUIInputMap(button, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);
        return button;
    }

    private static void connectActionAndButton(Action action, AbstractButton button) {
        SelectionStateAdapter adapter = new SelectionStateAdapter(action, button);
        adapter.configure();
    }

    /**
     * Class that connects the selection state of the action
     * to the selection state of the button.
     *
     * @author R.J. Lorimer
     */
    private static class SelectionStateAdapter implements PropertyChangeListener, ItemListener {
        private Action action;
        private AbstractButton button;
        public SelectionStateAdapter(Action theAction, AbstractButton theButton) {
            action = theAction;
            button = theButton;
        }
        protected void configure() {
            action.addPropertyChangeListener(this);
            button.addItemListener(this);
        }
        public void itemStateChanged(ItemEvent e) {
            boolean value = e.getStateChange() == ItemEvent.SELECTED;
            Boolean valueObj = Boolean.valueOf(value);
            action.putValue(ActionConstants.SELECTED_KEY, valueObj);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ActionConstants.SELECTED_KEY)) {
                Boolean newSelectedState = (Boolean)evt.getNewValue();
                button.setSelected(newSelectedState.booleanValue());
            }
        }
    }

    /**
     * Set the source of gps data.
     * @param gpsDataSource the source from where gps data can be obtained.
     */
    public void setGpsDataSource(GpsDataSource gpsDataSource) {
        this.gpsDataSource = gpsDataSource;
    }
}
