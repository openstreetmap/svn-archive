require '../tools'
require 'test/unit'
require 'test/unit/assertions'

include OSM

class ToolsTest < Test::Unit::TestCase

  def test_ok
    assert OK =~ /200\/OK/
    assert OK =~ /content-type:/i
  end

  def test_header
    assert HEADER =~ /UTF-8/
    assert HEADER =~ /\<osm/
  end

end
