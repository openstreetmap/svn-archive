package org.openstreetmap.josm.plugins.housenumbertool;

import java.io.Serializable;

public class TagData implements Serializable
{

   /**
	 * 
	 */
	private static final long serialVersionUID = -8522743126103045878L;
	private String value;
   
   public TagData(String value)
   {
      this.value = value;
   }
   public String getValue()
   {
      return value;
   }
   public void setValue(String value)
   {
      this.value = value;
   }
   
   
   
}
