import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;


public class FreemapMobile extends MIDlet implements CommandListener
{

    private Form form;
    Command exit, menu, back, ok;
    FMCanvas canvas;
    GPSListener gpsListener;
    LocationProvider lp;
    List list, zlList, cacheList;
    
    
    // Constructor for the class
    public FreemapMobile()
    {
        
        
    
    }

    // These are abstract so have to be overridden

    // startApp is the point of entry - rather like main()
    public void startApp()
    {

        canvas = new FMCanvas(this);
    
        
        // Create commands
        back = new Command("Back",Command.BACK,0);
        ok = new Command("OK",Command.OK,0);
        exit = new Command("Exit",Command.EXIT,0);
        menu = new Command("Menu", Command.OK, 0);    

        list = new List("Menu",List.IMPLICIT);
        list.append("Navigate",null);
        list.append("Zoom level",null);
        list.append("Cache",null);
        list.setCommandListener(this);

        zlList = new List ("Zoom",List.EXCLUSIVE);
        zlList.append("Low",null);
        zlList.append("Medium",null);
        zlList.append("High",null);
        zlList.setSelectedIndex(1,true);
        zlList.setCommandListener(this);

        cacheList = new List("Cache",List.IMPLICIT);
        cacheList.append("Clear",null);
        //cacheList.append("Set tile limit",null);
        cacheList.setCommandListener(this);

        canvas.addCommand(menu);
        canvas.addCommand(exit);
        list.addCommand(back);
        zlList.addCommand(back);
        zlList.addCommand(ok);
        cacheList.addCommand(back);
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
        int[] zooms = { 13, 14, 16};
        System.out.println("CommandAction");
        // What command type is it?
        if(c==exit)
        {
            // if an exit command, quit the app
             notifyDestroyed(); 
        }
        else if(s==list)
        {
            if(c==back)
            {
                Display.getDisplay(this).setCurrent(canvas);
                canvas.addCommand(menu);
            }
            else if (c==List.SELECT_COMMAND)
            {
                System.out.println("List.SELCT_COMMAND");
                
                canvas.addCommand(menu);
        
                String cmd = list.getString(list.getSelectedIndex());
                System.out.println(cmd);

                if(cmd.equals("Navigate"))
                {
                    list.set(list.getSelectedIndex(),"Stop",null);
                    new Thread()
                    {
                        public void run()
                        {
                            gpsListener.startGPSListen();
                        }
                    }.start();
                
                    if(canvas.getState()==FMCanvas.INACTIVE)
                        canvas.setState(FMCanvas.WAITING);

                    Display.getDisplay(this).setCurrent(canvas);
        
                }
                else if (cmd.equals("Stop"))
                {
                    list.set(list.getSelectedIndex(),"Navigate",null);
                    new Thread()
                    {
                        public void run()                
                        {
                            gpsListener.stopGPSListen();
                        }
                    }.start();
                    
                    Display.getDisplay(this).setCurrent(canvas);
                    if(canvas.getState()==FMCanvas.WAITING)
                        canvas.setState(FMCanvas.INACTIVE);
                }
                else if (cmd.equals("Zoom level"))
                {
                    Display.getDisplay(this).setCurrent(zlList);
                }
                else if (cmd.equals("Cache"))
                {
                    Display.getDisplay(this).setCurrent(cacheList);            
                }
            
            }
        }
        else if (s==cacheList)
        {
            System.out.println("cacheList");
            if(c==back)
            {
                Display.getDisplay(this).setCurrent(list);
            }
            else if (c==List.SELECT_COMMAND)
            {
                String cmd = cacheList.getString(cacheList.getSelectedIndex());
                if (cmd.equals("Clear"))
                {
                    canvas.getTileSource().clearCache();    
                    Display.getDisplay(this).setCurrent(canvas);            
                }
                   else if (cmd.equals("Set tile limit"))
                {
                }
            }
        }    
        else if (s==zlList)
        {
            if (c==ok)
            {        
                canvas.setZoom(zooms[zlList.getSelectedIndex()]);
                Display.getDisplay(this).setCurrent(canvas);        
            }    
            else if (c==back)
            {
                Display.getDisplay(this).setCurrent(cacheList);        
            }
        }    
        else if (s instanceof TextBox)
        {
            canvas.getTileSource().setTileLimit(Integer.parseInt
                                                (((TextBox)s).getString()));
        }
        else if (s==canvas)
        {
            if (c==menu)
            {
                canvas.removeCommand(menu);
        
                System.out.println("you started the menu");
                Display.getDisplay(this).setCurrent(list);    
        
            }
        }
    }

    void showAlert(String title,String message,AlertType type)
    {
        Alert a = new Alert(title,message,null,type);
        a.setTimeout(Alert.FOREVER);    
        Display.getDisplay(this).setCurrent(a);
    }
}

