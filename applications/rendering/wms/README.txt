OSM WMS
=======



Overview
--------

This script simply tries to implement a OGC-compliant WMS service.

By now, there is only one available layer, with very basic rendering, and only one projection (EPSG 4326)



Usage
-----

Simply copy all files to a PHP-enabled web server, and point your favorite WMS client to wms.php.

In order to coordinate systems other than EPSG 4326 to work, however, the proj4 library has to be installed in your system, and the "cs2cs" executable must be in the default path. In most linux distributions, this means that you just have to install the "proj" package.



Legal stuff
-----------

This is free software. Please do read the LICENSE file for more details. The software is (C) 2008 Iván Sánchez Ortega <ivan@sanchezortega.es>


This software uses DejaVu fonts, which are freely redistributable. Please do read http://dejavu.sourceforge.net/wiki/index.php/Main_Page for more details.

