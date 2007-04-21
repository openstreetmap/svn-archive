#!/usr/bin/ruby


#
#
# run this in a directory with gpx files in it 
#
# it will spit out a svg file
#
# example `./svg.rb > myfile.svg`
#
#
#

require "rexml/document"


puts '<?xml version="1.0" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" 
"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">

<svg width="100%" height="100%" version="1.1"
xmlns="http://www.w3.org/2000/svg">
'

maxlat = 0
minlat = 0
maxlon = 0
minlon = 0

first = true

`ls *.gpx`.each do |filename|
  file = File.new( filename.chomp )
  doc = REXML::Document.new file

  doc.elements.each("gpx/trk/trkseg/trkpt") do |element| 
    lat = element.attributes["lat"].to_f
    lon = element.attributes["lon"].to_f
    if first
      minlat = lat
      minlon = lon
      laxlat = lat
      maxlon = lon
      first = false
    else
      minlat = lat if lat < minlat
      minlon = lon if lon < minlon
      maxlat = lat if lat > maxlat
      maxlon = lon if lon > maxlon
    end
  end
end


xsize = 500
yscale = -Math.cos( (minlat + maxlat) / 2 / 180 * 3.141592 )



`ls *.gpx`.each do |filename|
  file = File.new( filename.chomp )
  doc = REXML::Document.new file

  first = true
  lat = 0
  lon = 0
  oldx = 0
  oldy = 0

  doc.elements.each("gpx/trk/trkseg/trkpt") do |element| 
    lat = element.attributes["lat"].to_f
    lon = element.attributes["lon"].to_f

    x = (lon - minlon) / (maxlon - minlon) * xsize
    y = xsize+ ((lat - minlat) / (maxlat - minlat) * xsize * yscale)
    if first
      puts "<polyline points=\"#{x},#{y}"
      first = false
    else
      puts ",#{x},#{y}"
    end
  end

  puts '" style="fill:white;stroke:black;stroke-width:2"/>'

end


puts '</svg>'
