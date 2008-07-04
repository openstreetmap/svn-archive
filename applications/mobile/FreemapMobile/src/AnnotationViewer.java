import javax.microedition.lcdui.*;
import javax.microedition.location.*;
import java.io.*;
import java.util.Enumeration;
import javax.microedition.lcdui.game.Sprite;

public class AnnotationViewer implements CommandListener
{
  QualifiedCoordinates location;
  double thresholdDistance;
  FreemapMobile app;
  LandmarkStore store;
  Landmark nearestAnnotation;
  Canvas photoCanvas;
  LandmarkDisplay ld;
  Command next, back;
  Image curImage;
  
  public AnnotationViewer (LandmarkStore store, double thresholdDistance,
                            FreemapMobile app)
  {
    this.store=store;
    this.thresholdDistance=thresholdDistance;
    this.app=app;
	curImage=null;
    photoCanvas = new Canvas()
	{
		public void paint(Graphics g)
		{
			if(curImage!=null)
			{
				g.drawImage(curImage,0,0,Graphics.TOP|Graphics.LEFT);
			}
		}
	};
    next=new Command("Next",Command.OK, 0);
    back=new Command("Back",Command.OK, 0);
    photoCanvas.addCommand(next);
    photoCanvas.addCommand(back);
	ld=new LandmarkDisplay(Display.getDisplay(app),app);
	ld.addCommand(next);
    ld.setCommandListener(this);
    photoCanvas.setCommandListener(this);
  }
  
 
  public void getNearestAnnotation(QualifiedCoordinates location)
              throws IOException
  {
      Landmark current=null;
      nearestAnnotation=null;
      double lowestDist=thresholdDistance;
      try
      {
        Enumeration en = store.getLandmarks();
        if(en!=null)
        {
          while(en.hasMoreElements())
          {
            current=(Landmark)en.nextElement();
            if(location.distance(current.getQualifiedCoordinates())<lowestDist)
            {
              lowestDist=location.distance(current.getQualifiedCoordinates());
              nearestAnnotation=current;
            }
          }
        }
        if(nearestAnnotation!=null)
        {
			app.getCanvas().setAnnotation(nearestAnnotation);
			app.getCanvas().setCommandListener(this);
			app.removeDefaultCommands();
			app.getCanvas().addCommand(next);
			app.getCanvas().addCommand(back);
		   try
			{
           	curImage = Image.createImage(PhotoLoader.getImage
				(nearestAnnotation.getName().substring(1)),
				0,0,photoCanvas.getWidth(),photoCanvas.getHeight(),
				Sprite.TRANS_NONE);
			}
			catch(IOException e)
			{
				curImage=null;
			}
           ld.load(nearestAnnotation,
                                          lowestDist/1000,
                          GProjection.direction
					             (location.getLongitude(),
					               location.getLatitude(),
					               nearestAnnotation.getQualifiedCoordinates().  
                              getLongitude(),
					               nearestAnnotation.getQualifiedCoordinates().
                         getLatitude()));
            ld.show();
        }
      }
      catch(IOException e)
      {
        throw e;
      }
    }
    
    
    
    
    
  public void commandAction(Command c, Displayable s)
	{
	   if(c==back)
	   {
		app.getCanvas().setAnnotation(null);
		app.getCanvas().setCommandListener(app);
		app.getCanvas().removeCommand(next);
		app.getCanvas().removeCommand(back);
		app.addDefaultCommands();
 		app.handleBackCommand();
     }
     else if (c==next)
     {
  		  if(s==ld)
  		  {
  		    Display.getDisplay(app).setCurrent
				(curImage==null ? app.getCanvas() : photoCanvas);
		  }
	  	  else if(s==photoCanvas)
	  	  {
			    Display.getDisplay(app).setCurrent(app.getCanvas());
          }
	  	  else if (s==app.getCanvas())
	  	  {
			    Display.getDisplay(app).setCurrent(ld);
          }
      }
  } 
}
