import java.util.*;
import java.lang.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.LatLonPoint;
import java.awt.event.*;

public class osmAppletMouseListener extends MapMouseAdapter
{

  osmDisplay osmD;
  osmPointsLayer osmPL;
  LatLonPoint pPressed = new LatLonPoint();
  LatLonPoint pReleased = new LatLonPoint();


  
  public osmAppletMouseListener(osmDisplay od, osmPointsLayer opl)
  {
    System.out.println("osmappletmouselistener instantiated");
    osmD = od;
    osmPL = opl;

  }

  public String[] getMouseModeServiceList()
  {
    System.out.println("asked for service list!!!!!");
  
    return new String[] { SelectMouseMode.modeID, NavMouseMode.modeID };
  
  }

  public boolean mousePressed(java.awt.event.MouseEvent e)
  {
    MapMouseEvent mme = (MapMouseEvent)e;
    LatLonPoint p = mme.getLatLon();

    pPressed = p;
    
    System.out.println("map pressed at " + p.getLatitude() + "," +  p.getLongitude());

    return true;
  } 

  public void mouseMoved()
  {

  }

  public boolean mouseMoved(java.awt.event.MouseEvent e)
  {
    return true;

  } 

  public boolean mouseClicked(java.awt.event.MouseEvent e)
  {
    return true;

  } 

  public boolean mouseDragged(java.awt.event.MouseEvent e)
  {
    return true;

  } 

  public void mouseEntered(java.awt.event.MouseEvent e)
  {

  } 

  public void mouseExited(java.awt.event.MouseEvent e)
  {

  } 

  public boolean mouseReleased(java.awt.event.MouseEvent e)
  {
    MapMouseEvent mme = (MapMouseEvent)e;
    pReleased = mme.getLatLon();
    
    System.out.println("map released at " + pReleased.getLatitude() + "," +  pReleased.getLongitude());

    osmPL.select(pPressed, pReleased);

    return true;

  } 




} // osmAppletMouseListener
