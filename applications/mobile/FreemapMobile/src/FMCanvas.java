import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;
import java.io.IOException;


public class FMCanvas extends Canvas
{
	static final int INACTIVE=0, WAITING=1, UPDATING=2, ACTIVE=3, GPS_FAILED=4;
	int zoom;
	GTile[] tiles;
	int nTileRows, nTileCols;
	int state;	

	
	// Have all these to minimise computations due to limited power of phone?
	int topLeftX, topLeftY, topLeftXTile, topLeftYTile, xMod256, yMod256;

	// saves converting gtiles back to lon/lat
	double lon,lat;

	public FMCanvas()
	{
			
			this.zoom=14;
			nTileCols = 2+getWidth()/256;
			nTileRows = 2+getHeight()/256;

			tiles = new GTile[nTileRows*nTileCols];

			for(int count=0; count<tiles.length; count++)
			{
				tiles[count] = new GTile();
				tiles[count].setZoom(zoom);
			}
			
			state=INACTIVE;
	}

	public void setZoom (int zoom)
	{
		this.zoom=zoom;
		for(int count=0; count<tiles.length; count++)
			tiles[count].setZoom(zoom);
		doUpdatePosition();
	}

	public void paint (Graphics g)
	{
		if(state==ACTIVE || state==GPS_FAILED)
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

			g.setColor(0xff0000);
			if(state==GPS_FAILED)
			{
				g.setFont( Font.getFont
					(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_LARGE));
				g.drawString("?",getWidth()/2,getHeight()/2,
							Graphics.TOP|Graphics.LEFT);
			}
			else
			{
				g.drawLine(getWidth()/2-10,getHeight()/2-10,getWidth()/2+10,
						getHeight()/2+10);
				g.drawLine(getWidth()/2-10,getHeight()/2+10,
						getWidth()/2+10,getHeight()/2-10);
			}
		}
		else
		{
			g.setColor(state==UPDATING ? 0xffffff: 0x00ffff);
			g.fillRect(0,0,getWidth(),getHeight());
			if(state==UPDATING || state==WAITING)
			{
				String text=(state==UPDATING) ? "Updating...":"Waiting...";
				g.setColor(0);
				g.setFont( Font.getFont
					(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM));
				g.drawString(text,
					getWidth()/2-32,getHeight()/2-16,
					Graphics.TOP|Graphics.LEFT);
			}
		}

		
	}

	
	public void updatePosition(double lon,double lat) 
	{
		
		// Get the pixel x and y for the top left of the display
	
		this.lon=lon;
		this.lat=lat;	
		doUpdatePosition();
	}

	
	private void doUpdatePosition()
	{
		topLeftX = GProjection.lonToX(lon,zoom)-getWidth()/2;
		topLeftY = GProjection.latToY(lat,zoom)-getHeight()/2;
		topLeftXTile = topLeftX/256;
		topLeftYTile = topLeftY/256;
		xMod256 = topLeftX%256;
		yMod256 = topLeftY%256;	

		// Instantiate thread here (not GTile) so we can easily repaint
		// when all tiles loaded
		Thread t = new Thread()
		{
			public void run()
			{
				int count=0;
	
				for(int row=0; row<nTileRows; row++)
				{	
					for(int col=0; col<nTileCols; col++)
					{
						if (tiles[count++].setCoords
								(topLeftXTile+col,topLeftYTile+row) &&
							state!=UPDATING)
						{
							state=UPDATING;
							repaint();
						}
					}
				}
				state=ACTIVE;
				repaint();
			}
		};
		t.start();
	}

	protected void keyPressed(int keyCode)
	{
		if(state==ACTIVE || state==GPS_FAILED)
		{
			
			switch(getGameAction(keyCode))
			{
				case LEFT:  updatePosition(lon-0.001,lat); break;
				case RIGHT: updatePosition(lon+0.001,lat); break;
				case UP:    updatePosition(lon,lat+0.001); break;
				case DOWN:  updatePosition(lon,lat-0.001); break;
				case FIRE:  setZoom(zoom==14 ? 16:14);	   break;	   
		
			}	
		}
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
}

