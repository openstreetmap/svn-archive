// Rough and ready

import java.util.Vector;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.Image;

public class PhotoLoader
{
  
  static Vector ids, images;
  
  static
  {
    ids=new Vector();
    images=new Vector();
  }
  
  public static Image getImage (String id) throws IOException
  {
  
      for(int count=0; count<ids.size(); count++)
      {
        if(ids.elementAt(count).equals(id))
          return (Image)images.elementAt(count);
      }
      
       
      String url = "http://www.free-map.org.uk/freemap/api/markers.php?"+
                       "action=getPhoto&id="+id; 
      HttpConnection conn = (HttpConnection)Connector.open(url);
   
      DataInputStream dis=
                    new DataInputStream(conn.openInputStream());
              
              
      Image image=Image.createImage(dis);
      images.addElement(image);
      ids.addElement(id);
      return image;
   }
}   
      
