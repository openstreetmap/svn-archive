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

def append_rt2(rt1, rt2_path)
  rt2 = {}
  File.open(rt2_path, File::RDONLY) do |f|
    until f.eof?
      line = " " + f.gets.chomp  # spacer for 1-based indexing
      line_id = line[6..15].strip.to_i
      unless rt2.has_key?(line_id)
        rt2[line_id] = []
      end
      coords = rt2[line_id]
      seq = line[16..18].strip.to_i - 1
      (0..9).each do |i|
        lat_s = line[(29 + (i * 19))..(37 + (i * 19))]
        long_s = line[(19 + (i * 19))..(28 + (i * 19))]
        if (lat_s != "+000000000") && (long_s != "+000000000")
          lat = lat_s.strip.to_f / 1000000
          long = long_s.strip.to_f / 1000000
          coords[(seq * 10) + i] = [lat, long]
        else
          coords[(seq * 10) + i] = nil
        end
      end
    end
  end
  rt2.keys.each do |line_id|
    rt2_coords = rt2[line_id].compact
    if rt1.has_key?(line_id)
      rt1_coords = rt1[line_id][2]
      coords = []
      coords << rt1_coords.first
      coords = coords.concat(rt2_coords)
      coords << rt1_coords[-1]
      rt1[line_id][2] = coords
    end
  end
end

def import_tiger(osm, rt1_path, rt2_path)
  rt = read_rt1(rt1_path)
  append_rt2(rt, rt2_path)
  $stderr.puts "Parsed #{rt.keys.length} RT chain records."
  return rt
end

def open_osm
  return OpenStreetMap.new("b@gimpert.com", "january")
end

begin
  if ((ARGV.length == 1) && (ARGV.first != "--reset")) ||
     (ARGV.length < 2) ||
     (ARGV.length > 3) ||
     (! File.exists?(File.expand_path(ARGV.first))) ||
     (! File.exists?(File.expand_path(ARGV[1])))

    raise "Pass a TIGER .RT1 file as the first argument, and an .RT2 as the second, with an optional starting index as the third."
  end
  osm = open_osm
  if ARGV.first == "--reset"
    osm.rollback
    puts "Reset finished."
    exit
  end
  tiger = import_tiger(osm, ARGV.first, ARGV[1])
  start_i = 0
  start_i = ARGV[2].to_i - 1 if ARGV.length == 3
  line_ids = tiger.keys
  (start_i..line_ids.length).each do |i|
    line_id = line_ids[i]
    street = tiger[line_id]
    name = street.first
    from_zip, to_zip = street[1]
    coords = street[2]
    attempt_count = 1
    loop do
      begin
        osm.newStreet(name, coords, from_zip, to_zip)
        $stderr.puts "*** Created street #{i + 1} of #{line_ids.length}, #{((i + 1).to_f / line_ids.length * 100).to_s[0..4]}%"
        break  # success
      rescue =>ex
        $stderr.puts "*** FAILED [#{ex}], on attempt number #{attempt_count}"
        $stderr.puts "*** response from server: #{osm.last_response.body}"
        attempt_count += 1
        raise "Exiting after #{attempt_count} attempts" if attempt_count >= NUM_ATTEMPTS
      end
    end
    sleep(0.05)
  end
ensure
  osm.close if osm
end

