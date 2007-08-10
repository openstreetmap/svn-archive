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

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.ImageProvider;

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
  
  protected void lakewalk(Point clickPoint) {
   LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);
   String line;
   
   File working_dir = new File (Main.pref.getPreferencesDir(), "plugins");
   working_dir = new File(working_dir, "Lakewalker");
   String target = Main.pref.get(LakewalkerPlugin.PREF_PYTHON) + " lakewalker.py";
   LatLon topLeft = Main.map.mapView.getLatLon(0, 0);
   LatLon botRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), Main.map.mapView.getHeight());
   
   target += " --lat=" + pos.lat();
   target += " --lon=" + pos.lon();
   target += " --left=" + topLeft.lon();
   target += " --right=" + botRight.lon();
   target += " --top=" + topLeft.lat();
   target += " --bottom=" + botRight.lat();
   target += " --josm";
   
   Collection<Command> commands = new LinkedList<Command>();
   Way way = new Way();
   Node lastNode = null;
   Node firstNode = null;
   
   try
   {
    Runtime rt = Runtime.getRuntime();
    System.out.println("dir: "+working_dir+", target: "+target);
    Process p = rt.exec(target, null, working_dir);
    System.out.println("Just Run");
    BufferedReader input =
     
      new BufferedReader
 
      (new InputStreamReader(p.getInputStream()));
    BufferedReader err = new BufferedReader(new InputStreamReader (p.getErrorStream())) ;
    
    /*
     * Lakewalker will output data it stdout. Each line has a code
     * in character 1 indicating the type of data on the line:
     * 
     * m text - Status message
     * l name [size] - Access landsat image name. size is returned if it needs to be downloaded.
     * e text - Error message
     * s nnn - Start node data stream, nnn seperate tracings to follow
     * t nnn - Start tracing, nnn nodes to follow
     * x [o] - End of Tracing. o indicates do not connect last node to first
     * n lat lon [o] - Node. o indicates it is an open node (not connected to the previous node)
     * z - End of data stream
     */
    while ((line = input.readLine()) != null) {
      System.out.println(line);
      char option = line.charAt(0);
      switch(option) {
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
          
      case 'x':
          Segment s = new Segment(lastNode, firstNode);
          commands.add(new AddCommand(s));
          way.segments.add(s);
          commands.add(new AddCommand(way));
          break;
      }
      
     }
    
    while ((line = err.readLine()) != null) {
       System.out.println(line);
     }
 
    input.close();
    p.destroy();

   }
   catch (Exception ex) {
     System.out.println("Exception caught: "+ex.getMessage());
   }
   
   if (!commands.isEmpty()) {
     Main.main.undoRedo.add(new SequenceCommand(tr("Lakewalker trace"), commands));
     Main.ds.setSelected(way);
   }
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
  
//  class DuplicateDialog extends JDialog {
//    private static final long serialVersionUID = 1L;
//    protected Box mainPanel;
//    protected IntConfigurer offset;
//    protected boolean cancelled;
//    protected String right;
//    protected String left;
//    protected JComboBox moveCombo;
//
//    public DuplicateDialog(String title) {
//      super();
//      this.setTitle(title);
//      this.setModal(true);
//      initComponents();
//    }
//
//    protected void initComponents() {
//      mainPanel = Box.createVerticalBox();
//      offset = new IntConfigurer("", tr("Offset (metres):  "), new Integer(15));
//      mainPanel.add(offset.getControls());
//      getContentPane().add(mainPanel);
//
//      right = tr("right/down");
//      left = tr("left/up");
//      Box movePanel = Box.createHorizontalBox();
//      movePanel.add(new JLabel(tr("Create new segments to the ")));
//      moveCombo = new JComboBox(new String[] {right, left});
//      movePanel.add(moveCombo);
//      movePanel.add(new JLabel(tr(" of existing segments.")));
//      mainPanel.add(movePanel);
//
//      Box buttonPanel = Box.createHorizontalBox();
//      JButton okButton = new JButton(tr("Ok"));
//      okButton.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//          cancelled = false;
//          setVisible(false);
//
//        }
//      });
//      JButton canButton = new JButton(tr("Cancel"));
//      canButton.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//          cancelled = true;
//          setVisible(false);
//        }
//      });
//      buttonPanel.add(okButton);
//      buttonPanel.add(canButton);
//      mainPanel.add(buttonPanel);
//
//      pack();
//    }
//
//    protected int getOffset() {
//      int off = offset.getIntValue(15);
//      return right.equals(moveCombo.getSelectedItem()) ? off : -off;
//    }
//
//    protected boolean isCancelled() {
//      return cancelled;
//    }
//
//  }
}
