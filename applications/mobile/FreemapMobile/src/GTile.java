import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;

public class GTile
{
	private int x,y,z;

	Image image;
	boolean failedLoad;
	
	
	public GTile()
	{
		x=y=z=-1;
		image=null;
		failedLoad=false;
	}

	public void setZoom(int z)
	{
		this.z=z;
	}

	public boolean setCoords(int x, int y) 
	{
		if(x!=this.x || y!=this.y)
		{
			this.x=x;
			this.y=y;
			
			failedLoad=false;
			image=null;
			try
			{
				image=TileSource.getTile(x,y,z);
			}
			catch(IOException e) 
			{ 
				image=null; 
				failedLoad=true;
				System.out.println("ERROR LOADING: " + e);
			}
			return true;
		}
		return false;
	}



	public Image getImage()
	{
		return image;
	}

	public String toString()
	{	
		return "x="+x+" y="+y+" z="+z;
	}

	public boolean isValid()
	{
		return image!=null; 
	}

	public void paint (Graphics g, int x, int y)

	{
		if(image!=null)
		{
			g.drawImage(image,
				x,y,
				Graphics.TOP|Graphics.LEFT);
		}
				
		else if(failedLoad)
		{
			String text =  "Error loading"; 
			Font f = Font.getFont
				(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_LARGE);
			g.setFont(f);
			g.setColor(0xff7070);
			g.fillRect(x,y,256,256);
			g.setColor(0xffffff);
			g.drawString(text,x+128 - f.stringWidth(text)/2,y+128,
							Graphics.TOP|Graphics.LEFT);
		}
	}			

	public int getX() 
	{
		return x;
	}

	public int getY()
	{
		return y;
	}
}
