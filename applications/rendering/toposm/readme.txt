TopOSM README

Lars Ahlzen
lars (at) ahlzen.com

TODO: Improve! 


Requirements:
-------------
* Standard UN*X tools (bash, sed, ...)
* Mapnik
* PostgreSQL
* _int.sql (from postgresql-contrib) for PostgreSQL
* PostGIS
* 900913.sql projection (from osm2pgsql) for PostGIS
* Python 2.5/2.6, with:
  - Numpy
  - Mapnik
* Imagemagick
* GDAL/OGR, including software (gdal_translate, gdalcontour etc.)
* osm2pgsql (for planet import)
* shp2pgsql (for NHD import)
* Perrygeo DEM tools (hillshade, color-relief)
  See: http://www.perrygeo.net/wordpress/?p=7
* gcc, g++ (for building PerryGeo tools)
* OptiPNG (for tile optimization)

Data needed:
------------
* builtup_area.shp (from OSM svn, mapnik)
* processed_p.shp (from OSM svn, mapnik)
* world_bnd_m.shp (from OSM svn, mapnik)
* USGS NED 1/3" http://openstreetmap.us/ned/13arcsec/grid/
  (you can use the supplied get_ned script)
* USGS NHD http://www.openstreetmap.us/nhd/
* Planet.osm http://planet.openstreetmap.org/

Setup
-----
* Build/install requirements (see above).
* Download required data sets.
* Setup PostgreSQL with PostGIS. See OSM wiki.
* Create a 'gis' database.
* Modify and run import_planet.
* Modify and run import_nhd.
* Modify and run prep_contours_table.
* Modify include/dbsettings.inc.
* Modify toposm.py.

Render an area:
---------------
This example will render the area UTM19T (defined in areas.py) from
zoom level 4 through 16:

$ python
Python 2.6.5 (r265:79063, Apr 16 2010, 13:57:41)
[GCC 4.4.3] on linux2
Type "help", "copyright", "credits" or "license" for more information.
>>> from toposm import *
>>> from areas import *
>>> prepare_data_single(UTM19T, 10, 16)
...
>>> render_tiles_single(UTM19T, 5, 16)
...
$ optimize_png.py tiles/contours
...
$ optimize_png.py tiles/features
...


Notes:

Make sure that you have plenty of disk space for temporary
files and tiles, and database space for contour lines.

The prepare_data* methods will "expand" the area to cover a full tile
at the lowest zoom level. This is why the example prepares zoom levels
10 through 16. Otherwise the area may be unnecessarily large.

The *_single methods are single-threaded versions. The multi-threaded
code appears to still suffer from a memory leak. For multi-core machines,
a workaround is to run separate simultaneous instances rendering
different areas.

Don't run prepare_data* on the same area twice or on overlapping
areas. You'll end up with duplicated contour lines, which is
not pretty.

In TopOSM, rendering quality takes precedence over speed. You'll
notice.

