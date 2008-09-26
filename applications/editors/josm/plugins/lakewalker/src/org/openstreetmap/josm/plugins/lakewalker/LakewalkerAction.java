package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;

import org.xml.sax.SAXException;

/**
 * Interface to Darryl Shpak's Lakewalker module
 * 
 * @author Brent Easton
 */
class LakewalkerAction extends JosmAction implements MouseListener {

  private static final long serialVersionUID = 1L;
  protected String name;
  protected Cursor oldCursor;
  protected List<Node> selectedNodes;
  protected Thread executeThread;
  protected boolean cancel;
  
  protected Collection<Command> commands = new LinkedList<Command>();
  protected Collection<Way> ways = new ArrayList<Way>();
  
  public LakewalkerAction(String name) {
    super(name, "lakewalker-sml", tr("Lake Walker."), KeyEvent.VK_L, KeyEvent.CTRL_MASK
        | KeyEvent.SHIFT_MASK, true);
    this.name = name;
    setEnabled(true);
  }
  
  public void actionPerformed(ActionEvent e) {

    Main.map.mapView.setCursor(oldCursor);

    if (Main.map == null) {
      JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
      return;
    }

    selectedNodes = new ArrayList<Node>();
    for (OsmPrimitive osm : Main.ds.getSelected()) {
      if (osm instanceof Node) {
        Node node = (Node) osm;
        selectedNodes.add(node);
      }
    }

    if (selectedNodes.isEmpty()) {
      oldCursor = Main.map.mapView.getCursor();
      Main.map.mapView.setCursor(ImageProvider.getCursor("crosshair", "lakewalker-sml"));
      Main.map.mapView.addMouseListener(this);
    }
    else {
      lakewalk(selectedNodes);
    }
  }

