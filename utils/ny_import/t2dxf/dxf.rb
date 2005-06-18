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

require "cgi"
require "net/http"
require "tempfile"
require "uri"

require "geometry"
require "tiger"
require "utm"

SCRIPT = "http://80.68.92.194/cgi-bin/ben/dxf"
DEFAULT_SOURCE = "http://www2.census.gov/geo/tiger/tiger2004fe/NY/tgr36061.zip"
CODE_BASE = "http://80.68.92.194/ben/"

def content_type
  return "Content-type: text/plain\r\n\r\n"
end

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

def line(x1, y1, x2, y2)
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
#{x1}
  20
#{y1}
  30
0.0
  11
#{x2}
  21
#{y2}
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
      s += line(prev_point.x, prev_point.y, pt.x, pt.y)
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

as_cgi = ! (ENV['SCRIPT_NAME'].nil? || ENV['SCRIPT_NAME'].empty?)

if as_cgi
  cgi = CGI.new("html3")

  if cgi['source'].nil? || cgi['source'].empty?
    cgi.out {
      cgi.head { cgi.title { "T2DXF" } } +
      cgi.body { cgi.form("GET") {
        cgi.h3 { "T2DXF" } +
        cgi.p {
          "Enter a TIGER source URL:" + cgi.br +
          cgi.text_field("source", DEFAULT_SOURCE, 60) + "&nbsp;" +
          cgi.submit("View as DXF")
        }
      } }
    }
    exit
  end

  unless cgi.params.keys.member?("dxf")
    dxf_url = "#{SCRIPT}?source=#{CGI.escape(cgi['source'])}&dxf"
    cgi.out {
      cgi.head { cgi.title { "T2DXF Viewer" } } +
      cgi.body {
        cgi.p { <<__EOF__
<applet codebase="#{CODE_BASE}" archive="dxfviewer.jar" code="de.escape.quincunx.dxf.DxfViewer" width="640" height="480" name="DXF_Viewer"><param name="file" value="#{dxf_url}"><param name="framed" value="false"></applet>
__EOF__
        } +
        cgi.p {
          cgi.a(dxf_url) { "DXF Source" }
        }
      }
    }
    exit
  end
end

begin
  if as_cgi
    tiger_url = cgi['source']
  else
    raise "Pass a URL to a TIGER county .ZIP as an argument" unless ARGV.length == 1
    tiger_url = ARGV.first
  end
  zip = wget(tiger_url)
  tmp = Tempfile.new("t2dxf")
  tmp.print(zip)
  tmp.close
  rt1 = `/usr/bin/env unzip -qq -p #{tmp.path} *.RT1`
  raise "Could not extract .RT1 file" unless $?.to_i.zero?
  rt2 = `/usr/bin/env unzip -qq -p #{tmp.path} *.RT2`
  raise "Could not extract .RT2 file" unless $?.to_i.zero?
  tmp.unlink

  tiger = Tiger.import(rt1, rt2)
  lines_dxf, min, max = lines(tiger)
  buffer = ''
  buffer += content_type
  buffer += header(min, max)
  buffer += lines_dxf
  buffer += closer
  $stdout.print buffer

rescue => ex
  if as_cgi
    cgi.out {
      cgi.head { cgi.title { "T2DXF" } } +
      cgi.body {
        cgi.h3 { "<font color=\"red\">T2DXF Error</font>" } +
        cgi.p {
          ex.message + cgi.br +
          ex.backtrace.join(cgi.br)
        }
      }
    }
  else
    raise
  end
end

