package iodb;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The dialog which presents a choice between imagery align options.
 * 
 * @author zverik
 */
public class OffsetDialog extends JDialog implements ActionListener {
    protected static final String PREF_CALIBRATION = "iodb.show.calibration";
    protected static final String PREF_DEPRECATED = "iodb.show.deprecated";

    private List<ImageryOffsetBase> offsets;
    private ImageryOffsetBase selectedOffset;
    private JPanel buttonPanel;

    public OffsetDialog( List<ImageryOffsetBase> offsets ) {
        super(JOptionPane.getFrameForComponent(Main.parent), ImageryOffsetTools.DIALOG_TITLE, ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        this.offsets = offsets;

        // make this dialog close on "escape"
        getRootPane().registerKeyboardAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void prepareDialog() {
        Box dialog = new Box(BoxLayout.Y_AXIS);
        updateButtonPanel();
        // todo: calibration objects and deprecated offsets button
        final JCheckBox calibrationBox = new JCheckBox(tr("Hide calibration geometries"));
        calibrationBox.setSelected(Main.pref.getBoolean(PREF_CALIBRATION, true));
        calibrationBox.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                Main.pref.put(PREF_CALIBRATION, calibrationBox.isSelected());
                updateButtonPanel();
            }
        });
        final JCheckBox deprecatedBox = new JCheckBox(tr("Show deprecated offsets"));
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
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setAlignmentX(CENTER_ALIGNMENT);

        dialog.add(buttonPanel);
        dialog.add(checkBoxPanel);
        dialog.add(cancelButton);

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
            if( !offset.isDeprecated() )
                popupMenu.add(new DeprecateOffsetAction(offset));
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
        }
        return filteredOffsets;
    }
    
    public ImageryOffsetBase showDialog() {
        selectedOffset = null;
        prepareDialog();
        setVisible(true);
        return selectedOffset;
    }

    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() instanceof OffsetDialogButton ) {
            selectedOffset = ((OffsetDialogButton)e.getSource()).getOffset();
        } else
            selectedOffset = null;
        setVisible(false);
    }
}
