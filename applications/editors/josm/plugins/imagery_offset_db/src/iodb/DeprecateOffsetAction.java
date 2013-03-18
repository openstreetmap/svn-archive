package iodb;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.*;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author zverik
 */
public class DeprecateOffsetAction extends AbstractAction {
    private ImageryOffsetBase offset;
    
    public DeprecateOffsetAction( ImageryOffsetBase offset ) {
        super(tr("Deprecate Offset"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
        this.offset = offset;
        setEnabled(offset != null && !offset.isDeprecated());
    }

    public void actionPerformed(ActionEvent e) {
        if( Main.map == null || Main.map.mapView == null || !Main.map.isVisible() )
            return;
        
        if( JOptionPane.showConfirmDialog(Main.parent,
                tr("Warning: deprecation is irreversible"), // todo: expand
                ImageryOffsetTools.DIALOG_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ) {
            return;
        }
        deprecateOffset(offset);
    }

    public static void deprecateOffset( ImageryOffsetBase offset ) {
        String userName = JosmUserIdentityManager.getInstance().getUserName();
        if( userName == null ) {
            JOptionPane.showMessageDialog(Main.parent, tr("To store imagery offsets you must be a registered OSM user."), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = "Please enter the reason why you mark this "
                + (offset instanceof ImageryOffset ? "imagery offset" : "calibraion object") + " as deprecated:";
        String reason = null;
        boolean iterated = false;
        while( reason == null ) {
            reason = JOptionPane.showInputDialog(Main.parent, message, ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
            if( reason == null || reason.length() == 0 ) {
                return;
            }
            if( reason.length() < 3 || reason.length() > 200 ) {
                reason = null;
                if( !iterated ) {
                    message = message + "\n" + tr("Reason text should be 3 to 200 letters long.");
                    iterated = true;
                }
            }
        }
        
        try {
            String query = "deprecate?id=" + offset.getId()
                + "&author=" + URLEncoder.encode(userName, "UTF8")
                + "&reason=" + URLEncoder.encode(reason, "UTF8");
            SimpleOffsetQueryTask depTask = new SimpleOffsetQueryTask(query, tr("Notifying the server of the deprecation..."));
            Main.worker.submit(depTask);
        } catch( UnsupportedEncodingException ex ) {
            // WTF
        }
    }
}
