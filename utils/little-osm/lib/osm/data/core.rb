class OsmPrimitive
  attr_reader :timestamp, :tags
  
  def initialize id, timestamp
    @id, @timestamp, @tags = id.to_i, timestamp, {}
    @timestamp = nil if @timestamp == "null"
    @timestamp = Time.parse(@timestamp) if @timestamp and @timestamp.kind_of? String
  end
  
  def []= name, value; @tags[name] = value end
  def [] name; @tags[name] end
  def to_i; @id.to_i end
  def to_uid; self.class.to_uid(self.to_i) end
end


class Node < OsmPrimitive
  attr_accessor :lat, :lon
  def initialize lat, lon, id = 0, timestamp = nil
    super id, timestamp
    @lat, @lon = lat.to_f, lon.to_f
  end
  
  def Node.to_uid id; id<<3 end
  def pos; [@lat,@lon] end
  def bbox; [@lat,@lon,@lat,@lon] end
end

class Segment < OsmPrimitive
  attr_accessor :from, :to
  def initialize from, to, id = 0, timestamp = nil
    super id, timestamp
    @from, @to = from, to
  end
  
  def Segment.to_uid id; (id<<3) + 1 end
  def bbox
    lat = [from.lat, to.lat]
    lon = [from.lon, to.lon]
    [lat.min, lon.min, lat.max, lon.max]
  end
end

class Way < OsmPrimitive
  attr_accessor :segment
  def initialize segment=[], id=0, timestamp=nil
    super id, timestamp
    @segment = segment
  end
  
  def Way.to_uid id; (id<<3) + 2 end
  def bbox
    bbox = [Float::MAX, Float::MAX, Float::MIN, Float::MIN]
    segment.each do |s|
      sb = s.bbox
      bbox[0] = sb[0] if sb[0] < bbox[0]
      bbox[1] = sb[1] if sb[1] < bbox[1]
      bbox[2] = sb[2] if sb[2] > bbox[2]
      bbox[3] = sb[3] if sb[3] > bbox[3]
    end
    bbox
  end
end
