package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Choice;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AddressDialog extends MyDialog {
    private static String lhousenum, lstreetname;
    private static boolean inc = true;
    private JTextField housenum = new JTextField();
    private JTextField streetname = new JTextField();
    private Choice cincdec = new Choice();

    public AddressDialog() {
        super(tr("Building address"));

        addLabelled(tr("House number:"), housenum);
        addLabelled(tr("Street Name:"), streetname);
        housenum.setText(nextHouseNum());
        streetname.setText(lstreetname);

        cincdec.add(tr("Increment"));
        cincdec.add(tr("Decrement"));
        cincdec.select(inc ? 0 : 1);
        addLabelled(tr("Numbers:"), cincdec);

        setContent(panel);
        setupDialog();
    }

    private static String nextHouseNum() {
        if (lhousenum == null)
            return "";
        try {
            Integer num = NumberFormat.getInstance().parse(lhousenum).intValue();
            if (inc)
                num = num + 2;
            else
                num = num - 2;
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
