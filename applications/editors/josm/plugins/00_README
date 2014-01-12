This is the SVN repository for JOSM plugins. JOSM plugins are extensions of the OSM editor JOSM,
see http://josm.openstreetmap.de/.

This repository includes the source and the current builds for each plugin:

* the source is maintained in the respective sub directories, i.e. 'wmsplugin' for the wmsplugin
* the current builds are available in '../dist'


FAQ
===
* How can I check in my own plugin?
  
  - You need an account for the OSM subversion repository, see
    http://wiki.openstreetmap.org/wiki/Accounts#SVN_access_.28OSM_software.29
  
  - Make sure to check out the tree  http://svn.openstreetmap.org/applications/editors/josm/
    (or one of its parents).
        svn checkout http://svn.openstreetmap.org/applications/editors/josm/
    
    This will also check out the JOSM source from http://josm.openstreetmap.de/svn.
        
    Note that the build environment is not complete if you check out 
    http://svn.openstreetmap.org/applications/editors/josm/plugins/ only.
        
  - Create your own plugin directory. You may create a copy of the template directory
    according to '00_plugin_dir_template'.
    
* How can I build my plugin?
  
  - Update build.xml in your plugin directory and run 'ant dist' 
  
  - You may also add your plugin to 'plugins/build.xml'
   
    <target name="dist" depends="compile">
        ....
        <ant antfile="build.xml" target="dist" dir="YourPluginDir"/>
    </target>

    <target name="clean">
        ....
        <ant antfile="build.xml" target="clean" dir="YourPluginDir"/>
    </target>


* How can I make my plugin available to other JOSM users?
  
  - The manifest of your plugin jar has to include the required meta information,
    see http://josm.openstreetmap.de/wiki/DevelopingPlugins.
    'build.xml' copied from '00_plugin_dir_template' will create a compliant manifest
    file for your plugin.
    
  - Check in your plugin jar into 'dist'. You have to do it manually. 
    There is no automatic build process. See also '00_plugin_dir_template/REAME'.
  
    
