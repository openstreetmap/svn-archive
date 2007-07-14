package mappaint;

import java.util.HashMap;
import java.util.Iterator;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
public class ElemStyles
{
	HashMap<String, ElemStyle> styles;

	public ElemStyles()
	{
		styles = new HashMap<String, ElemStyle>();
	}

	public void add (String k, String v, ElemStyle style)
	{
		String key = k + "=" + v;
		
		/* unfortunately, there don't seem to be an efficient way to */
		/* find out, if a given OsmPrimitive is an area or not, */
		/* so distinguish only between way and node here - for now */
		if(style instanceof AreaElemStyle) {
			key = key + "way";
		}
		else if(style instanceof LineElemStyle) {
			key = key + "way";
		}
		else if(style instanceof IconElemStyle) {
			key = key + "node";
		}
		styles.put(key, style);
	}

	public ElemStyle getStyle (OsmPrimitive p)
	{
		if(p.keys!=null)
		{
			String classname;
			
			if(p instanceof org.openstreetmap.josm.data.osm.Node) {
				classname = "node";
			} else {
				classname = "way";
			}
			Iterator<String> iterator = p.keys.keySet().iterator();
			while(iterator.hasNext())	
			{
				String key = iterator.next();
				String kv = key + "=" + p.keys.get(key) + classname;
				if(styles.containsKey(kv))
				{
					return styles.get(kv);
				}
			}
		}
		return null;
	}

	public boolean isArea(OsmPrimitive p)
	{
		return getStyle(p) instanceof AreaElemStyle;
	}
}
