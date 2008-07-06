import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;
import java.io.IOException;
import javax.microedition.rms.RecordStoreFullException;


public class FMCanvas extends Canvas
{
    // States as follows:
    // INACTIVE = before we've told the app to connect to GPS
    // WAITING = while waiting for the GPS to connect
    // UPDATING = while updating tiles, because we've moved out of 
    //            the area covered by the active tiles
    // ACTIVE = normal state, showing maps
    // GPS_FAILED = we tried to connect to GPS but couldn't get signal

    static final int INACTIVE=0, WAITING=1, UPDATING=2, ACTIVE=3, GPS_FAILED=4;
    int zoom;
    GTile[] tiles, newTiles;
    int nTileRows, nTileCols;
    int state;
    int source;    
    Image hourglass;
    FreemapMobile app;
    TileSource tileSource;
    boolean doShowPOIs;
    Landmark nearestAnnotation;

    
    // Have all these to minimise computations due to limited power of phone?
    int topLeftX, topLeftY, topLeftXTile, topLeftYTile, xMod256, yMod256;
    

    // saves converting gtiles back to lon/lat
    double lon,lat;

    public FMCanvas(FreemapMobile app)
    {
            this.app=app;
            this.zoom=14;
            this.source=TileSource.FREEMAP;
            nTileCols = 2+getWidth()/256;
            nTileRows = 2+getHeight()/256;

            tiles = new GTile[nTileRows*nTileCols];
            newTiles = new GTile[nTileRows*nTileCols];
            
            tileSource=TileSource.getInstance();

            for(int count=0; count<tiles.length; count++)
            {
                tiles[count] = new GTile();
                tiles[count].setZoom(zoom);
                newTiles[count] = new GTile();
                newTiles[count].setZoom(zoom);
            }
            
            state=INACTIVE;

        try
        {
        hourglass=Image.createImage("/hourglass.png");
        
        }
        catch(IOException e)
        {
        System.out.println("Can't load icons");
        }
    }

    public void setZoom (int zoom)
    {
		System.out.println("setZoom: state=" + state);
		this.zoom=zoom;
		if(state==ACTIVE)
			updatePosition(app.getLon(),app.getLat());
    }

