package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.gui.progress.ProgressMonitor;

public class Lakewalker {
    protected boolean cancel;

    private int maxnode;
    private int threshold;
    private int resolution;
    private int tilesize;
    private String startdir;
    private String wmslayer;

    private File workingdir;

    private int[] dirslat = new int[] {0,1,1,1,0,-1,-1,-1};
    private int[] dirslon = new int[] {1,1,0,-1,-1,-1,0,1};

    double start_radius_big = 0.001;
    double start_radius_small = 0.0002;

    public Lakewalker(int waylen, int maxnode, int threshold, double epsilon, int resolution, int tilesize, String startdir, String wmslayer, File workingdir){
        this.maxnode = maxnode;
        this.threshold = threshold;
        this.resolution = resolution;
        this.tilesize = tilesize;
        this.startdir = startdir;
        this.wmslayer = wmslayer;

        this.workingdir = workingdir;
    }

    /**
     *  east = 0
     *  northeast = 1
     *  north = 2
     *  northwest = 3
     *  west = 4
     *  southwest = 5
     *  south = 6
     *  southeast = 7
     */
    private int getDirectionIndex(String direction) throws ArrayIndexOutOfBoundsException{
        int i=0;
        if(direction.equals("East") || direction.equals("east")){
            i = 0;
        } else if(direction.equals("Northeast") || direction.equals("northeast")){
            i =  1;
        } else if(direction.equals("North") || direction.equals("north")){
            i =  2;
        } else if(direction.equals("Northwest") || direction.equals("northwest")){
            i =  3;
        } else if(direction.equals("West") || direction.equals("west")){
            i =  4;
        } else if(direction.equals("Southwest") || direction.equals("southwest")){
            i =  5;
        } else if(direction.equals("South") || direction.equals("south")){
            i =  6;
        } else if(direction.equals("Southeast") || direction.equals("southeast")){
            i =  7;
        } else {
            throw new ArrayIndexOutOfBoundsException(tr("Direction index ''{0}'' not found",direction));
        }
        return i;
    }

    /**
     * Do a trace
     *
     * @param lat
     * @param lon
     * @param tl_lon
     * @param br_lon
     * @param tl_lat
     * @param br_lat
     */
    public ArrayList<double[]> trace(double lat, double lon, double tl_lon, double br_lon, double tl_lat, double br_lat, ProgressMonitor progressMonitor) throws LakewalkerException {

        progressMonitor.beginTask(null);

        try {

            LakewalkerWMS wms = new LakewalkerWMS(this.resolution, this.tilesize, this.wmslayer, this.workingdir);
            LakewalkerBBox bbox = new LakewalkerBBox(tl_lat,tl_lon,br_lat,br_lon);

            Boolean detect_loop = false;

            ArrayList<double[]> nodelist = new ArrayList<double[]>();

            int[] xy = geo_to_xy(lat,lon,this.resolution);

            if(!bbox.contains(lat, lon)){
                throw new LakewalkerException(tr("The starting location was not within the bbox"));
            }

            int v;

            progressMonitor.indeterminateSubTask(tr("Looking for shoreline..."));

            while(true){
                double[] geo = xy_to_geo(xy[0],xy[1],this.resolution);
                if(bbox.contains(geo[0],geo[1])==false){
                    break;
                }

                v = wms.getPixel(xy[0], xy[1], progressMonitor.createSubTaskMonitor(0, false));
                if(v > this.threshold){
                    break;
                }

                int delta_lat = this.dirslat[getDirectionIndex(this.startdir)];
                int delta_lon = this.dirslon[getDirectionIndex(this.startdir)];

                xy[0] = xy[0]+delta_lon;
                xy[1] = xy[1]+delta_lat;

            }

            int[] startxy = new int[] {xy[0], xy[1]};
            double[] startgeo = xy_to_geo(xy[0],xy[1],this.resolution);

            //System.out.printf("Found shore at lat %.4f lon %.4f\n",lat,lon);

            int last_dir = this.getDirectionIndex(this.startdir);

            for(int i = 0; i < this.maxnode; i++){

                // Print a counter
                if(i % 250 == 0){
                    progressMonitor.indeterminateSubTask(tr("{0} nodes so far...",i));
                    //System.out.println(i+" nodes so far...");
                }

                // Some variables we need
                int d;
                int test_x=0;
                int test_y=0;
                int new_dir = 0;

                // Loop through all the directions we can go
                for(d = 1; d <= this.dirslat.length; d++){

                    // Decide which direction we want to look at from this pixel
                    new_dir = (last_dir + d + 4) % 8;

                    test_x = xy[0] + this.dirslon[new_dir];
                    test_y = xy[1] + this.dirslat[new_dir];

                    double[] geo = xy_to_geo(test_x,test_y,this.resolution);

                    if(!bbox.contains(geo[0], geo[1])){
                        System.out.println("Outside bbox");
                        break;
                    }

                    v = wms.getPixel(test_x, test_y, progressMonitor.createSubTaskMonitor(0, false));
                    if(v > this.threshold){
                        break;
                    }

                    if(d == this.dirslat.length-1){
                        System.out.println("Got stuck");
                        break;
                    }
                }

                // Remember this direction
                last_dir = new_dir;

                // Set the pixel we found as current
                xy[0] = test_x;
                xy[1] = test_y;

                // Break the loop if we managed to get back to our starting point
                if(xy[0] == startxy[0] && xy[1] == startxy[1]){
                    break;
                }

                // Store this node
                double[] geo = xy_to_geo(xy[0],xy[1],this.resolution);
                nodelist.add(geo);
                //System.out.println("Adding node at "+xy[0]+","+xy[1]+" ("+geo[1]+","+geo[0]+")");

                // Check if we got stuck in a loop
                double start_proximity = Math.pow((geo[0] - startgeo[0]),2) + Math.pow((geo[1] - startgeo[1]),2);

                if(detect_loop){
                    if(start_proximity < Math.pow(start_radius_small,2)){
                        System.out.println("Detected loop");
                        break;
                    }
                }else{
                    if(start_proximity > Math.pow(start_radius_big,2)){
                        detect_loop = true;
                    }
                }
            }

            return nodelist;
        } finally {
            progressMonitor.finishTask();
        }
    }

