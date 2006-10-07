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
		styles.put(key, style);
	}

	public ElemStyle getStyle (OsmPrimitive p)
	{
		if(p.keys!=null)
		{
			Iterator<String> iterator = p.keys.keySet().iterator();
			while(iterator.hasNext())	
			{
				String key = iterator.next();
				String kv = key + "=" + p.keys.get(key);
				if(styles.containsKey(kv))
				{
					return styles.get(kv);
				}
			}
		}
		return null;
	}
}
