package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;
import static buildings_tools.BuildingSizeDialog.addLabelled;

import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

public class AdvancedSettingsDialog extends ExtendedDialog {
	// TODO: Replace tag textbox to full-fledged tag editor
	JTextField tBTag = new JTextField();
	JCheckBox cBigMode = new JCheckBox(tr("Big buildings mode"));
	JCheckBox cSoftCur = new JCheckBox(tr("Rotate crosshair"));

	public AdvancedSettingsDialog() {
		super(Main.parent, tr("Advanced settings"),
				new String[] { tr("OK"), tr("Cancel") },
				true);
		contentConstraints = GBC.eol().fill().insets(15, 15, 15, 5);
		setButtonIcons(new String[] { "ok.png", "cancel.png" });

		final JPanel panel = new JPanel(new GridBagLayout());
		addLabelled(panel, tr("Building tag:"), tBTag);
		panel.add(cBigMode, GBC.eol().fill(GBC.HORIZONTAL));
		panel.add(cSoftCur, GBC.eol().fill(GBC.HORIZONTAL));

		tBTag.setText(ToolSettings.getTag());
		cBigMode.setSelected(ToolSettings.isBBMode());
		cSoftCur.setSelected(ToolSettings.isSoftCursor());

		setContent(panel);
		setupDialog();
		setVisible(true);
	}

	public String getTag() {
		return tBTag.getText();
	}

	public boolean isBBMode() {
		return cBigMode.isSelected();
	}

	public boolean isSoftCursor() {
		return cSoftCur.isSelected();
	}

	public void saveSettings() {
		ToolSettings.setTag(getTag());
		ToolSettings.setBBMode(isBBMode());
		ToolSettings.setSoftCursor(isSoftCursor());
	}
}
