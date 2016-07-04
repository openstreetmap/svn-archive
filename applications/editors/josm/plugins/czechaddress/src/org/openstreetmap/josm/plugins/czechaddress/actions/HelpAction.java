// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.czechaddress.actions;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.OpenBrowser;

/**
 * Action which shows the help page in browser.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class HelpAction extends JosmAction {

    public HelpAction() {
        super("Nápověda",
               "help.png",
               "Otevře nápovědu k pluginu CzechAddress",
               null, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenBrowser.displayUrl("http://wiki.openstreetmap.org/wiki/Cz:JOSM/Plugins/CzechAddress");
    }
}
