require File.dirname(__FILE__) + '/../test_helper'

class ShortLinkTest < ActiveSupport::TestCase
  ##
  # tests that encoding and decoding are working to within
  # the acceptable quantisation range.
  def test_encode_decode
    cases = Array.new
    1000.times do 
      cases << [ 180.0 * rand - 90.0, 360.0 * rand - 180.0, (18 * rand).to_i ]
    end

    cases.each do |lat, lon, zoom|
      lon2, lat2, zoom2 = ShortLink.decode(ShortLink.encode(lon, lat, zoom))
      # zooms should be identical
      assert_equal zoom, zoom2, "Decoding a encoded short link gives different zoom for (#{lat}, #{lon}, #{zoom})."
      # but the location has a quantisation error introduced at roughly 
      # one pixel (i.e: zoom + 8). the sqrt(5) is because each position 
      # has an extra bit of accuracy in the lat coordinate, due to the 
      # smaller range.
      distance = Math.sqrt((lat - lat2) ** 2 + (lon - lon2) ** 2)
      max_distance = 360.0 / (1 << (zoom + 8)) * 0.5 * Math.sqrt(5)
      assert max_distance > distance, "Maximum expected error exceeded: #{max_distance} <= #{distance} for (#{lat}, #{lon}, #{zoom})."
    end
  end
end
