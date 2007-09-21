Clopin
------

1 Tagging
Tag your route with the following tags:
  clopin:route= car | train | taxi | walk
  clopin:id= <some unique route identifier or name>

2 Rendering
2.1  Install a normal tiles@home client
2.2  Replace layers.conf with layers_clopin.conf
2.3  Render a zoom 12 tileset using the command: perl tilesGen.pl xy 1234 5678
2.4  DO NOT UPLOAD the results to the tiles@home server
2.5  Tar the contents of /temp/tile_12_1234_5678.tmpdir and copy to a directory on your own server.
2.6  Using the shell scripts provided in the server sub-directory unpack the tar file into a tile server directory tree using the following command:
     unpack.sh <name-of-route> 1234 5678
     This will create a sub-directory named <name-of-route> and populate it with a tile server directory structure containing all the .pngs from the tar file.
2.7  In the unlikely event that your route spans more than one z12 tileset repeat the above steps for each tileset.

3 Slippy Map
3.1  Install a standard OpenLayers slippy map on your web-server.
3.2  Configure an overlay layer that serves tiles from the tileset directory that you created above.
3.3  You will need to set up a transparent 404 tile to use for tiles outside the rendered area.

