// License: GPL. See LICENSE file for details.
// Derived from DownloadPrimitiveAction by Matthias Julius

package Undelete;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadPrimitiveTask;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.history.History;
import org.openstreetmap.josm.data.osm.history.HistoryDataSet;
import org.openstreetmap.josm.data.osm.history.HistoryNode;
import org.openstreetmap.josm.data.osm.history.HistoryOsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.history.HistoryLoadTask;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.widgets.OsmIdTextField;
import org.openstreetmap.josm.gui.widgets.OsmPrimitiveTypesComboBox;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;



public class Undelete extends Plugin {
    JMenuItem Undelete;

    public Undelete(PluginInformation info) {
    	super(info);
        Undelete = MainMenu.add(Main.main.menu.fileMenu, new UndeleteAction());

    }
    
    
    private class UndeleteAction extends JosmAction {
        private LinkedList<Command> cmds = new LinkedList<Command>();

        public UndeleteAction() {
        super(tr("Undelete object..."), "undelete", tr("Undelete object by id"), Shortcut.registerShortcut("tools:undelete", tr("File: {0}", tr("Undelete object...")),
        KeyEvent.VK_U, Shortcut.GROUP_EDIT, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.ALT_DOWN_MASK), true);
      }

      public void actionPerformed(ActionEvent e) {
        JCheckBox layer = new JCheckBox(tr("Separate Layer"));
        layer.setToolTipText(tr("Select if the data should be added into a new layer"));
        layer.setSelected(Main.pref.getBoolean("undelete.newlayer"));
        JPanel all = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0;
        all.add(new JLabel(tr("Object type:")), gc);
        OsmPrimitiveTypesComboBox cbType = new OsmPrimitiveTypesComboBox();
        cbType.setToolTipText("Choose the OSM object type");
        gc.weightx = 1;
        all.add(cbType, gc);
        gc.gridy = 1;
        gc.weightx = 0;
        all.add(new JLabel(tr("Object ID:")), gc);
        OsmIdTextField tfId = new OsmIdTextField();
        tfId.setText(Main.pref.get("undelete.osmid"));
        tfId.setToolTipText(tr("Enter the ID of the object that should be undeleted"));
        // forward the enter key stroke to the undelete button
        tfId.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        gc.weightx = 1;
        all.add(tfId, gc);
        gc.gridy = 2;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;
        gc.weightx = 0;
        gc.gridy = 3;
        all.add(layer, gc);
        ExtendedDialog dialog = new ExtendedDialog(Main.parent,
                tr("Undelete Object"),
                new String[] {tr("Undelete object"), tr("Cancel")}
        );
        dialog.setContent(all, false);
        dialog.setButtonIcons(new String[] {"undelete.png", "cancel.png"});
        dialog.setToolTipTexts(new String[] {
                tr("Start undeleting"),
                tr("Close dialog and cancel")
        });
        dialog.setDefaultButton(1);
        //dialog.configureContextsensitiveHelp("/Action/DownloadObject", true /* show help button */);
        dialog.showDialog();
        if (dialog.getValue() != 1) return;
        Main.pref.put("undelete.newlayer", layer.isSelected());
        Main.pref.putInteger("undelete.osmid", tfId.getOsmId());
        undelete(layer.isSelected(), cbType.getType(), tfId.getOsmId());
      }
    }      
          
    /**
     * Download the given primitive.
     */
    public void undelete(boolean newLayer, final OsmPrimitiveType type, final long id) {
        OsmDataLayer layer = Main.main.getEditLayer();
        if ((layer == null) || newLayer) {
            layer = new OsmDataLayer(new DataSet(), OsmDataLayer.createNewName(), null);
            Main.main.addLayer(layer);
        }
        
        final DataSet datas = layer.data;
        
        HistoryLoadTask task  = new HistoryLoadTask();
        task.add (id, type);
        
        
        Main.worker.execute(task);
        
        Runnable r = new Runnable() {
            public void run() {
                History h = HistoryDataSet.getInstance().getHistory(id, type);
                
                OsmPrimitive primitive;
                HistoryOsmPrimitive hPrimitive=h.getLatest();
                
                if (type.equals(OsmPrimitiveType.NODE))
                {
                  HistoryNode hNode = (HistoryNode) hPrimitive;
                  
                  Node node = new Node(id, (int) hNode.getVersion());
                  node.setCoor(hNode.getCoords());
                  
                  primitive=node;
                }
                else
                { primitive=new Node();}
                
                primitive.setKeys(hPrimitive.getTags());
                
                User user = User.createOsmUser(hPrimitive.getUid(), hPrimitive.getUser());
                
                primitive.setUser(user);
                
                primitive.setModified(true);
                
                datas.addPrimitive(primitive);                
                //HistoryBrowserDialogManager.getInstance().show(h);
            }
        };
        Main.worker.submit(r);
        
        //if (downloadReferrers) {
        //    Main.worker.submit(new DownloadReferrersTask(layer, id, type));
        //}
    }      
}
