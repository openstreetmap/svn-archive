// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.Splinex;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class SplinexPreference extends DefaultTabPreferenceSetting {

    public SplinexPreference() {
        super("spline2", tr("Splines"), tr("Spline drawing preferences"));
    }

    JSpinner spCurveSteps;

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
        SpinnerNumberModel model = new SpinnerNumberModel((int) Spline.PROP_SPLINEPOINTS.get(), 1, 100, 1);
        spCurveSteps = new JSpinner(model);
        JLabel label = new JLabel(tr("Curve steps"));
        p.add(label, GBC.std());
        label.setLabelFor(spCurveSteps);
        p.add(spCurveSteps, GBC.eol());
    }

    @Override
    public boolean ok() {
        Spline.PROP_SPLINEPOINTS.put((Integer) spCurveSteps.getValue());
        return false;
    }
}
