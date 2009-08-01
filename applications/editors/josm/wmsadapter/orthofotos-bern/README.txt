----------------------------------------------------------------------------
                     WMS Adapter for Orthofotos of Bern
----------------------------------------------------------------------------


This adapter translates JOSMs tile requests for orthofotos of Bern to tile
requests which can be handled by the WMS server of  Bern.

The adapter is a small web application which runs on your local computer.  


   +--------+                   +-------------+              +-----------+
   |  JOSM  |   tile request    | WMS Adapter |  tile req    | City Map  |                  
   |        | --------------->  |    for      | -----------> | of Bern   |
   |        |        tile       | Orthofotos  |     tile     |           |
   |        |  <-------------   |   of Bern   | <----------  |           |
   +--------+                   +-------------+              +-----------+

The adapter is responsible
o  for maintaining a valid session with the WMS server of Bern
o  translating lat/lon-coordinates in WGS84 to x/y-coordinates of CH1903

Limitations:
o although I tried to mimic the behaviour of a standard browser like Firefox as
  closely as possible I wasn't able to automatically retrieve a valid session
  ID from the WMS server of Bern. Session IDs retrieved automatically 
  timed out immediately.
  
  You therefore have to configure the WMS adapter with a valid session ID which
  you have to retrieve from the WMS server of Bern using your preferred web
  browser (see below in section Usage). 
   

INSTALLATION
------------

o  Download the latest orthofoto-bern-wms-adapter-<version>.zip from
   http://www.guggis.ch/orthofoto-bern-wms-adapter/ 

o  Unzip orthofoto-bern-wms-adapter-<version>.zip

   

CONFIGURING JOSM 
----------------
o Add the WMS adapter as WMS server 
  - press F12 to launch the configuration dialog
  - select the configuration screen for WMS server
  - add an entry with
       menu name = Orthofotos Bern 
       WMS-URL   = http://localhost:8787/orthofotos-bern?    !!! Note the trailing '?' !!!

             

USAGE 
------------
o Start the WMS adapter 

   c:\> java -jar winstone-0.9.10.jar --warfile=orthofoto-bern-wms-adapter.war 

   Use --httpPort=<port> to set another port the adapter is listening too.
 
o Get a valid session ID

   - Launch your browser and point it at
     http://www.stadtplan.bern.ch/TBInternet/default.aspx?User=1
    
   - View the current set of cookies in your browser and copy the cookie
        for domain  stadtplan.bern.ch
        with name   ASP.NET_SessionId 
     to the clipboard
     
o Configure WMS adapter with the session ID
   - Point your browser at 
        http://localhost:8787/orthofotos-bern
   - Enter the Session ID retrieved in the previous step
     and click on "Submit"
     
o Use JOSM
  - You may now use JOSM to retrieve orthofotos of Bern
     
   
    