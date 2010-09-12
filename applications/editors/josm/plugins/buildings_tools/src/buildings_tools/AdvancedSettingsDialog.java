package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.TagEditorModel;
import org.openstreetmap.josm.gui.tagging.TagEditorPanel;
import org.openstreetmap.josm.tools.GBC;

public class AdvancedSettingsDialog extends ExtendedDialog {
	private TagEditorModel tagsModel = new TagEditorModel();

	private JCheckBox cBigMode = new JCheckBox(tr("Big buildings mode"));
	private JCheckBox cSoftCur = new JCheckBox(tr("Rotate crosshair"));

	public AdvancedSettingsDialog() {
		super(Main.parent, tr("Advanced settings"),
				new String[] { tr("OK"), tr("Cancel") },
				true);
		contentInsets = new Insets(15, 15, 5, 15);
		setButtonIcons(new String[] { "ok.png", "cancel.png" });

		final JPanel panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel(tr("Buildings tags:")), GBC.eol().fill(GBC.HORIZONTAL));

		for (Entry<String, String> entry : ToolSettings.getTags().entrySet()) {
			tagsModel.add(entry.getKey(), entry.getValue());
		}
		panel.add(new TagEditorPanel(tagsModel, null), GBC.eop().fill(GBC.BOTH));

		panel.add(cBigMode, GBC.eol().fill(GBC.HORIZONTAL));
		panel.add(cSoftCur, GBC.eol().fill(GBC.HORIZONTAL));

		cBigMode.setSelected(ToolSettings.isBBMode());
		cSoftCur.setSelected(ToolSettings.isSoftCursor());

		setContent(panel);

		setupDialog();
		setVisible(true);
	}

	public boolean isBBMode() {
		return cBigMode.isSelected();
	}

	public boolean isSoftCursor() {
		return cSoftCur.isSelected();
	}

	public void saveSettings() {
		tagsModel.applyToTags(ToolSettings.getTags());
		ToolSettings.setBBMode(isBBMode());
		ToolSettings.setSoftCursor(isSoftCursor());
	}
}
