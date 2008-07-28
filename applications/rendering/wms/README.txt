OSM WMS
=======



Overview
--------

This script simply tries to implement a OGC-compliant WMS service.

By now, there is only one available layer, with very basic rendering. This WMS server makes use of the proj.4 library to serve the data in a variety of coordinate systems: EPSG 4326, as well as all the UTM zones and the (infamous) EPSG:900913 are available.



Usage
-----

Simply copy all files to a PHP-enabled web server, and point your favorite WMS client to wms.php.

In order to coordinate systems other than EPSG 4326 to work, however, the proj4 library has to be installed in your system, and the "cs2cs" executable must be in the default path. In most linux distributions, this means that you just have to install the "proj" package.



Usage with osmarender
---------------------

In order for the osmarender layer to work, you'll need:
- A tweaked osmarender install, as provided
- Perl and the perl modules needed for osmarender to work
- Inkscape
- Create a .gnome2 directory in the www-data home directory (usually /var/www) - without this, inkscape won't work

The tweaks applied to osmarender are:
- Neutralize the code that projects the data into epsg:900913 AKA "google's spherical mercator"
- Nuke the background rectangle



Legal stuff
-----------

This is free software. Please do read the LICENSE file for more details. The software is (C) 2008 Iván Sánchez Ortega <ivan@sanchezortega.es>


This software uses DejaVu fonts, which are freely redistributable. Please do read http://dejavu.sourceforge.net/wiki/index.php/Main_Page for more details.

