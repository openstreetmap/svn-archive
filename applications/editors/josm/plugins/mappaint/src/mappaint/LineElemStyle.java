package mappaint;
import java.awt.Color;

public class LineElemStyle extends ElemStyle
{
	int width;
	Color colour;

	public LineElemStyle (int width, Color colour, int minZoom)
	{
		this.width = width;
		this.colour = colour;
		this.minZoom = minZoom;
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
