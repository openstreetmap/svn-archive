module OSM

  require 'rexml/document'

  #include REXML

  class Gpx

    include REXML
  
    def initialize
      @root = Element.new 'gpx'
      @root.attributes['version'] = "1.0"
      @root.attributes['xmlns'] = "http://www.topografix.com/GPX/1/0/gpx.xsd"
    end

    def addnode(node)
      el1 = Element.new 'wpt' 
      el1.attributes['lat'] = node.latitude
      el1.attributes['lon'] = node.longitude
      el1.text = node.uid.to_s
      @root.add el1
    end

    def addtrk(trk)
      el1 = Element.new 'trk'
      el2 = Element.new 'trkseg'

      trk.each do |p|
        el3 = Element.new 'trkpt'
        el3.attributes['lat'] = p.latitude
        el3.attributes['lon'] = p.longitude
        el2.add el3
      end

      el1.add el2
      @root.add el1
      
    end

    def addline(line, node_a, node_b)
      el1 = Element.new('trk')
      el2 = Element.new('name')
      el2.text = line.to_s
      el1.add el2

      el3 = Element.new('trkseg')
      
      el4 = Element.new('trkpt')
      el4.attributes['lat'] = node_a.latitude
      el4.attributes['lon'] = node_a.longitude
      el5 = Element.new('name')
      el5.text = node_a.uid.to_s

      el4.add el5
      el3.add el4

      el6 = Element.new('trkpt')
      el6.attributes['lat'] = node_b.latitude
      el6.attributes['lon'] = node_b.longitude
      el7 = Element.new('name')
      el7.text = node_b.uid.to_s

      el6.add el7

      el3.add el6

      el1.add el3

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



