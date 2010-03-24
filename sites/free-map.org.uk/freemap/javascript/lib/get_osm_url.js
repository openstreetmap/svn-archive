
// From informationfreeway.org

function get_osm_url (bounds) 
{
   var xyz = new Array();
   var res = this.map.getResolution();
   xyz.x = Math.round ((bounds.left - this.maxExtent.left) / 
   (res * this.tileSize.w));
   xyz.y = Math.round ((this.maxExtent.top - bounds.top) / 
   (res * this.tileSize.h));
   xyz.z = this.map.getZoom();


    var limit = Math.pow(2, xyz.z);



 if (xyz.y < 0 || xyz.y >= limit)
    {
         return null;
    }
    else
      {
            xyz.x = ((xyz.x % limit) + limit) % limit;

               var path = xyz.z + "/" + xyz.x + "/" + xyz.y + "." + this.type; 
			   
                                                                                               var url;
																							   if(xyz.z>=10)
																								{
                  url="http://www.free-map.org.uk/cgi-bin/render2?"+
                     "x="+xyz.x+"&y="+xyz.y+"&z="+xyz.z;
				  	/*
                  url="http://www.free-map.org.uk/freemap/images/tiles2/"+
                     "/freemap/"+path;
					 */
																								}
																								else
																								{
																									url="http://tile.openstreetmap.org/mapnik/"+path;
																								}
																							   return url;
                                                                                    }
}
