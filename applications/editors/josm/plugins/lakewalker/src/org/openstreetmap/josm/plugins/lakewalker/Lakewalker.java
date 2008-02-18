package org.openstreetmap.josm.plugins.lakewalker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class Lakewalker {
	protected Collection<Command> commands = new LinkedList<Command>();
	protected Collection<Way> ways = new ArrayList<Way>();
	protected boolean cancel;
	  
	private int waylen;
	private int maxnode;
	private int threshold;
	private double epsilon;
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
		this.waylen = waylen;
		this.maxnode = maxnode;
		this.threshold = threshold;
		this.epsilon = epsilon;
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
			throw new ArrayIndexOutOfBoundsException("Direction index '"+direction+"' not found");
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
	public ArrayList<double[]> trace(double lat, double lon, double tl_lon, double br_lon, double tl_lat, double br_lat) throws LakewalkerException {
				
		LakewalkerWMS wms = new LakewalkerWMS(this.resolution, this.tilesize, this.wmslayer, this.workingdir);
		LakewalkerBBox bbox = new LakewalkerBBox(tl_lat,tl_lon,br_lat,br_lon);
		
		Boolean detect_loop = false;
		
		ArrayList<double[]> nodelist = new ArrayList<double[]>();

		int[] xy = geo_to_xy(lat,lon,this.resolution); 
		
		if(!bbox.contains(lat, lon)){
			throw new LakewalkerException("The starting location was not within the bbox");
		}
		
		int v;
		
		setStatus("Looking for shoreline...");
		
		while(true){
			double[] geo = xy_to_geo(xy[0],xy[1],this.resolution);
			if(bbox.contains(geo[0],geo[1])==false){
				break;
			}
			
			v = wms.getPixel(xy[0], xy[1],0xFF00FF00);
			if(v < 0){
				return null;
			}
			
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

		System.out.printf("Found shore at lat %.4f lon %.4f\n",lat,lon);
		
		int last_dir = this.getDirectionIndex(this.startdir);
		
		for(int i = 0; i < this.maxnode; i++){
			
			// Print a counter
			if(i % 250 == 0){
				setStatus(i+" nodes so far...");
				System.out.println(i+" nodes so far...");
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
					/**
					 * TODO: Handle this case
					 */
					System.out.println("Outside bbox");
					break;
				}
				
				v = wms.getPixel(test_x, test_y,0xFF0000FF);
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
			System.out.println("Adding node at "+xy[0]+","+xy[1]+" ("+geo[1]+","+geo[0]+")");
			
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
			
		// DEBUG
		File f = new File(this.workingdir,"temp.png");
		wms.saveimage(f,wms.image2);	
		
		return nodelist;
	}
	
	public ArrayList<double[]> vertex_reduce(ArrayList<double[]> nodes, double proximity){
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
	
	public double point_line_distance(double[] p1, double[] p2, double[] p3){
		
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
	
	public ArrayList<double[]> douglas_peucker(ArrayList<double[]> nodes, double epsilon){
		int farthest_node = -1;
		double farthest_dist = 0;
		double[] first = nodes.get(0);
		double[] last = nodes.get(nodes.size()-1);
		
		ArrayList<double[]> new_nodes = new ArrayList<double[]>();
		
		double d = 0;
		
		for(int i = 1; i < nodes.size(); i++){
			d = point_line_distance(nodes.get(i),first,last);
			if(d>farthest_dist){
				farthest_dist = d;
				farthest_node = i;
			}
		}
		
		ArrayList<double[]> seg_a = new ArrayList<double[]>();
		ArrayList<double[]> seg_b = new ArrayList<double[]>();
		
		if(farthest_dist > epsilon){
			seg_a = douglas_peucker(sublist(nodes,0,farthest_node+1),epsilon);
			seg_b = douglas_peucker(sublist(nodes,farthest_node,nodes.size()-1),epsilon);
				
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
		
	protected void setStatus(String s) {
	  Main.pleaseWaitDlg.currentAction.setText(s);
	  Main.pleaseWaitDlg.repaint();
	}
	
	/**
	 * Class to do checking of whether the point is within our bbox
	 * 
	 * @author Jason Reid
	 */
	private class LakewalkerBBox {
		
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
		  if(lat > this.top || lat < this.bottom){
		    return false;
		  }
		  if((this.right - this.left) % 360 == 0){
		    return true;
		  }
		  return (lon - this.left) % 360 <= (this.right - this.left) % 360;
		}
	}
	private void printarr(int[] a){
		for(int i = 0; i<a.length; i++){
			System.out.println(i+": "+a[i]);
		}
	}
}