    public void paint (Graphics g)
    {
        if(state==ACTIVE || state==GPS_FAILED || state==UPDATING)
        {
            int curX, curY = -yMod256, count=0;
        
            for(int row=0; row<nTileRows; row++)
            {        
                curX = -xMod256;
                for(int col=0; col<nTileCols; col++)
                {
                    if(curX<getWidth() && curY<getHeight() &&
                        curX>-256 && curY>-256)
                    {    
                        tiles[count].paint(g,curX,curY);
                    }
                    
                    curX+=256;
                    count++;
                }
                curY+=256;
            }

            if(nearestAnnotation != null)
            {
              g.setColor(0xffff00);
              QualifiedCoordinates qc=
                  nearestAnnotation.getQualifiedCoordinates();
              int anX = GProjection.lonToX(qc.getLongitude(), zoom),
                  anY = GProjection.latToY(qc.getLatitude(), zoom); 
                  
                
            
            
            g.fillArc(anX-topLeftX, anY-topLeftY, 10,10,0,360);
            }
        if (state==UPDATING)    
        {
        Image im=hourglass;
        g.drawImage 
        (im,getWidth()/2-im.getWidth()/2,
        getHeight()/2-im.getHeight()/2,
        Graphics.TOP|Graphics.LEFT);
            }
            else
            {
            	g.setColor(0xff0000);
                g.drawLine(getWidth()/2-10,getHeight()/2-10,getWidth()/2+10,
                        getHeight()/2+10);
                g.drawLine(getWidth()/2-10,getHeight()/2+10,
                        getWidth()/2+10,getHeight()/2-10);
            }
        }
        else if (state==WAITING||state==INACTIVE)
        {
            g.setColor(0x00ffff);
            g.fillRect(0,0,getWidth(),getHeight());
            if(state==WAITING)
            {
                g.setColor(0);
        centreMessage(g,"Waiting for GPS",
                Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,
                Font.SIZE_MEDIUM));
            }
        }
    }

       private void centreMessage(Graphics g,String text,Font f)
    {
        g.setFont(f);
        g.drawString(text,
                    getWidth()/2-f.stringWidth(text)/2,getHeight()/2-16,
                    Graphics.TOP|Graphics.LEFT);
    } 

    public void updatePosition(double lon,double lat) 
    {
		  if(lat<1 && lon<1)
		  {
		    lat=51.05;
		    lon=-0.72;
		  }
		 
        topLeftX = GProjection.lonToX(lon,zoom)-getWidth()/2;
        topLeftY = GProjection.latToY(lat,zoom)-getHeight()/2;
        topLeftXTile = topLeftX/256;
        topLeftYTile = topLeftY/256;
        
        

        // Instantiate thread here (not GTile) so we can easily repaint
        // when all tiles loaded
        Thread t = new Thread()
        {
            public void run()
            {
                
                int count=0;
                boolean found;
    
                // Load the image into newTiles so that we can continue to 
                // paint with the old tiles
                for(int row=0; row<nTileRows; row++)
                {    
                    for(int col=0; col<nTileCols; col++)
                    {
                        // If the coords have changed...
                        
                        if(newTiles[count].setCoords(topLeftXTile+col,
                            topLeftYTile+row,zoom,source))
                        {
                            System.out.println("Changing positon");
                            // Set state to UPDATING for first changed tile    
                            if(state!=UPDATING)
                            {
                                state=UPDATING;
                                repaint();
                            }

                            // First see if we can copy an existing image from
                            // the old tiles to the new tiles
                            found=false;
                            for(int count2=0; count2<tiles.length; count2++)
                            {
                            
                                if(newTiles[count].equals(tiles[count2]))
                {  
                                    newTiles[count].setImage
                                   (tiles[count2].getImage());
                                    found=true;
                                    break;
                                }
                            }
                        
                            // If not, load the image (either from network or
                            // record store)
                            if(found==false)
                            {
                                try
                                {
                                    newTiles[count].load(tileSource);
                                }
                                catch(RecordStoreFullException e)
                                {
                                    app.showAlert("Record store full",    
                                     "The phone cache is full - no more " +
                                    "tiles will be cached. All new tiles will "+
                                    "be loaded from the web. Clear cache to "+
                                    "resolve this.", AlertType.WARNING);
                                }
                                catch(IOException e)
                                {
                                    app.showAlert
                                    ("Error loading tile",e.toString(),
                                        AlertType.ERROR);
                                }
                            }
                        }
                        count++;
                    }
                }
                
                
                // Once all new tiles loaded, set tiles to be newTiles to 
                // reflect update
                if(state==UPDATING)
                {
            System.out.println("Resetting state to active");
                    state=ACTIVE;
                    for(count=0; count<tiles.length; count++)
                    {    
                        tiles[count].copyFrom(newTiles[count]);
                    }
                }
        xMod256=topLeftX % 256;
        yMod256=topLeftY % 256;
        repaint();    
            }
        };
        t.start();
    }

    protected void keyPressed(int keyCode)
    {
        if(true)//state==ACTIVE || state==GPS_FAILED)
        {
            double lon=app.getLon(), lat=app.getLat();
            switch(getGameAction(keyCode))
            {
                case LEFT:  lon -= 0.001; handleCursorPress(lon,lat);break;
                case RIGHT: lon += 0.001; handleCursorPress(lon,lat);break;
                case UP:    lat += 0.001; handleCursorPress(lon,lat);break;
                case DOWN:  lat -= 0.001; handleCursorPress(lon,lat);break;
                case FIRE:  app.addAnnotation();// add POIs/annotations
                   break;       
        
            }    
        }
    }    
    
    private void handleCursorPress(double lon, double lat)
    {
      app.updatePosition(lon,lat);
      updatePosition(lon,lat);
    }

    public void setState(int state)
    {
        // if waiting for first gps location, do not accept failed state
        if(!(state==GPS_FAILED && this.state==WAITING))
            this.state=state;
    }    

    public int getState()
    {
        return state;
    }

    public void setSource(int source)
    {
        this.source=source;
        updatePosition(app.getLon(),app.getLat());
    }

    public TileSource getTileSource()
    {
        return tileSource;
    }
	 
	 
	  public void showPOIs(boolean doShowPOIs)
	  {
	     this.doShowPOIs = doShowPOIs;
    }
    
    public void setAnnotation (Landmark annotation)
    {
      nearestAnnotation=annotation;
    }

	public Image getImage(int i)
	{
		return tiles[i].getImage();
	}
}

