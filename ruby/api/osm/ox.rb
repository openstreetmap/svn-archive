module OSM

require 'xml/libxml'

  class Ox # OSM XML

    include XML
  
    def initialize
		  @doc = Document.new
      @root = Node.new 'osm'
      @root['version'] = '0.3'
      @root['generator'] = 'OpenStreetMap server'
			@doc.root = @root
    end

    def add_node(node)
      el1 = Node.new 'node'
      el1['id'] = node.id.to_s
      el1['lat'] = node.latitude.to_s
      el1['lon'] = node.longitude.to_s
      el1['tags'] = node.tags
      if node.timestamp
        el1['visible'] = node.visible.to_s
        el1['timestamp'] = node.timestamp
      end
      @root << el1
    end

    def add_segment(seg)
      el1 = Node.new('segment')

      el1['id'] = seg.id.to_s
      el1['from'] = seg.node_a_id.to_s
      el1['to'] = seg.node_b_id.to_s
      el1['tags'] = seg.tags

      if seg.timestamp
        el1['visible'] = seg.visible.to_s
        el1['timestamp'] = seg.timestamp
      end

      @root << el1
    end

    def add_street(street)
      el1 = Node.new('street')
      el1['id'] = street.id.to_s
      el1['timestamp'] = street.timestamp

      street.segs.each do |n|
        el2 = Node.new('seg')
        el2['id'] = n.to_s
        el1 << el2
      end

      street.tags.each do |k,v|
        el2 = Node.new('tag')
        el2['k'] = k.to_s
        el2['v'] = v.to_s
        el1 << el2
      end

      @root << el1
    end

    def to_s
      return @doc.to_s
    end

		def dump(out)
		  @doc.dump(out)
    end
  end
  
end
