// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.terracer;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Destroyable;

/**
 * Plugin interface implementation for Terracer.
 *
 * @author zere - Copyright 2009 CloudMade Ltd
 */
public class TerracerPlugin extends Plugin implements Destroyable {
	private List<JosmAction> actions = Arrays.asList(new TerracerAction(), new ReverseTerraceAction());

    public TerracerPlugin(PluginInformation info) {
        super(info);
        for (JosmAction action : actions) {
        	MainMenu.add(MainApplication.getMenu().moreToolsMenu, action);
        }
    }

	@Override
	public void destroy() {
        final JMenu moreToolsMenu = MainApplication.getMenu().moreToolsMenu;
        final Map<Action, Component> actionsMap = Arrays.asList(moreToolsMenu.getMenuComponents()).stream()
                .filter(JMenuItem.class::isInstance).map(JMenuItem.class::cast)
                .collect(Collectors.toMap(JMenuItem::getAction, component -> component));

        for (final Entry<Action, Component> action : actionsMap.entrySet()) {
            if (actions.contains(action.getKey())) {
                moreToolsMenu.remove(action.getValue());
            }
        }
		actions.forEach(JosmAction::destroy);
	}
}
