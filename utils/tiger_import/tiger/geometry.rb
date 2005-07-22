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

  module Geometry
  
    class Point
    
      SAME_THRESHOLD = 0.0000005  # decent for Manhattan's lat/long points 
      SAME_THRESHOLD2 = SAME_THRESHOLD ** 2
      
      def initialize(x = 0, y = 0)
        @x, @y = x, y
      end
    
      def x; @x; end
      def x=(n); @x = n; end
      def y; @y; end
      def y=(n); @y = n; end
    
      def lat; @x; end
      def lat=(n); @x = n; end
      def long; @y; end
      def long=(n); @y = n; end

      def Point.same?(a, b)
        return ((a.x - b.x) ** 2 + (a.y - b.y) ** 2) <= SAME_THRESHOLD2
      end
  
      def to_s
        return "(#{x}, #{y})"
      end
      
    end
  
    class SegmentChain
    
      def initialize(tags, *points)
        @tags = tags
        if points.length > 1
          @points = points
        else
          @points = points.first
        end
        raise "Segment chain must have at least two points" if (! @points.respond_to?(:length)) || (@points.length < 1)
        @points[1..-1].each do |p|
          return unless Point.same?(p, head)
        end
        raise "Segment chain must have at least one separate point"
      end
  
      def tags; @tags; end
      def points; @points; end
      def head; @points.first; end
      def tail; @points.last; end
  
      def SegmentChain.mergeable?(a, b)
        return Point.same?(a.tail, b.head) ||
               Point.same?(a.tail, b.tail) ||
               Point.same?(a.head, b.head) ||
               Point.same?(a.head, b.tail)
      end

      def SegmentChain.merge_two(a, b)
        raise "Cannot merge two fully separate segment chains" unless SegmentChain.mergeable?(a, b)
        points = []
        if Point.same?(a.tail, b.head)
          points += a.points + b.points[1..-2]
          points += [b.tail] unless Point.same?(a.head, b.tail)
          merged_tags = a.tags + b.tags
        elsif Point.same?(a.head, b.tail)
          points += a.points.reverse + b.points.reverse[1..-2]
          points += [b.head] unless Point.same?(a.tail, b.head)
          merged_tags = a.tags.reverse + b.tags.reverse
        elsif Point.same?(a.head, b.head)
          points += a.points.reverse + b.points[1..-2]
          points += [b.tail] unless Point.same?(a.tail, b.tail)
          merged_tags = a.tags.reverse + b.tags
        else # elsif Point.same?(a.tail, b.tail)
          points += a.points + b.points.reverse[1..-2]
          points += [b.head] unless Point.same?(a.head, b.head)
          merged_tags = a.tags + b.tags.reverse
        end
        return SegmentChain.new(merged_tags, points)
      end
  
      def SegmentChain.merge(*chains_ar)
        if chains_ar.length > 1
          chains = chains_ar.dup
        else
          chains = chains_ar.first.dup
        end
        return chains if chains.length == 1
        loop do
          merged_something = false
          chains.each_index do |i|
            sub_chain_list = chains - [chains[i]]
            sub_chain_list.each_index do |j|
              a = chains[i]
              b = sub_chain_list[j]
              if SegmentChain.mergeable?(a, b)
                chains[i] = SegmentChain.merge_two(a, b)
                chains.delete(b)
                merged_something = true
                break
              end
            end
            break if merged_something  # to "continue" the outer loop
          end
          break unless merged_something
        end
        return chains
      end
  
      def to_s
        return "<#{@points.join(" -> ")}>"
      end
  
    end
  
  end

end

