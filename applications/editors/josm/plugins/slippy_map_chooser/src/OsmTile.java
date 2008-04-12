// License: GPL. Copyright 2007 by Tim Haussmann

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

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
	
	public void paint(Graphics g,TileDB db){
		
		if(iMapImage != null && ! isInvalid){
			g.drawImage( iMapImage, iX, iY, null );
		}
		else if(isInvalid){
			//draw nothing
		}
		else{
			// first try to interpolate tile from parent			
			OsmTile parent = getParent(db);
			if (parent!=null){
				Graphics2D g2d = (Graphics2D) g;
				AffineTransform oldTr = g2d.getTransform();
				Shape oldClip = g2d.getClip();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				
				// since the parent will paint 4 tiles in the 
				// current zoom level we have to clip the drawing
				// region to the current tile
				g2d.clipRect(iX, iY, WIDTH, HEIGHT);
				g2d.scale(2, 2);				
				parent.paint(g,db);
				
				g2d.setTransform(oldTr);
				g2d.setClip(oldClip);
			}
			else{
				// otherwise draw placeholder
				g.setColor(Color.RED);
				g.drawLine(iX, iY, iX+WIDTH-1, iY+HEIGHT-1);
				g.drawLine(iX, iY+HEIGHT-1, iX+WIDTH-1, iY);
				g.drawRect(iX, iY, WIDTH-1, HEIGHT-1);
			}
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
	
	public OsmTile getParent(TileDB db){
		return iZoomLevel == 0 ? null : db.get(parentKey());
	}

	public String parentKey() {
		return key(iZoomLevel - 1,iIndexX/2,iIndexY/2);
	}
}
