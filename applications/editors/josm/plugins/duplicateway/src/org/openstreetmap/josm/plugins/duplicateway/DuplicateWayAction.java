package org.openstreetmap.josm.plugins.duplicateway;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A plugin to add ways manipulation things
 * 
 * @author Thomas.Walraet
 */
class DuplicateWayAction extends JosmAction implements SelectionChangedListener, MouseListener {

  private static final long serialVersionUID = 1L;
  protected String name;
  protected Cursor oldCursor;
  protected List<Way> selectedWays;

  public DuplicateWayAction(String name) {
    super(name, "duplicateway", tr("Duplicate selected ways."), KeyEvent.VK_W, KeyEvent.CTRL_MASK
        | KeyEvent.SHIFT_MASK, true);
    this.name = name;
    setEnabled(false);
    DataSet.listeners.add(this);
  }

  public void actionPerformed(ActionEvent e) {

//    DataSet d = Main.ds;
    selectedWays = new ArrayList<Way>();
    for (OsmPrimitive osm : Main.ds.getSelected()) {
      if (osm instanceof Way) {
        Way way = (Way) osm;
        EastNorth last = null;
        for (Segment seg : way.segments) {
          if (last != null) {
            if (! seg.from.eastNorth.equals(last)) {
              JOptionPane.showMessageDialog(Main.parent, tr("Can't duplicate unnordered way."));
              return;
            }
          }
          last = seg.to.eastNorth;
        }
        selectedWays.add(way);
      }
    }

    if (Main.map == null) {
      JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
      return;
    }
    
    if (selectedWays.isEmpty()) {
      JOptionPane.showMessageDialog(Main.parent, tr("You must select at least one way."));
      return;
    }

    oldCursor = Main.map.mapView.getCursor();
    Main.map.mapView.setCursor(ImageProvider.getCursor("crosshair", "duplicate"));
    Main.map.mapView.addMouseListener(this);
  }
  
  public static Node createNode(double east, double north) {
    return new Node(Main.proj.eastNorth2latlon(new EastNorth(east, north)));
  }
  /**
   * Duplicate the selected ways. The distance to be offset is 
   * determined by finding the distance of the 'offset' point from 
   * the nearest segment. 
   * 
   * @param offset The point in screen co-ordinates used to calculate the offset distance
   */
  protected void duplicate(Point clickPoint) {

    EastNorth clickEN = Main.map.mapView.getEastNorth(clickPoint.x, clickPoint.y);

    /*
     * First, find the nearest Segment belonging to a selected way
     */
    Segment cs = null;
    for (Way way : selectedWays) {
      double minDistance = Double.MAX_VALUE;
      // segments
      for (Segment ls : way.segments) {
        if (ls.deleted || ls.incomplete)
          continue;
        double perDist = JosmVector.perpDistance(ls, clickEN);
        if (perDist < minDistance) {
          minDistance = perDist;
          cs = ls;
        }
      }
    }
    
    if (cs == null) {
      return;
    }
    
    /*
     * Find the distance we need to offset the new way
     * +ve offset is to the right of the initial way, -ve to the left
     */
    JosmVector closestSegment = new JosmVector(cs);
    double offset = closestSegment.calculateOffset(clickEN);
    
    Collection<Command> commands = new LinkedList<Command>();
    Collection<Way> ways = new LinkedList<Way>();
    
    /*
     * First new node is offset 90 degrees from the first point
     */
    for (Way way : selectedWays) {
      Way newWay = new Way();
      
      Node lastNode = null;
      JosmVector lastLine = null;
    
      for (Segment seg : way.segments) {
        JosmVector currentLine = new JosmVector(seg);
        Node newNode = null;
        
        if (lastNode == null) {
          JosmVector perpVector = new JosmVector(currentLine);
          perpVector.rotate90(offset);
          newNode = createNode(perpVector.getP2().getX(), perpVector.getP2().getY());
          commands.add(new AddCommand(newNode));
        }
        else {
//          if (lastLine != null &&
//              ((lastLine.getTheta() < 0 && currentLine.getTheta() > 0) ||
//              (lastLine.getTheta() > 0 && currentLine.getTheta() < 0))) {
//            offset = -offset;
//          }
//          if ()
//              ) {
//            offset = -offset;
//          }
          JosmVector bisector = lastLine.bisector(currentLine, offset);
          newNode = createNode(bisector.getP2().getX(), bisector.getP2().getY());
          commands.add(new AddCommand(newNode));
          Segment s = new Segment (newNode, lastNode);
          commands.add(new AddCommand(s));
          newWay.segments.add(0, s);
//          if ((lastLine.direction().equals("ne") && currentLine.direction().equals("se")) ||
//              (lastLine.direction().equals("se") && currentLine.direction().equals("ne")) ||
//              (lastLine.direction().equals("nw") && currentLine.direction().equals("sw")) ||
//              (lastLine.direction().equals("sw") && currentLine.direction().equals("nw"))) {
//            offset = -offset;
//          }
        }

        lastLine = currentLine;
        lastNode = newNode;
        
      }
      lastLine.reverse();
      lastLine.rotate90(-offset);
      Node newNode = createNode(lastLine.getP2().getX(), lastLine.getP2().getY());
      commands.add(new AddCommand(newNode));
      Segment s = new Segment (newNode, lastNode);
      commands.add(new AddCommand(s));
      newWay.segments.add(0, s);
      
      for (String key : way.keySet()) {
        newWay.put(key, way.get(key));
      }
      commands.add(new AddCommand(newWay));
      ways.add(newWay);
    }
  
    Main.main.undoRedo.add(new SequenceCommand(tr("Create duplicate way"), commands));
    Main.ds.setSelected(ways);
  }

  protected Node offsetNode(Node oldNode, double offsetE, double offsetN) {
    EastNorth en = Main.proj.latlon2eastNorth(oldNode.coor);
    EastNorth newEn = new EastNorth(en.east()+offsetE, en.north()+offsetN);
    LatLon ll = Main.proj.eastNorth2latlon(newEn);   
    return new Node(ll);
  }
  
  /**
   * Enable the "Duplicate way" menu option if at least one way is selected
   */
  public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
    for (OsmPrimitive osm : newSelection) {
      if (osm instanceof Way) {
          setEnabled(true);
          return;
      }
    }
    setEnabled(false);
  }

  public void mouseClicked(MouseEvent e) {
    Main.map.mapView.removeMouseListener(this);
    Main.map.mapView.setCursor(oldCursor);
    duplicate(e.getPoint());
  }

  public void mouseEntered(MouseEvent e) {
    // TODO Auto-generated method stub
    
  }

  public void mouseExited(MouseEvent e) {
    // TODO Auto-generated method stub
    
  }

  public void mousePressed(MouseEvent e) {
    // TODO Auto-generated method stub
    
  }

  public void mouseReleased(MouseEvent e) {
    // TODO Auto-generated method stub
    
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
