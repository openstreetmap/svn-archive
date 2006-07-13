require 'data/xml'
require 'test/unit'
require 'test/unit/assertions'

class OsmDataXmlTest < Test::Unit::TestCase
  def test_primitive
    osm = OsmPrimitive.new 23, (time=Time.now)
    osm['foo'] = 'bar'
    osm['bar'] = 'foo'
    osm['bar'] = nil
    e = osm.to_xml
    assert_equal 23, e.attribute('id').value.to_i
    assert_equal time.xmlschema, e.attribute('timestamp').value
    e.write(out="")
    assert_equal %Q{<osmprimitive timestamp='#{time.xmlschema}' id='23'><tag k='foo' v='bar'/><tag k='bar'/></osmprimitive>}, out
  end
  
  def test_node
    e = Node.new(1,2).to_xml
    assert_equal 1.0, e.attribute('lat').value.to_f
    assert_equal 2.0, e.attribute('lon').value.to_f
  end

  def test_node
    e = Segment.new(Node.new(1,2,42), Node.new(3,4,23)).to_xml
    assert_equal 42, e.attribute('from').value.to_i
    assert_equal 23, e.attribute('to').value.to_i
  end

  def test_way
    e = Way.new([Node.new(1,2,42), Node.new(3,4,23)]).to_xml
    segs = e.get_elements('seg')
    assert_equal 42, segs[0].attribute('id').value.to_i
    assert_equal 23, segs[1].attribute('id').value.to_i
  end
end
