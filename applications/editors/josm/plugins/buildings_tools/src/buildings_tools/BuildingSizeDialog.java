package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JCheckBox;

import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class BuildingSizeDialog extends MyDialog {
    private JFormattedTextField twidth = new JFormattedTextField(NumberFormat.getInstance());
    private JFormattedTextField tlenstep = new JFormattedTextField(NumberFormat.getInstance());
    private JCheckBox caddr = new JCheckBox(tr("Use Address dialog"));
    private JCheckBox cAutoSelect = new JCheckBox(tr("Auto-select building"));

    public BuildingSizeDialog() {
        super(tr("Set buildings size"));

        addLabelled(tr("Buildings width:"), twidth);
        addLabelled(tr("Length step:"), tlenstep);
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

        setupDialog();
        showDialog();
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
