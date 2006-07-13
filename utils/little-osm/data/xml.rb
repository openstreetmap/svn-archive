require 'data/core'
require 'rexml/document'
require 'time'

class OsmPrimitive
  def to_xml
    e = REXML::Element.new self.class.name.downcase
    e.add_attributes 'id'=>@id, 'timestamp'=>(@timestamp.xmlschema if @timestamp)
    @tags.each do |key, value|
      tag = REXML::Element.new 'tag'
      tag.add_attributes 'k' => key, 'v' => value
      e.add_element(tag)
    end if @tags
    e
  end
end

class Node < OsmPrimitive
  def to_xml
    e = super
    e.add_attributes 'lat'=>@lat, 'lon'=>@lon
    e
  end
end

class Segment < OsmPrimitive
  def to_xml
    e = super
    e.add_attributes 'from'=>@from.to_i, 'to'=>@to.to_i
    e
  end
end

class Way < OsmPrimitive
  def to_xml
    e = super
    @segment.each do |s|
      seg = REXML::Element.new 'seg'
      seg.add_attribute 'id', s.to_i
      e.add_element seg
    end
    e
  end
end
