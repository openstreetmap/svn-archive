package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

public class Address extends MapMode implements MouseListener, MouseMotionListener, ActionListener {
    private static final long serialVersionUID = 1L;
    
    // perhaps make all these tags configurable in the future
    private String tagHighway = "highway";
    private String tagHighwayName = "name";
    private String tagHouseNumber = "addr:housenumber";
    private String tagHouseStreet = "addr:street";
    private String tagBuilding = "building";
    private String relationAddrType = "associatedStreet";
    private String relationAddrName = "name";
    private String relationAddrStreetRole = "street";
    private String relationMemberHouse = "house";
    
    private JRadioButton plus_one = new JRadioButton("+1", false);
    private JRadioButton plus_two = new JRadioButton("+2", true); // enable this by default
    private JRadioButton minus_one = new JRadioButton("-1", false);
    private JRadioButton minus_two = new JRadioButton("-2", false);
    final JCheckBox tagPolygon = new JCheckBox(tr("on polygon"));

    JDialog dialog = null;
    JButton clearButton = null;
    final JTextField inputNumber = new JTextField();
    final JTextField inputStreet = new JTextField();
    JLabel link = new JLabel();
    private Way selectedWay;
    private boolean shift;
    private boolean ctrl;

    public Address(MapFrame mapFrame) {
        super(tr("Add address"), "buildings", 
                tr("Helping tool for tag address"),
                Shortcut.registerShortcut("mapmode:cadastre-fr-buildings", tr("Mode: {0}", tr("CadastreFR - Buildings")), KeyEvent.VK_E, Shortcut.DIRECT),
                mapFrame, getCursor());
    }

    @Override public void enterMode() {
        super.enterMode();
        if (dialog == null) {
            createDialog();
        }
        dialog.setVisible(true);
        Main.map.mapView.addMouseListener(this);
    }

