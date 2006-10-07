package mappaint;
import javax.swing.ImageIcon;

public class IconElemStyle extends ElemStyle
{
	ImageIcon icon;
	boolean annotate;

	public IconElemStyle (ImageIcon icon, boolean annotate)
	{
		this.icon=icon;
		this.annotate=annotate;
	}	
	
	public ImageIcon getIcon()
	{
		return icon;
	}

	public boolean doAnnotate()
	{
		return annotate;
	}

	@Override public String toString()
	{
		return "LineElemStyle:  icon= " + icon +  " annotate=" + annotate;
	}
}
