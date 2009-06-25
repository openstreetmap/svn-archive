Introduction
============

This code was an attempt at generating 'most photographed' heatmaps using the flickR API.
The code has been donated in the hope that the OpenStreetMap community can find uses for it in
other projects. I've separated out the various functions (location gathering, plotting and KML).

Currently the data source is flickR, and output pngs have *manually* been merged with OSM exports
(or displayed in KML using the supplied class). 

However I hope to remedy this and download appropriate images from OSM, maybe using PIL to do the overlay. 
Any pointers welcome!

I'm new to FOSS and OSM, so any style/coding suggestions are welcome :)

Quick Tour
==========
Location.py - a generic 'point marker'

locationFinder.py - responsible for generating a list of Location objects of interest.
This is a generic class which should be sub-classed for other applications.

locationFinder_flickr_locations.py - an example sub-class, implementing a flickR API geo query.
(*Needs flickR API key*)

heatmapPNGgenerator.py - generates a heatmap using matplotlib and a supplied locationFinder object.
Saves png file to given location.

KMLGenerator.py - given a locationFinder, a PNG file and a filename for the KML, saves a KML file with
an image overlay and placemarkers for the individual locations.

heatmappr.py is an end-to-end example/demo. 

Prerequisites
=============

These are the libraries you'll need to install
your mileage may vary if you use older libs than this.

Matplotlib 0.98.5.2 (http://matplotlib.sourceforge.net/users/installing.html)
Numpy 1.3.0

You'll need to be using Python 2.5 or later.

If you wish to try using the example (which uses the flickR API) you will need
to get yourself a flickR API key. If you're a flickR user, visit the following URL..

http://www.flickr.com/services/api/keys/apply/

..to apply for a key. 

Be careful not to commit a copy of locationFinder_flickr_locations.py with
a copy of your API, as it this will breach the flickr api TOS.

Known Issues
============
A number of asserts have been put in place to cope with known issues.

Google Earth Issues with KML overlays
========================
My version of GE for testing is 5.0.11733.9347

Sometimes, the photo overlay does not appear, but the pushpins do. 

If this happens, first try this:-

[1] Right-click on the file in the places palette, then choose 'Revert'. This reloads the file.
    You should also do this if you change the .kml file and want to see the changes in GE.
[2] If that doesn't work, right click the file in places palette, and choose 'Delete'. This doesn't delete
    the file, but simply removes it from the 'My Places' or 'My Temporary Places' folder. 
    Then open the file again.
[3] If that doesn't work, try restarting GE.
