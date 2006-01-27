module OSM

require 'xml/libxml'

  class Ox # Ox == osm xml ? ugh

    include XML
  
    def initialize
		  @doc = Document.new
      @root = Node.new 'osm'
      @root['version'] = '0.2'
			@doc.root = @root
    end

    def add_node(node)
      el1 = Node.new 'node'
      el1['uid'] = node.id.to_s
      el1['lat'] = node.latitude.to_s
      el1['lon'] = node.longitude.to_s
      el1['tags'] = node.tags
      @root << el1
    end

    def add_segment(line)
      el1 = Node.new('segment')

      el1['uid'] = line.id.to_s
      el1['from'] = line.node_a_id.to_s
      el1['to'] = line.node_b_id.to_s
      el1['tags'] = line.tags

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



