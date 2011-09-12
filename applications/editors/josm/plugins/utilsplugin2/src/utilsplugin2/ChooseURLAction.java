package utilsplugin2;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.Main;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.openstreetmap.josm.actions.JosmAction;
import static org.openstreetmap.josm.tools.I18n.tr;

public class ChooseURLAction extends JosmAction {

    public ChooseURLAction() {
        super(tr("Select custom URL"), "selecturl", tr("Select custom URL"),null,true,true);
         putValue("toolbar", "action/selectURL");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       showConfigDialog();
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }
        
    public static void showConfigDialog() {
        JPanel all = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        all.setLayout(layout);
        
        List<String> items = (List<String>) Main.pref.getCollection("utilsplugin2.urlHistory");
        int n=items.size()/2 , idxToSelect=-1;
        final String names[] =new String[n];
        final String vals[] =new String[n];
        String addr = Main.pref.get("utilsplugin2.customurl");
        for (int i=0;i<n;i++) {
            names[i]=items.get(i*2);
            vals[i]=items.get(i*2+1);
            if (vals[i].equals(addr)) idxToSelect=i; 
        }
        final JLabel label1=new JLabel(tr("Please select one of custom URLs (configured in Preferences)"));
        final JComboBox combo1=new JComboBox(names);
        final JTextField editField=new JTextField();
        final JCheckBox check1=new JCheckBox(tr("Ask every time"));
        
        
        combo1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int idx=combo1.getSelectedIndex();
                if (idx>=0) editField.setText(vals[idx]);
            }
        });
        combo1.setSelectedIndex(idxToSelect);
        check1.setSelected(Main.pref.getBoolean("utilsplugin2.askurl",false));
        
        editField.setEditable(false);
        
        all.add(label1,GBC.eop().fill(GBC.HORIZONTAL).insets(15,5,15,0));
        all.add(combo1,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        all.add(editField,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        all.add(check1,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        
        ExtendedDialog dialog = new ExtendedDialog(Main.parent,
                tr("Configure custom URL"),
                new String[] {tr("OK"),tr("Cancel"),}
        );
        dialog.setContent(all, false);
        dialog.setButtonIcons(new String[] {"ok.png","cancel.png",});
        dialog.setDefaultButton(1);
        dialog.showDialog();
        
        int idx = combo1.getSelectedIndex();
        if (dialog.getValue() ==1 && idx>=0) {
           Main.pref.put("utilsplugin2.customurl", vals[idx]);
           Main.pref.put("utilsplugin2.askurl", check1.isSelected());
        }
    }

    
    
}
