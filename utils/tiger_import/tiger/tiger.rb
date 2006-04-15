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

module Tiger

	require "tiger/geometry"
	require "tiger/utm"

	class Street

		def initialize(line_id, name, from_zip, to_zip, points)
			@line_id = line_id
			@name = name
			@from_zip, @to_zip = from_zip, to_zip
			@points = points
		end

		def line_id; @line_id; end
		def name; @name; end
		def from_zip; @from_zip; end
		def to_zip; @to_zip; end
		def points; @points; end

		def <=>(o)
			@line_id <=> o.line_id
		end

		def utm_points
			return @points.map do |pt|
				easting, northing, zone = Utm::to_utm(pt.lat, pt.long)
				Geometry::Point.new(easting, northing)
			end
		end

		def to_s
			return "#{@name}(#{@line_id}), from #{if @from_zip.nil?; "?" else @from_zip end} to #{if @to_zip.nil?; "?" else @to_zip end}, #{@points.join(" -> ")}"
		end
	
	end

	def Tiger.merge(streets, min_lat, max_lat, min_long, max_long)
		$stderr.puts "calculating \"degree area\""
		degree_area = (max_lat - min_lat) * (max_long - min_long)
		$stderr.puts "\t= #{degree_area}, same_threshold = #{same_threshold}"
		Geometry::Point.same_threshold = same_threshold
		$stderr.puts "sorting streets by name"
		by_name = {}
		streets.each do |st|
			by_name[st.name] = [] unless by_name.has_key?(st.name)
			by_name[st.name] << st
		end
		$stderr.puts "merging #{by_name.keys.length} streets:"
		merged_streets = []
		by_name.each_key do |name|
			$stderr.puts "\tstreet name \"#{name}\""
			$stderr.puts "\t\tinstantiating SegmentChains"
			same_named_streets = by_name[name]
			chains = same_named_streets.map do |st|
				street_points = st.points
				Geometry::SegmentChain.new([st], street_points)
			end
			merged_chains = Geometry::SegmentChain.merge(chains)
			$stderr.puts "\t\tinstantiating the Streets"
			merged_streets += merged_chains.map do |chain|
				first_tagged_chain = chain.tags.first
				last_tagged_chain = chain.tags.last
				Street.new(
					first_tagged_chain.line_id,
					first_tagged_chain.name,
					first_tagged_chain.from_zip,
					last_tagged_chain.to_zip,
					chain.points)
			end
		end
		return merged_streets
	end

	def Tiger.import(rt1_s, rt2_s, should_merge_streets = false)
		$stderr.puts "parsing primary streets"
		rt1 = {}
		rt1_s.split(/\n/).each do |base_line|
			line = " " + base_line.chomp  # spacer for 1-based indexing
			cfcc = line[56..58].strip
			next unless cfcc =~ /^A|P/  # A is a road, P is a provisional (unverified) road
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
		$stderr.puts "parsing secondary streets"
		rt2 = {}
		rt2_s.split(/\n/).each do |base_line|
			line = " " + base_line.chomp  # spacer for 1-based indexing
			line_id = line[6..15].strip.to_i
			rt2[line_id] = [] unless rt2.has_key?(line_id)
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
		$stderr.puts "merging primary and secondary streets"
		rt2.keys.each do |line_id|
			rt2_coords = rt2[line_id].compact
			if rt1.has_key?(line_id)
				rt1_coords = rt1[line_id][2]
				coords = []
				coords << rt1_coords.first
				coords = coords.concat(rt2_coords)
				coords << rt1_coords.last
				rt1[line_id][2] = coords
			end
		end
		$stderr.puts "converting into Street instances"
		min_lat, max_lat, min_long, max_long = nil, nil, nil, nil
		streets = rt1.keys.map do |line_id|
			name, zips, coords = rt1[line_id]
			from_zip, to_zip = zips
			points = coords.map do |coord|
				lat, long = coord
				min_lat = lat if min_lat.nil? || lat < min_lat
				max_lat = lat if max_lat.nil? || lat > max_lat
				min_long = long if min_long.nil? || long < min_long
				max_long = long if max_long.nil? || long > max_long
				pt = Geometry::Point.new
				pt.lat = lat
				pt.long = long
				pt
			end
			Street.new(line_id, name, from_zip, to_zip, points)
		end
		if should_merge_streets
			merged_streets = Tiger.merge(streets, min_lat, max_lat, min_long, max_long)
			$stderr.puts "returning merged, finished streets"
			merged_streets
		else
			$stderr.puts "returning (just) finished streets"
			streets
		end
	end

end

