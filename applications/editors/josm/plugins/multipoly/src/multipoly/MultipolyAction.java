// License: GPL.
package multipoly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Create multipolygon from selected ways automatically.
 *
 * New relation with type=multipolygon is created
 *
 * If one or more of ways is already in relation with type=multipolygon or the way os not closed,
 * then error is reported and no relation is created
 *
 * The "inner" and "outer" roles are guessed automatically.
 * First, bbox is calculated for each way. then the largest area is assumed to be outside
 * and the rest inside
 * In cases with one "outside" area and several cut-ins, the guess should be always good ...
 * In more complex (multiple outer areas) or buggy (inner and outer ways intersect) scenarios
 * the result is likely to be wrong.
 */
public class MultipolyAction extends JosmAction {

 public MultipolyAction() {
  super(tr("Create multipolygon"), null, tr("Create multipolygon."),
  Shortcut.registerShortcut("tools:multipoly", tr("Tool: {0}", tr("Create multipolygon")),
  KeyEvent.VK_M, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
  setEnabled(true);
 }

 /**
  * The action button has been clicked
  * @param e Action Event
  */
 @Override public void actionPerformed(ActionEvent e) {

  // Get all ways in some type=multipolygon relation
  HashSet<OsmPrimitive> relationsInMulti = new HashSet<OsmPrimitive>();
  for (Relation r : Main.main.getCurrentDataSet().getRelations()) {
   if (!r.isUsable()) continue;
   if (r.get("type")!="multipolygon") continue;
   for (RelationMember rm : r.getMembers()) {
    OsmPrimitive m=rm.getMember();
    if (m instanceof Way) {
     relationsInMulti.add(m);
    }
   }
  }

  //List of selected ways
  List<Way> selectedWays = new ArrayList<Way>();
  //Area of largest way (in square degrees)
  double maxarea=0;
  //Which way is the largest one (outer)
  Way maxWay=null;

  // For every selected way
  for (OsmPrimitive osm : Main.main.getCurrentDataSet().getSelected()) {
   if (osm instanceof Way) {
    Way way = (Way)osm;
    //Check if way is already in another multipolygon
    if (relationsInMulti.contains(osm)) {
     JOptionPane.showMessageDialog(Main.parent,tr("One of the selected ways is already part of another multipolygon."));
     return;
    }
    EastNorth first=null,last=null;
    //Boundingbox of way
    double minx=9999,miny=9999,maxx=-9999,maxy=-9999;
    for (Pair<Node,Node> seg : way.getNodePairs(false)) {
     if (first==null) first=seg.a.getEastNorth();
     last=seg.b.getEastNorth();
     double x=seg.a.getEastNorth().east();
     double y=seg.a.getEastNorth().north();
     if (x<minx) minx=x;
     if (y<miny) miny=y;
     if (x>maxx) maxx=x;
     if (y>maxy) maxy=y;
    }
    //Check if first and last node are the same
    if (!first.equals(last)) {
     JOptionPane.showMessageDialog(Main.parent,tr("Multipolygon must consist only of closed ways."));
     return;
    }
    //Determine area
    double area=(maxx-minx)*(maxy-miny);
    selectedWays.add(way);
    if (area>maxarea) {
     maxarea=area;
     maxWay=way;
    }
   }
  }

  if (Main.map == null) {
   JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
   return;
  }

  if (selectedWays.size()<2) {
   JOptionPane.showMessageDialog(Main.parent, tr("You must select at least two ways."));
   return;
  }

  Collection<Command> cmds = new LinkedList<Command>();
  //Create new relation
  Relation rel=new Relation();
  rel.put("type","multipolygon");
  //Add ways to it
  for (int i=0;i<selectedWays.size();i++) {
   Way s=selectedWays.get(i);
   String xrole="inner";
   if (s==maxWay) xrole="outer";
   RelationMember rm=new RelationMember(xrole,s);
   rel.addMember(rm);
  }
  //Add relation
  cmds.add(new AddCommand(rel));
  //Commit
  Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygon"), cmds));
  Main.map.repaint();
 }

}
