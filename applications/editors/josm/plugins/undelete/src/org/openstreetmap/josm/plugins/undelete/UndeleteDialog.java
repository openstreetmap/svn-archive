// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.undelete;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.widgets.OsmIdTextField;

public class UndeleteDialog extends ExtendedDialog {

    private final JCheckBox layer = new JCheckBox(tr("Separate Layer"));
    private final OsmIdTextField tfId = new OsmIdTextField();
    
    public UndeleteDialog(Component parent) {
        super(parent, tr("Undelete Object"), new String[] {tr("Undelete object"), tr("Cancel")});
        
        JPanel all = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0;
        all.add(new JLabel(tr("Object ID:")), gc);
        
        tfId.setText(Main.pref.get("undelete.osmid"));
        tfId.setToolTipText(tr("Enter the type and ID of the objects that should be undeleted, e.g., ''n1 w2''"));
        // forward the enter key stroke to the undelete button
        tfId.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        gc.weightx = 1;
        all.add(tfId, gc);
        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;
        gc.weightx = 0;
        gc.gridy++;

        layer.setToolTipText(tr("Select if the data should be added into a new layer"));
        layer.setSelected(Main.pref.getBoolean("undelete.newlayer"));
        all.add(layer, gc);
        setContent(all, false);
        setButtonIcons(new String[] { "undelete.png", "cancel.png" });
        setToolTipTexts(new String[] { tr("Start undeleting"), tr("Close dialog and cancel") });
        setDefaultButton(1);
    }
    
    public final boolean isNewLayerSelected() {
        return layer.isSelected();
    }
    
    public final String getOsmIdsString() {
        return tfId.getText();
    }
    
    public final List<PrimitiveId> getOsmIds() {
        return tfId.getIds();
    }
}
