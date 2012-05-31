// License: GPL. See LICENSE file for details.
// Derived from DownloadPrimitiveAction by Matthias Julius

package org.openstreetmap.josm.plugins.undelete;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationMemberData;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.history.History;
import org.openstreetmap.josm.data.osm.history.HistoryDataSet;
import org.openstreetmap.josm.data.osm.history.HistoryNode;
import org.openstreetmap.josm.data.osm.history.HistoryOsmPrimitive;
import org.openstreetmap.josm.data.osm.history.HistoryRelation;
import org.openstreetmap.josm.data.osm.history.HistoryWay;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.history.HistoryLoadTask;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.widgets.OsmIdTextField;
import org.openstreetmap.josm.gui.widgets.OsmPrimitiveTypesComboBox;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *  // TODO: undelete relation members if necessary
 */

public class Undelete extends Plugin {
    JMenuItem Undelete;

    public Undelete(PluginInformation info) {
        super(info);
        Undelete = MainMenu.addAfter(Main.main.menu.fileMenu, new UndeleteAction(), false, Main.main.menu.updateModified);

    }


    private class UndeleteAction extends JosmAction {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public UndeleteAction() {
        super(tr("Undelete object..."), "undelete", tr("Undelete object by id"), Shortcut.registerShortcut("tools:undelete", tr("File: {0}", tr("Undelete object...")),
        KeyEvent.VK_U, Shortcut.ALT_SHIFT), true);
      }

      @Override
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
        all.add(new JLabel(tr("Object ID:")), gc);
        OsmIdTextField tfId = new OsmIdTextField();
        tfId.setText(Main.pref.get("undelete.osmid"));
        tfId.setToolTipText(tr("Enter the type and ID of the objects that should be undeleted, e.g., ''n1 w2''"));
        // forward the enter key stroke to the undelete button
        tfId.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        gc.weightx = 1;
        all.add(tfId, gc);
        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;
        gc.weightx = 0;
        gc.gridy++;
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
        Main.pref.put("undelete.osmid", tfId.getText());
        undelete(layer.isSelected(), tfId.getIds(), 0);
      }
    }

    /**
     * Download the given primitive.
     */
    public void undelete(boolean newLayer, final List<PrimitiveId> ids, final long parent) {
        OsmDataLayer tmpLayer = Main.main.getEditLayer();
        if ((tmpLayer == null) || newLayer) {
            tmpLayer = new OsmDataLayer(new DataSet(), OsmDataLayer.createNewName(), null);
            Main.main.addLayer(tmpLayer);
        }

        final DataSet datas = tmpLayer.data;
        final OsmDataLayer layer=tmpLayer;

        HistoryLoadTask task  = new HistoryLoadTask();
        for (PrimitiveId id: ids) {
            task.add(id);
        }



        Main.worker.execute(task);

        Runnable r = new Runnable() {
            @Override
            public void run() {
              List<Node> nodes=new ArrayList<Node>();
              for (PrimitiveId pid: ids)
              {

                final Long id = pid.getUniqueId();
                final OsmPrimitiveType type = pid.getType();

                History h = HistoryDataSet.getInstance().getHistory(id, type);

                OsmPrimitive primitive;
                HistoryOsmPrimitive hPrimitive1=h.getLatest();
                HistoryOsmPrimitive hPrimitive2;

                boolean visible=hPrimitive1.isVisible();

                if (visible)
                {
                  // If the object is not deleted we get the real object
                  DownloadPrimitiveTask download=new DownloadPrimitiveTask(pid, layer);
                  System.out.println(tr("Will get {0}", pid));
                  download.run();


                  System.out.println(tr("Looking for {0}", pid));
                  primitive=datas.getPrimitiveById(id, type);
                  System.out.println(tr("Found {0}", String.valueOf(primitive.getId())));
                  if (parent>0 && type.equals(OsmPrimitiveType.NODE))
                  {
                      nodes.add((Node)primitive);
                  }
                }
                else
                {
                  if (type.equals(OsmPrimitiveType.NODE))
                  {
                    // We get all info from the latest version
                    hPrimitive2=hPrimitive1;

                    Node node = new Node(id, (int) hPrimitive1.getVersion());

                    HistoryNode hNode = (HistoryNode) hPrimitive1;
                    node.setCoor(hNode.getCoords());

                    primitive=node;
                    if (parent>0)
                    {
                      nodes.add(node);
                    }
                  }
                  else if (type.equals(OsmPrimitiveType.WAY))
                  {
                    // We get version and user from the latest version, nodes and tags from n-1 version
                    hPrimitive1 = h.getLatest();
                    hPrimitive2 = h.getByVersion(h.getNumVersions()-1);



                    Way way = new Way(id, (int) hPrimitive1.getVersion());

                    HistoryWay hWay = (HistoryWay) hPrimitive2;
                    //System.out.println(tr("Primitive {0} version {1}: {2} nodes", hPrimitive2.getId(), hPrimitive2.getVersion(), hWay.getNumNodes()));
                    List<PrimitiveId> nodeIds = new ArrayList<PrimitiveId>();
                    for (Long i: hWay.getNodes()) {
                        nodeIds.add(new SimplePrimitiveId(i, OsmPrimitiveType.NODE));
                    }
                    undelete(false, nodeIds, id);

                    primitive=way;

                  }
                  else
                  {
                      primitive=new Relation();
                      hPrimitive1=h.getLatest();
                      hPrimitive2=h.getByVersion(h.getNumVersions()-1);

                      Relation rel = new Relation(id, (int) hPrimitive1.getVersion());

                      HistoryRelation hRel = (HistoryRelation) hPrimitive2;

                      List<RelationMember> members = new ArrayList<RelationMember>(hRel.getNumMembers());
                      for (RelationMemberData m : hRel.getMembers()) {
                        OsmPrimitive p = datas.getPrimitiveById(m.getMemberId(), m.getMemberType());
                        if (p == null) {
                            switch (m.getMemberType()) {
                            case NODE: p = new Node(m.getMemberId()); break;
                            case WAY: p = new Way(m.getMemberId()); break;
                            case RELATION: p = new Relation(m.getMemberId()); break;
                            }
                            datas.addPrimitive(p);
                        }
                        members.add(new RelationMember(m.getRole(), p));
                      }

                      rel.setMembers(members);

                      primitive=rel;
                  }

                  User user = hPrimitive1.getUser();

                  primitive.setUser(user);

                  primitive.setKeys(hPrimitive2.getTags());

                  primitive.put("history", "retrieved using undelete JOSM plugin");

                  primitive.setModified(true);

                  datas.addPrimitive(primitive);
                }


                //HistoryBrowserDialogManager.getInstance().show(h);
              }
              if (parent>0 && !ids.isEmpty() && ids.iterator().next().getType().equals(OsmPrimitiveType.NODE))
              {
                Way parentWay=(Way)datas.getPrimitiveById(parent, OsmPrimitiveType.WAY);

                parentWay.setNodes(nodes);
              }
            }
        };
        Main.worker.submit(r);

        //if (downloadReferrers) {
        //    Main.worker.submit(new DownloadReferrersTask(layer, id, type));
        //}
    }
}
