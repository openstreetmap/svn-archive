package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.openstreetmap.josm.gui.tagging.TagEditorModel;
import org.openstreetmap.josm.gui.tagging.TagEditorPanel;
import org.openstreetmap.josm.tools.GBC;

public class AdvancedSettingsDialog extends MyDialog {
    private final TagEditorModel tagsModel = new TagEditorModel();

    private final JCheckBox cBigMode = new JCheckBox(tr("Big buildings mode"));
    private final JCheckBox cSoftCur = new JCheckBox(tr("Rotate crosshair"));
    private final JCheckBox cAddrNode = new JCheckBox(tr("Use address nodes under buildings"));

    public AdvancedSettingsDialog() {
        super(tr("Advanced settings"));

        panel.add(new JLabel(tr("Buildings tags:")), GBC.eol().fill(GBC.HORIZONTAL));

        for (Entry<String, String> entry : ToolSettings.getTags().entrySet()) {
            tagsModel.add(entry.getKey(), entry.getValue());
        }
        panel.add(new TagEditorPanel(tagsModel, null), GBC.eop().fill(GBC.BOTH));

        panel.add(cBigMode, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(cSoftCur, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(cAddrNode, GBC.eol().fill(GBC.HORIZONTAL));

        cBigMode.setSelected(ToolSettings.isBBMode());
        cSoftCur.setSelected(ToolSettings.isSoftCursor());
        cAddrNode.setSelected(ToolSettings.PROP_USE_ADDR_NODE.get());

        setupDialog();
        showDialog();
    }

    public void saveSettings() {
        tagsModel.applyToTags(ToolSettings.getTags());
        ToolSettings.saveTags();
        ToolSettings.setBBMode(cBigMode.isSelected());
        ToolSettings.setSoftCursor(cSoftCur.isSelected());
        ToolSettings.PROP_USE_ADDR_NODE.put(cAddrNode.isSelected());
    }
}
