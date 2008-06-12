import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;

public class TileSource
{
    static final int FREEMAP = 0,  OSMARENDER=1, OSM_MAPNIK=2;
	boolean wrapCache, rsfWarningGiven;
	private static TileSource instance = null;

	public static TileSource getInstance()
	{
		if(instance==null)
			instance=new TileSource();
		return instance;
	}

	private TileSource()
	{
		wrapCache = rsfWarningGiven = false;
	}

	public void setCacheWrap(boolean wrapCache)
	{
		this.wrapCache = wrapCache;
	} 

    
    // 050608 no longer attempts to load from JAR; this is because record 
    // store and web access have been tested and seem to (mostly) work
    // this will only throw IOExceptions arising from network errors -
    // other exceptions (e.g. RecordStore) dealt with internally
    
    public Image getTile(int x, int y, int z,int source) throws 
            IOException,RecordStoreFullException 
    {
        Image image=null;
        String url = "http://www.free-map.org.uk/cgi-bin/render2?x="+x+
                             "&y="+y+"&z="+z;
        
        // First try record store
        image = loadFromRecordStore(x,y,z,source);
        if(image==null)
        {    
            // If that doesn't work, load from the network
                System.out.println("Trying to connect");

                HttpConnection conn = (HttpConnection)Connector.open(url);
                System.out.println("Creating DataInputStream...");
                DataInputStream dis=
                    new DataInputStream(conn.openInputStream());
                System.out.println
                        ("Creating Image from DataInputStream...");
                image=Image.createImage(dis);
                foundMessage(x,y,z,"web");
            try
            {
                storeTile(x,y,z,source,image);
            }
            catch(RecordStoreFullException e)
            {
				if(rsfWarningGiven==false)
				{
					rsfWarningGiven=true;
                	throw e;
				}
            }
            catch(Exception e)
            {
                System.out.println("Error storing tile");
            }
        }
        else
        {
            foundMessage(x,y,z,"record store");
        }    
        
        return image;
    }


    private void storeTile(int x,int y,int z,int source,Image image) 
        throws IOException, RecordStoreFullException, RecordStoreException
    {
            int width=image.getWidth(),height=image.getHeight();
            System.out.println("Creating image width="+width+" height="+height+
                " size:" + width*height);
        
            int[] rgb = new int[width*height];
            System.out.println("done.");
            image.getRGB(rgb,0,width,0,0,width,height);
          
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(z);
            dos.writeInt(source);
            dos.writeInt(width);
            dos.writeInt(height);
            for(int count=0; count<rgb.length; count++)
            {
                dos.writeInt(rgb[count]);
            }
            dos.flush();
            dos.close();
            System.out.println("Wrote to stream");
            rgb=null;
            byte[] bytes = bos.toByteArray();
            bos.close();
            System.out.println("done. Creating record store...");
            RecordStore store = RecordStore.openRecordStore("tiles",true);
			if(store.getSizeAvailable() < bytes.length && wrapCache==true)
				freeRecordStoreSpace(store);	
            int id=store.addRecord(bytes,0,bytes.length);
            System.out.println("Stored tile in recordstore: x="+
                    x+" y="+y+" z="+z+" id="+id);
	   		store.closeRecordStore(); 
    }

    // Deletes the first record in the store to make way for a new one.
    // NOTE - this assumes all records are of the same size, which they will
    // be in our case (images stored as RGB)
    private void freeRecordStoreSpace(RecordStore store)
    {
        
        try
        {
            RecordEnumeration re=store.enumerateRecords(null,null,true);
            int id = re.nextRecordId();
            store.deleteRecord(id);
        }
        catch(Exception e)
        {
            System.out.println("Couldn't free record store space: "+ e);
        }
    }
    
    public void clearCache()
    {
        try
        {
            RecordStore.deleteRecordStore("tiles");
			rsfWarningGiven=false;
        }
        catch(Exception e)
        {
            System.out.println("Can't delete cache: "+e);
        }
    }

    private Image loadFromRecordStore(int x,int y,int z,int source) 
    {
        Image image;
        byte[] bytes;
        int curX,curY,curZ,curSource,width,height;
        DataInputStream dis;
        
        try
        {
            RecordStore store=RecordStore.openRecordStore("tiles",true);
            int count=1;
            RecordEnumeration re=store.enumerateRecords(null,null,true);
            while(re.hasNextElement())
            {        
                bytes=re.nextRecord();
                dis = new DataInputStream(new ByteArrayInputStream(bytes));
                curX = dis.readInt();
                curY = dis.readInt();
                curZ = dis.readInt();
                curSource = dis.readInt();
                if(x==curX && y==curY && z==curZ && source==curSource)
                {
                    width=dis.readInt();
                    height=dis.readInt();
                    int[] rgbData = new int[width*height];
                    for(int count2=0; count2<rgbData.length; count2++)
                        rgbData[count2] = dis.readInt(); 
                    image = Image.createRGBImage(rgbData,width,height,false);
                    return image;
                
                }
            }
            store.closeRecordStore();
        }
        catch (RecordStoreException e)
        {
            System.out.println
                 ("RecordStoreException reading from record store:" +
                             e+" x="+x+" y="+y+" z="+z);
        }
        catch (IOException e)
        {
               System.out.println 
                     ("IOException reading from record store:" + e+
                            " x="+x+" y="+y+" z="+z);
        }
        
        return null;
    }

    public void foundMessage(int x,int y,int z,String place)
    {
        System.out.println("Found tile "+x+"/"+y+"/"+z+" in:" + place);
    }

    
}
