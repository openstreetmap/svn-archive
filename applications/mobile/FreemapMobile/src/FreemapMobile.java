import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;


public class FreemapMobile extends MIDlet implements CommandListener
{

	private Form form;
	Command ok, exit, stop;
	FMCanvas canvas;
	GPSListener gpsListener;
	LocationProvider lp;
	
	// Constructor for the class
	public FreemapMobile()
	{
		canvas = new FMCanvas();
		
		
		// Create two commands
		ok = new Command("Navigate",Command.OK,0);
		exit = new Command("Exit",Command.EXIT,0);
		stop = new Command("Stop", Command.STOP, 0);

		canvas.addCommand(ok);
		canvas.addCommand(exit);
		canvas.setCommandListener(this);

		gpsListener = new GPSListener(canvas);
		
	
	}

	// These are abstract so have to be overridden

	// startApp is the point of entry - rather like main()
	public void startApp()
	{
		Display display = Display.getDisplay(this);
		display.setCurrent(canvas);
		//startGPSListen();
		
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
		// What command type is it?
		switch(c.getCommandType())
		{
			// if an exit command, quit the app
			case Command.EXIT: notifyDestroyed(); break;

			// if an OK command....
			case Command.OK: 
				System.out.println("You selected ok");
				Thread t = new Thread()
				{
					public void run()
					{
						gpsListener.startGPSListen();
					}
				};
				t.start();
				if(canvas.getState()==FMCanvas.INACTIVE)
					canvas.setState(FMCanvas.WAITING);
				canvas.removeCommand(ok);
				canvas.addCommand(stop);
				break;
			
			case Command.STOP:	
				gpsListener.stopGPSListen();
				canvas.addCommand(ok);
				canvas.removeCommand(stop);
				break;
					
		}

	}
}