  protected void lakewalk(Point clickPoint){
	/**
	 * Positional data
	 */
	final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);
	final LatLon topLeft = Main.map.mapView.getLatLon(0, 0);
	final LatLon botRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), Main.map.mapView
	     .getHeight());	    

	/**
	 * Cache/working directory location
	 */
	final File working_dir = new File(Main.pref.getPreferencesDir(), "plugins/Lakewalker");
	
	/*
	 * Collect options
	 */
	final int waylen = Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_SEG, 500);
	final int maxnode = Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_NODES, 50000);
	final int threshold = Main.pref.getInteger(LakewalkerPreferences.PREF_THRESHOLD_VALUE, 90);
	final double epsilon = Main.pref.getDouble(LakewalkerPreferences.PREF_EPSILON, 0.0003);
	final int resolution = Main.pref.getInteger(LakewalkerPreferences.PREF_LANDSAT_RES, 4000);
	final int tilesize = Main.pref.getInteger(LakewalkerPreferences.PREF_LANDSAT_SIZE, 2000);
	final String startdir = Main.pref.get(LakewalkerPreferences.PREF_START_DIR, "east");
	final String wmslayer = Main.pref.get(LakewalkerPreferences.PREF_WMS, "IR1");

	try {
        PleaseWaitRunnable lakewalkerTask = new PleaseWaitRunnable(tr("Tracing")){
          @Override protected void realRun() throws SAXException {
        	  processnodelist(pos, topLeft, botRight, waylen,maxnode,threshold,epsilon,resolution,tilesize,startdir,wmslayer,working_dir);
          }
          @Override protected void finish() {
            
          }
          @Override protected void cancel() {
            cancel();
          }
        };
        Thread executeThread = new Thread(lakewalkerTask);
        executeThread.start();
      }
      catch (Exception ex) {
        System.out.println("Exception caught: " + ex.getMessage());
      }      
  }
  
  private void processnodelist(LatLon pos, LatLon topLeft, LatLon botRight, int waylen, int maxnode, int threshold, double epsilon, int resolution, int tilesize, String startdir, String wmslayer, File workingdir){
	  
	ArrayList<double[]> nodelist = new ArrayList<double[]>();
	  
	Lakewalker lw = new Lakewalker(waylen,maxnode,threshold,epsilon,resolution,tilesize,startdir,wmslayer,workingdir);
	try {
		nodelist = lw.trace(pos.lat(),pos.lon(),topLeft.lon(),botRight.lon(),topLeft.lat(),botRight.lat());
	} catch(LakewalkerException e){
		System.out.println(e.getError());
	}
	
	System.out.println(nodelist.size()+" nodes generated");
	
	/**
	 * Run the nodelist through a vertex reduction algorithm
	 */
	
	setStatus("Running vertex reduction...");
	
	nodelist = lw.vertexReduce(nodelist, epsilon);
	
	System.out.println("After vertex reduction "+nodelist.size()+" nodes remain.");
	
	/**
	 * And then through douglas-peucker approximation
	 */
	
	setStatus("Running Douglas-Peucker approximation...");
	
	nodelist = lw.douglasPeucker(nodelist, epsilon);
	
	System.out.println("After Douglas-Peucker approximation "+nodelist.size()+" nodes remain.");
	  
	/**
	 * And then through a duplicate node remover
	 */
	
	setStatus("Removing duplicate nodes...");
	
	nodelist = lw.duplicateNodeRemove(nodelist);
	
	System.out.println("After removing duplicate nodes, "+nodelist.size()+" nodes remain.");
	  
	
	/**
	 * Turn the arraylist into osm nodes
	 */
	
	Way way = new Way();
	Node n = null;
	Node tn = null;
	Node fn = null;
	
	double eastOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_EAST_OFFSET, 0.0);
	double northOffset = Main.pref.getDouble(LakewalkerPreferences.PREF_NORTH_OFFSET, 0.0);
	char option = ' ';
	
	int nodesinway = 0;
	
	for(int i = 0; i< nodelist.size(); i++){
		if (cancel) {
			return;
	    }
		 	
		try {        	
		  LatLon ll = new LatLon(nodelist.get(i)[0]+northOffset, nodelist.get(i)[1]+eastOffset);
		  n = new Node(ll);
		  if(fn==null){
		    fn = n;
		  }
		  commands.add(new AddCommand(n));
		  
		} catch (Exception ex) {		 
		}	    
	      
		way.nodes.add(n);
		
		if(nodesinway > Main.pref.getInteger(LakewalkerPreferences.PREF_MAX_SEG, 500)){
			String waytype = Main.pref.get(LakewalkerPreferences.PREF_WAYTYPE, "water");
	        
	        if(!waytype.equals("none")){
	      	  way.put("natural",waytype);
	        }
	        
	        way.put("created_by", "Dshpak_landsat_lakes");
	        commands.add(new AddCommand(way));
	        
	        way = new Way();

	        way.nodes.add(n);
	        
	        nodesinway = 0;
		}
		nodesinway++;
	}
	
	
	String waytype = Main.pref.get(LakewalkerPreferences.PREF_WAYTYPE, "water");
    
    if(!waytype.equals("none")){
  	  way.put("natural",waytype);
    }
    
    way.put("created_by", "Dshpak_landsat_lakes");
    
	way.nodes.add(fn);
	
	commands.add(new AddCommand(way));
	
	if (!commands.isEmpty()) {
        Main.main.undoRedo.add(new SequenceCommand(tr("Lakewalker trace"), commands));
        Main.ds.setSelected(ways);
    } else {
  	  System.out.println("Failed");
    }
	
	commands = new LinkedList<Command>();
	ways = new ArrayList<Way>();
	
  }
  
  public void cancel() {
	  cancel = true;
  }

  protected void lakewalk(List nodes) {

  }

  public void mouseClicked(MouseEvent e) {
    Main.map.mapView.removeMouseListener(this);
    Main.map.mapView.setCursor(oldCursor);
    lakewalk(e.getPoint());
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }
  protected void setStatus(String s) {
	  Main.pleaseWaitDlg.currentAction.setText(s);
	  Main.pleaseWaitDlg.repaint();
  }
}
