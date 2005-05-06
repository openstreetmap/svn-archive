
import com.bbn.openmap.omGraphics.*;
import java.awt.Color;

public class Node extends OMCircle
{
  private int nUID;
  private double dLatitude, dLongitude;
  
  public Node(int uid, double latitude, double longitude)
  {
    super((float)latitude,
        (float)longitude,
        5f,
        com.bbn.openmap.proj.Length.METER);
    setLinePaint(Color.black);
    setSelectPaint(Color.red);
    setFillPaint(Color.black);

    nUID = uid;

  } // Node


  public int getUID()
  {
    return nUID;

  } // getUID
  
  public String toString()
  {
    return "(" + nUID + "," + getLatLon() + ")";
    

  } // toString

} // Node
