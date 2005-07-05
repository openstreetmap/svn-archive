#!/usr/bin/ruby

puts '<?xml version="1.0"?>
  <gpx
   version="1.0"
  creator="openstreetmap csv thing"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://www.topografix.com/GPX/1/0"
  xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">

  <trk><trkseg>
  '

firstline = true
oldid = 'blah'

while line = gets
    
  linearray = line.split(/,/)
  timestamp = linearray[0].gsub('  ','T').gsub('/','-') + 'Z'
  latitude = linearray[2]
  longitude = linearray[3]

  if oldid != linearray[1] then
    puts '</trkseg></trk><trk><trkseg>' unless firstline
    oldid = linearray[1]
    firstline = false
  end

  puts '<trkpt lat="' + latitude + '" lon="' + longitude + '">'
  puts '<ele>0</ele>'
  puts '<time>' + timestamp + '</time>'
  puts '</trkpt>'
  
end

puts '</trkseg></trk></gpx>'

