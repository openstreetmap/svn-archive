package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

import ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop.CustomizePublicTransportStopDialog;

/**
 * 
 * @author Rodion Scherbakov
 * Class of customizing of stop area
 * Action for josm editor
 */
public class CustomizeStopAction extends JosmAction implements IStopAreaCustomizer
{

	/**
	 * Menu icon file name
	 */
	private static final String CUSTOMIZE_STOP_ACTION_ICON_NAME = "bus.png";
	/**
	 * Menu item tooltip
	 */
	private static final String CUSTOMIZE_STOP_ACTION_MENU_TOOLTIP = "Customize stop under osm public transit standard v2";
	/**
	 * Menu item caption
	 */
	private static final String CUSTOMIZE_STOP_ACTION_MENU_NAME = "Customize stop";
	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = 6769508902749446137L;

	/**
	 * Constructor of stop area customizing action
	 * @param name Name of action in josm menu
	 * @param iconName Name of icon file for josm menu
	 * @param tooltip Tooltip of action in josm menu
	 * @param shortcut Short key of action on josm
	 * @param registerInToolbar Flag of registration in josm menu
	 */
	protected CustomizeStopAction(String name, String iconName, String tooltip,
            Shortcut shortcut, boolean registerInToolbar) 
	{
        super(name, iconName, tooltip, shortcut, registerInToolbar);
    }

    /**
     * Constructs a stop area customizing action.
     * @return the stop area customizing action
     */
    public static CustomizeStopAction createCustomizeStopAction() 
    {
    	CustomizeStopAction action = new CustomizeStopAction(
                tr(CUSTOMIZE_STOP_ACTION_MENU_NAME), CUSTOMIZE_STOP_ACTION_ICON_NAME,
                tr(CUSTOMIZE_STOP_ACTION_MENU_TOOLTIP),
                Shortcut.registerShortcut("tools:customizestop", tr("Tool: {0}", tr(CUSTOMIZE_STOP_ACTION_MENU_NAME)),
                		KeyEvent.VK_U, Shortcut.DIRECT), true);
        action.putValue("help", ht("/Action/CustomizeStopAction"));
        return action;
    }

    /**
     * Realization of action
     * Construct stop area object from selected object and show settings dialog
     */
    @Override
	public void actionPerformed(ActionEvent arg0) 
    {
		if (!isEnabled())
			return;
		CreateStopAreaFromSelectedObjectOperation createStopAreaFromSelectedObjectOperation = new CreateStopAreaFromSelectedObjectOperation(
		        getLayerManager().getEditDataSet());
		StopArea stopArea = createStopAreaFromSelectedObjectOperation.performCustomizing(null);
		if(stopArea == null)
			return;
		CustomizePublicTransportStopDialog dialog = new CustomizePublicTransportStopDialog(this, stopArea);
		dialog.setVisible(true);
	}

	/**
	 * Perform stop area customizing under user selection
	 * This method is launched by stop area settings dialog 
	 * @param stopArea Stop area object with new settings
	 */
	@Override
	public StopArea performCustomizing(StopArea stopArea)
	{
		CustomizeStopAreaOperation customizeStopAreaOperation = new CustomizeStopAreaOperation(getLayerManager().getEditDataSet());
		return customizeStopAreaOperation.performCustomizing(stopArea);
	}
    
}
