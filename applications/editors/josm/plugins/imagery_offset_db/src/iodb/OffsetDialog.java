package iodb;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.OpenBrowser;

/**
 * The dialog which presents a choice between imagery align options.
 * 
 * @author zverik
 */
public class OffsetDialog extends JDialog implements ActionListener, NavigatableComponent.ZoomChangeListener {
    protected static final String PREF_CALIBRATION = "iodb.show.calibration";
    protected static final String PREF_DEPRECATED = "iodb.show.deprecated";
    private static final int MAX_OFFSETS = Main.main.pref.getInteger("iodb.max.offsets", 5);
    private static final boolean MODAL = false; // modal does not work for executing actions

    private List<ImageryOffsetBase> offsets;
    private ImageryOffsetBase selectedOffset;
    private JPanel buttonPanel;

    public OffsetDialog( List<ImageryOffsetBase> offsets ) {
        super(JOptionPane.getFrameForComponent(Main.parent), ImageryOffsetTools.DIALOG_TITLE,
                MODAL ? ModalityType.DOCUMENT_MODAL : ModalityType.MODELESS);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        this.offsets = offsets;
        NavigatableComponent.addZoomChangeListener(this);

        // make this dialog close on "escape"
        getRootPane().registerKeyboardAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
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
    }

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

    public void zoomChanged() {
        for( Component c : buttonPanel.getComponents() ) {
            if( c instanceof OffsetDialogButton ) {
                ((OffsetDialogButton)c).updateLocation();
            }
        }
    }
    
    public ImageryOffsetBase showDialog() {
        selectedOffset = null;
        prepareDialog();
        setVisible(true);
        return selectedOffset;
    }

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

    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() instanceof OffsetDialogButton ) {
            selectedOffset = ((OffsetDialogButton)e.getSource()).getOffset();
        } else
            selectedOffset = null;
        NavigatableComponent.removeZoomChangeListener(this);
        setVisible(false);
        if( !MODAL && selectedOffset != null )
            applyOffset();
    }

    private class DeprecateOffsetListener implements QuerySuccessListener {
        ImageryOffsetBase offset;

        public DeprecateOffsetListener( ImageryOffsetBase offset ) {
            this.offset = offset;
        }

        public void queryPassed() {
            offset.setDeprecated(new Date(), JosmUserIdentityManager.getInstance().getUserName(), "");
            updateButtonPanel();
        }
    }

    class HelpAction extends AbstractAction {

        public HelpAction() {
            super(tr("Help"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            String base = "http://wiki.openstreetmap.org/wiki/";
            String page = "Imagery_Offset_Database";
            String lang = "RU:"; // todo: determine it
            OpenBrowser.displayUrl(base + lang + page);
        }
    }
}
