module MapBoundary
  def sanitise_boundaries(bbox)
    min_lon = [bbox[0].to_f,-180].max
    min_lat = [bbox[1].to_f,-90].max
    max_lon = [bbox[2].to_f,+180].min
    max_lat = [bbox[3].to_f,+90].min

    return min_lon, min_lat, max_lon, max_lat
  end

  def check_boundaries(min_lon, min_lat, max_lon, max_lat)
    # check the bbox is sane
    unless min_lon <= max_lon
      raise("The minimum longitude must be less than the maximum longitude, but it wasn't")
    end
    unless min_lat <= max_lat
      raise("The minimum latitude must be less than the maximum latitude, but it wasn't")
    end
    unless min_lon >= -180 && min_lat >= -90 && max_lon <= 180 && max_lat <= 90
      # Due to sanitize_boundaries, it is highly unlikely we'll actually get here
      raise("The latitudes must be between -90 and 90, and longitudes between -180 and 180")
    end

    # check the bbox isn't too large
    requested_area = (max_lat-min_lat)*(max_lon-min_lon)
    if requested_area > APP_CONFIG['max_request_area']
      raise("The maximum bbox size is " + APP_CONFIG['max_request_area'].to_s + 
        ", and your request was too large. Either request a smaller area, or use planet.osm")
    end
  end
end
