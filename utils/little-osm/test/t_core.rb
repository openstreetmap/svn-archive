require 'data/core'
require 'test/unit'
require 'test/unit/assertions'

class OsmDataTest < Test::Unit::TestCase
  
  def test_uid
    n = Node.new 1,2,123
    s = Segment.new Node.new(1,2),Node.new(3,4), 1
    w = Way.new 1
    assert_not_equal n.to_uid, s.to_uid
    assert_not_equal n.to_uid, w.to_uid
    assert_not_equal s.to_uid, w.to_uid
  end
  
  def test_class_uid
    assert_not_equal Node.to_uid(23), Segment.to_uid(23)
    assert_not_equal Node.to_uid(23), Way.to_uid(23)
    assert_not_equal Segment.to_uid(42), Way.to_uid(42)
  end
  
  def test_osm_primitive
    assert !OsmPrimitive.respond_to?(:timestamp=)
    
    osm = OsmPrimitive.new 1,Time.now
    assert_nil osm['key']
    osm['key'] = 'value'
    assert_equal 'value', osm['key']
    osm['key'] = nil
    assert_nil osm['key']
    assert_not_nil osm.timestamp
    assert osm.timestamp <= Time.now
    assert_equal 1,osm.to_i
  end
  
  def test_node
    n = Node.new 1,2, 123
    assert_equal 1,n.lat
    assert_equal 2,n.lon
    assert_equal 123, n.to_i
    assert_equal [1,2,1,2], n.bbox
  end
  
  def test_segment
    s = Segment.new Node.new(3,4), Node.new(1,2)
    assert_equal 4, s.from.lon
    assert_equal 1, s.to.lat
    assert_equal [1,2,3,4], s.bbox
  end
  
  def test_way
    w = Way.new [], 123
    assert_equal 123, w.to_i
    assert_equal 0, w.segment.size
    w = Way.new [Segment.new(Node.new(42,-2), Node.new(3,4)), Segment.new(Node.new(3,4), Node.new(5,6))]
    assert_equal 2, w.segment.size
    assert_equal 3, w.segment[0].to.lat
    assert_equal [3,-2,42,6], w.bbox
    w = Way.new [Segment.new(Node.new(-1,-1), Node.new(-1,-1))]
    assert_equal [-1,-1,-1,-1], w.bbox
  end
end

