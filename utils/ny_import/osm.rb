module OSM

  require 'xmlrpc/client'

  class OpenStreetMap
    
    ID_LOG_PATH = "osm-id.log"
    OSM_XMLRPC_URL = "http://www.openstreetmap.org/api/xml.jsp"
    
    def initialize(username, password)
      @osm = XMLRPC::Client.new2(OSM_XMLRPC_URL)
      @token = @osm.call("openstreetmap.login", username, password)
      raise "Invalid username and/or password" if @token =~ /^ERROR/
      open_id_log
    end
    
    def open_id_log
      @id_log = File.new(ID_LOG_PATH, File::CREAT | File::APPEND | File::WRONLY)
    end
    
    def close_id_log
      @id_log.close
    end
    
    def append_id(node_id)
      @id_log.puts node_id
    end
    
    def newNode(lat, long)
      node = @osm.call("openstreetmap.newNode", @token, lat, long)
      raise "Could not create node at (#{lat} #{long})" if node == -1
      append_id(node)
      return node
    end
    
    def newLine(lat1, long1, lat2, long2)
      from = newNode(lat1, long1)
      to = newNode(lat2, long2)
      line = @osm.call("openstreetmap.newLine", @token, from, to)
      raise "Could not create line from (#{lat1} #{long1}) to (#{lat2} #{long2})" if line == -1
      return line
    end
    
    def reset
      close_id_log
      File.open(ID_LOG_PATH, File::RDONLY) do |f|
        f.readlines.each do |line|
          node_id = line.strip.to_i
          success = @osm.call("openstreetmap.deleteNode", @token, node_id)
          raise "Could not delete node #{node_id}" unless success
        end
      end
      File.delete(ID_LOG_PATH)
      open_id_log
    end
    
    def close
      close_id_log
    end
    
  end

end

