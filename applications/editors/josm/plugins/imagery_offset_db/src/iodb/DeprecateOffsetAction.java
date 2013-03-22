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
 * A context-dependent action to deprecate an offset.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class DeprecateOffsetAction extends AbstractAction {
    private ImageryOffsetBase offset;
    private QuerySuccessListener listener;
    
    /**
     * Initialize an action with an offset object.
     */
    public DeprecateOffsetAction( ImageryOffsetBase offset ) {
        super(tr("Deprecate Offset"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
        this.offset = offset;
        setEnabled(offset != null && !offset.isDeprecated());
    }

    /**
     * Asks a user if they really want to deprecate an offset (since this
     * action is virtually irreversible) and calls
     * {@link #deprecateOffset(iodb.ImageryOffsetBase, iodb.QuerySuccessListener)}
     * on a positive answer.
     */
    public void actionPerformed(ActionEvent e) {
        if( Main.map == null || Main.map.mapView == null || !Main.map.isVisible() )
            return;
        
        String desc = offset instanceof ImageryOffset ? "imagery offset is wrong"
                : "calibration geometry is aligned badly";
        if( JOptionPane.showConfirmDialog(Main.parent,
                tr("Warning: deprecation is basically irreversible!\nAre you sure this " + desc + "?"),
                ImageryOffsetTools.DIALOG_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ) {
            return;
        }
        deprecateOffset(offset, listener);
    }

    /**
     * Installs a listener to process successful deprecation event.
     */
    public void setListener( QuerySuccessListener listener ) {
        this.listener = listener;
    }

    /**
     * Deprecate the given offset.
     * @see #deprecateOffset(iodb.ImageryOffsetBase, iodb.QuerySuccessListener) 
     */
    public static void deprecateOffset( ImageryOffsetBase offset ) {
        deprecateOffset(offset, null);
    }

    /**
     * Deprecate the given offset and call listener on success. Asks user the reason
     * and executes {@link SimpleOffsetQueryTask} with a query to deprecate the offset.
     */
    public static void deprecateOffset( ImageryOffsetBase offset, QuerySuccessListener listener ) {
        String userName = JosmUserIdentityManager.getInstance().getUserName();
        if( userName == null ) {
            JOptionPane.showMessageDialog(Main.parent, tr("To store imagery offsets you must be a registered OSM user."), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = "Please enter the reason why you mark this "
                + (offset instanceof ImageryOffset ? "imagery offset" : "calibraion geometry") + " as deprecated:";
        String reason = StoreImageryOffsetAction.queryDescription(message);
        if( reason == null )
            return;
        
        try {
            String query = "deprecate?id=" + offset.getId()
                + "&author=" + URLEncoder.encode(userName, "UTF8")
                + "&reason=" + URLEncoder.encode(reason, "UTF8");
            SimpleOffsetQueryTask depTask = new SimpleOffsetQueryTask(query, tr("Notifying the server of the deprecation..."));
            if( listener != null )
                depTask.setListener(listener);
            Main.worker.submit(depTask);
        } catch( UnsupportedEncodingException ex ) {
            // WTF
        }
    }
}
