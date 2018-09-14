package org.openstreetmap.josm.plugins.imagery_cachexport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.io.File;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

/**
 * Dialog to specify the imagery export directory.  The entered information is
 * stored in the JOSM preferences and used as default the next time the dialog
 * is displayed.
 */
public class ImageryTileExportDialog extends ExtendedDialog {
    private final static String EXPORT_DIR_PNAME = "imagery_cachexport.export_directory";
    private final static String APPEND_NAME_PNAME = "imagery_cachexport.append_name";
    private final JosmTextField exportDir = new JosmTextField(32);
    private final JCheckBox appendName = new JCheckBox(tr("Append cache name"));
    private final JLabel summary = new JLabel();
    private final String cacheName;

    public ImageryTileExportDialog(final CacheAccess<String, BufferedImageCacheEntry> cache,
                                   final String cacheName,
                                   final String cacheKeyPrefix) {
        super(MainApplication.getMainFrame(), tr("Export Tiles"), new String[] {tr("Ok"), tr("Cancel")});
        this.cacheName = cacheName;

        setButtonIcons(new String[] {"ok", "cancel"});
        final JPanel content = new JPanel(new GridBagLayout());

        content.add(new JLabel(tr("Cache name:")),
                    GBC.std().insets(0, 0, 5, 0));
        content.add(new JLabel(cacheName), GBC.eol());
        
        content.add(new JLabel(tr("Object count:")),
                    GBC.std().insets(0, 0, 5, 0));
        Integer count = getObjectCount(cache, cacheKeyPrefix);
        content.add(new JLabel(count.toString()), GBC.eol());

        exportDir.setHint(tr("file system path"));
        exportDir.setToolTipText(tr("File system path the tiles are exported to."));
        exportDir.setText(Config.getPref().get(EXPORT_DIR_PNAME, null));
        exportDir.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }
        });
        content.add(new JLabel(tr("Export directory:")),
                    GBC.std().insets(0, 0, 5, 0));
        content.add(exportDir, GBC.eol().fill(GBC.HORIZONTAL));

        appendName.setToolTipText(tr("Append cache name to export directory."));
        appendName.setSelected(Config.getPref().getBoolean(APPEND_NAME_PNAME));
        ChangeListener appendNameChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateSummary();
            }
        };
        appendName.addChangeListener(appendNameChangeListener);
        content.add(appendName, GBC.eol().insets(0, 0, 5, 0));

        updateSummary();
        content.add(summary, GBC.eol().insets(0, 0, 5, 0));

        // The false in the next line makes it a dynamic width, but the
        // scroll bars are gone...
        setContent(content, false);
        // Need to call setupDialog() to create the OK button.
        setupDialog();
        updateOkButton();
        showDialog();
    }

    /**
     * Calculate the number of objects that are stored in the cache.
     *
     * @param cache          Cache object.
     * @param cacheKeyPrefix Cache key prefix.
     *
     * @return The number of objects in the specified cache.
     */
    public static int getObjectCount(final CacheAccess<String, BufferedImageCacheEntry> cache,
                                     final String cacheKeyPrefix) {
        // Code based on josm/gui/preferences/imagery/CacheContentsPanel.java.
        Set<String> keySet = cache.getCacheControl().getKeySet();
        int counter = 0;
        for (String key: keySet) {
            String[] keyParts = key.split(":", 2);
            if (keyParts.length == 2) {
                if (cacheKeyPrefix.equals(keyParts[0])) {
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Construct path of export directory.
     *
     * @return Export directory path.  Returns {@code null} if no path was
     * 	       specified in the dialog.
     */
    public String getExportPath() {
        return getExportPath(null);
    }

    /**
     * Construct path of export directory.
     *
     * @param deflt Default path if no path was specified in the dialog.
     *
     * @return Export directory path.
     */
    public String getExportPath(String deflt) {
        String exportDirText = exportDir.getText();
        if (exportDirText.isEmpty()) {
            return deflt;
        }
        else if (appendName.isSelected()) {
            File path = new File(exportDirText, cacheName);
            return path.getAbsolutePath();
        }
        else {
            return new File(exportDirText).getAbsolutePath();
        }
    }

    /**
     * Construct string for dialog summary.
     *
     * @return Dialog summary string.
     */
    private String getSummary() {
        final String exportPath = getExportPath();
        if (exportPath != null) {
            return tr(// {0} is the directory path, it should be at the end of the message
                      "Tiles will be exported to directory {0}", exportPath);
        } else {
            return tr("No export directory set.");
        }
    }

    /**
     * Update the dialog summary that shows the path the tiles are exported
     * to.
     */
    private void updateSummary() {
        summary.setText(getSummary());
    }

    /**
     * Update the status of the OK button.  It will be grayed out if no valid
     * path is specified.
     */
    private void updateOkButton() {
        if (exportDir.getText().isEmpty()) {
            setOkEnabled(false);
        }
        else {
            setOkEnabled(true);
        }
    }

    /**
     * Update all elements that indicate the dialog status.
     */
    private void updateStatus() {
        updateSummary();
        updateOkButton();
    }

    /**
     * Enable or disable the OK button.  It gets disabled if one of the
     * dialog fields has an error.
     *
     * @param enabled {@code true} if the OK button is enabled,
     *       	      {@code false} if not.
     */
    private void setOkEnabled(boolean enabled) {
        if (buttons != null && !buttons.isEmpty()) {
            buttons.get(0).setEnabled(enabled);
        }
    }

    /**
     * Store the entered information in the JOSM preferences.
     */
    public void storePrefs() {
        Config.getPref().put(EXPORT_DIR_PNAME, exportDir.getText());
        Config.getPref().putBoolean(APPEND_NAME_PNAME, appendName.isSelected());
    }
}
