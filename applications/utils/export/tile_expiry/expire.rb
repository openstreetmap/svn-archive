#!/usr/bin/ruby

require 'rubygems'
require 'proj4'
require 'xml/libxml'
require 'set'
require 'postgres'

module Expire
  # projection object to go from latlon -> spherical mercator
  PROJ = Proj4::Projection.new(["+proj=merc", "+a=6378137", "+b=6378137", 
                                "+lat_ts=0.0", "+lon_0=0.0", "+x_0=0.0",
                                "+y_0=0", "+k=1.0", "+units=m", 
                                "+nadgrids=@null", "+no_defs +over"])
  
  # width/height of the spherical mercator projection
  SIZE=40075016.6855784
  # the size of the meta tile blocks
  METATILE = 8
  # the directory root for meta tiles
  HASH_ROOT = "/var/www/direct/"
  # lowest zoom that we want to expire
  MIN_ZOOM=5
  # highest zoom that we want to expire
  MAX_ZOOM=18
  # database parameters
  DBNAME="gis"
  DBHOST=""
  DBPORT=5432
  DBTABLE="planet_osm_nodes"
  
  # turns a spherical mercator coord into a tile coord
  def Expire.tile_from_merc(point, zoom)
    # renormalise into unit space [0,1]
    point.x = 0.5 + point.x / SIZE
    point.y = 0.5 - point.y / SIZE
    # transform into tile space
    point.x = point.x * 2 ** zoom
    point.y = point.y * 2 ** zoom
    # chop of the fractional parts
    [point.x.to_int, point.y.to_int, zoom]
  end
  
  # turns a latlon -> tile x,y given a zoom level
  def Expire.tile_from_latlon(latlon, zoom)
    # first convert to spherical mercator
    point = PROJ.forward(latlon)
    tile_from_merc(point, zoom)
  end
  
  # this must match the definition of xyz_to_meta in mod_tile
  def Expire.xyz_to_meta(root, x, y, z)
    # mask off the final few bits
    x &= ~(METATILE - 1)
    y &= ~(METATILE - 1)
    # generate the path
    hash_path = (0..4).collect { |i| 
      (((x >> 4*i) & 0xf) << 4) | ((y >> 4*i) & 0xf) 
    }.reverse.join('/')
    root + '/' + z.to_s + '/' + hash_path + ".meta"
  end
  
  # expire the meta tile by setting the modified time back to some
  # very stupidly early time, before OSM started
  def Expire.expire_meta(meta)
    puts "Expiring #{meta}"
    `touch -t 200001010000 #{meta}`
  end
  
  def Expire.expire(change_file)
    do_expire(change_file) do |set|
      new_set = Set.new
      meta_set = Set.new

      # turn all the tiles into expires, putting them in the set
      # so that we don't expire things multiple times
      set.each do |xy|
        # this has to match the routine in mod_tile
        meta = xyz_to_meta(HASH_ROOT, xy[0], xy[1], xy[2])
        
        meta_set.add(meta) if File.exist? meta
        
        # add the parent into the set for the next round
        new_set.add([xy[0] / 2, xy[1] / 2, xy[2] - 1])
      end
      
      # expire all meta tiles
      meta_set.each do |meta|
        expire_meta(meta)
      end

      # return the new set, consisting of all the parents
      new_set
    end
  end

  def Expire.do_expire(change_file, &block)
    # read in the osm change file
    doc = XML::Document.file(change_file)
    
    # hash map to contain all the nodes
    nodes = Hash.new
    
    # we put all the nodes into the hash, as it doesn't matter whether the node was
    # added, deleted or modified - the tile will need updating anyway.
    doc.find('//node').each do |node|
      point = Proj4::Point.new(Math::PI * node['lon'].to_f / 180, 
                               Math::PI * node['lat'].to_f / 180)
      nodes[node['id'].to_i] = tile_from_latlon(point, MAX_ZOOM)
    end
    
    # now we look for all the ways that have changed and put all of their nodes into
    # the hash too. this will add too many nodes, as it is possible a long way will be
    # changed at only a portion of its length. however, due to the non-local way that
    # mapnik does text placement, it may stil not be enough.
    #
    # also, we miss cases where nodes are deleted from ways where that node is not 
    # itself deleted and the coverage of the point set isn't enough to encompass the
    # change.
    conn = PGconn.connect(DBHOST, DBPORT, "", "", DBNAME)
    doc.find('//way/nd').each do |node|
      node_id = node['ref'].to_i
      unless nodes.include? node_id
        # this is a node referenced but not added, modified or deleted, so it should
        # still be in the postgis DB.
        res = conn.query("select lon, lat from #{DBTABLE} where id=#{node_id};")
        
        # bizarrely, sometimes we can't find the node. this seems to happen when the 
        # node is created and deleted within the diff period and (i guess therefore) 
        # doesn't appear in the diff, but may be referenced in a way. so... the way 
        # to deal with them is just to ignore them and output a warning?
        if res.empty? 
          puts "Failed to look up node #{node_id} referenced in way #{node.parent['id']}."
          
        else
          point = Proj4::Point.new(res[0][0].to_f, res[0][1].to_f)
          nodes[node_id] = tile_from_merc(point, MAX_ZOOM)
        end
      end
    end
    
    # create a set of all the tiles at the maximum zoom level which are touched by 
    # any of the nodes we've collected. we'll create the tiles at other zoom levels
    # by a simple recursion.
    set = Set.new nodes.values
    
    # expire tiles and shrink to the set of parents
    (MAX_ZOOM).downto(MIN_ZOOM) do |z|
      # allow the block to work on the set, returning the set at the next
      # zoom level
      set = yield set
    end
  end
end
