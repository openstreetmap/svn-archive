This is the Win32 installer generator for OpenStreetMap, to create a Windows 
like installer. This should ease installation and provides a reasonable set of 
default preferences for Windows users.

Currently only josm and a small assortment of josm plugins is included in the 
installer. Probably, other osm related applications like osmarender and mapnik 
will be added later.


install
-------
simply execute openstreetmap-setup-x.x.x.exe

uninstall
---------
use "control panel / software" to uninstall


current state of the art
------------------------
The installer will currently add:
- josm into "C:\Program Files\OpenStreetMap" (or the corresponding international dir)
- josm icons to the desktop and quick launch bar
- josm file associations to .osm and .gpx files
- some plugins to the current user profile (more to follow)
- default preferences to the current user profile (if not already existing)
- default bookmarks to the current user profile (if not already existing)

When the installed josm.exe is executed, it should ask the user to download 
JAVA 1.5 runtime if it's not already installed. However, I've not tested this. 

build the installer
-------------------
1.) You will need to download and install the following on your machine:
- cygwin bash and wget
- launch4j - http://launch4j.sourceforge.net/
- NSIS - http://nsis.sourceforge.net/

2.) Edit the three absolute paths in the file openstreetmap-setup.sh (in the calls 
to launch4jc and makensis)

3.) Start a cygwin shell and call ./openstreetmap-setup.sh

how the installer is build
--------------------------
First, wget will download the required files (e.g. the josm plugins) into the 
downloads subdir. Then jaunch4j wraps the josm.jar into a josm.exe, which 
makes registration of file extensions a lot easier. Then NSIS is called to 
create the actual openstreetmap-setup-x.x.x.exe.

known issues
------------
- absolute paths in openstreetmap-setup.sh
- bookmarks are of ulfl's personal interest (should be replaced e.g. by some "well known" places)
- version number fixed to 0.0.x (better use SVN version?)
- localisation/internationalisation settings (currently only english supported)
- josm should support "global settings" instead of only the personal profile
- josm should use some defaults already instead of the installer ones
- some way of automatic installer generation on the server (e.g. nightly build)?
- install all josm plugins by default and only enable them according to user wishes?
- make instalation of icons and file extensions optional?
