import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.location.*;
import java.util.Vector;


public class ActionList implements CommandListener, MenuAction,Parent
{
	protected List list;
	protected Vector menuActions;
	protected Parent parent;
	protected Display display;
	protected Command back;

    public ActionList (String title,Parent parent,
						int type,Display display)
    {
        
   		list=new List(title,type);     
   		list.setCommandListener(this); 
		menuActions=new Vector();
		this.parent=parent;
		this.display=display;
		back=new Command("Back",Command.BACK,0);
		list.addCommand(back);
    }

	public List getList()
	{
		return list;
	}

	public void clear()
	{
		list.deleteAll();
		menuActions.removeAllElements();
	}
	public String getEntry(int i)
	{
		return list.getString(i);
	}

	public void setEntry(int i,String entry)
	{
		list.set(i,entry,null);
	}

	public void addItem (String item, MenuAction m)
	{
		list.append(item,null);
		menuActions.addElement(m);
	}

	// defined by MenuAction
	public void action(int i)
	{
		display.setCurrent(list);
	}
	   
 
    // the code to respond to commands
    public void commandAction(Command c, Displayable s)
    {
	
        // What command type is it?
		if(c==back)
		{
			parent.handleBackCommand();
		}
		else if (c==List.SELECT_COMMAND)
		{
			int i = list.getSelectedIndex();
			MenuAction action = (MenuAction)menuActions.elementAt(i);
			action.action(i);
        }    
    }
		
	public void handleBackCommand()
	{
		display.setCurrent(list);
	}
}

