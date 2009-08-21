"Simplify" Osmosis Plugin  -- Harry Wood -  Aug 2009

This plugin filters to drop some elements in order to *simplify* the data. Currently it does one extremely crude form of simplification. It drops all nodes apart from the start and end nodes of every way.


So  .---.---.---.---.---.   becomes .------------------.

The bad news is, this is obviously completely destructive for most uses of map data. The good news is it reduces file sizes a lot (far fewer nodes and far fewer references to nodes within ways)

For certain types of analysis the data left behind is still perfectly useful. The particular use it was created for, was as a first step for analyzing motorway data across the whole of the US, checking for routing problems.

It could be extended to do more sophisticated types of simplification. For starters it could maintain better topological connectivity by keeping any mid-nodes of a way which are shared with any other way.


== Installation ==

Download the simplifyPlugin.jar file from ...somewhere  or run the ant build.xml to create it afresh under a new 'build' directory.

Place simplifyPlugin.jar in your osmosis 'lib' directory where it will be automatically added to your osmosis classpath. That's if you're using the bash script on linux. On windows you have fiddle with osmosis.bat to get it added to your classpath.

== Running ==

Reference the plugin in your command line arguments as follows

"-p SimplifyPlugin"  to tell osmosis to load the plugin (You dont need to do this every time if you set it in OSMOSIS_OPTIONS instead)

and later on...
 
"--simplify"    This invokes the task, and so this argument must appear somewhere inbetween "read-xml" and "write-xml". The task does't take any parameters (not until we develop it to do other stuff)