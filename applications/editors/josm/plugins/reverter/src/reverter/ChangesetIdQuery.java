package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.widgets.ChangesetIdTextField;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.tools.GBC;

import reverter.ChangesetReverter.RevertType;

@SuppressWarnings("serial")
public class ChangesetIdQuery extends ExtendedDialog {
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final ChangesetIdTextField tcid = new ChangesetIdTextField();
    private final HistoryComboBox cbId = new HistoryComboBox();
    private final ButtonGroup bgRevertType = new ButtonGroup();
    private final JRadioButton rbFull = new JRadioButton(tr("Revert changeset fully"));
    private final JRadioButton rbSelection = new JRadioButton(tr("Revert selection only"));
    private final JRadioButton rbSelectionUndelete = new JRadioButton(tr("Revert selection and restore deleted objects"));
    private final JCheckBox cbNewLayer = new JCheckBox(tr("Download as new layer"));
    
    public int getChangesetId() {
        return tcid.getChangesetId();
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
        
        panel.add(new JLabel(tr("Changeset id:")));

        cbId.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                return tcid;
            }
        });
        cbId.setToolTipText(tr("Enter the ID of the changeset that should be reverted"));
        restoreChangesetsHistory(cbId);

        // forward the enter key stroke to the revert button
        tcid.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        
        panel.add(cbId, GBC.eol().fill(GBC.HORIZONTAL));

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
    }

    @Override
    public void setupDialog() {
        setContent(panel, false);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        setDefaultButton(1);
        
        addWindowListener(new InternalWindowListener());
        super.setupDialog();
        
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
                    buttons.get(0).setEnabled(tcid.readIds());
                }
            }
        });
        
        if (Main.pref.getBoolean("downloadchangeset.autopaste", true)) { 
            tcid.tryToPasteFromClipboard();
        }
        
        // Initially sets the revert button consistent with text id field contents
        buttons.get(0).setEnabled(tcid.readIds());
    }

    /**
     * Restore the current history from the preferences
     *
     * @param cbHistory
     */
    protected void restoreChangesetsHistory(HistoryComboBox cbHistory) {
        List<String> cmtHistory = new LinkedList<String>(Main.pref.getCollection(getClass().getName() + ".changesetsHistory", new LinkedList<String>()));
        // we have to reverse the history, because ComboBoxHistory will reverse it again in addElement()
        Collections.reverse(cmtHistory);
        cbHistory.setPossibleItems(cmtHistory);
    }
    
    /**
     * Remind the current history in the preferences
     * @param cbHistory
     */
    protected void remindChangesetsHistory(HistoryComboBox cbHistory) {
        cbHistory.addCurrentItemToHistory();
        Main.pref.putCollection(getClass().getName() + ".changesetsHistory", cbHistory.getHistory());
    }
    
    private class InternalWindowListener implements WindowListener {
        @Override public void windowClosed(WindowEvent e) {
            if (e != null && e.getComponent() == ChangesetIdQuery.this && getValue() == 1) {
                if (!tcid.readIds()) {
                    return;
                }
                remindChangesetsHistory(cbId);
            }
        }
    
        @Override public void windowOpened(WindowEvent e) {}
        @Override public void windowClosing(WindowEvent e) {}
        @Override public void windowIconified(WindowEvent e) {}
        @Override public void windowDeiconified(WindowEvent e) {}
        @Override public void windowActivated(WindowEvent e) {}
        @Override public void windowDeactivated(WindowEvent e) {}
    }
}
