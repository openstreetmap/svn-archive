import javax.microedition.lcdui.*;

public class RadioButtons extends ActionList 
{
	Command ok;
	int selected;

	public RadioButtons(String title,Parent parent,Display display)
	{
		super(title,parent,List.EXCLUSIVE,display);
		ok=new Command("OK",Command.OK,0);
		list.addCommand(ok);
	}

	public void commandAction(Command c,Displayable s)
	{
		System.out.println("RadioButtons.CommandAction");
		if(c==back)
		{
			parent.handleBackCommand();
		}
		else if (c==ok)
		{
			selected=list.getSelectedIndex();
			System.out.println("Chose radio button: "+ selected);
			MenuAction action = (MenuAction)(menuActions.elementAt(selected));
			action.action(selected);
		}
		else if (c==List.SELECT_COMMAND)
		{
		}
	}
	public void action(int i)
	{
		System.out.println("RadioButtons.action: i="+i);
		super.action(i);
	}
}
