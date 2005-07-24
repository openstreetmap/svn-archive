module OSM

  require 'rexml/document'

  #include REXML

  class Gpx

    include REXML
  
    def initialize
      @root = Element.new 'gpx'
      @root.attributes['version'] = '1.0'
    end

    def addnode(node)
      el1 = Element.new 'wpt' 
      el1.attributes['lat'] = node.latitude
      el1.attributes['lon'] = node.longitude
      el1.text = node.uid.to_s
      @root.add el1
    end

    def to_s
      return @root.to_s
    end

  end


end



