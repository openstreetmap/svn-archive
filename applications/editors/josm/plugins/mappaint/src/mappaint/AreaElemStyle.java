package mappaint;
import java.awt.Color;

public class AreaElemStyle extends ElemStyle
{
	Color colour;

	public AreaElemStyle (Color colour, int minZoom)
	{
		this.colour = colour;
		this.minZoom = minZoom;
	}

	public Color getColour()
	{
		return colour;
	}

	@Override public String toString()
	{
		return "AreaElemStyle:   colour=" + colour;
	}
}
