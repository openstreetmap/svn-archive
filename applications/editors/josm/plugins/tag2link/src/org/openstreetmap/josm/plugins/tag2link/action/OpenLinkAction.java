package org.openstreetmap.josm.plugins.tag2link.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkConstants;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.tools.OpenBrowser;

public class OpenLinkAction extends JosmAction implements Tag2LinkConstants {

    private String url;
    
    public OpenLinkAction(Link link) {
        super(tr(link.name), ICON_16, tr("Launch browser with information about the selected object"), null, true);
        this.url = link.url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenBrowser.displayUrl(url);
    }
}
