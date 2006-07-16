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

  def OsmPrimitive.from_xml e
    raise "Unknown tag '#{e.name}'" unless %W{node segment way}.contains? e.name  #security concern
    osm = eval("e.name.capitalize").from_xml e
    e.each { |tag| osm[tag.attributes["k"]] = tag.attributes["v"] if tag.name == "tag" }
    osm
  end
end

class Node < OsmPrimitive
  def to_xml
    e = super
    e.add_attributes 'lat'=>@lat, 'lon'=>@lon
    e
  end

  def Node.from_xml e
    a = e.attributes
  	Node.new a["lat"], a["lon"], a["id"], a["timestamp"]
  end
end

class Segment < OsmPrimitive
  def to_xml
    e = super
    e.add_attributes 'from'=>@from.to_i, 'to'=>@to.to_i
    e
  end

  def Segment.from_xml e
    a = e.attributes
  	Segment.new a["from"].to_i, a["to"].to_i, a["id"], a["timestamp"]
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

  def Way.from_xml e
    segs = []
    e.each { |seg| segs << seg if seg.name == "seg" }
  	Way.new segs, e.attributes["id"], e.attributes["timestamp"]
  end
end
