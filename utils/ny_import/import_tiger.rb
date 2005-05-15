#!/usr/bin/env ruby

require 'osm'
include OSM

# TODO
# merge streets like 5th Ave

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

begin
  osm = OpenStreetMap.new("b@gimpert.com", "january")
  if ARGV == ["--reset"]
    osm.rollback
    puts "Reset finished."
    exit
  end
  if (ARGV.length != 2) || (! File.exists?(File.expand_path(ARGV.first))) || (! File.exists?(File.expand_path(ARGV[1])))
    raise "Pass a TIGER .RT1 file as the first argument, and an .RT2 as the second"
  end
  tiger = import_tiger(osm, ARGV.first, ARGV[1])
  tiger.keys.each do |line_id|
    street = tiger[line_id]
    name = street.first
    from_zip, to_zip = street[1]
    coords = street[2]
    osm.newStreet(name, coords, from_zip, to_zip)
  end
ensure
  osm.close if osm
end

