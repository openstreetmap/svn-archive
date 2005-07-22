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

require "osm"
include OSM
require "tiger/tiger"

NUM_ATTEMPTS = 5

def open_osm
  return OpenStreetMap.new("b@gimpert.com", "january")
end

begin
  if ((ARGV != ["--reset"]) &&
      ((ARGV.length < 2) ||
       (ARGV.length > 3) ||
       (! File.exists?(File.expand_path(ARGV.first))) ||
       (! File.exists?(File.expand_path(ARGV[1])))))

    raise "Pass a TIGER .RT1 file as the first argument, and an .RT2 as the second, with an optional starting index as the third."
  end
  osm = open_osm
  if ARGV.first == "--reset"
    osm.rollback
    puts "*** Reset finished"
    exit
  end
  rt1_s, rt2_s = nil, nil
  File.open(ARGV.first, File::RDONLY) do |f| rt1_s = f.read end
  File.open(ARGV[1], File::RDONLY) do |f| rt2_s = f.read end
  streets = Tiger::import(rt1_s, rt2_s)
  start_i = 0
  start_i = ARGV[2].to_i - 1 if ARGV.length == 3
  line_ids = streets.map { |street| street.line_id }.sort
  (start_i...line_ids.length).each do |i|
    line_id = line_ids[i]
    street = streets.find { |street| street.line_id == line_id }
    coords = street.points.map { |pt| [pt.x, pt.y] }
    attempt_count = 1
    loop do
      begin
        osm.newStreet(street.name, coords, street.from_zip, street.to_zip)
        $stderr.puts "*** Created street #{i + 1} of #{line_ids.length}, #{((i + 1).to_f / line_ids.length * 100).to_s[0..4]}%"
        break  # success
      rescue =>ex
        $stderr.puts "*** FAILED [#{ex}], on attempt number #{attempt_count}"
        $stderr.puts "*** response from server: #{osm.last_response.body}"
        osm = open_osm  # bounce the XMLRPC connection
        attempt_count += 1
        raise "Exiting after #{attempt_count} attempts" if attempt_count >= NUM_ATTEMPTS
      end
    end
    sleep(0.05)
  end
ensure
  osm.close if osm
end

