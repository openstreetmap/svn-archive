FontName 0.7 - NSIS plugin to get TTF Font Name
-----------------------------------------------

Created by Vytautas Krivickas

This plugin can be used from NSIS to get the TTF Font's Name out of a .TTF file.

This plugin has been designed to assist in registering fonts.

INCLUDES
--------

This plugin includes the following include files:
  * FontName.nsh      - FontName plugin header files
  * FontReg.nsh       - Font Registration Functions
  * FontRegAdv.nsh    - Font Registration Functions with backup functionality

USAGE
-----

To use the plugin simply include the FontName.nsh file into your script then 
use one of the following macros:

  !insertmacro FontName "fontfile.ttf"
  
Now the stack contains the name of the Font or if the errors flag was set the 
error message translated to your current language, or English if the translation
is not yet available.

  !insertmacro FontNameVer

This macro will push to the stack the translated version string for the FontName Plugin.

HELP
----

If you have found a bug in this plugin or have translated the required strings in
the include function to your language I would love to hear from you.

If you wish to contact me my PM and email information can be found here:

http://forums.winamp.com/member.php?s=&action=getinfo&userid=111891