    @Override public void exitMode() {
        if (Main.map.mapView != null) {
            super.exitMode();
            Main.map.mapView.removeMouseListener(this);
        }
        dialog.setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        MapView mv = Main.map.mapView;
        Point mousePos = e.getPoint();
        List<Way> mouseOnExistingWays = new ArrayList<Way>();
        List<Way> mouseOnExistingBuildingWays = new ArrayList<Way>();
        mouseOnExistingWays = new ArrayList<Way>();
        Node currentMouseNode = mv.getNearestNode(mousePos, OsmPrimitive.isSelectablePredicate);
        if (currentMouseNode != null) {
            // click on existing node
            setNewSelection(currentMouseNode);
            String num = currentMouseNode.get(tagHouseNumber);
            if (num != null //
                    && currentMouseNode.get(tagHouseStreet) == null //
                    && findWayInRelationAddr(currentMouseNode) == null //
                    && !inputStreet.getText().equals("")) {
                // address already present but not linked to a street
                Collection<Command> cmds = new LinkedList<Command>();
                addStreetNameOrRelation(currentMouseNode, cmds);
                Command c = new SequenceCommand("Add node address", cmds);
                Main.main.undoRedo.add(c);
                setNewSelection(currentMouseNode);
            } else {
                if (num != null) {
                    try {
                        // add new address
                        Integer.parseInt(num); 
                        inputNumber.setText(num);
                        applyInputNumberChange();
                    } catch (NumberFormatException en) {
                        System.out.println("Unable to parse house number \"" + num + "\"");
                    }
                }
                if (currentMouseNode.get(tagHouseStreet) != null) {
                    inputStreet.setText(currentMouseNode.get(tagHouseStreet));
                    if (ctrl) {
                        Collection<Command> cmds = new LinkedList<Command>();
                        addAddrToPrimitive(currentMouseNode, cmds);
                        if (num == null)
                            applyInputNumberChange();
                    }
                    setSelectedWay((Way)null);
                } else {
                    // check if the node belongs to an associatedStreet relation
                    Way wayInRelationAddr = findWayInRelationAddr(currentMouseNode);
                    if (wayInRelationAddr == null) {
                        // node exists but doesn't carry address information : add tags like a new node
                        if (ctrl) {
                            applyInputNumberChange();
                        }
                        Collection<Command> cmds = new LinkedList<Command>();
                        addAddrToPrimitive(currentMouseNode, cmds);
                    } else {
                        inputStreet.setText(wayInRelationAddr.get(tagHighwayName));
                        setSelectedWay(wayInRelationAddr);
                    }
                }
            }
        } else {
            List<WaySegment> wss = mv.getNearestWaySegments(mousePos, OsmPrimitive.isSelectablePredicate);
            for(WaySegment ws : wss) {
                if (ws.way.get(tagHighway) != null && ws.way.get(tagHighwayName) != null)
                    mouseOnExistingWays.add(ws.way);
                else if (ws.way.get(tagBuilding) != null && ws.way.get(tagHouseNumber) == null)
                    mouseOnExistingBuildingWays.add(ws.way);
            }
            if (mouseOnExistingWays.size() == 1) {
                // clicked on existing highway => set new street name
                inputStreet.setText(mouseOnExistingWays.get(0).get(tagHighwayName));
                setSelectedWay(mouseOnExistingWays.get(0));
                inputNumber.setText("");
                setNewSelection(mouseOnExistingWays.get(0));
            } else if (mouseOnExistingWays.size() == 0) {
                // clicked a non highway and not a node => add the new address 
                if (inputStreet.getText().equals("") || inputNumber.getText().equals("")) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    Collection<Command> cmds = new LinkedList<Command>();
                    if (ctrl) {
                        applyInputNumberChange();
                    }
                    if (tagPolygon.isSelected()) {
                        addAddrToPolygon(mouseOnExistingBuildingWays, cmds);
                    } else {
                        Node n = createNewNode(e, cmds);
                        addAddrToPrimitive(n, cmds);
                    }
                }
            }
        }

    }
    
    private Way findWayInRelationAddr(Node n) {
        List<OsmPrimitive> l = n.getReferrers();
        for (OsmPrimitive osm : l) {
            if (osm instanceof Relation && osm.hasKey("type") && osm.get("type").equals(relationAddrType)) {
                for (RelationMember rm : ((Relation)osm).getMembers()) {
                    if (rm.getRole().equals(relationAddrStreetRole)) {
                        OsmPrimitive osp = rm.getMember();
                        if (osp instanceof Way && osp.hasKey(tagHighwayName)) {
                            return (Way)osp;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void addAddrToPolygon(List<Way> mouseOnExistingBuildingWays, Collection<Command> cmds) {
        for (Way w:mouseOnExistingBuildingWays) {
            addAddrToPrimitive(w, cmds);
        }
    }
    
    private void addAddrToPrimitive(OsmPrimitive osm, Collection<Command> cmds) {
        // add the current tag addr:housenumber in node and member in relation (if so configured)
        if (shift) {
            try {
                revertInputNumberChange();
            } catch (NumberFormatException en) {
                System.out.println("Unable to parse house number \"" + inputNumber.getText() + "\"");
            }

        }
        cmds.add(new ChangePropertyCommand(osm, tagHouseNumber, inputNumber.getText()));
        addStreetNameOrRelation(osm, cmds);
        try {
            applyInputNumberChange();
            Command c = new SequenceCommand("Add node address", cmds);
            Main.main.undoRedo.add(c);
            setNewSelection(osm);
        } catch (NumberFormatException en) {
            System.out.println("Unable to parse house number \"" + inputNumber.getText() + "\"");
        }
    }
    
    private Relation findRelationAddr(Way w) {
        List<OsmPrimitive> l = w.getReferrers();
        for (OsmPrimitive osm : l) {
            if (osm instanceof Relation && osm.hasKey("type") && osm.get("type").equals(relationAddrType)) {
                return (Relation)osm;
            }
        }
        return null;
    }
    
    private void addStreetNameOrRelation(OsmPrimitive osm, Collection<Command> cmds) {
        if (Main.pref.getBoolean("cadastrewms.addr.dontUseRelation", false)) {
            cmds.add(new ChangePropertyCommand(osm, tagHouseStreet, inputStreet.getText()));
        } else if (selectedWay != null) {
            Relation selectedRelation = findRelationAddr(selectedWay);
            // add the node to its relation
            if (selectedRelation != null) {
                RelationMember rm = new RelationMember(relationMemberHouse, osm);
                Relation newRel = new Relation(selectedRelation);
                newRel.addMember(rm);
                cmds.add(new ChangeCommand(selectedRelation, newRel));
            } else {
                // create new relation
                Relation newRel = new Relation();
                newRel.put("type", relationAddrType);
                newRel.put(relationAddrName, selectedWay.get(tagHighwayName));
                newRel.addMember(new RelationMember(relationAddrStreetRole, selectedWay));
                newRel.addMember(new RelationMember(relationMemberHouse, osm));
                cmds.add(new AddCommand(newRel));
            }
        }
    }

    private Node createNewNode(MouseEvent e, Collection<Command> cmds) {
        // DrawAction.mouseReleased() but without key modifiers
        Node n = new Node(Main.map.mapView.getLatLon(e.getX(), e.getY()));
        cmds.add(new AddCommand(n));
        List<WaySegment> wss = Main.map.mapView.getNearestWaySegments(e.getPoint(), OsmPrimitive.isSelectablePredicate);
        Map<Way, List<Integer>> insertPoints = new HashMap<Way, List<Integer>>();
        for (WaySegment ws : wss) {
            List<Integer> is;
            if (insertPoints.containsKey(ws.way)) {
                is = insertPoints.get(ws.way);
            } else {
                is = new ArrayList<Integer>();
                insertPoints.put(ws.way, is);
            }

            is.add(ws.lowerIndex);
        }
        Set<Pair<Node,Node>> segSet = new HashSet<Pair<Node,Node>>();
        ArrayList<Way> replacedWays = new ArrayList<Way>();
        ArrayList<Way> reuseWays = new ArrayList<Way>();
        for (Map.Entry<Way, List<Integer>> insertPoint : insertPoints.entrySet()) {
            Way w = insertPoint.getKey();
            List<Integer> is = insertPoint.getValue();
            Way wnew = new Way(w);
            pruneSuccsAndReverse(is);
            for (int i : is) {
                segSet.add(Pair.sort(new Pair<Node,Node>(w.getNode(i), w.getNode(i+1))));
            }
            for (int i : is) {
                wnew.addNode(i + 1, n);
            }
            cmds.add(new ChangeCommand(insertPoint.getKey(), wnew));
            replacedWays.add(insertPoint.getKey());
            reuseWays.add(wnew);
        }
        adjustNode(segSet, n);

        return n;
    }
    
    private static void adjustNode(Collection<Pair<Node,Node>> segs, Node n) {

        switch (segs.size()) {
        case 0:
            return;
        case 2:
            // This computes the intersection between
            // the two segments and adjusts the node position.
            Iterator<Pair<Node,Node>> i = segs.iterator();
            Pair<Node,Node> seg = i.next();
            EastNorth A = seg.a.getEastNorth();
            EastNorth B = seg.b.getEastNorth();
            seg = i.next();
            EastNorth C = seg.a.getEastNorth();
            EastNorth D = seg.b.getEastNorth();

            double u=det(B.east() - A.east(), B.north() - A.north(), C.east() - D.east(), C.north() - D.north());

            // Check for parallel segments and do nothing if they are
            // In practice this will probably only happen when a way has been duplicated

            if (u == 0) return;

            // q is a number between 0 and 1
            // It is the point in the segment where the intersection occurs
            // if the segment is scaled to lenght 1

            double q = det(B.north() - C.north(), B.east() - C.east(), D.north() - C.north(), D.east() - C.east()) / u;
            EastNorth intersection = new EastNorth(
                    B.east() + q * (A.east() - B.east()),
                    B.north() + q * (A.north() - B.north()));

            int snapToIntersectionThreshold
            = Main.pref.getInteger("edit.snap-intersection-threshold",10);

            // only adjust to intersection if within snapToIntersectionThreshold pixel of mouse click; otherwise
            // fall through to default action.
            // (for semi-parallel lines, intersection might be miles away!)
            if (Main.map.mapView.getPoint(n).distance(Main.map.mapView.getPoint(intersection)) < snapToIntersectionThreshold) {
                n.setEastNorth(intersection);
                return;
            }

        default:
            EastNorth P = n.getEastNorth();
            seg = segs.iterator().next();
            A = seg.a.getEastNorth();
            B = seg.b.getEastNorth();
            double a = P.distanceSq(B);
            double b = P.distanceSq(A);
            double c = A.distanceSq(B);
            q = (a - b + c) / (2*c);
            n.setEastNorth(new EastNorth(B.east() + q * (A.east() - B.east()), B.north() + q * (A.north() - B.north())));
        }
    }
    
    static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    private static void pruneSuccsAndReverse(List<Integer> is) {
        //if (is.size() < 2) return;

        HashSet<Integer> is2 = new HashSet<Integer>();
        for (int i : is) {
            if (!is2.contains(i - 1) && !is2.contains(i + 1)) {
                is2.add(i);
            }
        }
        is.clear();
        is.addAll(is2);
        Collections.sort(is);
        Collections.reverse(is);
    }

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", null);
        } catch (Exception e) {
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }
    
    private void applyInputNumberChange() {
        Integer num = Integer.parseInt(inputNumber.getText());
        if (plus_one.isSelected())
            num = num + 1;
        if (plus_two.isSelected())
            num = num + 2;
        if (minus_one.isSelected() && num > 1)
            num = num - 1;
        if (minus_two.isSelected() && num > 2)
            num = num - 2;
        inputNumber.setText(num.toString());
    }
    
    private void revertInputNumberChange() {
        Integer num = Integer.parseInt(inputNumber.getText());
        if (plus_one.isSelected())
            num = num - 1;
        if (plus_two.isSelected())
            num = num - 2;
        if (minus_one.isSelected() && num > 1)
            num = num + 1;
        if (minus_two.isSelected() && num > 2)
            num = num + 2;
        inputNumber.setText(num.toString());
    }
    
    private void createDialog() {
        ImageIcon iconLink = ImageProvider.get(null, "Mf_relation.png");
        link.setIcon(iconLink);
        link.setEnabled(false);
        JPanel p = new JPanel(new GridBagLayout());
        JLabel number = new JLabel(tr("Next no"));
        JLabel street = new JLabel(tr("Street"));
        p.add(number, GBC.std().insets(0, 0, 0, 0));
        p.add(inputNumber, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));
        p.add(street, GBC.std().insets(0, 0, 0, 0));
        JPanel p2 = new JPanel(new GridBagLayout());
        inputStreet.setEditable(false);
        p2.add(inputStreet, GBC.std().fill(GBC.HORIZONTAL).insets(5, 0, 0, 0));
        p2.add(link, GBC.eol().insets(10, 0, 0, 0));
        p.add(p2, GBC.eol().fill(GBC.HORIZONTAL));
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inputNumber.setText("");
                inputStreet.setText("");
                setSelectedWay((Way)null);
            }
        });
        ButtonGroup bgIncremental = new ButtonGroup();
        bgIncremental.add(plus_one);
        bgIncremental.add(plus_two);
        bgIncremental.add(minus_one);
        bgIncremental.add(minus_two);
        p.add(minus_one, GBC.std().insets(10, 0, 10, 0));
//        p.add(plus_one, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
        p.add(plus_one, GBC.std().insets(0, 0, 10, 0));
        tagPolygon.setSelected(Main.pref.getBoolean("cadastrewms.addr.onBuilding", false));
        tagPolygon.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                Main.pref.put("cadastrewms.addr.onBuilding", tagPolygon.isSelected());
            }
        });
        p.add(tagPolygon, GBC.eol().fill(GBC.HORIZONTAL).insets(0, 0, 0, 0));
        p.add(minus_two, GBC.std().insets(10, 0, 10, 0));
        p.add(plus_two, GBC.std().insets(0, 0, 10, 0));
        p.add(clearButton, GBC.eol().fill(GBC.HORIZONTAL).insets(0, 0, 0, 0));
    
        final Object[] options = {};
        final JOptionPane pane = new JOptionPane(p,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
                null, options, null);
        dialog = pane.createDialog(Main.parent, tr("Enter addresses"));
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);
        dialog.addComponentListener(new ComponentAdapter() {
            protected void rememberGeometry() {
                Main.pref.put("cadastrewms.addr.bounds", dialog.getX()+","+dialog.getY()+","+dialog.getWidth()+","+dialog.getHeight());
            }
            @Override public void componentMoved(ComponentEvent e) {
                rememberGeometry();
            }
            @Override public void componentResized(ComponentEvent e) {
                rememberGeometry();
            }
        });
        dialog.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                exitMode();
                Main.map.selectMapMode((MapMode)Main.map.getDefaultButtonAction());
            }
            public void windowClosed(WindowEvent e) {}
            public void windowActivated(WindowEvent arg0) {}
            public void windowDeactivated(WindowEvent arg0) {}
            public void windowDeiconified(WindowEvent arg0) {}
            public void windowIconified(WindowEvent arg0) {}
            public void windowOpened(WindowEvent arg0) {}
        });
        String bounds = Main.pref.get("cadastrewms.addr.bounds",null);
        if (bounds != null) {
            String[] b = bounds.split(",");
            dialog.setBounds(new Rectangle(
                    Integer.parseInt(b[0]),Integer.parseInt(b[1]),Integer.parseInt(b[2]),Integer.parseInt(b[3])));
        }
}
    
    private void setSelectedWay(Way w) {
        this.selectedWay = w;
        if (w == null) {
            link.setEnabled(false);
        } else
            link.setEnabled(true);
    }
    
    private void setNewSelection(OsmPrimitive osm) {
        Collection<OsmPrimitive> newSelection = new LinkedList<OsmPrimitive>(Main.main.getCurrentDataSet().getSelected());
        newSelection.clear();
        newSelection.add(osm);
        getCurrentDataSet().setSelected(osm);
    }

}
