package mappaint;

abstract public class ElemStyle
{
	// zoom range to display the feature
	protected long minScale;
	protected long maxScale;

	public long getMinScale()
	{
		return minScale;
	}
	public long getMaxScale()
	{
		return maxScale;
	}
}



