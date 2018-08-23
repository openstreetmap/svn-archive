// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imagery_offset_db;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

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
     * @param offset offset object
     */
    public DeprecateOffsetAction(ImageryOffsetBase offset) {
        super(tr("Deprecate Offset"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
        this.offset = offset;
        setEnabled(offset != null && !offset.isDeprecated());
    }

    /**
     * Asks a user if they really want to deprecate an offset (since this
     * action is virtually irreversible) and calls
     * {@link #deprecateOffset(ImageryOffsetBase, QuerySuccessListener)}
     * on a positive answer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!MainApplication.isDisplayingMapView() || !MainApplication.getMap().isVisible())
            return;

        String desc = offset instanceof ImageryOffset ?
                tr("Are you sure this imagery offset is wrong?") :
                    tr("Are you sure this calibration geometry is aligned badly?");
                if (JOptionPane.showConfirmDialog(MainApplication.getMainFrame(),
                        tr("Warning: deprecation is basically irreversible!")+ "\n" + desc,
                        ImageryOffsetTools.DIALOG_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                    return;
                }
                deprecateOffset(offset, listener);
    }

    /**
     * Installs a listener to process successful deprecation event.
     * @param listener success listener
     */
    public void setListener(QuerySuccessListener listener) {
        this.listener = listener;
    }

    /**
     * Deprecate the given offset.
     * @param offset offset object
     * @see #deprecateOffset(ImageryOffsetBase, QuerySuccessListener)
     */
    public static void deprecateOffset(ImageryOffsetBase offset) {
        deprecateOffset(offset, null);
    }

    /**
     * Deprecate the given offset and call listener on success. Asks user the reason
     * and executes {@link SimpleOffsetQueryTask} with a query to deprecate the offset.
     * @param offset offset object
     * @param listener success listener
     */
    public static void deprecateOffset(ImageryOffsetBase offset, QuerySuccessListener listener) {
        String userName = UserIdentityManager.getInstance().getUserName();
        if (userName == null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("To store imagery offsets you must be a registered OSM user."),
                    ImageryOffsetTools.DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = offset instanceof ImageryOffset
                ? tr("Please enter the reason why you mark this imagery offset as deprecated")
                        : tr("Please enter the reason why you mark this calibration geometry as deprecated");
                String reason = StoreImageryOffsetAction.queryDescription(message + ":");
                if (reason == null)
                    return;

                try {
                    String query = "deprecate?id=" + offset.getId()
                    + "&author=" + URLEncoder.encode(userName, "UTF8")
                    + "&reason=" + URLEncoder.encode(reason, "UTF8");
                    SimpleOffsetQueryTask depTask = new SimpleOffsetQueryTask(query, tr("Notifying the server of the deprecation..."));
                    if (listener != null)
                        depTask.setListener(listener);
                    MainApplication.worker.submit(depTask);
                } catch (UnsupportedEncodingException ex) {
                    Logging.error(ex);
                }
    }
}
