package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

import reverter.ChangesetReverter.RevertType;

@SuppressWarnings("serial")
public class ChangesetIdQuery extends ExtendedDialog {
    private final NumberFormat format = NumberFormat.getIntegerInstance();
    private final JFormattedTextField tcid = new JFormattedTextField(format);
    private final ButtonGroup bgRevertType = new ButtonGroup();
    private final JRadioButton rbFull = new JRadioButton(tr("Revert changeset fully"));
    private final JRadioButton rbSelection = new JRadioButton(tr("Revert selection only"));
    private final JRadioButton rbSelectionUndelete = new JRadioButton(tr("Revert selection and restore deleted objects"));
    private final JCheckBox cbNewLayer = new JCheckBox(tr("Download as new layer"));
    
    private final Color defaultForegroundColor = tcid.getForeground();

    public int getChangesetId() {
        try {
            return format.parse(tcid.getText()).intValue();
        } catch (ParseException e) {
            return 0;
        }
    }
    
    /**
     * Replies true if the user requires to download into a new layer
     *
     * @return true if the user requires to download into a new layer
     */
    public boolean isNewLayerRequired() {
        return cbNewLayer.isSelected();
    }

    public RevertType getRevertType() {
        if (rbFull.isSelected()) return RevertType.FULL;
        if (rbSelection.isSelected()) return RevertType.SELECTION;
        if (rbSelectionUndelete.isSelected()) return RevertType.SELECTION_WITH_UNDELETE;
        return null;
    }

    public ChangesetIdQuery() {
        super(Main.parent, tr("Revert changeset"), new String[] {tr("Revert"),tr("Cancel")}, true);
        contentInsets = new Insets(10,10,10,5);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Changeset id:")));
        panel.add(tcid, GBC.eol().fill(GBC.HORIZONTAL));

        bgRevertType.add(rbFull);
        bgRevertType.add(rbSelection);
        bgRevertType.add(rbSelectionUndelete);

        rbFull.setSelected(true);
        panel.add(rbFull, GBC.eol().insets(0,10,0,0).fill(GBC.HORIZONTAL));
        panel.add(rbSelection, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(rbSelectionUndelete, GBC.eol().fill(GBC.HORIZONTAL));

        cbNewLayer.setToolTipText(tr("<html>Select to download data into a new data layer.<br>"
                +"Unselect to download into the currently active data layer.</html>"));
        panel.add(cbNewLayer, GBC.eol().fill(GBC.HORIZONTAL));
        
        final DataSet ds = Main.main.getCurrentDataSet();
        
        // Disables "Download in new layer" choice if there is no current data set (i.e no data layer)
        if (ds == null) {
            cbNewLayer.setSelected(true);
            cbNewLayer.setEnabled(false);
        }
        // Disables selection-related choices of there is no current selected objects
        if (ds == null || ds.getAllSelected().isEmpty()) {
            rbSelection.setEnabled(false);
            rbSelectionUndelete.setEnabled(false);
        }
        
        // Enables/disables the Revert button when a changeset id is correctly set or not
        tcid.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void removeUpdate(DocumentEvent e) { idChanged(); }
            @Override public void insertUpdate(DocumentEvent e) { idChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { idChanged(); }
            private void idChanged() {
                if (tcid.hasFocus()) {
                    boolean idOK = getChangesetId() > 0;
                    tcid.setForeground(idOK ? defaultForegroundColor : Color.RED);
                    buttons.get(0).setEnabled(idOK);
                }
            }
        });
        
        setContent(panel);
        setupDialog();
        
        // Initially disables the Revert button
        buttons.get(0).setEnabled(false);
                
        // When pressing Enter in the changeset id field, validate the dialog if the id is correct (i.e. Revert button enabled)
        tcid.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}
            @Override public void keyTyped(KeyEvent e) { }
            @Override public void keyReleased(KeyEvent e) {
                if (e != null && e.getKeyCode() == KeyEvent.VK_ENTER && buttons.get(0).isEnabled()) {
                    buttons.get(0).doClick();
                }
            }
        });
    }
}
