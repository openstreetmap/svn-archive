package mappaint;
import java.awt.Color;

public class LineElemStyle extends ElemStyle
{
	int width;
	int realWidth = 0; //the real width of this line in meter
	Color colour;
	boolean dashed = false;

	public LineElemStyle (int width, int realWidth, Color colour, boolean dashed, int maxScale, int minScale)
	{
		this.width = width;
		this.realWidth = realWidth;
		this.colour = colour;
		this.dashed = dashed;
		this.maxScale = maxScale;
		this.minScale = minScale;
	}

	public int getWidth()
	{
		return width;
	}

	public int getRealWidth()
	{
		return realWidth;
	}

	public Color getColour()
	{
		return colour;
	}

	@Override public String toString()
	{
		return "LineElemStyle:  width= " + width + "realWidth= " + realWidth +  " colour=" + colour + " dashed=" + dashed;
	}
}
