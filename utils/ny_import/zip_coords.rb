#!/usr/bin/env ruby

=begin Copyright (C) 2004 Ben Gimpert (ben@somethingmodern.com)

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

require 'osm'
include OSM

# TODO
# merge streets like 5th Ave

NUM_ATTEMPTS = 5

def read_rt1(path)
  rt1 = {}
  File.open(path, File::RDONLY) do |f|
    until f.eof?
      line = " " + f.gets.chomp  # spacer for 1-based indexing
      line_id = line[6..15].strip.to_i
      prefix = line[18..19].strip; prefix = nil if prefix.empty?
      base_name = line[20..49].strip; base_name = nil if base_name.empty?
      line_type = line[50..53].strip; line_type = nil if line_type.empty?
      suffix = line[54..55].strip; suffix = nil if suffix.empty?
      name = [prefix, base_name, line_type, suffix].compact.join(" ")
      from_zip = line[107..111].strip; from_zip = nil if from_zip.empty?
      from_lat = line[201..209].strip.to_f / 1000000
      from_long = line[191..200].strip.to_f / 1000000
      to_zip = line[112..116].strip; to_zip = nil if to_zip.empty?
      to_lat = line[220..228].strip.to_f / 1000000
      to_long = line[210..219].strip.to_f / 1000000
      rt1[line_id] = [name, [from_zip, to_zip], [[from_lat, from_long], [to_lat, to_long]]]
    end
  end
  return rt1
end

if (ARGV.length != 1) || (! File.exists?(File.expand_path(ARGV.first)))
  raise "Pass a TIGER .RT1 file as the first argument"
end
zips = read_rt1(ARGV.first)

