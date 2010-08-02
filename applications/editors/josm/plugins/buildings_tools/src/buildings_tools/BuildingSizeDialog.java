package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class BuildingSizeDialog extends ExtendedDialog {
	private JFormattedTextField twidth = new JFormattedTextField(NumberFormat.getInstance());
	private JFormattedTextField tlenstep = new JFormattedTextField(NumberFormat.getInstance());
	private JCheckBox caddr = new JCheckBox(tr("Use Address dialog"));
	private JCheckBox cAutoSelect = new JCheckBox(tr("Auto-select building"));

	static void addLabelled(JPanel panel, String str, Component c) {
		JLabel label = new JLabel(str);
		panel.add(label, GBC.std());
		label.setLabelFor(c);
		panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
	}

	public BuildingSizeDialog() {
		super(Main.parent, tr("Set buildings size"),
				new String[] { tr("OK"), tr("Cancel") },
				true);
		contentInsets = new Insets(15, 15, 5, 15);
		setButtonIcons(new String[] { "ok.png", "cancel.png" });

		final JPanel panel = new JPanel(new GridBagLayout());
		addLabelled(panel, tr("Buildings width:"), twidth);
		addLabelled(panel, tr("Length step:"), tlenstep);
		panel.add(caddr, GBC.eol().fill(GBC.HORIZONTAL));
		panel.add(cAutoSelect, GBC.eol().fill(GBC.HORIZONTAL));

		twidth.setValue(ToolSettings.getWidth());
		tlenstep.setValue(ToolSettings.getLenStep());
		caddr.setSelected(ToolSettings.isUsingAddr());
		cAutoSelect.setSelected(ToolSettings.isAutoSelect());

		JButton bAdv = new JButton(tr("Advanced..."));
		bAdv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AdvancedSettingsDialog dlg = new AdvancedSettingsDialog();
				if (dlg.getValue() == 1) {
					dlg.saveSettings();
				}
			}
		});
		panel.add(bAdv, GBC.eol().insets(0, 5, 0, 0).anchor(GBC.EAST));

		setContent(panel);
		setupDialog();
		setVisible(true);
	}

	public double width() {
		try {
			return NumberFormat.getInstance().parse(twidth.getText()).doubleValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public double lenstep() {
		try {
			return NumberFormat.getInstance().parse(tlenstep.getText()).doubleValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	public boolean useAddr() {
		return caddr.isSelected();
	}

	public void saveSettings() {
		ToolSettings.setSizes(width(), lenstep());
		ToolSettings.setAddrDialog(useAddr());
		ToolSettings.setAutoSelect(cAutoSelect.isSelected());
	}
}
