package mappaint;
import javax.swing.ImageIcon;

public class IconElemStyle extends ElemStyle
{
	ImageIcon icon;
	boolean annotate;

	public IconElemStyle (ImageIcon icon, boolean annotate, int maxScale, int minScale)
	{
		this.icon=icon;
		this.annotate=annotate;
		this.maxScale = maxScale;
		this.minScale = minScale;
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
		return "IconElemStyle:  icon= " + icon +  " annotate=" + annotate;
	}
}
