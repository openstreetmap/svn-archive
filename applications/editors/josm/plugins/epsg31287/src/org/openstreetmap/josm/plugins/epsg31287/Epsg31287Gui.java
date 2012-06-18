package org.openstreetmap.josm.plugins.epsg31287;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.preferences.projection.AbstractProjectionChoice;
import org.openstreetmap.josm.tools.GBC;

public class Epsg31287Gui extends AbstractProjectionChoice {

	public Epsg31287Gui() {
		super("epsg31287:epsg31287", tr("Epsg31287"));
	}

	private double dx;
	private double dy;

	// PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public JPanel getPreferencePanel(ActionListener listener) {
		//p.add(new HtmlPanel("<i>EPSG:31287 - Bessel 1841 in Lambert_Conformal_Conic_2SP</i>"), GBC.eol().fill(GBC.HORIZONTAL));
		//p.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

		JTextField gdx = new JTextField(""+dx);
		JTextField gdy = new JTextField(""+dy);

		p.add(new JLabel(tr("dx")), GBC.std().insets(5,5,0,5));
		p.add(GBC.glue(1, 0), GBC.std().fill(GBC.HORIZONTAL));
		p.add(gdx, GBC.eop().fill(GBC.HORIZONTAL));

		p.add(new JLabel(tr("dy")), GBC.std().insets(5,5,0,5));
		p.add(GBC.glue(1, 0), GBC.std().fill(GBC.HORIZONTAL));
		p.add(gdy, GBC.eop().fill(GBC.HORIZONTAL));
		p.add(GBC.glue(1, 1), GBC.eol().fill(GBC.BOTH));
		return p;
	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public Collection<String> getPreferences(JPanel p) {
		dx = new Double(((JTextField)p.getComponent(2)).getText());
		dy = new Double(((JTextField)p.getComponent(5)).getText());
		return Arrays.asList(""+dx,""+dy);
	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public Collection<String> getPreferencesFromCode(String code) {
		dx = 85.0;
		dy = 45.0;
		return null;
	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public void setPreferences(Collection<String> args) {
		if(args != null)
		{
			String[] array = args.toArray(new String[0]);
			if (array.length > 0) {
				try {
					dx = Double.parseDouble(array[0]);
				} catch(NumberFormatException e) {}
			}
			if (array.length > 1) {
				try {
					dy = Double.parseDouble(array[1]);
				} catch(NumberFormatException e) {}
			}
		}
	}

	@Override
	public Projection getProjection() {
		return new ProjectionEPSG31287(dx, dy);
	}

	@Override
	public String[] allCodes() {
		return new String[] {ProjectionEPSG31287.getProjCode()};
	}

}
