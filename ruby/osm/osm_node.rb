=begin
Copyright 2005 Rob McKinnon (robmckinnon@users.sourceforge.net)

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

class Array

  def unflatten subarray_size
    raise %[Cannot unflatten as array size is not a multiple of #{subarray_size}] if self.size % subarray_size != 0
    a = Array.new
    subarray_count = self.size / subarray_size
    subarray_count.times do |row|
      b = Array.new
      subarray_size.times {|i| b << self.shift}
      a << b
    end
    a
  end

end

module OSM

  class Node
    attr_reader :id, :lat, :long

    def Node::create_nodes data
      data = data.unflatten 3
      nodes = data.collect {|d| Node.new d[0], d[1], d[2]}
    end

    def initialize id, lat, long
      @id, @lat, @long =id, lat, long
    end

    def to_s
      %{node #{@id}: [#{@lat}, #{@long}]}
    end

  end

  class Line
    attr_reader :id, :node1, :node2

    def Line::create_lines line_data
      lines = line_data.collect {|d| Line.new(d[0], d[1], d[2])}
      lines.delete_if {|l| !l.has_nodes}
    end

    def initialize id, node1, node2
      @id, @node1, @node2 = id, node1, node2
    end

    def to_s
      %{line #{@id}: [#{start_node.to_s}, #{end_node.to_s}]}
    end

    def start_node
      OSM::node @node1
    end

    def end_node
      OSM::node @node2
    end

    def has_nodes
      start_node and end_node
    end

  end

end