    /**
     * Remove duplicate nodes from the list
     *
     * @param nodes
     * @return
     */
    public ArrayList<double[]> duplicateNodeRemove(ArrayList<double[]> nodes){

        if(nodes.size() <= 1){
            return nodes;
        }

        double lastnode[] = new double[] {nodes.get(0)[0], nodes.get(0)[1]};

        for(int i = 1; i < nodes.size(); i++){
            double[] thisnode = new double[] {nodes.get(i)[0], nodes.get(i)[1]};

            if(thisnode[0] == lastnode[0] && thisnode[1] == lastnode[1]){
                // Remove the node
                nodes.remove(i);

                // Shift back one index
                i = i - 1;
            }
            lastnode = thisnode;
        }

        return nodes;
    }

    /**
     * Reduce the number of vertices based on their proximity to each other
     *
     * @param nodes
     * @param proximity
     * @return
     */
    public ArrayList<double[]> vertexReduce(ArrayList<double[]> nodes, double proximity){

        // Check if node list is empty
        if(nodes.size()<=1){
            return nodes;
        }

        double[] test_v = nodes.get(0);
        ArrayList<double[]> reducednodes = new ArrayList<double[]>();

        double prox_sq = Math.pow(proximity, 2);

        for(int v = 0; v < nodes.size(); v++){
            if(Math.pow(nodes.get(v)[0] - test_v[0],2) + Math.pow(nodes.get(v)[1] - test_v[1],2) > prox_sq){
                reducednodes.add(nodes.get(v));
                test_v = nodes.get(v);
            }
        }

        return reducednodes;
    }

    public double pointLineDistance(double[] p1, double[] p2, double[] p3){

        double x0 = p1[0];
        double y0 = p1[1];
        double x1 = p2[0];
        double y1 = p2[1];
        double x2 = p3[0];
        double y2 = p3[1];

        if(x2 == x1 && y2 == y1){
            return Math.sqrt(Math.pow(x1-x0,2) + Math.pow(y1-y0,2));
        } else {
            return Math.abs((x2-x1)*(y1-y0) - (x1-x0)*(y2-y1)) / Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
        }
    }

