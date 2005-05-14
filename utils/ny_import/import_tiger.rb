#!/usr/bin/env ruby

require 'osm'
include OSM

begin
  osm = OpenStreetMap.new("b@gimpert.com", "january")
  if ARGV == ["--reset"]
    osm.reset
    puts "reset nodes"
    exit
  end
  l = osm.newLine(42.020226, -88.35293, 42.020226, -88.351)
  osm.reset
  puts "new = #{l}"
ensure
  osm.close if osm
end

