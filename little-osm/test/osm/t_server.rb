require 'test/unit'
require 'osm/server'

include WEBrick

class OsmServerTest < Test::Unit::TestCase
  
  def test_mounts_exist
    s = OsmServer.new :Logger => Log.new([]) # disable log output
    mounts = s.instance_variable_get(:@mount_tab).instance_variable_get(:@tab)
    mounts.each do |x|
      next unless x[0] =~ /\/api\//
      assert_equal HTTPServlet::CGIHandler, x[1][0]
      assert File.exist?(x[1][1][0])
    end

  end

end
