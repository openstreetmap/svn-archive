// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class DownloadDataAction extends JosmAction {

    private final URL url;
    
    public DownloadDataAction(Module module, String name, URL url) {
        super(false);
        CheckParameterUtil.ensureParameterNotNull(name, "name");
        CheckParameterUtil.ensureParameterNotNull(url, "url");
        putValue(Action.NAME, name);
        putValue("toolbar", ("opendata_download_"+module.getDisplayedName()+"_"+name).toLowerCase().replace(" ", "_"));
        this.url = url;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Main.main.menu.openLocation.openUrl(true, url.toString());
    }
}
