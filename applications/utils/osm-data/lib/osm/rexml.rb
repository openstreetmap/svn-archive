# Adds to_xml, to_rexml, from_xml and from_rexml capabilities to the core objects.
#
# ==Example
#  node = OsmPrimitive.from_xml '<node id="1" lat="51" lon="1" />'
#  node.lat, node.lon = 52, 2
#  print node.to_xml                 => <node id="1" lat="52" lon="2" />

require 'osm/data'
require 'rexml/document'
require 'time'

module OSM

  class OsmPrimitive

    # Return this primitive as REXML element.
    def to_rexml
      e = REXML::Element.new self.class.canonical_name
      e.add_attributes 'id'=>@id, 'timestamp'=>(@timestamp.xmlschema if @timestamp)
      @tags.each do |key, value|
        tag = REXML::Element.new 'tag'
        tag.add_attributes 'k' => key, 'v' => value
        e.add_element(tag)
      end if @tags
      e
    end

    # Return the primitive as xml string
    def to_xml
      self.to_rexml.to_s
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

  end

  class Node < OsmPrimitive

    def to_rexml
      e = super
      e.add_attributes 'lat'=>@lat, 'lon'=>@lon
      e
    end

    # Create a node out of the REXML element
    def Node.from_rexml e
      a = e.attributes
      Node.new :lat=>a["lat"], :lon=>a["lon"], :id=>a["id"], :timestamp=>a["timestamp"]
    end

  end

  class Segment < OsmPrimitive

    def to_rexml
      e = super
      e.add_attributes 'from'=>@from.to_i, 'to'=>@to.to_i
      e
    end

    # Create a segment out of an REXML element
    def Segment.from_rexml e
      a = e.attributes
      Segment.new :from=>a["from"].to_i, :to=>a["to"].to_i, :id=>a["id"], :timestamp=>a["timestamp"]
    end

  end

  class Way < OsmPrimitive

    def to_rexml
      e = super
      @segments.each do |s|
        seg = REXML::Element.new 'seg'
        seg.add_attribute 'id', s.to_i
        e.add_element seg
      end
      e
    end

    # Create an way out of an REXML structure
    def Way.from_rexml e
      segs = []
      e.each_element { |seg| segs << Segment.from_rexml(seg) if seg.name == "seg" }
      Way.new :segments=>segs, :id=>e.attributes["id"], :timestamp=>e.attributes["timestamp"]
    end

  end
end
