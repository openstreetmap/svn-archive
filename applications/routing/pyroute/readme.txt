Quick start:

Just type "python route.py data.osm node1 node2 cycle" from command line
where node1 and node2 are nodes in the data.osm file.


Dependancy graph:

 +-- loadOsm.py: parses OSM files and stores them in memory
   |             also creates tables of routable segments 
   |
   +-- route.py: routes though data using A*
     |
     +-- routeAsGpx.py: command-line utility to do a route
     |                  and save it as a GPX file
     |
     +-- routeAsOsm.py: same thing, but saves as OSM XML file
     |
     +-- gui.py: experimental GUI for mobile applications

 +-- pyroute.py: original version of the routing program, with
                 everything in one file.  Outputs to a PNG image
                 showing the map, the route, and debugging

The 'library' programs can be run from the command-line too
* loadOsm will load a file and tell you statistics about
  the routes available inside it
* route will do routing from the command-line and display it
  as a list of nodes
