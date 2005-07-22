#!/usr/bin/env ruby

=begin Copyright (C) 2005 Ben Gimpert (ben@somethingmodern.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

=end

require "net/http"
require "tempfile"
require "uri"

require "tiger/tiger"
require "tiger/geometry"

DEFAULT_SOURCE = "http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip"

def header(min, max)
  return <<__EOF__
  0
SECTION
  2
HEADER
  9
$ACADVER
  1
AC1009
  9
$HANDSEED
  5
FFFF
  9
$PLIMMIN
  10
#{min.x}
  20
#{min.y}
  9
$PLIMMAX
  10
#{max.x}
  20
#{max.y}
  0
ENDSEC
__EOF__
end

def closer
  return <<__EOF__
  0
ENDSEC
  0
EOF
__EOF__
end

def line(a, b)
  return <<__EOF__
  0
LINE
  100
AcDbEntity
  100
AcDbLine
  8
0
  10
#{a.x}
  20
#{a.y}
  30
0.0
  11
#{b.x}
  21
#{b.y}
  31
0.0
__EOF__
end

def lines(tiger)
  s = <<__EOF__
  0
SECTION
  2
ENTITIES
__EOF__
  tiger.each do |street|
    utm_points = street.utm_points
    prev_point = utm_points.first
    utm_points[1..-1].each do |pt|
      s += line(prev_point, pt)
      prev_point = pt
    end
  end
  all_utm_points = tiger.map { |st| st.utm_points }.flatten
  all_utm_x_points = all_utm_points.map { |pt| pt.x }
  all_utm_y_points = all_utm_points.map { |pt| pt.y }
  min = Geometry::Point.new(all_utm_x_points.min, all_utm_y_points.min)
  max = Geometry::Point.new(all_utm_x_points.max, all_utm_y_points.max)
  return [s, min, max]
end

def wget(url)
  parsed = URI.parse(url)
  if parsed.scheme == "file"
    File.open(parsed.path, File::RDONLY) do |f|
      return f.read
    end
  else
    http = Net::HTTP.new(parsed.host, parsed.port)
    resp, data = http.get(parsed.path, "Host" => parsed.host)
    return data
  end
end

raise "Pass a URL to a TIGER county .ZIP as an argument" unless ARGV.length == 1
tiger_url = ARGV.first
zip = wget(tiger_url)
tmp = Tempfile.new("t2dxf")
tmp.print(zip)
tmp.close
$stderr.puts "extracting TIGER archive"
rt1 = `/usr/bin/env unzip -qq -p #{tmp.path} *.RT1`
raise "Could not extract .RT1 file" unless $?.to_i.zero?
rt2 = `/usr/bin/env unzip -qq -p #{tmp.path} *.RT2`
raise "Could not extract .RT2 file" unless $?.to_i.zero?
tmp.unlink

$stderr.puts "importing TIGER data"
tiger = Tiger.import(rt1, rt2)
lines_dxf, min, max = lines(tiger)
buffer = ''
buffer += header(min, max)
buffer += lines_dxf
buffer += closer
$stdout.print buffer

