                 OSM static osmarender tile generator
                 ------------------------------------

Requires:
 * xalan2 or xsltproc
 * ImageMagick (for identify)
 * inkscape
 * vips <http://www.vips.ecs.soton.ac.uk/> - needs tools, not just library

Will take a .osm file, pass it through osmarender at a number of different
scale factors. It will then output these svgs at increasing DPI levels,
and chop them up into tiles. Finally, the bundled display.html will be
copied in, to allow browing of the tiles.

If a copy of osmarender is found in the osmarender/ subdirectory, then that
will be used for rendering. Otherwise, a fresh svn checkout will be made
and used.

The tiles will be output to a subdirectory, named based on the .osm file
being rendered.


Program Arguments:
 -initial-dpi <dpi>
      The DPI to output the initial map at. (Subsequent scales will be
       done at higher DPIs than this).
      Should be chosen to produce a map somewhere between 400x400 and 600x600
 -osmarender-scales <scale,scale,scale>
      The list of osmarender scales to generate the image at. Will generate
       one set of tiles for each scale listed, each set of tiles at twice
       the DPI of the last.
      You need to list a scale for each zoom level you want, but they can
       be the same

      eg     -osmarender-scales 0.3,0.5,1.0,1.0
 <filename.osm>
      The .osm file to render


Outstanding Issues:
* For each tile we generate, we call vips. This can be a bit slow, so we
   should really find a way to have vips spit out several tiles once it has
   started processing the large image
* You have to figure out the initial DPI yourself. Without the .osm file
   including the bounding box, I'm not sure how we can calculate one for you
* As you change the osmarender scale, not only does the page size change, but
   the aspect ratio does too. So, your tiles will never be the same size or
   shape.
* The browsing interface is pretty basic, and could really use being made
   a lot snazzier.