        /*
    public ArrayList<double[]> douglasPeuckerNR(ArrayList<double[]> nodes, double epsilon){
        command_stack = [(0, len(nodes) - 1)]

        Vector result_stack = new Vector();

        while(command_stack.size() > 0){
            cmd = command_stack.pop();
            if(type(cmd) == tuple){
                (start, end) = cmd
                (node, dist) = dp_findpoint(nodes, start, end)
                if(dist > epsilon){
                    command_stack.append("+")
                    command_stack.append((start, node))
                    command_stack.append((node, end))
                } else {
                    result_stack.append((start, end))
                }
            } elseif(cmd == "+"){
                first = result_stack.pop()
                second = result_stack.pop()
                if(first[-1] == second[0]){
                    result_stack.append(first + second[1:])
                    //print "Added %s and %s; result is %s" % (first, second, result_stack[-1])
                }else {
                    error("ERROR: Cannot connect nodestrings!")
                    #print first
                    #print second
                    return;
                }
            } else {
                error("ERROR: Can't understand command \"%s\"" % (cmd,))
                return

        if(len(result_stack) == 1){
            return [nodes[x] for x in result_stack[0]];
        } else {
            error("ERROR: Command stack is empty but result stack has %d nodes!" % len(result_stack));
            return;
        }

        farthest_node = None
        farthest_dist = 0
        first = nodes[0]
        last = nodes[-1]

        for(i in xrange(1, len(nodes) - 1){
            d = point_line_distance(nodes[i], first, last)
            if(d > farthest_dist){
                farthest_dist = d
                farthest_node = i
            }
        }
        if(farthest_dist > epsilon){
            seg_a = douglas_peucker(nodes[0:farthest_node+1], epsilon)
            seg_b = douglas_peucker(nodes[farthest_node:-1], epsilon)
            //print "Minimized %d nodes to %d + %d nodes" % (len(nodes), len(seg_a), len(seg_b))
            nodes = seg_a[:-1] + seg_b
        } else {
            return [nodes[0], nodes[-1]];
        }
        return nodes;
    }
        */

    public ArrayList<double[]> douglasPeucker(ArrayList<double[]> nodes, double epsilon, int depth){

        // Check if node list is empty
        if(nodes.size()<=1 || depth > 500){
            return nodes;
        }

        int farthest_node = -1;
        double farthest_dist = 0;
        double[] first = nodes.get(0);
        double[] last = nodes.get(nodes.size()-1);

        ArrayList<double[]> new_nodes = new ArrayList<double[]>();

        double d = 0;

        for(int i = 1; i < nodes.size(); i++){
            d = pointLineDistance(nodes.get(i),first,last);
            if(d>farthest_dist){
                farthest_dist = d;
                farthest_node = i;
            }
        }

        List<double[]> seg_a;
        List<double[]> seg_b;

        if(farthest_dist > epsilon){
            seg_a = douglasPeucker(sublist(nodes,0,farthest_node+1),epsilon, depth+1);
            seg_b = douglasPeucker(sublist(nodes,farthest_node,nodes.size()-1),epsilon,depth+1);

            new_nodes.addAll(seg_a);
            new_nodes.addAll(seg_b);
        } else {
            new_nodes.add(nodes.get(0));
            new_nodes.add(nodes.get(nodes.size()-1));
        }
        return new_nodes;
    }

    private ArrayList<double[]> sublist(ArrayList<double[]> l, int i, int f) throws ArrayIndexOutOfBoundsException {
        ArrayList<double[]> sub = new ArrayList<double[]>();

        if(f<i || i < 0 || f < 0 || f > l.size()){
            throw new ArrayIndexOutOfBoundsException();
        }

        for(int j = i; j < f; j++){
            sub.add(l.get(j));
        }
        return sub;
    }

    public double[] xy_to_geo(int x, int y, double resolution){
        double[] geo = new double[2];
        geo[0] = y / resolution;
        geo[1] = x / resolution;
        return geo;
    }

    public int[] geo_to_xy(double lat, double lon, double resolution){
        int[] xy = new int[2];

        xy[0] = (int)Math.floor(lon * resolution + 0.5);
        xy[1] = (int)Math.floor(lat * resolution + 0.5);

        return xy;
    }

    /*
     * User has hit the cancel button
     */
    public void cancel() {
      cancel = true;
    }

    /**
     * Class to do checking of whether the point is within our bbox
     *
     * @author Jason Reid
     */
    private static class LakewalkerBBox {

        private double top = 90;
        private double left = -180;
        private double bottom = -90;
        private double right = 180;

        protected LakewalkerBBox(double top, double left, double bottom, double right){
          this.left = left;
          this.right = right;
          this.top = top;
          this.bottom = bottom;
        }

        protected Boolean contains(double lat, double lon){
          if(lat >= this.top || lat <= this.bottom){
            return false;
          }
          if(lon >= this.right || lon <= this.left){
              return false;
          }
          if((this.right - this.left) % 360 == 0){
            return true;
          }
          return (lon - this.left) % 360 <= (this.right - this.left) % 360;
        }
    }
}

