// Manages the whole annotation-adding process, from sending an annotation
// to the server to (optionally) taking a picture.
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.control.GUIControl;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class AnnotationManager implements CommandListener
{
	FreemapMobile app;
	double lat,lon;
	Command back, ok;
	VideoControl videoControl;
	String id;
	TextField details;
	ChoiceGroup type;

	class AnnotationForm extends Form
	{
  		public AnnotationForm()
  		{
    		super("Add details");
    		details=new TextField("Add details","",256,TextField.ANY);
    		append(details);
    		type=new ChoiceGroup("Type",Choice.POPUP);
    		type.append("directions",null);
    		type.append("info",null);
    		type.append("hazard",null);
    		append(type);
	  		addCommand(back);
	  		addCommand(ok);
	  		setCommandListener(AnnotationManager.this);
		}
	}

	class CameraYesNoForm extends Form
	{
		public CameraYesNoForm()
		{
			super("Take picture?");
			append("Take picture?");
	  		addCommand(back);
	  		addCommand(ok);
	  		setCommandListener(AnnotationManager.this);
		}
	}

	// Reference: http://developers.sun.com/mobility/midp/articles/picture/


	class CameraTestForm extends Form 
	{
  
  		public CameraTestForm()
  		{
    		super("Camera Test");
    		back = new Command("Back", Command.BACK, 0);
    		ok = new Command("OK", Command.OK, 0);
    		addCommand(back);
    		addCommand(ok);
			setCommandListener(AnnotationManager.this);

    		try
    		{
    			Player player = Manager.createPlayer("capture://video");
    			player.realize();
   				videoControl = (VideoControl)player.getControl("VideoControl");
    			Item item = (Item)videoControl.initDisplayMode
        			(GUIControl.USE_GUI_PRIMITIVE,null);
   				append(item);
   				Display.getDisplay(app).setCurrent(this);
    		}
    		catch(MediaException e)
    		{
    			app.showAlert("Camera Media Error",e.toString(),
								AlertType.ERROR);
				app.handleBackCommand();
    		}
    		catch(IOException e)
    		{
    			app.showAlert("Camera IO Error",e.toString(),AlertType.ERROR);
				app.handleBackCommand();
    		}
  		}
	}

	AnnotationForm annotationForm;
	CameraYesNoForm cameraYesNoForm;
	CameraTestForm cameraTestForm;

	public AnnotationManager(FreemapMobile app)
	{
		this.app=app;
		back=new Command("Back",Command.BACK,0);
		ok=new Command("OK",Command.OK,0);
	}

    // For adding a new annotation or POI
    public void annotate(double lon,double lat)
    {
	  this.lat=lat;
	  this.lon=lon;
      // load up an annotation addition form
      annotationForm = new AnnotationForm();
      Display.getDisplay(app).setCurrent(annotationForm);
    }
    
    public void sendAnnotation(String annotation,String type)
    { 
       
        class AnnotationSendThread extends Thread
        {
          String annotation, type;
          
          public AnnotationSendThread(String annotation,String type)
          {
            this.annotation=annotation;
            this.type=type;
          }
          public void run()
          {
            try
            {
            
              String url="http://www.free-map.org.uk/freemap/api/markers.php?"+
                  "action=add&type="+type+"&description="+annotation+
                  "&lat="+lat+"&lon="+lon;
			  System.out.println("Sending to : " + url);
              HttpConnection conn = (HttpConnection)Connector.open(url);
   
			  // Supply authentication if we have username/password
			  /*
			  if((!(username.equals("")) && !(password.equals("")))     
			  {
				String b64=Base64Coder.encodeString
						(username+":"+password);
				conn.setRequestProperty("Authorization","Basic "+b64);	
			  }
			  */

              // from http://java.sun.com/javame/reference/apis/jsr118/
              // We're not posting anything, so it seems we can just get the
              // response code direct
              int rc = conn.getResponseCode();
			  System.out.println("Response code: " + rc);
              if(rc!=HttpConnection.HTTP_OK)
              {
                app.showAlert("Error sending annotation",
                          "The server returned a response code: " + rc,
                          AlertType.ERROR);
				
      			Display.getDisplay(app).setCurrent(annotationForm);
              }
              else
              {
                InputStream in = conn.openInputStream();
				// This won't give you the content-length
                int len = (int)conn.getLength();
				System.out.println("Content-length: " +len);
				// so we have to do it this way...
				int i;
				StringBuffer sb=new StringBuffer();
				System.out.print("*");
				while ((i=in.read()) != -1)
				{
					System.out.print((char)i);
					sb.append((char)i);
				}
				id=new String(sb);
                System.out.println("Added successfully - new ID="+id);
                app.showAlert("Annotation added successfully",
                          "Annotation added successfully, ID=" + id,
                          AlertType.INFO);

				// now give the user the option to take a photo
				cameraYesNoForm=new CameraYesNoForm();
				Display.getDisplay(app).setCurrent(cameraYesNoForm);
              }
            }
            catch(IOException e)
            {
			  app.showAlert("Error uploading",
							"Error uploading: "+ e,
							AlertType.ERROR);
      		  Display.getDisplay(app).setCurrent(annotationForm);
            }
          }
        }
        new AnnotationSendThread(annotation,type).run();
    }

	public void commandAction(Command c, Displayable s)
	{
  		if(s==annotationForm)
  		{
			if(c==back)
		  	{
			   app.handleBackCommand();
		  	}
		  	else if (c==ok)
		  	{
				System.out.println(details.getString());
				System.out.println(type.getSelectedIndex());
		    	sendAnnotation(details.getString(),
                          type.getString(type.getSelectedIndex()));
      		}
		}
		else if (s==cameraYesNoForm)
		{
			if(c==back)
				app.handleBackCommand();
			else
			{
			cameraTestForm=new CameraTestForm();
				Display.getDisplay(app).setCurrent(cameraTestForm);
			}
		}
		else if (s==cameraTestForm)
		{
    		if(c==back)
    		{
      			app.handleBackCommand();
    		}
    		else
    		{
      			try
      			{
      				byte[] imgBytes = videoControl.getSnapshot("encoding=jpeg");
					String filename = "freemap-"+id+".jpg";
					FileConnection fc = (FileConnection)
						Connector.open("file:///root1/photos/"+filename);
					fc.create();
					OutputStream os = fc.openOutputStream();
					os.write(imgBytes);
					fc.close();
					os.close();
      			}
				catch(MediaException e)
				{ 
					app.showAlert("Error capturing photo",e.toString(),	
								AlertType.ERROR);
				}
				catch(IOException e)
				{ 
					app.showAlert("Error saving photo",e.toString(),	
								AlertType.ERROR);
				}
    		}
	   	}
    }
}
