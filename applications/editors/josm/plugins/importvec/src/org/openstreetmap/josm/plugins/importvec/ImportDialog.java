package org.openstreetmap.josm.plugins.importvec;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

public class ImportDialog extends ExtendedDialog {
    private JFormattedTextField tsdiv = new JFormattedTextField(NumberFormat.getInstance());
    private JFormattedTextField tsnum = new JFormattedTextField(NumberFormat.getInstance());
    private JFormattedTextField tsteps = new JFormattedTextField(NumberFormat.getIntegerInstance());

    public ImportDialog() {
        super(Main.parent, tr("Import vector graphics"),
                new String[] { tr("OK"), tr("Cancel") },
                true);
        contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(new String[] { "ok.png", "cancel.png" });
        

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Scale:")), GBC.eol().fill(GBC.HORIZONTAL));
        final JPanel pscale = new JPanel(new GridBagLayout());
        pscale.add(tsdiv,GBC.std().fill(GBC.HORIZONTAL));
        pscale.add(new JLabel(tr("unit(s) = ")),GBC.std().insets(10, 0, 0, 0));
        pscale.add(tsnum,GBC.std().fill(GBC.HORIZONTAL));
        pscale.add(new JLabel(tr("m")),GBC.std().insets(10, 0, 0, 0));
        panel.add(pscale,GBC.eop().fill(GBC.HORIZONTAL));
        
        final JLabel label = new JLabel("Curve steps:");
        panel.add(label, GBC.std());
        label.setLabelFor(tsteps);
        panel.add(tsteps, GBC.eol().fill(GBC.HORIZONTAL));
        
        tsnum.setValue(Settings.getScaleNumerator());
        tsdiv.setValue(Settings.getScaleDivisor());
        tsteps.setValue(Settings.getCurveSteps());

        setContent(panel);
        setupDialog();
        setVisible(true);
    }
    
    public double getScaleNumerator() {
        try {
            return NumberFormat.getInstance().parse(tsnum.getText()).doubleValue();
        } catch (ParseException e) {
            return 1;
        }
    }
    
    public double getScaleDivisor() {
        try {
            double result = NumberFormat.getInstance().parse(tsdiv.getText()).doubleValue();
            if (result <= 0.0001)
                return 1;
            return result;
        } catch (ParseException e) {
            return 1;
        }
    }

    public int getCurveSteps() {
        try {
            int result = NumberFormat.getIntegerInstance().parse(tsdiv.getText()).intValue();
            if (result < 1)
                return 1;
            return result;
        } catch (ParseException e) {
            return 4;
        }
    }

    
    public void saveSettings() {
        Settings.setScaleNumerator(getScaleNumerator());
        Settings.setScaleDivisor(getScaleDivisor());
        Settings.setCurveSteps(getCurveSteps());
    }

}
