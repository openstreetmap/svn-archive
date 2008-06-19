import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.rms.RecordStoreFullException;

public class GTile
{
    private int x,y,z, source;

    Image image;
    boolean failedLoad;
    
    
    public GTile()
    {
        x=y=z=-1;
        image=null;
        failedLoad=false;
        source=TileSource.FREEMAP;
    }

    public void setZoom(int z)
    {
        this.z=z;
    }

    public void setSource(int source)
    {
        this.source=source;
    }

    public int getSource()
    {
        return source;
    }

    public boolean setCoords(int x, int y, int z,int source) 
    {
        if(x!=this.x || y!=this.y || z!=this.z || source!=this.source)
        {
            this.x=x;
            this.y=y;
            this.z=z;
            this.source=source;
            return true;
        }
        return false;
    }

    public boolean load(TileSource ts) throws IOException,
                    RecordStoreFullException
    {        
        failedLoad=false;
        image=null;
        try
        {
            image=ts.getTile(x,y,z,source);
            
        }
        catch(IOException e) 
        { 
            image=null; 
            failedLoad=true;
            System.out.println("ERROR LOADING: " + e);
            throw e;
        }
        
        return true;
    }



    public Image getImage()
    {
        return image;
    }

    public void setImage(Image image)
    {
        this.image=image;
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
        if((x==-1 && y==-1) || image==null)
        {
            g.setColor(0x00ffff);
            g.fillRect(x,y,256,256);
        }
        else if(image!=null)
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

    public boolean equals (GTile other)
    {
        return x==other.x && y==other.y && z==other.z && source==other.source;
    }

    public void copyFrom(GTile other)
    {
        x=other.x;
        y=other.y;
        z=other.z;
        source=other.source;
        image=other.image;
    }
        
}
