// License: GPL. Copyright 2007 by Tim Haussmann


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JComponent;



/**
 *  The TileDB is responsible for fetching and storing all needed map tiles. The tiles
 *  are stored in memory and are fetched one by one in background.
 *  Tiles are stored in a HashTable. The path of the tile on the tile server (i.e. '/12/3/5.png') 
 *  acts as key and the OsmTile as value.
 *  @author Tim Haussmann
 *
 */
public class TileDB{

	public static final int PRIO_HIGH = 1;
	public static final int PRIO_LOW = 2;
	
	//Osm tile server address
	private static final String OSM_TILE_SERVER = "http://tile.openstreetmap.org";
	
	//Queue for loading tiles
	private Vector<OsmTile> iTileQueue = new Vector<OsmTile>();	
	
	//DB for holding the downloaded tiles
	private Hashtable<String,OsmTile> iHashTable = new Hashtable<String,OsmTile>(SlippyMapChooserPlugin.MAX_TILES_IN_DB + 50);
	
	//the order in that the tiles have been downloaded, needed to delete old ones
	private Vector<String> iTileOrder = new Vector<String>(SlippyMapChooserPlugin.MAX_TILES_IN_DB + 50);
	
	//to update the map after a tile has been loaded
	private JComponent iSlippyMapChooser;
	
	private int[] iMaxLatValues;
	private int[] iMaxLonValues;
	
	/**
	 * Creates the TileDB.
	 * @param aWorldChooser
	 */
	public TileDB(JComponent aWorldChooser){
		iSlippyMapChooser = aWorldChooser;
		
		iMaxLatValues = new int[20];
		iMaxLonValues = new int[20];
		for(int i=0; i<20; i++){
			iMaxLatValues[i] = OsmMercator.LatToY(OsmMercator.MIN_LAT, i)/OsmTile.HEIGHT;
			iMaxLonValues[i] = OsmMercator.LonToX(180, i)/OsmTile.WIDTH;
		}
		
		new TileFetcherThread(this);
		new TileFetcherThread(this);
		new TileFetcherThread(this);
		new TileFetcherThread(this);
	}
	
	/**
	 * Creates a OsmTile if it's not in the Hashtable. New created tiles are added to the
	 * end of the download queue (Vector) and the DB (Hashtable). After that the fetching thread is 
	 * notified.
	 * @param aZoomLevel the zoomlevel for the tile to create
	 * @param aIndexX the X-index of the tile to create. Must be within [0..2^zoomlevel[
	 * @param aIndexY the Y-index of the tile to create. Must be within [0..2^zoomlevel[
	 * @param aPriority PRIO_HIGH adds this tile at the waiting queue start  PRIO_LOW at the end
	 */
	public synchronized void loadTile(int aZoomLevel, int aIndexX, int aIndexY, int aPriority)
	{		
		
		if(aZoomLevel <0 || aIndexX <0 || aIndexY <0){
			return;
		}else if(iMaxLonValues[aZoomLevel] <= aIndexX ||
				iMaxLatValues[aZoomLevel] <= aIndexY ){
			return;
		}
		String key = OsmTile.key(aZoomLevel, aIndexX, aIndexY);
		OsmTile t = iHashTable.get(key);
		if(t != null){
			//if a tile is already in the DB and still in the laoding queue
			//move it to the beginning of the queue in order to make the currently
			//visible tiles load first
			if(iTileQueue.remove(t)){				
				iTileQueue.add(0,t);
			}
			return;
		}
		
		t = new OsmTile(aZoomLevel, aIndexX, aIndexY);		
		iHashTable.put(t.toString(),t);	
		iTileOrder.add(0,key);
		
		if(aPriority == PRIO_LOW){
			iTileQueue.addElement(t);
		}else if(aPriority == PRIO_HIGH){
			iTileQueue.add(0,t);
		}else{
			iTileQueue.addElement(t);
		}
		
		//check if old tiles need to be deleted
		if(iTileOrder.size() > SlippyMapChooserPlugin.MAX_TILES_IN_DB){
			for(int i=0; i<SlippyMapChooserPlugin.MAX_TILES_REDUCE_BY; i++){
				String removedKey = iTileOrder.remove(iTileOrder.size()-1);
				iHashTable.remove(removedKey);
			}
		}
		
		//Wake up the fetching threads
		synchronized (this) {
			notifyAll();
		}
	}
	
	/**
	 * Returns the next tile that needs to be loaded. This method is called by the threads
	 * that fetch the tiles. If the download queue is empty the calling threads are waiting.
	 * @return the first tile in the download queue with the same zoom level that is currently displayed or just the first. 
	 */
	private OsmTile getNextTile(){
		if(iTileQueue.size() > 0){
			OsmTile t;
			Enumeration<OsmTile> e = iTileQueue.elements();
			while(e.hasMoreElements()){
				t = e.nextElement();
				if(t.getZoomlevel() == SlippyMapChooser.iZoomlevel){
					return t;
				}
			}
			return iTileQueue.firstElement();	
		}
		else{			
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
			return iTileQueue.firstElement();	
		}
	}
	
	/**
	 * Access tiles in the TileDB.
	 * @return an Enumeration that holds all OsmTiles currently stored in the TileDB (Hashtable)
	 */
	public Enumeration<OsmTile> elements() {	
		return iHashTable.elements();
	}
	
	/**
	 * Returns a OsmTile by key
	 * @param aKey 
	 * @return
	 */
	public OsmTile get(String aKey){
		return iHashTable.get(aKey);
	}
	
	/**
	 * Delete OsmTiles from the TileDB.
	 * @param aTile that is to be deleted.
	 */
	public void remove(OsmTile aTile){
		iHashTable.remove(aTile.toString());
		iTileQueue.removeElement(aTile);
	}

	/**
	 * Returns the number of OsmTiles waiting to be fetched from the server.
	 * @return 
	 */
	public int getLoadingQueueSize() {
		return iTileQueue.size();
	}

	/**
	 * Returns the number of tiles stored in the TileDB
	 * @return
	 */
	public int getCachedTilesSize() {		
		return iHashTable.size();
	}
	
	
	/***************************************************
	 * Private inner class to fetch the tiles over http
	 * *************************************************
	 */
	
	class TileFetcherThread implements Runnable{
		
		private TileDB iTileDB;
		private Thread iThread;
		
		public TileFetcherThread(TileDB aTileDB){
			iTileDB = aTileDB;
			iThread = new Thread(this);
			iThread.start();
		}
		
		public void run(){
			
			while(true){
				
				//get the next tile to load
				OsmTile t = iTileDB.getNextTile();
				
				URL tileUrl = null;
				try {
					
					//build the url to the tile
					tileUrl = new URL(OSM_TILE_SERVER + t.getRemotePath());
										
					//get the tile
					InputStream in = tileUrl.openStream();
				
					//the tile needs the image...					
					t.setImage(ImageIO.read(in));
					
				} catch (MalformedURLException e) {
//					System.out.println("Catched: " + e.getMessage());
//					e.printStackTrace();				
					t.setImage(null);
				} catch (IOException e) {
//					System.out.println("Catched: " + e.getMessage());
//					e.printStackTrace();	
					t.setImage(null);
				} catch (Exception e){
//					System.out.println("Catched: " + e.getMessage());
//					e.printStackTrace();
					t.setImage(null);
				}
				
				iTileQueue.removeElement(t);
				iSlippyMapChooser.repaint();	
			}
		}		
	}
}
