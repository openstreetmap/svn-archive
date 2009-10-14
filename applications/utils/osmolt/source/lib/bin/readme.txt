NAME
      Osmolt - make simple POI-Maps

SYNOPSIS
      osmolt [options].. [ARGs]...

DESCRIPTION
      Makes a regional Point-Of-Interests-Map
      
      If you don't konw how to use Osmolt, start it without any arguments for start the GUI.
      
      
      
      Java-call: java -jar osmolt.jar [options][ARGs]
      
      Linux-call: osmolt [options][ARGs]
      Windows-call: osmolt.bat [options][ARGs]

      
      
      Options
        -d  : print debug-messages
        -u  : Update: change only the OLT-File
        -?
        -h  : print debug-messages

      required ARG:
        -f [mapFeatures]    XML file with requested Tags (see readme)
      optional ARGs:
        -p [Path]           Folder to save the files in
        -b [boundingBox]    boundingBox thats differs from the mapFeature-File
        -t [int]            Timeout

      examples:
        osmolt f=bicycle_rental.xml
        osmolt f=bicycle_rental.xml b=[2.20825,49.40382,7.33887,53.80065]
        osmolt f=bicycle_rental.xml b=[2.20825,49.40382,7.33887,53.80065] p=outputfolder/

      possible boundingBox formats: 
        [bbox=minlon,minlat,maxlon,maxlat]
        [minlon,minlat,maxlon,maxlat]
        minlon,minlat,maxlon,maxlat



=================================================================================================================================

 osm mkall
   make all Osmolt-maps in this folder and its subfolder

 print this help
    mkall
 make all maps in the specified folder (osmolt.jar must be in the same folder as "mkall")
    mkall [path-to-folder]
 make all maps in the specified folder with the specified path of the osmolt.jar
    mkall [path-to-folder] [path-to-osmolt.jar]

=================================================================================================================================
=================================================================================================================================

Source is in the Jar-File (/src)

Osmolt  Copyright (C) 2008  Josias Polchau

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.



