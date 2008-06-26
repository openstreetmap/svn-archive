
import javax.microedition.lcdui.*;

public class LoginForm extends Form implements CommandListener
{
  TextField username,password; 
  FreemapMobile app;
  Command back, ok;
  
  public LoginForm(FreemapMobile app)
  {
    super("Login");
    this.app=app;
    username=new TextField("Username","",256,TextField.ANY);
    append(username);
    password=new TextField("Password","",256,TextField.PASSWORD);
    append(password);
    
    back=new Command("Back",Command.BACK,0);
	addCommand(back);
	ok=new Command("OK",Command.OK,0);
	addCommand(ok);

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
		    app.login(username.getString(),password.getString());
         }
    }
}
