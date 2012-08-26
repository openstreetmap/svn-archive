
OSM2GTFS - Very basic program to help create GTFS data from fully defined OSM public transit routes

By: Mike N niceman@att.net

License: This program is released to the public domain


Written with Visual Studio 2008 - should work with VS 2010


Known current limitations -
   Only processes Bus routes
   Expects stops to be a single node.  Stops which are ways will be ignored: possible enhancement  - this program could find the 
   center of a closed way and create a node.
   Feature 'frequencies.txt' (Stop times are not fixed) checkbox has not been implemented, and is ignored.
   
There are many assumptions and pitfalls today.   The code is not the cleanest, and doesn't always gracefully handle and diagnose unexpected data input format.

The assumption is that GTFS data will be created once with this program, then the GTFS data will only be edited by the transit operator
using an industry standard GTFS maintenance tool.   Therefore, multiple edit sessions are awkward (see below).  A full relational DB would
be the proper solution to make the program more robust across multiple edit sessions as OSM data is further modified.

Steps:

1. OSM Data

Public routes defined using the Oxomoa scheme.  For best results, define using the JOSM Public Transport plugin:  
http://wiki.openstreetmap.org/wiki/JOSM/Plugins/public_transport

 - All routes should have a route_master master relation It is required today by this program, even though strictly speaking, a route that never retraces itself 
 wouldn't need a route master.
 
 - Each route relation member should contain the route path as well as any stops next to that part of the route.
 
 - Each route path 'way' should have a role of 'forward' or 'backward' to define the travel path.   
     The Public Transport plugin can automatically
   assign the correct roles once the first segments have been defined with a forward/backward role.   
   ** After all ways have been assigned, check to be sure it follows the route travel direction, from top to bottom.  Use the
   'Reflect' button if necessary to reverse the path.
   - The first assigned time point should be the first way of one of the route relations
 
 - With the Public Transport Plugin, for each route, 
	select the Route Patterns tab
	Select the route to check
	Click on the Itinerary tab.  
	If there are any gaps, select the SORT button.   The gaps should disappear.   Save/upload if changed.   
	   NOTE: Editors don't always maintain relation sort order.   This is fine, as long as the plugin can piece the route together, this
	   program will also be able to piece the route together.
	   
 - Optional - the stops should all have descriptive OSM 'name' tags. For example "Main Street @ Oak Street".   
  If there is no name, it will just assign an ID number for OpenTripPlanner to use.
	   
- The order of ways in the .OSM file and relations do not matter.  This program will sort them.
- The order of stops in the .OSM file and relations do not matter.   This program will sort them.

- The order of route_master relation members is important - the first route relation member should contain the first timed stop point.
  Retaining sort order in OSM relation members is problematic - see procedure Form1.SortRouteMasterMembers() to adapt to your data if you can 
  define the first route member programatically - for example pick the member identified as direction=outbound , etc.

- Create an OSM data file (XML format) that includes the routes and stops.   All data is loaded into memory at runtime, so it is good to 
   work with a small area (less than 100-200 Megabytes).

2. Working Directory Structure

 folder - .OSM data file
   |----OSM_2_GTFS  (The program automatically creates this below the .OSM file)
      |----Backup Contains multi-generational backup.   In some cases, you might need to 
        recover files from here back 1 or 2 revisions to retrieve the last schedule if there is an error.
      
3. Typical work flow.
 
  - Place .OSM data file into folder.
  - Run program
  - Press the "Browse" button to select the .OSM file.
  - Press the "Load GTFS" button to load the GTFS data from the OSM file.   The program may call out errors at this time if
    it is unable to determine the bus route with contiguous OSM way connections.
      - The program will create and overwrite routes.txt, stops.txt, and shapes.txt 
  - Press "Setup time schedules"
		- Routes are listed in the left column.  As each route is highlighted, the stops for that route are shown on the grid.
  - Check that one or more GTFS Services have been defined.  This is typically something like 'Weekday', 'Weekend', 'Saturday', or 'Sunday'.
    To add a new service, enter it in the box above "Add Service", then press "Add Service".  Click the days the service applies to.
    
    Now the total number of schedules to be created is nRoutes * nServices.   If there are 20 routes and 3 services, as many as 60 time grid schedules should be entered.
     
    To work with the time grid:
      - Enter the start times for each trip on the first stop on the left for the day.
      - Enter the clocked times for each stop in the top row.  Note: leave unknown times blank.  GTFS requires an end time for the last stop.
      - Press "Fill Time Rectangle" to duplicate the relative time schedule for the rest of the day.
      - Copy and paste of a column or rectangular block of times can be used between schedule pages or Excel.
      - Take care not to paste beyond the last column to the right.   
      - Pasting more rows than currently defined will automatically add rows.

  When finished, press "Save All" to save the rest of the GTFS files.

  The program may be closed.  Use the Google Transitfeed validator to check the validity of the files.
  
  Repeat all steps here if the .OSM data is corrected, or to add more time schedules.


4. CAUTION CAUTION -

  The stop times and route travel distance are assigned only as the stop times are read from the time grid.   
  They are not loaded from the files during a re-edit.
  To resume an edit session, just scroll through and highlight each route on the left side once when resuming the session.  
  All routes and stop times are shown.  
  It is not necessary to highlight each service as well - only the routes.
  
  DATA CAN BE LOST IF THIS STEP IS NOT FOLLOWED WHEN RE-OPENING THE PROGRAM TO CONTINUE AN EDIT SESSION.
  
        
5. GTFS Files   
   
agency.txt <--- You create with an editor
calendar_dates.txt <--- Optional, create with an editor
fare_attributes.txt <--- Optional, create with an editor
fare_rules.txt <--- Optional, create with an editor
google_transit.zip <--- Optional, create as a zip file for use by OpenTripPlanner, just a zip file of all .txt files

routes.txt <---  Automatically created by program at run time from OSM data.
shapes.txt <---  Automatically created by program at run time from OSM data.
stops.txt <-- Automatically created by program at run time from OSM data.

stop_times.txt <-- Created by program at runtime, saved with schedule
trips.txt <-- Created by program at runtime, saved with schedule
calendar.txt <-- Created by program at runtime, saved with schedule

      