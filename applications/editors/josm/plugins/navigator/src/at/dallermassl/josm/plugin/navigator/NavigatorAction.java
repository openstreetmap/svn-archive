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
public class NavigatorAction extends AbstractAction {
  private NavigatorPlugin navigatorPlugin;

  public NavigatorAction(NavigatorPlugin navigatorPlugin) {
    super(tr("Navigate"));
    this.navigatorPlugin = navigatorPlugin;
    
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    navigatorPlugin.navigate();
  }

}
