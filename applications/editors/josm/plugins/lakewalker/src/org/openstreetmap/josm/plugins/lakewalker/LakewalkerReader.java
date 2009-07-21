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
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

public class LakewalkerReader {
    protected Collection<Command> commands = new LinkedList<Command>();
    protected Collection<Way> ways = new ArrayList<Way>();
    protected boolean cancel;

    /*
    * Read the data
    */
    public void read(BufferedReader input, ProgressMonitor progressMonitor) {
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

    	progressMonitor.beginTask(null);
    	try {
    		Way way = new Way();
    		String line;
    		progressMonitor.indeterminateSubTask(tr("Initializing"));
    		double eastOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_EAST_OFFSET, 0.0);
    		double northOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_NORTH_OFFSET, 0.0);
    		char option = ' ';

    		try {
    			Node n = null;  // The current node being created
    			Node tn = null; // The last node of the previous way
    			Node fn = null; // Node to hold the first node in the trace

    			while ((line = input.readLine()) != null) {
    				if (cancel)
    					return;
    				System.out.println(line);
    				option = line.charAt(0);
    				switch (option) {
    				case 'n':
    					String[] tokens = line.split(" ");

    					if(tn==null){
    						try {
    							LatLon ll = new LatLon(Double.parseDouble(tokens[1])+northOffset,
    									Double.parseDouble(tokens[2])+eastOffset);
    							n = new Node(ll);
    							if(fn==null)
    								fn = n;
    							commands.add(new AddCommand(n));
    						}
    						catch (Exception ex) {}
    					} else {
    						// If there is a last node, and this node has the same coordinates
    						// then we substitute for the previous node
    						n = tn;
    						tn = null;
    					}
    					way.nodes.add(n);
    					break;
    				case 's':
    					progressMonitor.indeterminateSubTask(line.substring(2));
    					break;
    				case 'x':
    					String waytype = Main.pref.get(LakewalkerPreferences.PREF_WAYTYPE, "water");

    					if(!waytype.equals("none"))
    						way.put("natural",waytype);
    					way.put("source", Main.pref.get(LakewalkerPreferences.PREF_SOURCE, "Landsat"));
    					commands.add(new AddCommand(way));
    					break;
    				case 't':
    					way = new Way();
    					tn = n;
    					break;
    				case 'e':
    					cancel = true;
    					break;
    				}
    			}
    			input.close();

    			// Add the start node to the end of the trace to form a closed shape
    			way.nodes.add(fn);
    		}
    		catch (Exception ex) { }

    		if (!commands.isEmpty()) {
    			Main.main.undoRedo.add(new SequenceCommand(tr("Lakewalker trace"), commands));
    			Main.main.getCurrentDataSet().setSelected(ways);
    		}
    	} finally {
    		progressMonitor.finishTask();
    	}
    }

    /*
    * User has hit the cancel button
    */
    public void cancel() {
        cancel = true;
    }
}