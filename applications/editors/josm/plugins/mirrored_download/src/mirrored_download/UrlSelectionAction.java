// License: GPL. For details, see LICENSE file.
package mirrored_download;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;


/**
 * TODO: Write comment.
 */
public class UrlSelectionAction extends JosmAction {

    public UrlSelectionAction() {
        super(tr("Select OSM mirror URL"), (String)null, tr("Select OSM mirror URL to download from."),
                null, true, "mirroreddownload/urlselection", true);
        putValue("help", ht("/Action/SelectUrl"));
    }

    public void actionPerformed(ActionEvent e) {
        UrlSelectionDialog dialog = UrlSelectionDialog.getInstance();
        dialog.setVisible(true);
    }
}
