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
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

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
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.tools.ImageProvider;
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

    /*
     * Collect options
     */
    File working_dir = new File(Main.pref.getPreferencesDir(), "plugins");
    working_dir = new File(working_dir, "Lakewalker");
    String target = Main.pref.get(LakewalkerPreferences.PREF_PYTHON) + " lakewalker.py";
    LatLon topLeft = Main.map.mapView.getLatLon(0, 0);
    LatLon botRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), Main.map.mapView
        .getHeight());

    /*
     * Build command line
     */
    target += " --lat=" + pos.lat();
    target += " --lon=" + pos.lon();
    target += " --left=" + topLeft.lon();
    target += " --right=" + botRight.lon();
    target += " --top=" + topLeft.lat();
    target += " --bottom=" + botRight.lat();
    target += " --maxnodes=" + Main.pref.get(LakewalkerPreferences.PREF_MAX_NODES, "50000");
    target += " --threshold=" + Main.pref.get(LakewalkerPreferences.PREF_THRESHOLD, "35");
    target += " --josm";


    


    try {
      /*
       * Start the Lakewalker
       */
      Runtime rt = Runtime.getRuntime();
      System.out.println("dir: " + working_dir + ", target: " + target);
      Process p = rt.exec(target, null, working_dir);
      final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      
      /*
       * Start a thread to read the output
       */
      final LakewalkerReader reader = new LakewalkerReader();
      
      PleaseWaitRunnable lakewalkerTask = new PleaseWaitRunnable(tr("Tracing")){
        @Override protected void realRun() throws SAXException {
          reader.read(input);
        }
        @Override protected void finish() {
          
        }
        @Override protected void cancel() {
          reader.cancel();
          
        }
      };
      Main.worker.execute(lakewalkerTask); 
    }
    catch (Exception ex) {
      System.out.println("Exception caught: " + ex.getMessage());
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


  // class DuplicateDialog extends JDialog {
  // private static final long serialVersionUID = 1L;
  // protected Box mainPanel;
  // protected IntConfigurer offset;
  // protected boolean cancelled;
  // protected String right;
  // protected String left;
  // protected JComboBox moveCombo;
  //
  // public DuplicateDialog(String title) {
  // super();
  // this.setTitle(title);
  // this.setModal(true);
  // initComponents();
  // }
  //
  // protected void initComponents() {
  // mainPanel = Box.createVerticalBox();
  // offset = new IntConfigurer("", tr("Offset (metres): "), new Integer(15));
  // mainPanel.add(offset.getControls());
  // getContentPane().add(mainPanel);
  //
  // right = tr("right/down");
  // left = tr("left/up");
  // Box movePanel = Box.createHorizontalBox();
  // movePanel.add(new JLabel(tr("Create new segments to the ")));
  // moveCombo = new JComboBox(new String[] {right, left});
  // movePanel.add(moveCombo);
  // movePanel.add(new JLabel(tr(" of existing segments.")));
  // mainPanel.add(movePanel);
  //
  // Box buttonPanel = Box.createHorizontalBox();
  // JButton okButton = new JButton(tr("Ok"));
  // okButton.addActionListener(new ActionListener() {
  // public void actionPerformed(ActionEvent e) {
  // cancelled = false;
  // setVisible(false);
  //
  // }
  // });
  // JButton canButton = new JButton(tr("Cancel"));
  // canButton.addActionListener(new ActionListener() {
  // public void actionPerformed(ActionEvent e) {
  // cancelled = true;
  // setVisible(false);
  // }
  // });
  // buttonPanel.add(okButton);
  // buttonPanel.add(canButton);
  // mainPanel.add(buttonPanel);
  //
  // pack();
  // }
  //
  // protected int getOffset() {
  // int off = offset.getIntValue(15);
  // return right.equals(moveCombo.getSelectedItem()) ? off : -off;
  // }
  //
  // protected boolean isCancelled() {
  // return cancelled;
  // }
  //
  // }
}
