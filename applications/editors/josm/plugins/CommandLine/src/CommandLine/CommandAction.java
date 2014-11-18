/*
 *      CommandAction.java
 *
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *
 */
package CommandLine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.ImageProvider;

public class CommandAction extends JosmAction {
    private final CommandLine parentPlugin;
    private final Command parentCommand;
    public CommandAction(Command parentCommand, CommandLine parentPlugin) {
        super(tr(parentCommand.name), "blankmenu", tr(parentCommand.name), null, true, parentCommand.name, true);
        if (!parentCommand.icon.equals("")) {
            try {
                putIcons(CommandLine.pluginDir, parentCommand.icon);
            } catch (RuntimeException e) {
                putIcons(null, "blankmenu");
            }
        }

        this.parentCommand = parentCommand;
        this.parentPlugin = parentPlugin;
    }

    private void putIcons(String subdir, String name) {
        putValue(Action.SMALL_ICON, new ImageProvider(subdir, name).setSize(ImageProvider.ImageSizes.SMALLICON).get());
        putValue(Action.LARGE_ICON_KEY, new ImageProvider(subdir, name).setSize(ImageProvider.ImageSizes.LARGEICON).get());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parentPlugin.startCommand(parentCommand);
        parentPlugin.history.addItem(parentCommand.name);
    }
}
