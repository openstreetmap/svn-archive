package org.openstreetmap.josm.plugins.lakewalker;

import org.openstreetmap.josm.Main;
import java.awt.Image;
import javax.imageio.*;
import java.math.*;
import java.io.*;
import java.net.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;

public class LakewalkerWMS {
	
	private BufferedImage image;
	private int imagex;
	private int imagey;
	
	// Vector to cache images in memory
	private Vector<BufferedImage> images = new Vector<BufferedImage>();
	// Hashmap to hold the mapping of cached images 
	private HashMap<String,Integer> imageindex = new HashMap<String,Integer>();
	
	private int resolution;
	private int tilesize;
	
	private String wmslayer;
	
	private File working_dir;
	
	public LakewalkerWMS(int resolution, int tilesize, String wmslayer, File workdir){
		this.resolution = resolution;
		this.tilesize = tilesize;
		this.working_dir = workdir;
		this.wmslayer = wmslayer;
	}
	
	public BufferedImage getTile(int x, int y) throws LakewalkerException {
		String status = getStatus();
		setStatus("Downloading image tile...");
		
		String layer = "global_mosaic_base";
		
		int[] bottom_left_xy = new int[2]; 
		bottom_left_xy[0] = floor(x,this.tilesize);
		bottom_left_xy[1] = floor(y,this.tilesize);
		
        int[] top_right_xy = new int[2]; 
        top_right_xy[0] = (int)bottom_left_xy[0] + this.tilesize;
        top_right_xy[1] = (int)bottom_left_xy[1] + this.tilesize;
        
        double[] topright_geo = xy_to_geo(top_right_xy[0],top_right_xy[1],this.resolution);
        double[] bottomleft_geo = xy_to_geo(bottom_left_xy[0],bottom_left_xy[1],this.resolution);
                  
		String filename = this.wmslayer+"/landsat_"+this.resolution+"_"+this.tilesize+
			"_xy_"+bottom_left_xy[0]+"_"+bottom_left_xy[1]+".png";
		
		// The WMS server only understands decimal points using periods, so we need
		// to convert to a locale that uses that to build the proper URL
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat)nf;
		df.applyLocalizedPattern("0.000000");
		
		String urlloc = "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers="+layer+
			"&styles="+wmslayer+"&srs=EPSG:4326&format=image/png"+
			"&bbox="+df.format(bottomleft_geo[1])+","+df.format(bottomleft_geo[0])+
			","+df.format(topright_geo[1])+","+df.format(topright_geo[0])+
			"&width="+this.tilesize+"&height="+this.tilesize;
				
        File file = new File(this.working_dir,filename);
        
        // Calculate the hashmap key
    	String hashkey = Integer.toString(bottom_left_xy[0])+":"+Integer.toString(bottom_left_xy[1]);
    	
        // See if this image is already loaded
        if(this.image != null){  
        	if(this.imagex != bottom_left_xy[0] || this.imagey != bottom_left_xy[1]){
        		
        		// Check if this image exists in the hashmap
        		if(this.imageindex.containsKey(hashkey)){
        			// Store which image we have
        			this.imagex = bottom_left_xy[0];
        			this.imagey = bottom_left_xy[1];
        			
        			// Retrieve from cache
        			this.image = this.images.get(this.imageindex.get(hashkey));
        			return this.image;
        		} else {
        			this.image = null;
        		}
        	} else {
        		return this.image;
        	}
        }
        
	    try {	    	
	    	System.out.println("Looking for image in disk cache: "+filename);
	    	
	        // Read from a file
	        this.image = ImageIO.read(file);
	    
	        this.images.add(this.image);
	        this.imageindex.put(hashkey,this.images.size()-1);
	        
	    } catch(FileNotFoundException e){
	    	System.out.println("Could not find cached image, downloading.");
	    } catch(IOException e){
	    	System.out.println(e.getMessage());
	    } catch(Exception e){
	    	System.out.println(e.getMessage());
	    }
	    
	    if(this.image == null){
	    	/**
	    	 * Try downloading the image
	    	 */
		    try {	        	
	        	System.out.println("Downloading from "+urlloc);
	        	
	        	// Read from a URL
	        	URL url = new URL(urlloc);
	        	this.image = ImageIO.read(url);
	        } catch(MalformedURLException e){
	        	System.out.println(e.getMessage());
	        } catch(IOException e){
	        	System.out.println(e.getMessage());
	        } catch(Exception e){
	        	System.out.println(e.getMessage());
		    }
	        
	        this.images.add(this.image);
	        this.imageindex.put(hashkey,this.images.size()-1);
	        
	        this.saveimage(file,this.image);
	    }
	    
	    this.imagex = bottom_left_xy[0];
	    this.imagey = bottom_left_xy[1];
	    
	    if(this.image == null){
	    	throw new LakewalkerException("Could not acquire image");
	    }
		
	    setStatus(status);
	    
		return this.image;
	}
	
	public void saveimage(File file, BufferedImage image){
        /**
         * Save the image to the cache
         */
        try {
        	ImageIO.write(image, "png", file);
        	System.out.println("Saved image to cache");
        } catch(Exception e){
        	System.out.println(e.getMessage());
        }
	}
	
	public int getPixel(int x, int y) throws LakewalkerException{

		// Get the previously shown text
		
		
		BufferedImage image = null;

		try {
			image = this.getTile(x,y);
		} catch(LakewalkerException e){
			System.out.println(e.getError());
			throw new LakewalkerException(e.getMessage());			
		}
	
		int tx = floor(x,this.tilesize);
		int ty = floor(y,this.tilesize);
				
		int pixel_x = (x-tx);
		int pixel_y = (this.tilesize-1)-(y-ty);
					
		//System.out.println("("+x+","+y+") maps to ("+pixel_x+","+pixel_y+") by ("+tx+", "+ty+")");
		
		int rgb = image.getRGB(pixel_x,pixel_y);
		
		int pixel;
		
		int r = (rgb >> 16) & 0xff;
        int g = (rgb >>  8) & 0xff;
        int b = (rgb >>  0) & 0xff;

        pixel = (int)((0.30 * r) + (0.59 * b) + (0.11 * g));
                
		return pixel; 
	}
	
	public int floor(int num, int precision){
		double dnum = num/(double)precision;
		BigDecimal val = new BigDecimal(dnum) ;
		val = val.setScale(0, BigDecimal.ROUND_FLOOR);
		return val.intValue()*precision;
	}
	
	public double floor(double num) {
		BigDecimal val = new BigDecimal(num) ;
		val = val.setScale(0, BigDecimal.ROUND_FLOOR);
		return val.doubleValue() ;
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
	
	private void printarr(int[] a){
		for(int i = 0; i<a.length; i++){
			System.out.println(i+": "+a[i]);
		}
	}
	protected void setStatus(String s) {
		Main.pleaseWaitDlg.currentAction.setText(s);
		Main.pleaseWaitDlg.repaint();
	}
	protected String getStatus(){
		return Main.pleaseWaitDlg.currentAction.getText();
	}
}
