module OSM

  require 'rexml/document'

  #include REXML

  class Ox # Ox == osm xml ? ugh

    include REXML
  
    def initialize
      @root = Element.new 'osm'
      @root.attributes['version'] = '0.2'
    end

    def add_node(node)
      el1 = Element.new 'node'
      el1.attributes['uid'] = node.uid
      el1.attributes['lat'] = node.latitude
      el1.attributes['lon'] = node.longitude
      el1.attributes['tags'] = node.tags
      @root.add el1
    end

    def add_segment(line)
      el1 = Element.new('segment')

      el1.attributes['uid'] = line.uid
      el1.attributes['from'] = line.node_a_uid
      el1.attributes['to'] = line.node_b_uid
      el1.attributes['tags'] = line.tags

      @root.add el1
    end

    def to_s
      return @root.to_s
    end

    def to_s_pretty
      hum = ''

      @root.write(hum, 0)
      return hum
    end

  end


end



