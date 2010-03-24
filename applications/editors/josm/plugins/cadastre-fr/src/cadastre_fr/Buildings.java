package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Trace a way around buildings from cadastre images. 
 * Inspired by Lakewalker plugin.
 * @author Pieren
 */
public class Buildings extends MapMode implements MouseListener, MouseMotionListener {
    
    private static final long serialVersionUID = 1L;
    GeorefImage selectedImage;
    WMSLayer selectedLayer;
    private EastNorth clickedEastNorth;
    private class Pixel {
        public Point p;
        public int dir;
        public Pixel(int x, int y, int dir) {
            this.p = new Point(x,y);
            this.dir = dir;
        }
        @Override
        public int hashCode() {
            return p.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pixel)
                return p.equals(new Point(((Pixel)obj).p.x, ((Pixel)obj).p.y));
            return p.equals(obj);
        }
    }
    private ArrayList<Pixel> listPixels = new ArrayList<Pixel>();
    
    private static final int cMaxnode = 10000;
    private static final double cDistanceForOptimization = 0.7;
    private int[] dirsX = new int[] {1,1,0,-1,-1,-1,0,1};
    private int[] dirsY = new int[] {0,1,1,1,0,-1,-1,-1};

    private int orange = Color.ORANGE.getRGB(); // new color of pixels ending nowhere (cul-de-sac)
    BuildingsImageModifier bim = new BuildingsImageModifier();

    private double snapDistance = Main.pref.getDouble("cadastrewms.snap-distance", 50); // in centimeters
    private double snapDistanceSq = snapDistance*snapDistance;
    private double dx, dy;

    
    public Buildings(MapFrame mapFrame) {
        super(tr("Grab buildings"), "buildings",
                        tr("Extract building on click (vector images only)"),
                        Shortcut.registerShortcut("mapmode:buildings", tr("Mode: {0}", tr("Buildings")), KeyEvent.VK_E, Shortcut.GROUP_EDIT),
                        mapFrame, ImageProvider.getCursor("normal", "move"));
    }

    @Override public void enterMode() {
        super.enterMode();
        boolean atLeastOneBuildingLayer = false;
        for (Layer layer : Main.map.mapView.getAllLayers()) {
            if (layer instanceof WMSLayer && ((WMSLayer)layer).isBuildingsOnly()) {
                atLeastOneBuildingLayer = true;
                break;
            }
        }
        if (atLeastOneBuildingLayer && Main.main.getCurrentDataSet() != null) {
            Main.map.mapView.addMouseListener(this);
            Main.map.mapView.addMouseMotionListener(this);
        } else {
            JOptionPane.showMessageDialog(Main.parent,tr("This feature requires (at least) one special cadastre\nBuildings layer and an OSM data layer."));
            exitMode();
            Main.map.selectMapMode((MapMode)Main.map.getDefaultButtonAction());
        }
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        selectedImage = null;
        // ctrl = do not merge the new polygon with adjacent elements
        boolean ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        // shift = do not use the parcel as a separator
        boolean shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        // boolean alt = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
        for (Layer layer : Main.map.mapView.getAllLayers()) {
            if (layer.isVisible() && layer instanceof WMSLayer && ((WMSLayer)layer).isBuildingsOnly() ) {
                clickedEastNorth = Main.map.mapView.getEastNorth(e.getX(), e.getY());
                selectedLayer = ((WMSLayer) layer);
                selectedImage = selectedLayer.findImage(clickedEastNorth);
            }
        }
        if (selectedImage != null) {
            int x = (int)((clickedEastNorth.east() - selectedImage.min.east())*selectedImage.getPixelPerEast());
            int y = selectedImage.image.getHeight() - (int)((clickedEastNorth.north() - selectedImage.min.north())*selectedImage.getPixelPerNorth());
            int rgb = selectedImage.image.getRGB(x, y);
            System.out.println("image found"+", x="+x+", y="+y+", RGB="+rgb);
            boolean clickOnRoof = bim.isRoofColor(rgb, shift);
            boolean clickOnBuilding = bim.isBuildingColor(rgb, shift);
            if (clickOnRoof || clickOnBuilding) {
                if (traceBuilding(x, y, clickOnBuilding, shift) && listPixels.size() > 3) {
                    Way wayToCreate = new Way();
                    Way way2 = new Way();
                    double pPE = selectedImage.getPixelPerEast();
                    double pPN = selectedImage.getPixelPerNorth();
                    for (int i=0; i < listPixels.size(); i++) {
                        EastNorth en = new EastNorth(selectedImage.min.east() + ((listPixels.get(i).p.x + 0.5)/ pPE),
                                selectedImage.max.north() - ((listPixels.get(i).p.y + 0.5)/ pPN));
                        Node nodeToAdd = new Node(Main.proj.eastNorth2latlon(en));
                        wayToCreate.addNode(nodeToAdd);
                    }
                    wayToCreate.addNode(wayToCreate.getNode(0)); // close the way
                    new SimplifyWay().simplifyWay(wayToCreate, 0.2);
                    // move the node closing the loop and simplify again
                    for (int i = 1; i < wayToCreate.getNodesCount(); i++) {
                        way2.addNode(wayToCreate.getNode(i));
                    }
                    way2.addNode(way2.getNode(0));
                    new SimplifyWay().simplifyWay(way2, 0.2);
                    simplifyAngles(way2);
                    Way wayToAdd = new Way();
                    Collection<Command> cmds = new LinkedList<Command>();
                    if (ctrl) {
                        for (int i = 0; i < way2.getNodesCount()-1; i++) {
                            wayToAdd.addNode(way2.getNode(i));
                            cmds.add(new AddCommand(wayToAdd.getNode(i)));
                        }
                        wayToAdd.addNode(wayToAdd.getNode(0)); // close the polygon !
                    } else {
                        for (int i = 0; i < way2.getNodesCount()-1; i++) {
                            Node nearestNode = getNearestNode(way2.getNode(i));
                            if (nearestNode == null) {
                                // check if we can join new node to existing ways
                                List<WaySegment> wss = getNearestWaySegments(way2.getNode(i));
                                wayToAdd.addNode(way2.getNode(i));
                                cmds.add(new AddCommand(way2.getNode(i)));
                                if (wss.size() > 0) {
                                    cmds.add(new MoveCommand(way2.getNode(i), dx, dy));
                                    joinNodeToExistingWays(wss, way2.getNode(i), cmds);
                                }
                            } else {
                                // replace new node by an existing nearest node
                                wayToAdd.addNode(nearestNode);
                                cmds.add(new MoveCommand(nearestNode, dx, dy));
                            }
                        }
                        wayToAdd.addNode(wayToAdd.getNode(0)); // close the polygon !
                        for (int i = 1; i < wayToAdd.getNodesCount(); i++) {
                            Node nodeToJoin = existingNodesInNewSegment(wayToAdd.getNode(i-1), wayToAdd.getNode(i), wayToAdd);
                            // check if we join new way to existing nodes
                            if (nodeToJoin != null) {
                                List<WaySegment> wss = new LinkedList<WaySegment>();
                                wss.add(new WaySegment(wayToAdd, i-1));
                                wayToAdd = joinNodeToExistingWays(wss, nodeToJoin, cmds);
                                cmds.add(new MoveCommand(nodeToJoin, dx, dy));
                                i--; // re-assess the new segment (perhaps several nodes to join)
                            }
                        }
                    }
                    cmds.add(new AddCommand(wayToAdd));
                    if (clickOnBuilding)
                        addBuildingTags(cmds, wayToAdd);
                    if (clickOnRoof) {
                        addRoofTags(cmds, wayToAdd);
                    }
                    Main.main.undoRedo.add(new SequenceCommand(tr("Create building"), cmds));
                    getCurrentDataSet().setSelected(wayToAdd);
                    Main.map.repaint();
                }
            }
        }
    }
    
    @Override public void mouseDragged(MouseEvent e) {
    }

    @Override public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }

    @Override public void mouseClicked(MouseEvent e) {
    }
    
    private void addBuildingTags(Collection<Command> cmds, Way wayToAdd) {
        cmds.add(new ChangePropertyCommand(wayToAdd, "building", "yes"));
    }
    
    private void addRoofTags(Collection<Command> cmds, Way wayToAdd) {
        cmds.add(new ChangePropertyCommand(wayToAdd, "building", "yes"));
        cmds.add(new ChangePropertyCommand(wayToAdd, "wall", "no"));
    }
    
    private boolean traceBuilding (int x, int y, boolean buildingColors, boolean ignoreParcels) {
        // search start point at same x but smallest y (upper border on the screen)
        int startY = 0; int startX = x;
        while (y > 0) {
            y--;
            if (!bim.isBuildingOrRoofColor(selectedImage.image, x, y, buildingColors, ignoreParcels)) {
                System.out.println("at "+x+","+y+" color was "+selectedImage.image.getRGB(x,y));
                y++;
                startY = y;
                break;
            }
        }
        if (startY == 0) {
            System.out.println("border not found");
            return false;
        } else
            System.out.println("start at x="+startX+", y="+startY);
        listPixels.clear();
        int test_x = 0;
        int test_y = 0;
        int new_dir = 0;
        addPixeltoList(x, y, new_dir);
        int last_dir = 1;
        for(int i = 0; i < cMaxnode; i++){
            //System.out.println("node "+i);
            for(int d = 1; d <= this.dirsY.length; d++){
                new_dir = (last_dir + d + 4) % 8;
                test_x = x + this.dirsX[new_dir];
                test_y = y + this.dirsY[new_dir];
                if (test_x < 0 || test_x >= selectedImage.image.getWidth() ||
                        test_y < 0 || test_y >= selectedImage.image.getHeight()){
                    System.out.println("Outside image");
                    return false;
                }
                if (bim.isBuildingOrRoofColor(selectedImage.image, test_x, test_y, buildingColors, ignoreParcels)){
                    System.out.println("building color at "+test_x+","+test_y+" new_dir="+new_dir);
                    break;
                }

                if(d == this.dirsY.length-1){
                    System.out.println("Got stuck at "+x+","+y);
                    // cul-de-sac : disable current pixel and move two steps back
                    selectedImage.image.setRGB(x, y, orange);
                    if (removeTwoLastPixelsFromList()) {
                        x = listPixels.get(listPixels.size()-1).p.x;
                        y = listPixels.get(listPixels.size()-1).p.y;
                        last_dir = listPixels.get(listPixels.size()-1).dir; 
                        System.out.println("return at "+x+","+y+" and try again");
                        d = 1;
                        continue;
                    } else {
                        System.out.println("cannot try another way");
                        return false;
                    }
                }
            }
//            if (last_dir == new_dir)
//                // Same direction. First simplification by removing previous pixel.
//                listPixels.remove(listPixels.size()-1);
            last_dir = new_dir;
            // Set the pixel we found as current
            x = test_x;
            y = test_y;
            // Break the loop if we managed to get back to our starting point
            if (x == startX && y == startY) {
                System.out.println("loop closed at "+x+","+y+", exit");
                break;
            } else if (listPixels.contains(new Pixel(x, y, 0))){
                int j = listPixels.indexOf(new Pixel(x, y, 0));
                int l = listPixels.size();
                for (int k = j; k < l; k++)
                    listPixels.remove(j);
            }
            addPixeltoList(x, y, new_dir);
        }
        System.out.println("list size="+listPixels.size());
        return true;
    }
    
    private void addPixeltoList(int x, int y, int dir) {
        listPixels.add( new Pixel(x, y, dir));
        System.out.println("added pixel at "+x+","+y);
    }
    
    private boolean removeTwoLastPixelsFromList() {
        if (listPixels.size() > 2) {
            System.out.println("remove "+listPixels.get(listPixels.size()-1).p.x + ","+listPixels.get(listPixels.size()-1).p.y);
            listPixels.remove(listPixels.size()-1);
            System.out.println("remove "+listPixels.get(listPixels.size()-1).p.x + ","+listPixels.get(listPixels.size()-1).p.y);
            listPixels.remove(listPixels.size()-1);
            return true;
        }
        return false;
    }
    
    private BBox getSnapDistanceBBox(Node n) {
        return new BBox(Main.proj.eastNorth2latlon(new EastNorth(n.getEastNorth().east() - snapDistance, n.getEastNorth().north() - snapDistance)),
                Main.proj.eastNorth2latlon(new EastNorth(n.getEastNorth().east() + snapDistance, n.getEastNorth().north() + snapDistance)));
    }

    private Point getPointInCm(Node n) {
        return new Point(new Double(n.getEastNorth().getX()*100).intValue(),
                new Double(n.getEastNorth().getY()*100).intValue());
    }
    
    public Node getNearestNode(Node newNode) {
        Point newPoint = getPointInCm(newNode);
        DataSet ds = getCurrentDataSet();
        if (ds == null)
            return null;

        double minDistanceSq = snapDistanceSq;
        Node minNode = null;
        for (Node n : ds.searchNodes(getSnapDistanceBBox(newNode))) {
            if (!n.isUsable()) {
                continue;
            }
            Point sp = new Point(new Double(n.getEastNorth().getX()*100).intValue(),
                    new Double(n.getEastNorth().getY()*100).intValue());
            double dist = newPoint.distanceSq(sp); // in centimeter !
            if (dist < minDistanceSq) {
                minDistanceSq = dist;
                minNode = n;
            }
            // when multiple nodes on one point, prefer new or selected nodes
            else if (dist == minDistanceSq && minNode != null
                    && ((n.isNew() && ds.isSelected(n))
                            || (!ds.isSelected(minNode) && (ds.isSelected(n) || n.isNew())))) {
                minNode = n;
            }
        }
        if (minNode != null) {
            dx = (newNode.getEastNorth().getX() - minNode.getEastNorth().getX())/2;
            dy = (newNode.getEastNorth().getY() - minNode.getEastNorth().getY())/2;
        }
        return minNode;
    }

    private List<WaySegment> getNearestWaySegments(Node newNode) {
        Point newPoint = new Point(new Double(newNode.getEastNorth().getX()*100).intValue(),
                new Double(newNode.getEastNorth().getY()*100).intValue());
        TreeMap<Double, List<WaySegment>> nearest = new TreeMap<Double, List<WaySegment>>();
        DataSet ds = getCurrentDataSet();
        if (ds == null)
            return null;

        for (Way w : ds.searchWays(getSnapDistanceBBox(newNode))) {
            if (!w.isUsable()) {
                continue;
            }
            Node lastN = null;
            int i = -2;
            for (Node n : w.getNodes()) {
                i++;
                if (n.isDeleted() || n.isIncomplete()) {
                    continue;
                }
                if (lastN == null) {
                    lastN = n;
                    continue;
                }

                Point A = getPointInCm(lastN);
                Point B = getPointInCm(n);
                double c = A.distanceSq(B);
                double a = newPoint.distanceSq(B);
                double b = newPoint.distanceSq(A);
                double perDist = a - (a - b + c) * (a - b + c) / 4 / c;
                if (perDist < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq) {
                    if (ds.isSelected(w)) {
                        perDist -= 0.00001;
                    }
                    List<WaySegment> l;
                    if (nearest.containsKey(perDist)) {
                        l = nearest.get(perDist);
                    } else {
                        l = new LinkedList<WaySegment>();
                        nearest.put(perDist, l);
                    }
                    double ratio = A.distance(newPoint)/A.distance(B);
                    Point perP = new Point(A.x+new Double((B.x-A.x)*ratio).intValue(),
                            A.y+new Double((B.y-A.y)*ratio).intValue());
                    dx = (perP.x-newPoint.x)/200.0; // back to meters this time and whole distance by two 
                    dy = (perP.y-newPoint.y)/200.0;
//                    System.out.println(angle+","+ ratio+","+perP );
                    l.add(new WaySegment(w, i));
                }

                lastN = n;
            }
        }
        ArrayList<WaySegment> nearestList = new ArrayList<WaySegment>();
        for (List<WaySegment> wss : nearest.values()) {
            nearestList.addAll(wss);
        }
        return nearestList;
    }
    
    private Node existingNodesInNewSegment(Node n1, Node n2, Way way) {
        double minx = Math.min(n1.getEastNorth().getX(), n2.getEastNorth().getX())*100;
        double miny = Math.min(n1.getEastNorth().getY(), n2.getEastNorth().getY())*100;
        double maxx = Math.max(n1.getEastNorth().getX(), n2.getEastNorth().getX())*100;
        double maxy = Math.max(n1.getEastNorth().getY(), n2.getEastNorth().getY())*100;
//        if ((maxx-minx)/2 < snapDistance && (maxy-miny)/2 < snapDistance) {
//            return null;
//        }
        BBox bbox = new BBox( Main.proj.eastNorth2latlon(new EastNorth((minx-snapDistance)/100, (miny-snapDistance)/100)), 
                Main.proj.eastNorth2latlon(new EastNorth((maxx+snapDistance)/100, (maxy+snapDistance)/100)));
        DataSet ds = getCurrentDataSet();
        if (ds == null) {
            return null;
        }
        Node ret = null;
        List<Node> nodesInBbox = ds.searchNodes(bbox);
        for (Node n:nodesInBbox) {
            Point A = getPointInCm(n1);
            Point B = getPointInCm(n2);
            Point existingPoint = getPointInCm(n);
            double c = A.distanceSq(B);
            double a = existingPoint.distanceSq(B);
            double b = existingPoint.distanceSq(A);
            double perDist = a - (a - b + c) * (a - b + c) / 4 / c;
            if (perDist < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq
               && n.isUsable() && !way.getNodes().contains(n)) {
                ret = n;
                // shift the existing node to the half distance of the joined new segment
                double ratio = A.distance(existingPoint)/A.distance(B);
                Point perP = new Point(A.x+new Double((B.x-A.x)*ratio).intValue(),
                        A.y+new Double((B.y-A.y)*ratio).intValue());
                dx = (perP.x-existingPoint.x)/200.0; // back to meters this time and whole distance by two 
                dy = (perP.y-existingPoint.y)/200.0;
                break;
            }
        }
//        System.out.println("Found "+nodesInBbox.size()+", join node "+ret+" to new segment; "+Main.proj.latlon2eastNorth(bbox.getBottomRight())+","+Main.proj.latlon2eastNorth(bbox.getTopLeft()));
        return ret;
    }
    
    private Way joinNodeToExistingWays(List<WaySegment> wss, Node newNode, Collection<Command> cmds) {
        HashMap<Way, List<Integer>> insertPoints = new HashMap<Way, List<Integer>>();
        for (WaySegment ws : wss) {
            List<Integer> is;
            if (insertPoints.containsKey(ws.way)) {
                is = insertPoints.get(ws.way);
            } else {
                is = new ArrayList<Integer>();
                insertPoints.put(ws.way, is);
            }

            if (ws.way.getNode(ws.lowerIndex) != newNode && ws.way.getNode(ws.lowerIndex+1) != newNode) {
                is.add(ws.lowerIndex);
            }
        }
        
        Way wnew = null;
        for (Map.Entry<Way, List<Integer>> insertPoint : insertPoints.entrySet()) {
            List<Integer> is = insertPoint.getValue();
            if (is.size() == 0)
                continue;

            Way w = insertPoint.getKey();
            List<Node> nodesToAdd = w.getNodes();
            pruneSuccsAndReverse(is);
            for (int i : is) {
                nodesToAdd.add(i+1, newNode);
            }
            wnew = new Way(w);
            wnew.setNodes(nodesToAdd);
            cmds.add(new ChangeCommand(w, wnew));
        }
        return wnew;
    }
    
    private static void pruneSuccsAndReverse(List<Integer> is) {
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
    
    /*
     * The standard simplifier leaves sometimes closed nodes at buildings corners. 
     * We remove here the node not altering the building angle.
     */
    private void simplifyAngles(Way way){
        for (int i=1; i<way.getNodes().size(); i++){
            Node n1 = way.getNode(i-1);
            Node n2 = way.getNode(i);
            double dist = getPointInCm(n1).distance(getPointInCm(n2))/100;
//            System.out.println("dist="+dist+":"+(dist < cDistanceForOptimization));                
            if (dist < cDistanceForOptimization) {
                Node n0, n3;
                if (i > 1)
                    n0 = way.getNode(i-2);
                else
                    n0 = way.getNode(way.getNodes().size()-1);
                if (i < way.getNodes().size()-1)
                    n3 = way.getNode(i+1);
                else
                    n3 = way.getNode(0);
                double angle1 = AngleOfView(n1.getCoor().getX(), n1.getCoor().getY(),
                        n0.getCoor().getX(), n0.getCoor().getY(),
                        n2.getCoor().getX(), n2.getCoor().getY());
//                System.out.println("angle n0,n1,n2="+(angle1*180/Math.PI));
                double angle2 = AngleOfView(n2.getCoor().getX(), n2.getCoor().getY(),
                        n1.getCoor().getX(), n1.getCoor().getY(),
                        n3.getCoor().getX(), n3.getCoor().getY());
//                System.out.println("angle n1,n2,n3="+(angle2*180/Math.PI));
                if (angle1 > Math.PI*0.9 && angle1 < Math.PI*1.1) {
                    way.removeNode(n1);
                    System.out.println("remove n1");                
                } else if  (angle2 > Math.PI*0.9 && angle2 < Math.PI*1.1) {
                    way.removeNode(n2);
                    System.out.println("remove n2");                
                } else
                    System.out.println("no angle near PI");
            }                
        }        
    }

    private double AngleOfView ( double ViewPt_X, double ViewPt_Y,
            double Pt1_X, double Pt1_Y,
            double Pt2_X, double Pt2_Y ) {
        double a1, b1, a2, b2, a, b, t, cosinus ;
        a1 = Pt1_X - ViewPt_X ;
        a2 = Pt1_Y - ViewPt_Y ;
        b1 = Pt2_X - ViewPt_X ;
        b2 = Pt2_Y - ViewPt_Y ;
        a = Math.sqrt( (a1*a1) + (a2*a2) );
        b = Math.sqrt ( (b1*b1) + (b2*b2) );
        if ( (a == 0.0) || (b == 0.0) )
            return (0.0) ;
        cosinus = (a1*b1+a2*b2) / (a*b) ;
        t = Math.acos ( cosinus );
        //t = t * 180.0 / Math.PI ;
        return (t);
    }
    
    /* 
     * coming from SimplifyWayAction
     */
//    private boolean isRequiredNode(Way way, Node node) {
//        boolean isRequired =  Collections.frequency(way.getNodes(), node) > 1;
//        if (! isRequired) {
//            List<OsmPrimitive> parents = new LinkedList<OsmPrimitive>();
//            parents.addAll(node.getReferrers());
//            parents.remove(way);
//            isRequired = !parents.isEmpty();
//        }
//        if (!isRequired) {
//            isRequired = node.isTagged();
//        }
//        return isRequired;
//    }
}
