// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class AddressDialog extends MyDialog {
    private static String lhousenum, lstreetname;
    private static int inc = 0;
    private JTextField housenum = new JTextField();
    private JTextField streetname = new JTextField();
    private JSpinner incSpinner;

    public AddressDialog() {
        super(tr("Building address"));

        addLabelled(tr("House number:"), housenum);
        addLabelled(tr("Street Name:"), streetname);
        housenum.setText(nextHouseNum());
        streetname.setText(lstreetname);

        SpinnerNumberModel inc_model = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        incSpinner = new JSpinner(inc_model);
        incSpinner.setValue(inc);
        addLabelled(tr("House number increment:"), incSpinner);

        setContent(panel);
        setupDialog();
    }

    private static String nextHouseNum() {
        if (lhousenum == null)
            return "";
        try {
            Integer num = NumberFormat.getInstance().parse(lhousenum).intValue() + inc;
            return num.toString();
        } catch (ParseException e) {
            return lhousenum;
        }
    }

    public final void saveValues() {
        lhousenum = housenum.getText();
        lstreetname = streetname.getText();
        inc = (Integer) incSpinner.getValue();
    }

    public final String getHouseNum() {
        return housenum.getText();
    }

    public final String getStreetName() {
        return streetname.getText();
    }
}
