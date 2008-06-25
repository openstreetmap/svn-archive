
import javax.microedition.lcdui.*;

public class AnnotationForm extends Form implements CommandListener
{
  TextField details;
  ChoiceGroup type; 
  FreemapMobile app;
  Command back, ok;
  
  public AnnotationForm(FreemapMobile app)
  {
    super("Add details");
    this.app=app;
    details=new TextField("Add details","",256,TextField.ANY);
    append(details);
    type=new ChoiceGroup("Type",Choice.POPUP);
    type.append("directions",null);
    type.append("info",null);
    type.append("hazard",null);
    append(type);
    
    back=new Command("Back",Command.BACK,0);
	  addCommand(back);
	  ok=new Command("OK",Command.OK,0);
	  addCommand(ok);
	  this.app=app;
	  setCommandListener(this);
  }
  
  
    // the code to respond to commands
    public void commandAction(Command c, Displayable s)
    {
        // Just call the parent's handleBackCommand()
		  if(c==back)
		  {
			   app.handleBackCommand();
		  }
		  else if (c==ok)
		  {
			System.out.println(details.getString());
			System.out.println(type.getSelectedIndex());
		    app.sendAnnotation(details.getString(),
                          type.getString(type.getSelectedIndex()));
      }
    }
  
}
