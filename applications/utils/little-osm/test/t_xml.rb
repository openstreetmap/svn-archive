require 'data/xml'
require 'test/unit'
require 'test/unit/assertions'
require 'rexml/document'

class OsmDataXmlTest < Test::Unit::TestCase
  def test_node
    e = REXML::Document.new(Node.new(1,2,{'foo'=>'bar','baz'=>nil}).to_xml).root
    assert_equal "node", e.name
    assert_equal "0", e.attribute('id').value
    assert_equal 1.0, e.attribute('lat').value.to_f
    assert_equal 2.0, e.attribute('lon').value.to_f
    assert_equal [['foo','bar'],['baz',nil]], e.elements.to_a.collect do |x| [x.attribute('k'),x.attribute('v')] end
  end

  def test_node
    e = REXML::Document.new(Segment.new(Node.new(1,2,nil,42), Node.new(3,4,nil,23)).to_xml).root
    assert_equal 42, e.attribute('from').value.to_i
    assert_equal 23, e.attribute('to').value.to_i
  end

  def test_way
    e = REXML::Document.new(Way.new([Node.new(1,2,nil,42), Node.new(3,4,nil,23)]).to_xml).root
    segs = e.get_elements('seg')
    assert_equal 42, segs[0].attribute('id').value.to_i
    assert_equal 23, segs[1].attribute('id').value.to_i
  end
end
