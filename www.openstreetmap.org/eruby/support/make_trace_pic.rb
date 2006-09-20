#!/usr/bin/ruby

require 'RMagick'
require 'osm/dao.rb'


width = 250
height = 250


gpx_id = ARGV[0].to_i

dao = OSM::Dao.instance

dbh = dao.get_connection

res = dbh.query("select max(0.0000001*latitude), min(0.0000001*latitude), max(0.0000001*longitude), min(0.0000001*longitude) from gps_points where gpx_id = #{gpx_id}")

lat_max = 0.0
lat_min = 0.0
lon_max = 0.0
lon_min = 0.0

res.each do |row|
  lat_max = row[0].to_f
  lat_min = row[1].to_f
  lon_max = row[2].to_f
  lon_min = row[3].to_f
end


rat= Math.cos( ((lat_max + lat_min)/2.0) /  180.0 * 3.141592)

lat_range = lat_max - lat_min
lon_range = lon_max - lon_min

if lat_range > lon_range
  diff = (lat_range - lon_range) / 2.0
  lon_min -= diff
  lon_max += diff
else
  diff = (lon_range - lat_range) / 2.0
  lat_min -= diff
  lat_max += diff
end

proj = OSM::Mercator.new((lat_min + lat_max) / 2, (lon_max + lon_min) / 2, (lat_max - lat_min) / width / rat, width, height)

gc = Magick::Draw.new

gc.stroke_linejoin('miter')
gc.stroke('#000000')
gc.stroke_width(3)

points = dbh.query("select (0.0000001*latitude) AS latitude, (0.0000001*longitude) AS longitude from gps_points where gpx_id=#{gpx_id}")

puts 'drawing...'

oldpx = 0.0
oldpy = 0.0

first = true

points.each do |p|
  px = proj.x(p[1].to_f)
  py = proj.y(p[0].to_f)
  gc.line(px, py, oldpx, oldpy ) unless first
  first = false
  oldpy = py
  oldpx = px
end


canvas = Magick::Image.new(width, height) {
  self.background_color = 'pink'
}

begin
  gc.draw(canvas)
rescue ArgumentError
end

transparent_canvas = canvas.transparent('pink', Magick::TransparentOpacity)
transparent_canvas.format = 'PNG'
transparent_canvas.write("/tmp/#{gpx_id}.png")

`convert -scale 50x50 /tmp/#{gpx_id}.png /tmp/#{gpx_id}-icon.png`
`scp /tmp/#{gpx_id}*.png 128.40.58.202:/var/www/openstreetmap/trace-images/`
