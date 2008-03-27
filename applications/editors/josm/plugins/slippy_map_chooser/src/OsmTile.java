// License: GPL. Copyright 2007 by Tim Haussmann

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;

/**
 * @author Tim Haussmann 
 */


public class OsmTile {
	
	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	
	private int iX = 0;
	private int iY = 0;
	
	public static final int TileBackgroundColor = 0xe9d3b1;  
	
	private BufferedImage iMapImage;
	
	private int iZoomLevel = -1;
	private int iIndexY = -1;
	private int iIndexX = -1;
	
	//image does not exist
	private boolean isInvalid = false;
	
	//for a faster equals implementation
	private int iHash;
	
	public OsmTile( int aZoomLevel, int aIndexX, int aIndexY){
			
		iZoomLevel = aZoomLevel;
		iIndexX = aIndexX;
		iIndexY = aIndexY;
		
		iX = WIDTH * iIndexX;
		iY = HEIGHT * iIndexY;
		
		iHash = toString().hashCode();
	}
	
	public int getZoomlevel(){
		return iZoomLevel;
	}
	
	public void paint(Graphics g){
		
		if(iMapImage != null && ! isInvalid){
			g.drawImage( iMapImage, iX, iY, null );
		}
		else if(isInvalid){
			//draw nothing
		}
		else{
			g.setColor(Color.RED);
			g.drawLine(iX, iY, iX+WIDTH-1, iY+HEIGHT-1);
			g.drawLine(iX, iY+HEIGHT-1, iX+WIDTH-1, iY);
			g.drawRect(iX, iY, WIDTH-1, HEIGHT-1);
		}
	} 
	
	public String toString() {
		return String.valueOf(iZoomLevel) +"/" +
		String.valueOf(iIndexX) + "/" + 
		String.valueOf(iIndexY);
	}
	public static String key(int aZoomLevel, int aIndexX, int aIndexY){
		return String.valueOf(aZoomLevel) +"/" +
		String.valueOf(aIndexX) + "/" + 
		String.valueOf(aIndexY);
	}

	/**
	 * Callback for the TileDB to set the Image of this tile after loading.
	 * @param aImage
	 */
	public void setImage(BufferedImage aImage) {
		iMapImage = aImage;
		if(iMapImage == null){
			isInvalid = true;
		}
	}

	/**
	 * @return the path of this map tile on the remote server (i.e. '/1/2/12.png')
	 */
	public String getRemotePath() {
		return "/"+toString()+".png";
	}
	
}
