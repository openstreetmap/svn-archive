package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Choice;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class AddressDialog extends ExtendedDialog {
    private static String lhousenum,lstreetname;
    private static boolean inc = true;
    private JTextField housenum = new JTextField();
    private JTextField streetname = new JTextField();
    private Choice cincdec = new Choice();

    private JPanel panel = new JPanel(new GridBagLayout());
    private void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std());
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
    }

    public AddressDialog() {
        super(Main.parent, tr("Building address"),
                new String[] { tr("OK"), tr("Cancel") },
                true);

        contentInsets = new Insets(15,15,5,15);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });

        addLabelled(tr("House number:"),housenum);
        addLabelled(tr("Street Name:"),streetname);
        housenum.setText(nextHouseNum());
        streetname.setText(lstreetname);

        cincdec.add(tr("Increment"));
        cincdec.add(tr("Decrement"));
        cincdec.select(inc?0:1);
        addLabelled(tr("Numbers:"), cincdec);

        setContent(panel);
        setupDialog();
        setVisible(true);
    }

    private static String nextHouseNum() {
        if (lhousenum==null) return "";
        try {
            Integer num = NumberFormat.getInstance().parse(lhousenum).intValue();
            if (inc) num=num+2; else num = num-2;
            return num.toString();
        } catch (ParseException e) {
            return lhousenum;
        }
    }
    public void saveValues() {
        lhousenum = housenum.getText();
        lstreetname = streetname.getText();
        inc = cincdec.getSelectedIndex() == 0;
    }
    public String getHouseNum() {
        return housenum.getText();
    }
    public String getStreetName() {
        return streetname.getText();
    }
}
