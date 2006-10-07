package mappaint;
import java.awt.Color;

public class LineElemStyle extends ElemStyle
{
	int width;
	Color colour;

	public LineElemStyle (int width, Color colour)
	{
		this.width = width;
		this.colour = colour;
	}

	public int getWidth()
	{
		return width;
	}

	public Color getColour()
	{
		return colour;
	}

	@Override public String toString()
	{
		return "LineElemStyle:  width= " + width +  " colour=" + colour;
	}
}
