require '../tools'
require 'test/unit'
require 'test/unit/assertions'
require 'stringio'
require 'uri'

class ToolsTest < Test::Unit::TestCase

  def setup
    @msg = StringIO.new ""
    @old, $stdout = $stdout, @msg
  end

  def teardown
    $stdout = @old
  end

  def test_ok
    ok
    str = @msg.string
    assert str =~ /200\/OK/
    assert str =~ /content-type:/i
    assert str =~ /content-type:/i
  end
  
  def test_header
    header
    str = @msg.string
    assert str =~ /UTF-8/
    assert str =~ /\<osm/
  end
  
  def test_get_queries
    Thread.current['uri'] = URI.parse "foo.html?a=b&c=d&e&f=g"
    q = get_queries
    assert_equal 'b', q['a']
    assert_equal 'd', q['c']
    assert_equal '', q['e']
    assert_equal 'g', q['f']
  end
end
