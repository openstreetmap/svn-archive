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

$stderr.print "Reading the new TIGER Zip codes..."
big_zips = {}
File.open("zip_coords.csv", File::RDONLY) do |f|
  CSV::Reader.parse(f) do |row|
    zip, lt, lg = row.first.to_s, row[1].to_s.to_f, row[2].to_s.to_f
    next if zip =~ /^\d{5}-\d{4}/
    big_zips{zip} = [lt, lg]
  end
end
$stderr.puts " done."

$stderr.print "Reading the old Zip codes..."
old_zips = {}
File.open("zip_coords.old.csv", File::RDONLY) do |f|
  CSV::Reader.parse(f) do |row|
    zip, lt, lg = row.first.to_s, row[1].to_s.to_f, row[2].to_s.to_f
    old_zips{zip} = [lt, lg]
  end
end
$stderr.puts " done."

