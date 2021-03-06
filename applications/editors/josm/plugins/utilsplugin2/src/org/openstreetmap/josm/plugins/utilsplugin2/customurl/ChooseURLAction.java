// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.customurl;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

/**
 * Select custom URL
 */
public class ChooseURLAction extends JosmAction {

    public ChooseURLAction() {
        super(tr("Select custom URL"), "selecturl", tr("Select custom URL"), null, true, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showConfigDialog(false);
    }

    @Override
    protected boolean listenToSelectionChange() {
        return false;
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getActiveDataSet() != null);
    }

    public static int showConfigDialog(final boolean fast) {
        JPanel all = new JPanel(new GridBagLayout());

        List<String> items = URLList.getURLList();
        String addr = URLList.getSelectedURL();
        int n = items.size()/2, idxToSelect = -1;
        final String[] names = new String[n];
        final String[] vals = new String[n];
        for (int i = 0; i < n; i++) {
            names[i] = items.get(i*2);
            vals[i] = items.get(i*2+1);
            if (vals[i].equals(addr)) idxToSelect = i;
        }
        final JLabel label1 = new JLabel(tr("Please select one of custom URLs (configured in Preferences)"));
        final JList<String> list1 = new JList<>(names);
        final JTextField editField = new JTextField();
        final JCheckBox check1 = new JCheckBox(tr("Ask every time"));

        final ExtendedDialog dialog = new ExtendedDialog(MainApplication.getMainFrame(),
                tr("Configure custom URL"),
                new String[] {tr("OK"), tr("Cancel")}
                );
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int idx = list1.getSelectedIndex();
                if (idx >= 0) editField.setText(vals[idx]);
            }
        });
        list1.setSelectedIndex(idxToSelect);
        check1.setSelected(Config.getPref().getBoolean("utilsplugin2.askurl", false));

        editField.setEditable(false);

        all.add(label1, GBC.eop().fill(GBC.HORIZONTAL).insets(15, 5, 15, 0));
        all.add(list1, GBC.eop().fill(GBC.HORIZONTAL).insets(5, 5, 0, 0));
        all.add(editField, GBC.eop().fill(GBC.HORIZONTAL).insets(5, 5, 0, 0));
        all.add(check1, GBC.eop().fill(GBC.HORIZONTAL).insets(5, 5, 0, 0));

        int ret = dialog.setContent(all, false)
              .setButtonIcons(new String[] {"ok", "cancel"})
              .setDefaultButton(1)
              .showDialog()
              .getValue();

        int idx = list1.getSelectedIndex();
        if (ret == 1 && idx >= 0) {
            URLList.select(vals[idx]);
            Config.getPref().putBoolean("utilsplugin2.askurl", check1.isSelected());
        }
        return ret;
    }
}
