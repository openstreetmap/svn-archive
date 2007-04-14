=begin
Copyright 2005 Ben Gimpert (ben@somethingmodern.com)
          2005 Rob McKinnon (robmckinnon@users.sourceforge.net)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
=end

module OSM

  require 'xmlrpc/client'
  require 'osm/osm_node'

  class OpenStreetMap
    
    JOURNAL_PATH = "osm-journal.txt"
    OSM_XMLRPC_URL = "http://www.openstreetmap.org/api/xml.jsp"
    XMLRPC_TIMEOUT = 60 * 10  # ten minutes
    ZIP_CODE_KEY_NAME = "zipCode"
    NAME_KEY_NAME = "name"
    
    @nodes = Hash.new

    def initialize(username, password)
      @debug = false
      @osm = XMLRPC::Client.new2(OSM_XMLRPC_URL, nil, XMLRPC_TIMEOUT)
      @token = call("login", username, password)
      raise "Invalid username and/or password" if @token =~ /^ERROR/
      open_journal
      @zip_key_id = get_key_id(ZIP_CODE_KEY_NAME)
      @name_key_id = get_key_id(NAME_KEY_NAME)
    end

    def coords_to_s(coords)
      return coords.map do |coord|
        "(#{coord.first}, #{coord[1]})"
      end.join(" -> ")
    end

    def last_response; @last_response; end

    def log_ text
      $stderr.print text if @debug
    end
    
    def log text
      $stderr.puts text if @debug
    end

    def call(method, *args)
      result = nil
      if method == "login"
        log_ "openstreetmap.login(#{args.join(", ")}) -> "
        result = @osm.call("openstreetmap.#{method}", *args)
      else
        # don't display the token, redundant
        log_ "    #{method}(#{args[1..-1].join(", ")}) -> "
        result = @osm.call("openstreetmap.#{method}", @token, *args)
      end
      @last_response = @osm.http_last_response
  	  log "#{result}"
      
      return result
    end
        
    def get_key_id(key_name)
      keys = call("getAllKeys", true)
      raise "Could not retrieve all keys" if keys.nil?
      keys.each_index do |i|
        return keys[i - 1].to_i if keys[i] == key_name
      end
      key_id = call("newKey", key_name)
      raise "Could not create ZIP code key [#{key_name}]" if key_id == -1
      journal("(\"deleteKey\", @token, #{key_id})")
      return key_id
    end
    
    def close
      close_journal
    end
    
    def open_journal
      @journal = File.new(JOURNAL_PATH, File::CREAT | File::APPEND | File::WRONLY)
    end
    
    def close_journal
      @journal.close unless @journal.nil? || @journal.closed?
    end
    
    def journal(s)
      @journal.puts s
    end
    
    def rollback
      close_journal
      File.open(JOURNAL_PATH, File::RDONLY) do |f|
        f.readlines.each do |line|
          code_snippet = line.strip
          begin
            success = eval("call#{code_snippet}")
          rescue XMLRPC::FaultException => ex
            log "Could not rollback on line [#{code_snippet}]" unless success
          end
        end
      end
      File.delete(JOURNAL_PATH)
      open_journal
    end
    
    def newNode(lat, long)
      raise "Invalid latitude [#{lat}] in newNode" if lat.nil? || (! lat.respond_to?(:to_f))
      raise "Invalid longitude [#{long}] in newNode" if long.nil? || (! long.respond_to?(:to_f))
      node = call("newNode", lat, long)
      raise "Could not create node at (#{lat} #{long})" if node == -1
      journal("(\"deleteNode\", @token, #{node})")
      return node
    end
    
    def newLine(from_lat, from_long, to_lat, to_long)
      from_id = newNode(from_lat, from_long)
      to_id = newNode(to_lat, to_long)
      line_id = call("newLine", from_id, to_id)
      raise "Could not create line from (#{from_lat} #{from_long}) to (#{to_lat} #{to_long})" if line_id == -1
      journal("(\"openstreetmap.deleteLine\", @token, #{line_id})")
      return line_id, to_id
    end
    
    def newExtendedLine(from_node_id, to_lat, to_long)
      to_id = newNode(to_lat, to_long)
      line_id = call("newLine", from_node_id, to_id)
      raise "Could not create line from ID #{from_node_id} to (#{to_lat} #{to_long})" if line_id == -1
      journal("(\"openstreetmap.deleteLine\", @token, #{line_id})")
      return line_id, to_id
    end
    
    def assoc_zip(line_id, zip)
      call("updateStreetSegmentKeyValue", line_id, @zip_key_id, zip.to_s)
    end

    def newStreet(name, coords, from_zip = nil, to_zip = nil)
      log "newStreet [#{name}], ZIPs: [#{from_zip}] to [#{to_zip}], coords: #{coords_to_s(coords)}"
      raise "Attempt to create street with less than two coordinates" if coords.nil? || (coords.length < 2)
      line_id, prev_node_id = newLine(coords.first.first, coords.first[1], coords[1].first, coords[1][1])
      street_id = call("newStreet", line_id)
      raise "Could not create new street" if street_id == -1
      journal("(\"deleteStreet\", @token, #{street_id})")
      unless name.nil? || name.empty?
        success = call("updateStreetKeyValue", street_id, @name_key_id, name)
        raise "Could not name street #{street_id} \"#{name}\"" unless success
      end
      assoc_zip(line_id, from_zip) unless from_zip.nil? || from_zip.empty?
      if coords.length > 2
        (2..(coords.length - 1)).each do |i|
          to_lat, to_long = coords[i]
          line_id, prev_node_id = newExtendedLine(prev_node_id, to_lat, to_long)
          success = call("addSegmentToStreet", street_id, line_id)
          raise "Could not add segment #{line_id} to street #{street_id}" unless success
          journal("(\"dropSegmentFromStreet\", @token, #{street_id}, #{line_id})")
          assoc_zip(line_id, to_zip) unless to_zip.nil? || to_zip.empty? || (i < (coords.length - 1))
        end
      end
    end

    def get_nodes(from_lat, from_long, to_lat, to_long)
      data = call('getNodes', from_lat, from_long, to_lat, to_long)
      nodes = Node.create_nodes(data)
      nodes.each {|node| add_node node}
      nodes
    end

    def get_lines(from_lat, from_long, to_lat, to_long)
      data = get_nodes(from_lat, from_long, to_lat, to_long)
      ids = data.collect {|d| d.id}
      data = call('getLines', ids)
      Line.create_lines(data)
    end

    def add_node node
      @nodes[node.id] = node
    end

    def node id
      node = @nodes[id]

      if node.nil?
        
      end
    end

  end

end

