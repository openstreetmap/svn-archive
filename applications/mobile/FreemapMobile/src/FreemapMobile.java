import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.microedition.io.*;
import java.util.Enumeration;


public class FreemapMobile extends MIDlet implements CommandListener,Parent
{

    private Form form;
    Command exit, menu, back, ok;
    FMCanvas canvas;
    GPSListener gpsListener;
    LocationProvider lp;
    ActionList mainMenu, zlMenu, cacheMenu, poiMenu, serverMenu;
    LandmarkStore osmStore,freemapStore;
    POIManager poiManager;
    double lat,lon;
	String username, password;
    
    // Constructor for the class
    public FreemapMobile()
    {
    
    }

    // These are abstract so have to be overridden

    // startApp is the point of entry - rather like main()
    public void startApp()
    {

        class MainMenuHandler implements MenuAction
        {
            public void action(int i)
            {
                handleBackCommand();
                switch(i)
                {
                    case 0: // handle navigate
                        String entry = mainMenu.getEntry(0);
                        if(entry.equals("Navigate"))
                        {
                            mainMenu.setEntry(0,"Stop");
                            new Thread()
                            {
                                public void run()
                                {
                                    gpsListener.startGPSListen();
                                }
                            }.start();
                
                            if(canvas.getState()==FMCanvas.INACTIVE)
                                canvas.setState(FMCanvas.WAITING);
                        }
                        else
                        {
                            mainMenu.setEntry(0,"Navigate");
                            new Thread()
                            {
                                public void run()                
                                {
                                    gpsListener.stopGPSListen();
                                }
                            }.start();
                        }
                        break;
                }
            }
        }

        class ZoomMenuHandler implements MenuAction
        {
            public void action (int i)
            {
                handleBackCommand();
                // handle zoom level selection
                int[] zooms = { 13, 14, 16 };
                System.out.println("i="+i+" zooms[i]="+zooms[i]);
                canvas.setZoom(zooms[i]);
            }
        }

        class CacheMenuHandler implements MenuAction
        {
            public void action (int i)
            {
                handleBackCommand();
                switch(i)
                {
                    case 0: // handle clear cache
                        canvas.getTileSource().clearCache();    
                        break;
                }
            }
        }

        class POIMenuHandler implements MenuAction
        {
            public void action(int i)
            {
                handleBackCommand();
                switch(i)
                {
                case 0:
                    new Thread()
                    {
                        public void run()
                        {
                            try
                            {    
                                double w=lon-0.05,
                                        e=lon+0.05,
                                        s=lat-0.05,
                                        n=lat+0.05;
                                  
                                  
                                LandmarkLoader osm = new LandmarkLoader
                                  (osmStore,w,s,e,n,
                                  "http://osmxapi.hypercube.telascience.org/"+
                                "api/0.5/"+    
                                "node[amenity|tourism|natural|place=village|"+
                                    "hamlet|town|pub|peak|restaurant|hotel]"+
                                "[bbox="+w+","+s+","+e+","+n+"]",
                                   new OSMParserHandler()
                                  );
                                
                                LandmarkLoader freemap = new LandmarkLoader
                                  (freemapStore,w,s,e,n,
                                  "http://www.free-map.org.uk/freemap/api/"+
                                "markers.php?action=get"+
                                "&bbox="+w+","+s+","+e+","+n,
                                  new FreemapParserHandler()
                                  );
                                
                                osm.load();
                                freemap.load();
                                  
                               
                            }
                            catch(Exception e)
                            {
								                showAlert("Error loading",
                                "Error parsing:" + e,
								              AlertType.ERROR);
                            }
                        }
                    }.start();
                break;
                
                case 2: // show/hide
                  String entry = poiMenu.getEntry(2);
                  if(entry.equals("Show"))
                  {
                    poiMenu.setEntry(2,"Hide");
                    canvas.showPOIs(true);
                  }
                  else
                  {
                    poiMenu.setEntry(2,"Show");
                    canvas.showPOIs(false);
                  }
                }
            }
        }                    
                        
            
		class ServerMenuHandler implements MenuAction
		{
			public void action(int i)
			{
				switch(i)
				{
					case 0:
						if(serverMenu.getEntry(0).equals("Login"))
						{
							serverMenu.setEntry(0,"Logout");
							LoginForm loginForm = 
								new LoginForm(FreemapMobile.this);
							Display.getDisplay(FreemapMobile.this).	
								setCurrent(loginForm);
						}
						else
						{
							serverMenu.setEntry(0,"Login");
							username=password="";
						}
				}
			}
		}


        try
        {
            osmStore = LandmarkStore.getInstance("OSM_POIs");
            if(osmStore==null)
            {
                LandmarkStore.createLandmarkStore("OSM_POIs");
                osmStore = LandmarkStore.getInstance("OSM_POIs");
                osmStore.addCategory("pub");
                osmStore.addCategory("restaurant");
                osmStore.addCategory("hotel");
                osmStore.addCategory("peak");
                osmStore.addCategory("village");
                osmStore.addCategory("hamlet");
                osmStore.addCategory("town");
            }
            freemapStore = LandmarkStore.getInstance("Freemap_annotations");
            if(freemapStore==null)
            {
                LandmarkStore.createLandmarkStore("Freemap_annotations");
                freemapStore = LandmarkStore.getInstance("Freemap_annotations");
                freemapStore.addCategory("hazard");
                freemapStore.addCategory("info");
                freemapStore.addCategory("directions");
            }    
        }
        catch(Exception e)
        {
            System.out.println("Couldn't create landmark store: "+ e);
        }
        exit = new Command("Exit",Command.EXIT,0);
        menu = new Command("Menu", Command.OK, 0);    

        canvas = new FMCanvas(this);
        MainMenuHandler mainMenuHandler=new MainMenuHandler();
        ZoomMenuHandler zoomMenuHandler=new ZoomMenuHandler();
        CacheMenuHandler cacheMenuHandler=new CacheMenuHandler();
        POIMenuHandler poiMenuHandler=new POIMenuHandler();
		ServerMenuHandler serverMenuHandler = new ServerMenuHandler();
    
        poiManager=new POIManager(this,osmStore);  

        mainMenu = new ActionList("Menu",this,List.IMPLICIT,
                        Display.getDisplay(this));
        zlMenu = new RadioButtons ("Zoom",mainMenu,Display.getDisplay(this));
        cacheMenu = new ActionList("Tile Cache",mainMenu,List.IMPLICIT,
                        Display.getDisplay(this));
        poiMenu = new ActionList("POIs",mainMenu,List.IMPLICIT,
                        Display.getDisplay(this));
		serverMenu = new ActionList("Server",mainMenu,List.IMPLICIT,
						Display.getDisplay(this));


        mainMenu.addItem("Navigate",mainMenuHandler);
        mainMenu.addItem("Zoom level",zlMenu);
        mainMenu.addItem("Cache",cacheMenu);
        mainMenu.addItem("POIs",poiMenu);
        mainMenu.addItem("Tests",serverMenu);
        zlMenu.addItem("Low",zoomMenuHandler);
        zlMenu.addItem("Medium",zoomMenuHandler);
        zlMenu.addItem("High",zoomMenuHandler);
        cacheMenu.addItem("Clear",cacheMenuHandler);
        poiMenu.addItem("Update",poiMenuHandler);
        poiMenu.addItem("Find", poiManager);
        poiMenu.addItem("Show",poiMenuHandler);
		serverMenu.addItem("Login",serverMenuHandler);

      

        canvas.addCommand(menu);
        canvas.addCommand(exit);
        canvas.setCommandListener(this);

        gpsListener = new GPSListener(this);
        Display display = Display.getDisplay(this);
        display.setCurrent(canvas);    

		lat=51.05;
		lon=-0.72;
            
       	username=password=""; 
    }
    

