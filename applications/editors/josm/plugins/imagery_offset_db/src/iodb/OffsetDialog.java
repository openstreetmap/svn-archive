package iodb;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.tools.*;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The dialog which presents a choice between imagery align options.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class OffsetDialog extends JDialog implements ActionListener, MapView.ZoomChangeListener, MapViewPaintable {
    protected static final String PREF_CALIBRATION = "iodb.show.calibration";
    protected static final String PREF_DEPRECATED = "iodb.show.deprecated";
    private static final int MAX_OFFSETS = Main.main.pref.getInteger("iodb.max.offsets", 5);

    /**
     * Whether to create a modal frame. It turns out, modal dialogs
     * block swing worker thread, so offset deprecation, for example, takes
     * place only after the dialog is closed. Very inconvenient.
     */
    private static final boolean MODAL = false;

    private List<ImageryOffsetBase> offsets;
    private ImageryOffsetBase selectedOffset;
    private JPanel buttonPanel;

    /**
     * Initialize the dialog and install listeners. 
     * @param offsets The list of offset to choose from.
     */
    public OffsetDialog( List<ImageryOffsetBase> offsets ) {
        super(JOptionPane.getFrameForComponent(Main.parent), ImageryOffsetTools.DIALOG_TITLE,
                MODAL ? ModalityType.DOCUMENT_MODAL : ModalityType.MODELESS);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        this.offsets = offsets;

        // make this dialog close on "escape"
        getRootPane().registerKeyboardAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    /**
     * Creates the GUI.
     */
    private void prepareDialog() {
        updateButtonPanel();
        final JCheckBox calibrationBox = new JCheckBox(tr("Calibration geometries"));
        calibrationBox.setSelected(Main.pref.getBoolean(PREF_CALIBRATION, true));
        calibrationBox.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                Main.pref.put(PREF_CALIBRATION, calibrationBox.isSelected());
                updateButtonPanel();
            }
        });
        final JCheckBox deprecatedBox = new JCheckBox(tr("Deprecated offsets"));
        deprecatedBox.setSelected(Main.pref.getBoolean(PREF_DEPRECATED, false));
        deprecatedBox.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                Main.pref.put(PREF_DEPRECATED, deprecatedBox.isSelected());
                updateButtonPanel();
            }
        });
        Box checkBoxPanel = new Box(BoxLayout.X_AXIS);
        checkBoxPanel.add(calibrationBox);
        checkBoxPanel.add(deprecatedBox);
        JButton cancelButton = new JButton(tr("Cancel"), ImageProvider.get("cancel"));
        cancelButton.addActionListener(this);
        JButton helpButton = new JButton(new HelpAction());
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelPanel.add(cancelButton);
        cancelPanel.add(helpButton);

        Box dialog = new Box(BoxLayout.Y_AXIS);
        dialog.add(buttonPanel);
        dialog.add(checkBoxPanel);
        dialog.add(cancelPanel);

        dialog.setBorder(new CompoundBorder(dialog.getBorder(), new EmptyBorder(5, 5, 5, 5)));
        setContentPane(dialog);
        pack();
        setLocationRelativeTo(Main.parent);
    }

    /**
     * As the name states, this method updates the button panel. It is called
     * when a user clicks filtering checkboxes or deprecates an offset.
     */
    private void updateButtonPanel() {
        List<ImageryOffsetBase> filteredOffsets = filterOffsets();
        if( buttonPanel == null )
            buttonPanel = new JPanel();
        buttonPanel.removeAll();
        buttonPanel.setLayout(new GridLayout(filteredOffsets.size(), 1, 0, 5));
        for( ImageryOffsetBase offset : filteredOffsets ) {
            OffsetDialogButton button = new OffsetDialogButton(offset);
            button.addActionListener(this);
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new OffsetInfoAction(offset));
            if( !offset.isDeprecated() ) {
                DeprecateOffsetAction action = new DeprecateOffsetAction(offset);
                action.setListener(new DeprecateOffsetListener(offset));
                popupMenu.add(action);
            }
            button.setComponentPopupMenu(popupMenu);
            buttonPanel.add(button);
        }
        pack();
        Main.map.mapView.repaint();
    }

    /**
     * Make a filtered offset list out of the full one. Takes into
     * account both checkboxes.
     */
    private List<ImageryOffsetBase> filterOffsets() {
        boolean showCalibration = Main.pref.getBoolean(PREF_CALIBRATION, true);
        boolean showDeprecated = Main.pref.getBoolean(PREF_DEPRECATED, false);
        List<ImageryOffsetBase> filteredOffsets = new ArrayList<ImageryOffsetBase>();
        for( ImageryOffsetBase offset : offsets ) {
            if( offset.isDeprecated() && !showDeprecated )
                continue;
            if( offset instanceof CalibrationObject && !showCalibration )
                continue;
            filteredOffsets.add(offset);
            if( filteredOffsets.size() >= MAX_OFFSETS )
                break;
        }
        return filteredOffsets;
    }

    /**
     * This listener method is called when a user pans or zooms the map.
     * It does nothing, only passes the event to all displayed offset buttons.
     */
    public void zoomChanged() {
        for( Component c : buttonPanel.getComponents() ) {
            if( c instanceof OffsetDialogButton ) {
                ((OffsetDialogButton)c).updateLocation();
            }
        }
    }

    /**
     * Draw dots on the map where offsets are located. I doubt it has practical
     * value, but looks nice.
     */
    public void paint( Graphics2D g, MapView mv, Bounds bbox ) {
        if( offsets == null )
            return;

        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        for( ImageryOffsetBase offset : filterOffsets() ) {
            Point p = mv.getPoint(offset.getPosition());
            g2.setColor(Color.BLACK);
            g2.fillOval(p.x - 2, p.y - 2, 5, 5);
            g2.setColor(Color.WHITE);
            g2.drawOval(p.x - 3, p.y - 3, 7, 7);
        }
    }
    
    /**
     * Display the dialog and get the return value is case of a modal frame.
     * Creates GUI, install a temporary map layer (see {@link #paint} and
     * shows the window.
     * @return Null for a non-modal dialog, the selected offset
     * (or, again, a null value) otherwise.
     */
    public ImageryOffsetBase showDialog() {
        selectedOffset = null;
        prepareDialog();
        MapView.addZoomChangeListener(this);
        if( !MODAL ) {
            Main.map.mapView.addTemporaryLayer(this);
            Main.map.mapView.repaint();
        }
        setVisible(true);
        return selectedOffset;
    }

    /**
     * This is a listener method for all buttons (except "Help").
     * It assigns a selected offset value and closes the dialog.
     * If the dialog wasn't modal, it applies the offset immediately.
     * Should it apply the offset either way? Probably.
     * @see #applyOffset()
     */
    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() instanceof OffsetDialogButton ) {
            selectedOffset = ((OffsetDialogButton)e.getSource()).getOffset();
        } else
            selectedOffset = null;
        boolean closeDialog = MODAL || selectedOffset == null
                || selectedOffset instanceof CalibrationObject
                || Main.pref.getBoolean("iodb.close.on.select", true);
        if( closeDialog ) {
            MapView.removeZoomChangeListener(this);
            setVisible(false);
        }
        if( !MODAL ) {
            if( closeDialog ) {
                Main.map.mapView.removeTemporaryLayer(this);
                Main.map.mapView.repaint();
            }
            if( selectedOffset != null ) {
                applyOffset();
                if( !closeDialog )
                    updateButtonPanel();
            }
        }
    }


    /**
     * Either applies imagery offset or adds a calibration geometry layer.
     * If the offset for each type was chosen for the first time ever,
     * it displays an informational message.
     */
    public void applyOffset() {
        if( selectedOffset instanceof ImageryOffset ) {
            ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
            ImageryOffsetTools.applyLayerOffset(layer, (ImageryOffset)selectedOffset);
            Main.map.repaint();
            if( !Main.pref.getBoolean("iodb.offset.message", false) ) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("The topmost imagery layer has been shifted to presumably match\n"
                        + "OSM data in the area. Please check that the offset is still valid\n"
                        + "by downloading GPS tracks and comparing them and OSM data to the imagery."),
                        ImageryOffsetTools.DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
                Main.pref.put("iodb.offset.message", true);
            }
        } else if( selectedOffset instanceof CalibrationObject ) {
            CalibrationLayer clayer = new CalibrationLayer((CalibrationObject)selectedOffset);
            Main.map.mapView.addLayer(clayer);
            clayer.panToCenter();
            if( !Main.pref.getBoolean("iodb.calibration.message", false) ) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("A layer has been added with a calibration geometry. Hide data layers,\n"
                        + "find the corresponding feature on the imagery layer and move it accordingly."),
                        ImageryOffsetTools.DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
                Main.pref.put("iodb.calibration.message", true);
            }
        }
    }

    /**
     * A lisntener for successful deprecations.
     */
    private class DeprecateOffsetListener implements QuerySuccessListener {
        ImageryOffsetBase offset;

        /**
         * Initialize the listener with an offset.
         */
        public DeprecateOffsetListener( ImageryOffsetBase offset ) {
            this.offset = offset;
        }

        /**
         * Remove the deprecated offset from the offsets list. Then rebuild the button panel.
         */
        public void queryPassed() {
            offset.setDeprecated(new Date(), JosmUserIdentityManager.getInstance().getUserName(), "");
            updateButtonPanel();
        }
    }

    /**
     * Opens a web browser with the wiki page in user's language.
     */
    class HelpAction extends AbstractAction {

        public HelpAction() {
            super(tr("Help"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            String base = Main.pref.get("url.openstreetmap-wiki", "http://wiki.openstreetmap.org/wiki/");
            String lang = LanguageInfo.getWikiLanguagePrefix();
            String page = "Imagery_Offset_Database";
            try {
                // this logic was snatched from {@link org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog.HelpAction}
                HttpURLConnection conn = Utils.openHttpConnection(new URL(base + lang + page));
                conn.setConnectTimeout(Main.pref.getInteger("socket.timeout.connect", 10) * 1000);
                if( conn.getResponseCode() != 200 ) {
                    conn.disconnect();
                    lang = "";
                }
            } catch( IOException ex ) {
                lang = "";
            }
            OpenBrowser.displayUrl(base + lang + page);
        }
    }
}
