package utilsplugin2.customurl;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.Main;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.SelectionManager;
import static org.openstreetmap.josm.tools.I18n.tr;

public class ChooseURLAction extends JosmAction {

    public ChooseURLAction() {
         super(tr("Select custom URL"), "selecturl", tr("Select custom URL"),null,true,true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       showConfigDialog(false);
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }
        
    public static void showConfigDialog(final boolean fast) {
        JPanel all = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        all.setLayout(layout);
        
        List<String> items = URLList.getURLList();
        String addr = URLList.getSelectedURL();
        int n=items.size()/2 , idxToSelect=-1;
        final String names[] =new String[n];
        final String vals[] =new String[n];
        for (int i=0;i<n;i++) {
            names[i]=items.get(i*2);
            vals[i]=items.get(i*2+1);
            if (vals[i].equals(addr)) idxToSelect=i; 
        }
        final JLabel label1=new JLabel(tr("Please select one of custom URLs (configured in Preferences)"));
        final JList<String> list1=new JList<String>(names);
        final JTextField editField=new JTextField();
        final JCheckBox check1=new JCheckBox(tr("Ask every time"));
        
        final ExtendedDialog dialog = new ExtendedDialog(Main.parent,
                tr("Configure custom URL"),
                new String[] {tr("OK"),tr("Cancel"),}
        );
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int idx=list1.getSelectedIndex();
                if (idx>=0) editField.setText(vals[idx]);
            }
        });
        list1.setSelectedIndex(idxToSelect);
        check1.setSelected(Main.pref.getBoolean("utilsplugin2.askurl",false));
        
        editField.setEditable(false);
        
        all.add(label1,GBC.eop().fill(GBC.HORIZONTAL).insets(15,5,15,0));
        all.add(list1,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        all.add(editField,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        all.add(check1,GBC.eop().fill(GBC.HORIZONTAL).insets(5,5,0,0));
        
        
        dialog.setContent(all, false);
        dialog.setButtonIcons(new String[] {"ok.png","cancel.png",});
        dialog.setDefaultButton(1);
        dialog.showDialog();
        
        int idx = list1.getSelectedIndex();
        if (dialog.getValue() ==1 && idx>=0) {
           URLList.select(vals[idx]);
           Main.pref.put("utilsplugin2.askurl", check1.isSelected());
        }
    }

    
    
}
