package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.text.ParseException;

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
	private JPanel panel = new JPanel(new GridBagLayout());
	private JCheckBox caddr = new JCheckBox(tr("Use Address dialog"));
	private void addLabelled(String str, Component c) {
		JLabel label = new JLabel(str);
		panel.add(label, GBC.std());
		label.setLabelFor(c);
		panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
	}
	public BuildingSizeDialog() {
		super(Main.parent, tr("Set buildings size"), 
				new String[] { tr("OK"), tr("Cancel") },
				true);
		contentConstraints = GBC.eol().fill().insets(15,15,15,5);
		setButtonIcons(new String[] {"ok.png", "cancel.png" });
		
		addLabelled(tr("Buildings width:"),twidth);
		addLabelled(tr("Length step:"),tlenstep);
		twidth.setValue(DrawBuildingAction.getWidth());
		tlenstep.setValue(DrawBuildingAction.getLenStep());
		panel.add(caddr,GBC.eol().fill(GBC.HORIZONTAL));
		setContent(panel);
		setupDialog();
		setVisible(true);
	}
	public double width()
	{
		try
		{
		  return NumberFormat.getInstance().parse(twidth.getText()).doubleValue();
		} catch (ParseException e)
		{			
		  return 0;
		}
	}
	public double lenstep()
	{
		try
		{
			  return NumberFormat.getInstance().parse(tlenstep.getText()).doubleValue();
		} catch (ParseException e)
		{			
		  return 0;
		}
	}
	public boolean useAddr()
	{
		return caddr.isSelected();
	}
}
