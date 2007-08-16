/* LakewalkerReader.java
 * 
 * Read and process data from a Lakwalker python module
 * 
 */
package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

public class LakewalkerReader {
  
  protected Collection<Command> commands = new LinkedList<Command>();
  protected Collection<Way> ways = new ArrayList<Way>();
  protected boolean cancel;
  
  /*
   * Read the data
   */
  public void read(BufferedReader input) {
    /*
     * Lakewalker will output data it stdout. Each line has a code in
     * character 1 indicating the type of data on the line:
     * 
     * m text - Status message l name [size] - Access landsat image name. size
     * is returned if it needs to be downloaded. e text - Error message s nnn -
     * Start node data stream, nnn seperate tracings to follow t nnn - Start
     * tracing, nnn nodes to follow x [o] - End of Tracing. o indicates do not
     * connect last node to first n lat lon [o] - Node. o indicates it is an
     * open node (not connected to the previous node) z - End of data stream
     */

    Way way = new Way();
    Node lastNode = null;
    Node firstNode = null;
    String line;
    Main.pleaseWaitDlg.currentAction.setText("Initializing");
    Main.pleaseWaitDlg.repaint();

    try {
      while ((line = input.readLine()) != null) {
        if (cancel) {
          return;
        }
        System.out.println(line);
        char option = line.charAt(0);
        switch (option) {
        case 'n':
          String[] tokens = line.split(" ");
          try {
            LatLon ll = new LatLon(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
            Node n = new Node(ll);
            commands.add(new AddCommand(n));
            if (lastNode != null) {
              Segment s = new Segment(lastNode, n);
              commands.add(new AddCommand(s));
              way.segments.add(s);
            }
            else {
              firstNode = n;
            }
            lastNode = n;
          }
          catch (Exception ex) {

          }
          break;

        case 's':
          Main.pleaseWaitDlg.currentAction.setText(line.substring(2));
          Main.pleaseWaitDlg.repaint();
          break;
          
        case 'x':
          Segment s = new Segment(lastNode, firstNode);
          commands.add(new AddCommand(s));
          way.segments.add(s);
          way.put("created_by", "Dshpak_landsat_lakes");
          commands.add(new AddCommand(way));
          break;
        }
      } 
      input.close();
    }

    catch (Exception ex) {
    }
    
    if (!commands.isEmpty()) {
      Main.main.undoRedo.add(new SequenceCommand(tr("Lakewalker trace"), commands));
      Main.ds.setSelected(ways);
    }
  }
  
  /*
   * User has hit the cancel button
   */
  public void cancel() {
    cancel = true;
  }
  
}