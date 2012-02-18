// License: GPL. Copyright 2007 by Immanuel Scholz and others
package mirrored_download;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.JosmAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadGpsTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * TODO: Write comment.
 */
public class UrlSelectionAction extends JosmAction {

    public UrlSelectionAction() {
        super(tr("Select URL..."), null, tr("Select URL to download from."),
                Shortcut.registerShortcut("file:selecturl", tr("File: {0}", tr("Select URL...")), KeyEvent.VK_D, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/SelectUrl"));
    }

    public void actionPerformed(ActionEvent e) {
        UrlSelectionDialog dialog = UrlSelectionDialog.getInstance();
        dialog.setVisible(true);
    }
}
