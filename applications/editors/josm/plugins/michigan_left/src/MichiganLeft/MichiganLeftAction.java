package MichiganLeft;

import static org.openstreetmap.josm.tools.I18n.tr;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

public class MichiganLeftAction extends JosmAction {
    private LinkedList<Command> cmds = new LinkedList<Command>();
    
    public MichiganLeftAction() {
    super(tr("Michigan Left"), "michigan_left", tr("Adds no left turn for sets of 4 or 5 ways."), Shortcut.registerShortcut("tools:michigan_left", tr("Tool: {0}", tr("Michigan Left")),
    KeyEvent.VK_M, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
  }

  public void actionPerformed(ActionEvent e) {
    Collection<OsmPrimitive> selection = Main.main.getCurrentDataSet().getSelected();

    int ways = 0;
    for (OsmPrimitive prim : selection) {
      if (prim instanceof Way)  ways++;
    }

    if ((ways != 4) && (ways !=5)) {
      JOptionPane.showMessageDialog(Main.parent, tr("Please select 4 or 5 ways to assign no left turns."));
      return;
    }

    if (ways == 5)
    {
      // Find extremities of ways
      Hashtable ExtremNodes=new Hashtable();
      for (OsmPrimitive prim : selection) {
        if (prim instanceof Way)
        {
          Way way = (Way) prim;
          incrementHashtable(ExtremNodes, way.firstNode());
          incrementHashtable(ExtremNodes, way.lastNode());
        }
      }
      System.out.println(tr("{0} extrem nodes.", ExtremNodes.size()));     
      
      ArrayList<Node> viaNodes=new ArrayList<Node>();
      // find via nodes (they have 3 occurences in the list)
      for (Enumeration enumKey = ExtremNodes.keys() ; enumKey.hasMoreElements(); )
      {
        Node extrem=(Node)enumKey.nextElement();
        Integer nb=(Integer) ExtremNodes.get(extrem);
        System.out.println(tr("Via node {0}, {1}", extrem.getId(), nb.intValue()));
        if (nb.intValue() == 3)
        {
          viaNodes.add(extrem);
        }
      } 
      System.out.println(tr("{0} via nodes.", viaNodes.size()));     
  
      if (viaNodes.size() != 2) {
        JOptionPane.showMessageDialog(Main.parent, tr("Unable to find via nodes. Please check your selection"));
        return;
      }
      
      Node viaFirst = viaNodes.get(0);
      Node viaLast = viaNodes.get(1);      // Find middle segment

      Way middle = null;
      for (OsmPrimitive prim : selection) {
        if (prim instanceof Way)
        {
          Way way = (Way) prim;
          Node first = way.firstNode();
          Node last = way.lastNode();


          if ((first.equals(viaFirst) && last.equals(viaLast)) || (first.equals(viaLast) && last.equals(viaFirst)))
            middle=way;
        }
      }
      System.out.println(tr("MIddle way: {0}", middle.getId()));       
         
      // Build relations
      for (OsmPrimitive prim : selection) {
        if (prim instanceof Way)
        {
          Way way = (Way) prim;
          if (way != middle)
          {
            Node first = way.firstNode();
            Node last = way.lastNode();

            if (first==viaFirst)
              buildRelation(middle, way, viaNodes.get(0));
            else if (first==viaLast)
              buildRelation(middle, way, viaNodes.get(1));
            else if (last==viaFirst)
              buildRelation(way, middle, viaNodes.get(0));
            else if (last==viaLast)
              buildRelation(way, middle, viaNodes.get(1));
          }
        }
      }
      Command c = new SequenceCommand(tr("Create Michigan left turn restriction"), cmds);
      Main.main.undoRedo.add(c);
      cmds.clear();     
    }
  }

  public void incrementHashtable(Hashtable hash, Node node)
  {
    System.out.println(tr("Processing {0}", node.getId()));
    if (hash.containsKey(node))
    {
      Integer nb=(Integer) hash.get(node);
      hash.put(node, new Integer (nb.intValue()+1));
      System.out.println(tr("Old value", nb.intValue()));
    }
    else
      hash.put(node, new Integer (1));
  }
  
  public void buildRelation(Way fromWay, Way toWay, Node viaNode)
  {
    System.out.println(tr("Relation: from {0} to {1} via {2}", fromWay.getId(), toWay.getId(), viaNode.getId()));       
    
    Relation relation = new Relation();
    
    RelationMember from = new RelationMember("from", fromWay);
    relation.addMember(from);
    
    RelationMember to = new RelationMember("to", toWay);
    relation.addMember(to);
    
    RelationMember via = new RelationMember("via", viaNode);
    relation.addMember(via);
    
    relation.put("type", "restriction");
    relation.put("restriction", "no_left_turn");

    cmds.add(new AddCommand(relation));
  }  

}
