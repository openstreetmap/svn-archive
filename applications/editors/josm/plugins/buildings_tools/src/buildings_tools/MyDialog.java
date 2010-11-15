package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

public class MyDialog extends ExtendedDialog {
    private static final String[] buttonTexts = new String[] { tr("OK"), tr("Cancel") };
    private static final String[] buttonIcons = new String[] { "ok.png", "cancel.png" };

    protected JPanel panel = new JPanel(new GridBagLayout());

    protected void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std());
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
    }

    public MyDialog(String title) {
        super(Main.parent, title, buttonTexts, true);
        contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(buttonIcons);

        setContent(panel);
        setDefaultButton(1);
    }
}
