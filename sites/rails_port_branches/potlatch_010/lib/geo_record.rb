module GeoRecord
  def self.included(base)
    base.extend(ClassMethods)
  end

  def before_save
    self.update_tile
  end

  # Is this node within -90 >= latitude >= 90 and -180 >= longitude >= 180
  # * returns true/false
  def in_world?
    return false if self.lat < -90 or self.lat > 90
    return false if self.lon < -180 or self.lon > 180
    return true
  end

  def update_tile
    self.tile = QuadTile.tile_for_point(lat, lon)
  end

  def lat=(l)
    self.latitude = (l * 10000000).round
  end

  def lon=(l)
    self.longitude = (l * 10000000).round
  end

  # Return WGS84 latitude
  def lat
    return self.latitude.to_f / 10000000
  end

  # Return WGS84 longitude
  def lon
    return self.longitude.to_f / 10000000
  end

  # Potlatch projections
  def lon_potlatch(baselong,masterscale)
    (self.lon-baselong)*masterscale
  end

  def lat_potlatch(basey,masterscale)
    -(lat2y(self.lat)-basey)*masterscale
  end
  
private
  
  def lat2y(a)
    180/Math::PI * Math.log(Math.tan(Math::PI/4+a*(Math::PI/180)/2))
  end

  module ClassMethods
    def find_by_area(minlat, minlon, maxlat, maxlon, options)
      self.with_scope(:find => {:conditions => OSM.sql_for_area(minlat, minlon, maxlat, maxlon)}) do
        return self.find(:all, options)
      end
    end
  end
end

