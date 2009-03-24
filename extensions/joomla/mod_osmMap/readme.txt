Joomla!Extension: mod_osmMap

Compatible with Joomla 1.5 ONLY
thanks to Benjamin Hättasch 
created and freely modified based on: http://joomlacode.org/gf/project/mod_googlemap/

Installation Instructions:

Creating the module from the source:
Take all directories and files under the root of this module and zip it into 
a file called mod_osmMap-<versionnumber>.zip



Extract the mod_osmMap Installer archive out of this archive, 
but don't unpack it. In the Joomla! administration area, 
go to: Extensions > Install/Uninstall. 
In the upload box, then select the archive containing mod_osmMap on your HD, 
select it, and click "Upload and Install." After installation, enable the Module from: 
Extensions > Moduele Manager > mod_osmMap 

Before you can use the module, you will have to change the settings of the 
Module. 

Settings: 
Attention: This Setting is important, without this, your module won't work: 


Now the Map should work, using the other default settings. 
But such a map wouldn't make any sense. So you should reset the next Values, to get your own map. 

JOOMLA MODULE ADMINISTRATION

First option block: 
	Placement of the map. 
	With this you can set the height and the width of the map. 
	and the id that will be assigned to the div that contains the map for later reference.

Second option block: Place at the map 
	You can set Latitude and Longitude or X and Y of the centerpoint of the map. 
	X and Y can be used with the “NL” maptype, lat/lon with the WGS84 maptype (Global)
	More maptypes and better placement options will be included in the next version. 
	Secondary you can set the zooming level. 
	You should play a little bit with this to find the best level.

Third option block: 
Currently two maptypes are supported: 
 -> NL – Rijksdriehoeksstelsel (X and Y in meters) for the Netherlands. 
    Connects to the openstreetmap tileserver at http://nl.openstreet.nl/
and 
 -> WGS84 – Global (lat,lon) in degrees for the rest of the world. 
    Connects to the openstreetmap tileserver at http://www.openstreetmap.org. 
    Coordinates are entered in lat/lon; the map is projected in the same projection as the google maps.

Fourth option block: 
You can turn the openlayers (http://www.openlayers.org) 
controls on and off which will give you everything from a static, 
non moving map (everything off) to a map with movement control. 
Play around with these options to see the effects on the map.

Fifth option block: 
You can show a “Reset” link under the map which will jump back to the maps default settings.

This module is open for contribution. 
If you feel something should change, 
or functions need to be added for you, 
please contact me at: milo@opengeo.nl

Enjoy this module!

Kind regards,

Milo van der Linden
Stichting OpenGeo: Foundation for stimulating free Geodata and Geotools for everyone.
http://www.opengeo.nl
http://www.openstreetmap.org
http://www.openlayers.org


 

