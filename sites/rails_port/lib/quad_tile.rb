module QuadTile
  begin
    require "quad_tile/quad_tile_so"
  rescue MissingSourceFile
    def self.tile_for_point(lat, lon)
      x = ((lon + 180) * 65535 / 360).round
      y = ((lat + 90) * 65535 / 180).round

      return tile_for_xy(x, y)
    end

    def self.tiles_for_area(minlat, minlon, maxlat, maxlon)
      minx = ((minlon + 180) * 65535 / 360).round
      maxx = ((maxlon + 180) * 65535 / 360).round
      miny = ((minlat + 90) * 65535 / 180).round
      maxy = ((maxlat + 90) * 65535 / 180).round
      tiles = []

      minx.upto(maxx) do |x|
        miny.upto(maxy) do |y|
          tiles << tile_for_xy(x, y)
        end
      end

      return tiles
    end

    def self.tile_for_xy(x, y)
      t = 0

      16.times do
        t = t << 1
        t = t | 1 unless (x & 0x8000).zero?
        x <<= 1
        t = t << 1
        t = t | 1 unless (y & 0x8000).zero?
        y <<= 1
      end

      return t
    end
  end

  def self.sql_for_area(minlat, minlon, maxlat, maxlon)
    sql = Array.new
    single = Array.new

    iterate_tiles_for_area(minlat, minlon, maxlat, maxlon) do |first,last|
      if first == last
        single.push(first)
      else
        sql.push("tile BETWEEN #{first} AND #{last}")
      end
    end

    sql.push("tile IN (#{single.join(',')})") if single.size > 0

    return "( " + sql.join(" OR ") + " )"
  end

  def self.iterate_tiles_for_area(minlat, minlon, maxlat, maxlon)
    tiles = tiles_for_area(minlat, minlon, maxlat, maxlon)
    first = last = nil

    tiles.sort.each do |tile|
      if last.nil?
        first = last = tile
      elsif tile == last + 1
        last = tile
      else
        yield first, last

        first = last = tile
      end
    end

    yield first, last unless last.nil?
  end

  private_class_method :tile_for_xy, :iterate_tiles_for_area
end
