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

require 'csv'

def dist(a, b)
  alt, alg = a
  blt, blg = b
  return ((alt.to_f - blt.to_f) ** 2 + (alg.to_f - blg.to_f) ** 2) ** 0.5
end

$stderr.print "Reading the new Zip codes"
new_zips = {}
File.open("zip_coords_base.csv", File::RDONLY) do |f|
  until f.eof?
    zip, lt, lg = f.gets.split(/,/)
    new_zips[zip] = [lt, lg]
    $stderr.print "." if (new_zips.keys.length % 100).zero?
  end
end
$stderr.puts

$stderr.print "Reading the old Zip codes"
old_zips = {}
File.open("zip_coords.old.csv", File::RDONLY) do |f|
  until f.eof?
    zip, lt, lg = f.gets.split(/,/).map do |x| eval(x) end   # eval to lose double quotes
    old_zips[zip] = [lt, lg]
    $stderr.print "." if (old_zips.keys.length % 100).zero?
  end
end
$stderr.puts

common_zips = old_zips.keys & new_zips.keys
puts "Number of old Zip codes: #{old_zips.keys.length}"
puts "Number of new Zip codes: #{new_zips.keys.length}"
puts "Number of Zip codes in common: #{common_zips.length}"
puts "Common Zip codes:"
common_zips.sort.each do |zip|
  puts "    #{zip}, distance = #{dist(new_zips[zip], old_zips[zip])}"
end