    public void pauseApp()
    {
        
    }
    
    public void destroyApp(boolean unconditional)
    {
    }    
   
 
    // the code to respond to commands
    public void commandAction(Command c, Displayable s)
    {
        if(c==exit)
        {
            // if an exit command, quit the app
             notifyDestroyed(); 
        }
        else if (c==menu)
        {
            canvas.removeCommand(menu);
            Display.getDisplay(this).setCurrent(mainMenu.getList());
            
	    }
    }

    void showAlert(String title,String message,AlertType type)
    {
        Alert a = new Alert(title,message,null,type);
        a.setTimeout(Alert.FOREVER);    
        Display.getDisplay(this).setCurrent(a);
    }

    public void handleBackCommand()
    {
        canvas.addCommand(menu);
        Display.getDisplay(this).setCurrent(canvas);
    }
    
    public void updatePosition(double lon,double lat)
    {
	  if(lon<1 && lat<1)
	  {
		this.lat=51.05;
		this.lon=-0.72;
	  }
	  else
	  {
      this.lat=lat;
      this.lon=lon;
	  }
      canvas.updatePosition(lon,lat);
      poiManager.setCurrentCoords(new QualifiedCoordinates(lat,lon,Float.NaN,
                                                          Float.NaN,Float.NaN));
    }
    
	public void login(String username,String password)
	{
		this.username=username;
		this.password=password;
		handleBackCommand();
	}
    
    public double getLon()
    {
      return lon;
    }
    
    public double getLat()
    {
      return lat;
    }

	public void addAnnotation()
	{
		gpsListener.forceUpdate();
		AnnotationManager am=new AnnotationManager(this);
		am.annotate(lon,lat);
	}
}

