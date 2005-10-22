require 'xml/mapping'

class OSMNode; end
class OSMSegment; end

class OSMXML
  include XML::Mapping

  text_node :version, "@version"
  array_node :nodes, "nodes", "node", :class=>OSMNode
  array_node :segements, "segments", "segment", :class=>OSMSegment
end

class OSMNode
  include XML::Mapping

  text_node :mid, "@id"
  text_node :lat, "@lat"
  text_node :lon, "@lon"

  text_node :tags, "tags", :default_value=>nil

  def id_i
    mid.to_i 
  end

  def lat_f
    lat.to_f
  end

  def lon_f
    lon.to_f
  end
end

class OSMSegment
  include XML::Mapping

  text_node :mid, "@id"
  text_node :from, "@from"
  text_node :to, "@to"

  text_node :tags, "tags", :default_value=>nil

  def id_i
    mid.to_i
  end

  def from_i
    from.to_i
  end

  def to_i
    to.to_i
  end

end



