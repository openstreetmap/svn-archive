This is the Win32 installer generator for JOSM, to create a Windows 
like installer. This should ease installation and provides a reasonable set of 
default preferences for Windows users.

Currently only josm and a small assortment of josm plugins is included in the 
installer.

As other osm related applications like osmarender and mapnik have a lot more
UNIX related dependencies that will make them complicated to install, only JOSM
is currently installed "the easy way".


install
-------
simply execute josm-setup-latest.exe

uninstall
---------
use "control panel / software" to uninstall


current state of the art
------------------------
The installer will currently add:
- josm into "C:\Program Files\JOSM" (or the corresponding international dir)
- josm icons to the desktop and quick launch bar
- josm file associations to .osm and .gpx files
- some assorted plugins into "C:\Program Files\JOSM\plugins" (more to follow?)
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

2.) Edit the two absolute paths in the file josm-setup.sh (in the calls 
to launch4jc and makensis)

3.) Start a cygwin shell and call ./josm-setup.sh

how the installer is build
--------------------------
First, wget will download the required files (e.g. the josm plugins) into the 
downloads subdir. Then jaunch4j wraps the josm.jar into a josm.exe, which 
makes registration of file extensions a lot easier. Then NSIS is called to 
create the actual josm-setup-latest.exe.

known issues
------------
- absolute paths in openstreetmap-setup.sh
- bookmarks are of ulfl's personal interest (should be replaced e.g. by some "well known" places)
- version number fixed to latest (JOSM and Plugins have different SVN versions, some plugins not even in SVN)
- localisation/internationalisation settings (currently only english supported)
- josm should support "global settings" instead of only the personal profile
- josm should use some defaults already instead of the installer ones
- some way of automatic installer generation on the server (e.g. nightly build)?
- install all josm plugins by default and only enable them according to user wishes?
- make installation of icons and file extensions optional
