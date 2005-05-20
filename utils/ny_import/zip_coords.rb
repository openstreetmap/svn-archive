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

require 'csv'
require 'net/http'

# from "http://www.yoshidam.net/diary/ymhtml.rb-0.1.7"
require 'ymhtml'

def get_zips(base_path)
  rtz = {}
  File.open("#{base_path}.RTZ", File::RDONLY) do |f|
    until f.eof?
      line = " " + f.gets.chomp  # spacer for 1-based indexing
      line_id = line[6..15].strip.to_i
      left_zip4 = line[19..22].strip; left_zip4 = nil if left_zip4.empty? || left_zip4.to_i.zero?
      right_zip4 = line[23..26].strip; right_zip4 = nil if right_zip4.empty? || right_zip4.to_i.zero?
      rtz[line_id] = [] unless rtz.has_key?(line_id)
      rtz[line_id] << [left_zip4, right_zip4]
    end
  end
  zips = {}
  File.open("#{base_path}.RT1", File::RDONLY) do |f|
    until f.eof?
      line = " " + f.gets.chomp  # spacer for 1-based indexing
      line_id = line[6..15].strip.to_i
      left_zip = line[107..111].strip; left_zip = nil if left_zip.empty? || left_zip.to_i.zero?
      left_lat = line[201..209].strip.to_f / 1000000
      left_long = line[191..200].strip.to_f / 1000000
      right_zip = line[112..116].strip; right_zip = nil if right_zip.empty? || right_zip.to_i.zero?
      right_lat = line[220..228].strip.to_f / 1000000
      right_long = line[210..219].strip.to_f / 1000000
      left_zips = []
      left_zips << left_zip unless left_zip.nil?
      right_zips = []
      right_zips << right_zip unless right_zip.nil?
      if rtz.has_key?(line_id)
        rtz[line_id].each do |zip4|
          left_zip4, right_zip4 = zip4
          left_zips << "#{left_zip}-#{left_zip4}" unless left_zip4.nil?
          right_zips << "#{right_zip}-#{right_zip4}" unless right_zip4.nil?
        end
      end
      left_zips.each do |zip|
        next if zips.has_key?(zip)
        zips[zip] = [left_lat, left_long]
      end
      right_zips.each do |zip|
        next if zips.has_key?(zip)
        zips[zip] = [right_lat, right_long]
      end
    end
  end
  return zips
end

def http_get(host, path)
	resp, data = Net::HTTP.new(host, 80).get(path, "Host" => host)
	return data
end

data = http_get("www.census.gov", "/geo/www/tiger/tiger2004fe/tgr2004fe.html")
YmHTML::Parser.new.parse(data) do |type, tag, contents|
  if (type == :START_ELEM) &&
     (tag =~ /^a$/i) &&
     (contents['href'] =~ /^http:\/\/www2?\.census\.gov\/geo\/tiger\/tiger2004fe\/([A-Z]{2})\//)
    state = $1
    $stderr.puts "Parsed state #{state}"
    state_data = http_get("www2.census.gov", "/geo/tiger/tiger2004fe/#{state}/")
    YmHTML::Parser.new.parse(state_data) do |type, tag, contents|
      if (type == :START_ELEM) &&
         (tag =~ /^a$/i) &&
         (contents['href'] =~ /^(tgr\d+\.zip)/)
        county = $1
        $stderr.puts "Parsed county [#{county}] in #{state}"
        county_url = "http://www2.census.gov/geo/tiger/tiger2004fe/#{state}/#$1"
        `rm -rf tmp/`
        `mkdir tmp/`
        `wget #{county_url} -o /dev/null -O tmp/tiger_county.zip`
        `unzip -d tmp/ tmp/tiger_county.zip`
        $stderr.puts "Unzipped county for URL [#{county_url}]"
        base_path = File.new(Dir["tmp/TGR*"].first).path[0..-5]
        $stderr.puts "Extracted base path [#{base_path}]"
        zips = get_zips(base_path)
        $stderr.puts "Parsed ZIP codes out of the TIGER data"
        zips.keys.sort.each do |zip|
          $stdout.puts CSV.generate_line([zip, zips[zip]].flatten)
        end
        $stderr.puts "Output #{zips.keys.length} ZIP codes"
      end
    end
  end
end

