module OSM

  require 'xml/libxml'
  require 'zlib'

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
      split_tags(el1, node.tags)
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

      split_tags(el1, seg.tags)
      if seg.timestamp
        el1['visible'] = seg.visible.to_s
        el1['timestamp'] = seg.timestamp
      end

      @root << el1
    end

    def add_multi(multi, type)
      el1 = Node.new(type.to_s)
      el1['id'] = multi.id.to_s
      el1['timestamp'] = multi.timestamp

      multi.segs.each do |n|
        el2 = Node.new('seg')
        el2['id'] = n.to_s
        el1 << el2
      end

      multi.tags.each do |k,v|
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

    def split_tags(el, tags)
      tags.split(';').each do |tag|
        parts = tag.split('=')
        key = ''
        val = ''
        key = parts[0].strip unless parts[0].nil?
        val = parts[1].strip unless parts[1].nil?
        if key != '' && val != ''
          el2 = Node.new('tag')
          el2['k'] = key.to_s
          el2['v'] = val.to_s
          el << el2
        end

      end
    end


    def print_http(r)

      #if gzip encoding, use gzip stream instead of plain

      gzipped = r.headers_in['Accept-Encoding'] && r.headers_in['Accept-Encoding'].match(/gzip/)

      if gzipped

        buffer = OSM::StringIO.new
        z = Zlib::GzipWriter.new(buffer, 9)
        z.write self.to_s
        z.close
        str = buffer.to_s
        r.headers_out['Content-Encoding'] = 'gzip'
        r.headers_out['Content-Length'] = str.length.to_s
        r.content_type = 'text/html'
        r.send_http_header
        print str

      else
        puts self.to_s
      end

    end

  end
end
