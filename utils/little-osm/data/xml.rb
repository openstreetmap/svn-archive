# Adds to_xml and from_xml capabilities to the core objects.
# Both functions use strings as parameter/return, which has to be in correct
# osm-xml syntax

# Example:
#  node = OsmPrimitive.from_xml '<node id="1" lat="51" lon="1" />'
#  node.lat, node.lon = 52, 2
#  print node.to_xml                 => <node id="1" lat="52" lon="2" />


require 'data/core'
require 'rexml/document'
require 'time'

module OSM

  class OsmPrimitive

    # Return this primitive as REXML element. Called from subclasses to fill
    # in the common attributes
    def to_rexml
      e = REXML::Element.new self.canonical_name
      e.add_attributes 'id'=>@id, 'timestamp'=>(@timestamp.xmlschema if @timestamp)
      @tags.each do |key, value|
        tag = REXML::Element.new 'tag'
        tag.add_attributes 'k' => key, 'v' => value
        e.add_element(tag)
      end if @tags
      e
    end

    # Create an primitive out of an xml string.
    def OsmPrimitive.from_xml str
      self.from_rexml REXML::Document.new(str.to_s).root
    end

    # Create an primitive out of an rexml element.
    def OsmPrimitive.from_rexml e
      raise "Unknown tag '#{e.name}'" unless %W{node segment way}.include? e.name  #security concern
      osm = eval(e.name.capitalize).from_xml e
      e.each_element { |tag| osm[tag.attributes["k"]] = tag.attributes["v"] if tag.name == "tag" }
      osm
    end

    protected :to_rexml
  end

  class Node < OsmPrimitive

    # Return the node as xml string
    def to_xml
      e = to_rexml
      e.add_attributes 'lat'=>@lat, 'lon'=>@lon
      e.to_s
    end

    # Create a node out of the REXML element
    def Node.from_rexml e
      a = e.attributes
      Node.new :lat=>a["lat"], :lon=>a["lon"], :id=>a["id"], :timestamp=>a["timestamp"]
    end

  end

  class Segment < OsmPrimitive

    # Return the segment as xml string
    def to_xml
      e = to_rexml
      e.add_attributes 'from'=>@from.to_i, 'to'=>@to.to_i
      e.to_s
    end

    # Create a segment out of an REXML element
    def Segment.from_rexml e
      a = e.attributes
      Segment.new :from=>a["from"].to_i, :to=>a["to"].to_i, :id=>a["id"], :timestamp=>a["timestamp"]
    end

  end

  class Way < OsmPrimitive

    # Return the way as XML string
    def to_xml
      e = to_rexml
      @segments.each do |s|
        seg = REXML::Element.new 'seg'
        seg.add_attribute 'id', s.to_i
        e.add_element seg
      end
      e.to_s
    end

    # Create an way out of an REXML structure
    def Way.from_rexml e
      segs = []
      e.each_element { |seg| segs << Segment.from_rexml(seg) if seg.name == "seg" }
      Way.new :segments=>segs, :id=>e.attributes["id"], :timestamp=>e.attributes["timestamp"]
    end

  end

end
