/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * @author cdaller
 *
 */
public class NavigatorAction extends AbstractAction implements SelectionChangedListener {
  private NavigatorPlugin navigatorPlugin;
  private List<Node> selectedNodes;
  private int selectionChangedCalls;

  public NavigatorAction(NavigatorPlugin navigatorPlugin) {
    super(tr("Navigate"));
    this.navigatorPlugin = navigatorPlugin;
    selectedNodes = new ArrayList<Node>();
    DataSet.listeners.add(this);
    
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    navigatorPlugin.navigate(selectedNodes);

  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.util.Collection)
   */
  public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
    ++selectionChangedCalls;
//    System.out.println("new selection: " + newSelection);
//    System.out.println("selection till now: " + selectedNodes);
    if(selectionChangedCalls > 1 && (newSelection == null || newSelection.size() == 0)) {
      System.out.println("clearing selection for navigation");
      selectedNodes.clear();
      selectionChangedCalls = 0;
      return;
    }
    if(selectionChangedCalls > 1) {
      selectionChangedCalls = 0;
    }
    Node node;
    // find a newly selected node and add it to the selection
    for(OsmPrimitive selectedElement : newSelection) {
      if(selectedElement instanceof Node) {
        node = (Node)selectedElement;
        if(!selectedNodes.contains(node)) {
          selectedNodes.add(node);
//          System.out.println("adding node " + node.id);
          System.out.println("navigation nodes: " + selectedNodes);
        }
      }
    }    
  }

}
