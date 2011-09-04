/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilsplugin2;

import java.util.List;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

import static org.openstreetmap.josm.tools.I18n.*;

public class UtilsPluginPreferences  implements PreferenceSetting {
    private final String defaultURL = "http://ru.wikipedia.org/w/index.php?search={name}&fulltext=Search";

    HistoryComboBox combo1=new HistoryComboBox();
    JTable table;

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel pp = gui.createPreferenceTab("utils", tr("Utilsplugin2 settings [TESTING]"),
                tr("Here you can change some preferences of Utilsplugin2 functions"));
        JPanel all = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        all.setLayout(layout);
        
        // FIXME: get rid of hardcoded URLS
        List<String> items = (List<String>) Main.pref.getCollection("utilsplugin2.urlHistory");
        if (items==null || items.size()==0) {
            items= new ArrayList<String>();
            items.add("Wikipedia");
            items.add("http://en.wikipedia.org/w/index.php?search={name}&fulltext=Search");
            items.add("Wikipedia RU");
            items.add(defaultURL);
            items.add("LatLon buildings");
            items.add("http://latlon.org/buildings?zoom=17&lat={#lat}&lon={#lon}&layers=B");
            items.add("AMDMi3 Russian streets");
            items.add("http://addresses.amdmi3.ru/?zoom=11&lat={#lat}&lon={#lon}&layers=B00");
            items.add("Element history");
            items.add("http://www.openstreetmap.org/browse/{#type}/{#id}/history");
            items.add("Browse element");
            items.add("http://www.openstreetmap.org/browse/{#type}/{#id}");
        }
        String addr = Main.pref.get("utilsplugin2.customurl", defaultURL);
        //System.out.println("pref:"+addr);
        
        table=new JTable(new DefaultTableModel(null,new String[]{"Title","URL"}));
        fillRows(items);
        
        HtmlPanel help = new HtmlPanel(tr("Please edit custom URLs and select one row to use with the tool<br/>"
                + " <b>&#123;key&#125;</b> is replaced with the tag value<br/>"
                + " <b>&#123;#id&#125;</b> is replaced with the element ID<br/>"
                + " <b>&#123;#type&#125;</b> is replaced with \"node\",\"way\" or \"relation\" <br/>"
                + " <b>&#123;#lat&#125; , &#123;#lon&#125;</b> is replaced with element latitude/longitude"));
        //help.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.));

        all.add(new JLabel(tr("Custom URL configuration")),GBC.eop().insets(5,10,0,0));
        all.add(help,GBC.eop().insets(5,10,0,0));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(0).setMaxWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                DefaultTableModel model = (DefaultTableModel)(e.getSource());
                String columnName = model.getColumnName(column);
                if (row<0  || column<0) return;
                String data = (String)model.getValueAt(row, column);
                if (data!=null && data.length()>0 && row==model.getRowCount()-1) 
                    model.addRow(new String[]{"",""});
            }
        });
        all.add(table,GBC.eop().fill());
        
        pp.add(all, GBC.eol().fill(GridBagConstraints.BOTH));
    }

    private void fillRows(List<String> items) {
        int p=0,row=0;
        String name, url;
        String data[][] = new String[][] {{"",""}};
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int n=items.size();
        while (true) {
            if (p>=n) break;
            name = items.get(p);
            //System.out.println("name="+name);
            p++;
            if (p>=n) break;
            url = items.get(p);
            //System.out.println("url="+url);
            p++;
            model.addRow(new String[]{name,url});
            row++;
        }
        model.addRow(new String[]{"",""});
    }

    @Override
    public boolean ok() {
        String addr=combo1.getText();
        TableModel model = (table.getModel());
        String v;
        ArrayList<String> lst=new ArrayList<String>();
        int n=model.getRowCount();
        for (int i=0;i<n;i++) {
            v=(String) model.getValueAt(i, 0);
            if (v.length()==0) continue;
            lst.add(v);
            v=(String) model.getValueAt(i, 1);
            lst.add(v);
        }
        Main.pref.putCollection("utilsplugin2.urlHistory",lst);
        int row=table.getSelectedRow();
        if (row!=-1) {
            v=(String) model.getValueAt(row, 1);
            Main.pref.put("utilsplugin2.customurl",v);
        }
        
        try {
            Main.pref.save();
        } catch (IOException ex) {
            Logger.getLogger(UtilsPluginPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
}
