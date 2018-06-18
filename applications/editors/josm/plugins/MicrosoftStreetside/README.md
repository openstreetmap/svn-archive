Microsoft Streetside JOSM Plugin
======

The Microsoft JOSM Streetside (https://www.microsoft.com/en-us/maps/streetside) Plugin enables the viewing of Microsoft Streetside 360 degree imagery in the JOSM editor. It is based on the implementation of the Mapillary plugin (https://wiki.openstreetmap.org/wiki/JOSM/Plugins/Mapillary).

Once JOSM is started and the MicrosoftStreetside plugin is selected in the JOSM Preferences, available Streetside imagery can be display within the boundary of the downloaded JOSM area, by selecting Imagery -> Streetside, from the main menu. Once blue bubbles appear on the map signifying Streetside imagery, clicking on a bubble results in the 360 degree imagery being displayed in the 360 degree viewer, which can be undocked and enlarged to allow for better viewing.

## Documentation

You can find out more about how to use the plugin and how it works in the [project Wiki](https://github.com/JOSM/MicrosoftStreetside/wiki).

If you want to know how to configure the plugin, you can look at the [Configuration Wiki page](https://github.com/JOSM/MicrosoftStreetside/wiki/Configuration).

## Building from source
Checkout the JOSM source, compile it and checkout the plugin source:

    svn co http://svn.openstreetmap.org/applications/editors/josm josm
    cd josm/core
    ant clean dist
    cd ../plugins
    rm -rf areaselector
    git clone https://github.com/JOSM/JOSM-areaselector.git areaselector
    cd areaselector
    ant clean install
    
Now Restart JOSM and activate the MicrosoftStreeside plugin in your preferences. 
The MicrosoftStreetside menu items will appear in the JOSM main menu after JOSM is
restarted.

Details about plugin development can be found [in the JOSM wiki](https://josm.openstreetmap.de/wiki/DevelopersGuide/DevelopingPlugins).

## License

This plugin is based on the Mapilary developed by developed and maintained by nokutu (nokutu@openmailbox.org) and extended to display Streetside imagery by Rene Rhodes (renerr18) You can contact Rene on github.

This software is licensed under [GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html). 

### Third party resources

The MicrosoftStreetside plugin used JavaFX to implement the Streetside 360 degree viewer [JavaFX](https://en.wikipedia.org/wiki/JavaFX), which, while bundled in the Oracle Java SE 1.8, is not an official part of the 
Java SE 1.8 specification, and may not function properly with alternative JDKs (e.g. OpenJDK is not currently supported). JavaFX is licensed under the same terms as Java SE (http://www.oracle.com/technetwork/java/javase/terms/license/index.html).

The plugin also makes use of the compact Resty libary for communicating with RESTful web services from Java (https://beders.github.io/Resty/Resty/Overview.html). Resty is licensed under the MIT license (https://github.com/go-resty/resty/blob/master/LICENSE).
