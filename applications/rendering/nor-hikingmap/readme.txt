
Norwegian hiking map
====================

Mapnik style sheets that implements conventions often seen in
Norwegian hiking and nordic skiing maps. The separate style sheets
for winter and summer, that feature different colour schemes.

The summer map is a hiking map and includes support for these tags:

trailblazed=yes (and marked_trail=blue for compatibility)
trail_visibility=* -- indistinct trails are shown with open dashing
tracktype=grade1-2 -- often used on "trillestier"
tracktype=grade3-5 -- tracks from tractors or logging machines

The winter map tones down paths and instead shows skiing tracks:

piste:type=nordic|downhill
piste:difficulty=*
piste:grooming=*

Additionally, hiking related POIs are shown from lower zoom levels.
Examples are trailhead parking lots "utfartsparkering";
amenity=parking with hiking=yes, sports chapels;
amenity=place_of_worship with hiking=yes, serviced and unlocked cabins
and emergency shelters, lean-tos and viewpoints.

The style sheets are modular, making reuse and customization easier.

Maintained by Vidar Bronken Gundersen (vibrog)


Cartography and symbols
=======================

The cartography in these maps are influenced by hiking maps published
by Asker Skiklubb (http://asker-skiklubb.no/), which are essentially
orienteering maps that have been cleaned up visually, and added POIs,
place names and highlighting of skiing tracks and marked hiking trails.

Symbols are taken from a standard created by the Norwegian mapping
authorities, that are now freely available, and used with permission.

  Symbolfonter for friluftsliv og sport
  [en:Symbol fonts for recreation and sport] (1997). Statens kartverk
  Landkartdivisjonen. ISBN 82-90408-52-8, Retrieved from
  http://www.statkart.no/filestore/Standardisering/docs/symbol.pdf

This standard defines CMYK colour values for symbol classes.
The RGB equivalents used:

#d0006f (purple) -- activities
#007d33 (green)  -- nature areas
#b21416 (red)    -- culture and entertainment
#007db4 (blue)   -- services and transport


Contours or hillshading
=======================

Terrain and elevation are normally shown using contour lines and/or
hillshading in hiking maps. Norway are affected by the fact that
the SRTM DEM stops at 60.4° north, which includes "Nordmarka".
viewfinderpanoramas.com and ASTER offer more coverage, but
legal restrictions on reuse of these data sets are unclear.


Installation and usage
======================

The style sheets have been tested with Mapnik 0.7.1 and 0.6.
Assume the inc folder from the standard OSM Mapnik style sheet.
It is suggested to create a symlink to a checkout of this.

Import OSM data using the custom osm2pgsql style
hiking.osm2pgsql.style

Point your generate_tiles.py script or mod_tile config
to hikingmap.xml and/or pistemap.xml


TODO
====

sport=* symbols from zoom 17
tourism=guest_house, tourism=alpine_hut
motorsport
leisure=fishing
clay pigeon, pistol
"post i butikk"
natural=stone
text: place=locality|farm|isolated_dwelling|island|islet|suburb
text: road names from zoom=15/16
man_made=pier
highway=turning_circle or (highway=* and area=yes)
barrier=gate|rift_gate|bollard|cycle_barrier|stile|windfall
