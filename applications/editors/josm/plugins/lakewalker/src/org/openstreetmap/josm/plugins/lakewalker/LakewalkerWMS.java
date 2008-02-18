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

public class LakewalkerWMS {
	
	public LakewalkerWMS(){
		
	}
	
	private BufferedImage image;
	private int imagex;
	private int imagey;
	
	public BufferedImage image2 = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
	
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
		String layer = "global_mosaic_base";
		
		int[] bottom_left_xy = new int[2]; 
		bottom_left_xy[0] = (int)(x / this.tilesize) * this.tilesize;
		bottom_left_xy[1] = (int)(y / this.tilesize) * this.tilesize;
		
        int[] top_right_xy = new int[2]; 
        top_right_xy[0] = (int)bottom_left_xy[0] + this.tilesize;
        top_right_xy[1] = (int)bottom_left_xy[1] + this.tilesize;
        
        double[] topright_geo = xy_to_geo(top_right_xy[0],top_right_xy[1]);
        double[] bottomleft_geo = xy_to_geo(bottom_left_xy[0],bottom_left_xy[1]);
          
		String filename = this.wmslayer+"/landsat_"+this.resolution+"_"+this.tilesize+
			"_xy_"+bottom_left_xy[0]+"_"+bottom_left_xy[1]+".png";
		
		String urlloc = String.format("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers="+layer+
			"&styles="+wmslayer+"&srs=EPSG:4326&format=image/png"+
			"&bbox=%.6f,%.6f,%.6f,%.6f&width=%d&height=%d",
			bottomleft_geo[1],bottomleft_geo[0],topright_geo[1],topright_geo[0],
			this.tilesize,this.tilesize);			
		
        File file = new File(this.working_dir,filename);
        
        // See if this image is already loaded
        if(this.image != null){
        	if(this.imagex != bottom_left_xy[0] || this.imagey != bottom_left_xy[1]){
        		this.image = null;
        	} else {
        		return this.image;
        	}
        }
        
	    try {	    	
	    	System.out.println("Looking for image in cache: "+filename);
	    	
	        // Read from a file
	        this.image = ImageIO.read(file);
	    
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
	        
	        this.saveimage(file,this.image);
	    }
	    
	    this.imagex = bottom_left_xy[0];
	    this.imagey = bottom_left_xy[1];
	    
	    if(this.image == null){
	    	throw new LakewalkerException("Could not acquire image");
	    }
		
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
	
	
	
	public int getPixel(int x, int y, int pixcol){
		
		BufferedImage image = null;

		try {
			image = this.getTile(x,y);
		} catch(LakewalkerException e){
			System.out.println(e.getError());
			return -1;
		}

		int tx = (int)(Math.floor(x / this.tilesize) * this.tilesize);
		int ty = (int)(Math.floor(y / this.tilesize) * this.tilesize);
		
		int pixel_x = (x-tx);
		int pixel_y = (this.tilesize-1)-(y-ty);
				
		int rgb = image.getRGB(pixel_x,pixel_y);
		
		// DEBUG: set the pixels
		this.image2.setRGB(pixel_x,pixel_y,pixcol);
		
		int pixel;
		
		int r = (rgb >> 16) & 0xff;
        int g = (rgb >>  8) & 0xff;
        int b = (rgb >>  0) & 0xff;

        //pixel = rgbToGrey(pixel); //(r+g+b)/3; //pixel & 0xff;
        
        int pixel2 = (int)((0.212671 * r) + (0.715160 * b) + (0.072169 * b));
        
        pixel = (int)((0.30 * r) + (0.59 * b) + (0.11 * g));
                
		//System.out.println(pixel_y+","+pixel_x+"  "+r+","+g+","+b+"="+pixel+"("+pixel2+")");

		return pixel; 
	}
	
	private int rgbToGrey(int color) {
		Color c = new Color(color);
	    int red = c.getRed();
	    int green = c.getGreen();
	    int blue = c.getBlue();
	    int tot = (red + green + blue) / 3;
	    return tot;
	}

	private void printarr(int[] a){
		for(int i = 0; i<a.length; i++){
			System.out.println(i+": "+a[i]);
		}
	}
	private double[] xy_to_geo(int x, int y){
		double[] geo = new double[2];
	    geo[0] = (double)y / (double)this.resolution;
	    geo[1] = (double)x / (double)this.resolution;
	    return geo;
	}
	
	private int[] geo_to_xy(double lat, double lon){
		int[] xy = new int[2];
		
		xy[0] = (int)(Math.floor(lon * this.resolution) + 0.5);
		xy[1] = (int)(Math.floor(lat * this.resolution) + 0.5);
		
		return xy;
	}
}
