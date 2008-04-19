#!/usr/bin/python

import cairo
import cgi
import mapnik
import os
import shutil
import sys
import tempfile

# Routine to output HTTP headers
def output_headers(content_type, filename = "", length = 0):
  print "Content-Type: %s" % content_type
  if filename:
    print "Content-Disposition: attachment; filename=\"%s\"" % filename
  if length:
    print "Content-Length: %d" % length
  print ""

# Routine to output the contents of a file
def output_file(file):
  file.seek(0)
  shutil.copyfileobj(file, sys.stdout)

# Routine to get the size of a file
def file_size(file):
  return os.fstat(file.fileno()).st_size

# Routine to report an error
def output_error(message):
  output_headers("text/html")
  print "<html>"
  print "<head>"
  print "<title>Error</title>"
  print "</head>"
  print "<body>"
  print "<h1>Error</h1>"
  print "<p>%s</p>" % message
  print "</body>"
  print "</html>"

# Parse CGI parameters
form = cgi.FieldStorage()

# Validate the parameters
if not form.has_key("bbox"):
  # No bounding box specified
  output_error("No bounding box specified")
elif not form.has_key("scale"):
  # No scale specified
  output_error("No scale specified")
elif not form.has_key("format"):
  # No format specified
  output_error("No format specified")
else:
  # Create projection object
  prj = mapnik.Projection("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over");

  # Get the bounds of the area to render, and project it to the map projection
  bbox = [float(x) for x in form.getvalue("bbox").split(",")]
  bbox = mapnik.forward_(mapnik.Envelope(*bbox), prj)

  # Calculate the size of the final rendered image
  scale = float(form.getvalue("scale"))
  width = int(bbox.width() / scale / 0.00028)
  height = int(bbox.height() / scale / 0.00028)

  # Limit the size of map we are prepared to produce
  if width * height > 4000000:
    # Map is too large (limit is approximately A2 size)
    output_error("Map too large")
  else:
    # Create map
    map = mapnik.Map(width, height)

    # Load map configuration
    mapnik.load_map(map, "/home/jburgess/live/osm.xml")

    # Zoom the map to the bounding box
    map.zoom_to_box(bbox)

    # Render the map
    if form.getvalue("format") == "png":
      image = mapnik.Image(map.width, map.height)
      mapnik.render(map, image)
      png = image.tostring("png") 
      output_headers("image/png", "map.png", len(png))
      print png
    elif form.getvalue("format") == "jpeg":
      image = mapnik.Image(map.width, map.height)
      mapnik.render(map, image)
      jpeg = image.tostring("jpeg") 
      output_headers("image/jpeg", "map.jpg", len(jpeg))
      print jpeg
    elif form.getvalue("format") == "svg":
      file = tempfile.NamedTemporaryFile()
      surface = cairo.SVGSurface(file.name, map.width, map.height)
      mapnik.render(map, surface)
      surface.finish()
      output_headers("image/svg+xml", "map.svg", file_size(file))
      output_file(file)
    elif form.getvalue("format") == "pdf":
      file = tempfile.NamedTemporaryFile()
      surface = cairo.PDFSurface(file.name, map.width, map.height)
      mapnik.render(map, surface)
      surface.finish()
      output_headers("application/pdf", "map.pdf", file_size(file))
      output_file(file)
    elif form.getvalue("format") == "ps":
      file = tempfile.NamedTemporaryFile()
      surface = cairo.PSSurface(file.name, map.width, map.height)
      mapnik.render(map, surface)
      surface.finish()
      output_headers("application/postscript", "map.ps", file_size(file))
      output_file(file)
    else:
      output_error("Unknown format '%s'" % form.getvalue("format"))
