# Contains the basic data primitives of OSM. Tags
# can be accessed by using the [] operator.
# Timestamp is accessed via "timestamp" getter. The id
# can be retrieved as to_i.

# Primitives can be created either by specifying an
# array, in which case the order of the values is important
# or by specifying an hash, where the hash keys will be
# the parameters. Tags are always transfered as hashes.

# Example:

#  # create two nodes
#  node1 = Node.new :lat=>51, :lon=>1
#  node2 = Node.new 52, 1, {"name"=>"My Home", "class"=>"poi"}
#  # create a segment
#  segment = Segment.new :from=>node1, :to=>node2
#  # create a way
#  way = Way.new [segment]
#  # do some funny tag stuff
#  way[name] = "Way to " + node2[name].downcase


# In OSM, data primitives can be "incomplete", in which
# case only the id is known. Then, you may only call to_i
# to retrieve the id (in fact, you will get an integer instead
# of an OSM object for incomplete data types. That's the
# reason for accessing the id via .to_i ;)

# You can access an unique id from every complete primitive
# by calling .to_uid. This identifier is unique among all
# objects (not only among objects of the own class).
# There are conversion functions to retrieve the id and the
# class from an uid: uid_to_id and uid_to_class as well as
# to construct an uid: idclass_to_uid


module OSM

  # Base class for handling the common stuff in any primitive.
  class OsmPrimitive
    protected :initialize
    attr_reader :timestamp, :tags

    def initialize tags = {}, id = 0, timestamp = nil
      @tags, @id, @timestamp = (tags||{}), id.to_i, timestamp
      # be nice and try to convert from an SQL string..
      @timestamp = nil if %W{null NULL Null nil NIL Nil 0}.include? @timestamp
      @timestamp = Time.parse(@timestamp) if @timestamp and @timestamp.kind_of? String
    end

    def []= name, value; @tags[name] = value end
    def [] name; @tags[name] end

    # Access the id via to_i. This works for incomplete primitives as well
    def to_i; @id.to_i end

    # Return the unique identifier, which is unique among all OsmPrimitives
    def to_uid; idclass_to_uid self.to_i,self.class end
  end


  # A Node with latitude and longitude coordinates
  class Node < OsmPrimitive
    attr_accessor :lat, :lon

    # Create the node. Parameters are either specified directly or as a hash.
    # If specified directly, the parameter must be in the following order
    # (only lat and lon is required):  lat, lon, tags, id, timestamp
    def initialize params, *other
      if params.kind_of? Hash and other.empty?
        super params[:tags], params[:id], params[:timestamp]
        @lat, @lon = params[:lat].to_f, params[:lon].to_f
      else
        super(*other[1..-1])
        @lat, @lon = params.to_f, other[0].to_f
      end
    end

    def pos; [@lat,@lon] end
    def bbox; [@lat,@lon,@lat,@lon] end
  end

  # A segment with a "from" and a "to" node
  class Segment < OsmPrimitive
    attr_accessor :from, :to

    # Create a segment either with a hash or with specific parameter. See
    # Node#initialize for more.
    # The parameter order is (from and to required): from, to, tags, id, timestamp.
    def initialize params, *other
      if params.kind_of? Hash and other.empty?
        super params[:tags], params[:id], params[:timestamp]
        @from, @to = params[:from], params[:to]
      else
        super(*other[1..-1])
        @from, @to = params, other[0]
      end
    end

    def bbox
      lat = [from.lat, to.lat]
      lon = [from.lon, to.lon]
      [lat.min, lon.min, lat.max, lon.max]
    end
  end

  # A way with a list of segments
  class Way < OsmPrimitive
    attr_accessor :segments

    # Create a way either with a hash or with specific parameter. See
    # Node#initialize for more.
    def initialize params, *other
      if params.kind_of? Hash and other.empty?
        super params[:tags], params[:id], params[:timestamp]
        @segments = params[:segments]
      else
        super(*other)
        @segments = params
      end
    end

    def bbox
      bbox = [Float::MAX, Float::MAX, -Float::MAX, -Float::MAX]
      @segments.each do |s|
        sb = s.bbox
        bbox[0] = sb[0] if sb[0] < bbox[0]
        bbox[1] = sb[1] if sb[1] < bbox[1]
        bbox[2] = sb[2] if sb[2] > bbox[2]
        bbox[3] = sb[3] if sb[3] > bbox[3]
      end
      bbox
    end
  end

  # Some short uid helper functions.

  # Return the osm-id that is behind the uid
  def uid_to_id uid; uid.to_i >> 3 end

  # Return the class this uid belongs to
  def uid_to_class uid; [Node, Segment, Way][uid.to_i&7] end

  # Return the uid for this id/class pair
  def idclass_to_uid id,klass
    klass = eval klass.capitalize if klass.kind_of? String
    (id.to_i<<3)+[Node,Segment,Way].index(klass)
  end

end
