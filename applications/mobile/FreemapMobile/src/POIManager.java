// POIManager is a class which manages points of interest and their categories
// as well as the menus used to control them.

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;
import java.util.Vector;

import java.io.*;


import java.util.Enumeration;

public class POIManager implements MenuAction
{
  ActionList poiTypeList; // list of POI types
  POIList poiList; // list of POIs
  Vector categories; // categories corresponding to the POI types
  LandmarkStore store;
  Coordinates currentCoords;
  
  public POIManager(FreemapMobile app,LandmarkStore store)
  {
    poiTypeList=new ActionList("POI types",app,List.IMPLICIT,
                Display.getDisplay(app));
    poiList = new POIList(this,poiTypeList,Display.getDisplay(app));
    this.store=store;
    categories=new Vector();
    
    // get the categories by querying the landmark store
    Enumeration cat=store.getCategories();
    String elem;
    while(cat.hasMoreElements())
    {
      elem=(String)(cat.nextElement());
      poiTypeList.addItem(elem,poiList);
      categories.addElement(elem);
    }
  }
 

  public String getCategory(int i)
  {
    return (String)categories.elementAt(i);
  }



  // Used for passing in the current GPS coords to the POIManager
  // This is so that we can get the distances of the POIs
  public void setCurrentCoords(Coordinates currentCoords)
  {
    this.currentCoords=currentCoords;
  }
  
  public Coordinates getCurrentCoords()
  {
    return currentCoords;
  }
  
  // The action() method of POIManager performs the action associated with
  // the poiTypeList, which is the standard ActionList behaviour of displaying
  // the list. In other words, if a POIManager is defined as a 
  // MenuAction, the poiTypeList will appear.
  public void action(int i)
  {
    poiTypeList.action(i);
  }
  
  public LandmarkStore getLandmarkStore()
  {
    return store;
  }
}


// A list of points of interest.
// A POITypeList has one single POIList within it (not one POIList per category)
// That one POIList will be blanked and repopulated whenever an entry in a
// POITypeList is selected.

class POIList extends ActionList
{
	POIManager poiManager;


	public POIList (POIManager poiManager,ActionList parent,Display display)
	{
		super("POIs",parent,List.IMPLICIT,display);
		this.poiManager = poiManager;
	
	}
   
  // Called when a POIlist is invoked as an action from the parent (the 
  // poiTypeList)
  // Blanks the POI list and fills it with POIs of the category corresponding
  // to the selection.
	public void action (int i)
	{
  		super.action(i);
		String category=poiManager.getCategory(i);
		System.out.println("************DELETE ALL*********************");
		clear();
		Coordinates currentGPScoords = poiManager.getCurrentCoords();
		try
		{
		  // Get all the landmarks in this category
		  Enumeration pois=poiManager.getLandmarkStore().
						getLandmarks(category,null);
		  if(pois!=null)
		  {
		  	Landmark landmark=null;
		
		  	while(pois.hasMoreElements())
		  	{
			 	landmark = (Landmark)pois.nextElement();	
				System.out.println("Landmark:"+landmark.getName());
		    	// Create a menu item for each landmark. The action of each 
				// menu item
		    	// will be a LandmarkDisplay containing details of each landmark.
			 	LandmarkDisplay d=new LandmarkDisplay(landmark,
          		landmark.getQualifiedCoordinates().distance
					(currentGPScoords)/1000,
				GProjection.direction
					(currentGPScoords.getLongitude(),
					currentGPScoords.getLatitude(),
					landmark.getQualifiedCoordinates().getLongitude(),
					landmark.getQualifiedCoordinates().getLatitude()),
					display,this);
			 	addItem(landmark.getName(),d);			 
 			}
		  }
		}
		catch(IOException e)
		{
		  System.out.println("Error getting landmarks: " + e);
    	}
	}
}

// A LandmarkDisplay will display a description of and distance to the landmark
// The action of each entry in a POIList will be a LandmarkDisplay of that POI.
class LandmarkDisplay extends Form implements MenuAction,CommandListener
{
	Command back;
	Display display;
	Parent parent;
	QualifiedCoordinates coords; // the coordinates of the Landmark
	Coordinates current; // the current GPS coordinates
	String description, direction;
  double distance;

	public LandmarkDisplay(Landmark landmark,double distance,
						String direction,
                        Display display,Parent parent)
	{
	  super(landmark.getName());
	  coords=landmark.getQualifiedCoordinates();
	  description=landmark.getDescription();
	  back=new Command("Back",Command.BACK,0);
	  addCommand(back);
		this.display=display;
		this.parent=parent;
		this.distance=distance;
		this.direction=direction;
	  setCommandListener(this);
	}
	

 
   // The action just displays the LandmarkDisplay
	public void action (int i)
	{
		if(description!=null) append(description);
		String s = String.valueOf(distance);
		String aaa=s.substring(0,Math.min(4,s.length()))+" km " + direction;
		System.out.println(aaa);
		append(new TextField("Distance:",
			s.substring(0,Math.min(4,s.length()))+" km " + direction,20,
          TextField.UNEDITABLE));
          
		// display details of the landmark feature
		display.setCurrent(this);
	}

    // the code to respond to commands
    public void commandAction(Command c, Displayable s)
    {
        // Just call the parent's handleBackCommand()
		  if(c==back)
		  {
			 parent.handleBackCommand();
		  }
    }
}
