package ext_tools.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

import ext_tools.ExtTool;

public class EditToolDialog extends ExtendedDialog {
    private ExtTool tool;

    private JPanel panel = new JPanel(new GridBagLayout());
    private JTextField name = new JTextField();
    private JTextField cmdline = new JTextField();

    private void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std());
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }

    private void load() {
        name.setText(tool.name);
        cmdline.setText(tool.cmdline);
    }

    private void save() {
        tool.name = name.getText();
        tool.cmdline = cmdline.getText();
    }

    public EditToolDialog(ExtTool tool) {
        super(Main.parent, tr("Edit tool"),
                new String[] { tr("OK"), tr("Cancel") },
                true);
        contentConstraints = GBC.eol().fill().insets(15, 15, 15, 5);
        setButtonIcons(new String[] { "ok.png", "cancel.png" });

        this.tool = tool;

        addLabelled(tr("Name:"), name);
        addLabelled(tr("CmdLine:"), cmdline);

        load();

        setContent(panel);
        setupDialog();
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        if (evt.getActionCommand().equals(tr("OK"))) {
            save();
        }
        super.buttonAction(buttonIndex, evt);
    }
}
