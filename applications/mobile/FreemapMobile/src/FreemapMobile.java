import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;


public class FreemapMobile extends MIDlet implements CommandListener,Parent
{

    private Form form;
    Command exit, menu, back, ok;
    FMCanvas canvas;
    GPSListener gpsListener;
    LocationProvider lp;
   	ActionList mainMenu, zlMenu, cacheMenu; 
    
    
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
        		int[] zooms = { 13, 14, 16};
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
			
        exit = new Command("Exit",Command.EXIT,0);
        menu = new Command("Menu", Command.OK, 0);    

        canvas = new FMCanvas(this);
		MainMenuHandler mainMenuHandler=new MainMenuHandler();
		ZoomMenuHandler zoomMenuHandler=new ZoomMenuHandler();
		CacheMenuHandler cacheMenuHandler=new CacheMenuHandler();
    

        mainMenu = new ActionList("Menu",this,List.IMPLICIT,
						Display.getDisplay(this));
        zlMenu = new RadioButtons ("Zoom",mainMenu,Display.getDisplay(this));
        cacheMenu = new ActionList("Cache",mainMenu,List.IMPLICIT,
						Display.getDisplay(this));

        zlMenu.addItem("Low",zoomMenuHandler);
        zlMenu.addItem("Medium",zoomMenuHandler);
        zlMenu.addItem("High",zoomMenuHandler);

		cacheMenu.addItem("Clear",cacheMenuHandler);

        mainMenu.addItem("Navigate",mainMenuHandler);
        mainMenu.addItem("Zoom level",zlMenu);
        mainMenu.addItem("Cache",cacheMenu);

        canvas.addCommand(menu);
        canvas.addCommand(exit);
        canvas.setCommandListener(this);

        gpsListener = new GPSListener(canvas);
        Display display = Display.getDisplay(this);
        display.setCurrent(canvas);    
            
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
        
			System.out.println("you started the menu");
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
}

