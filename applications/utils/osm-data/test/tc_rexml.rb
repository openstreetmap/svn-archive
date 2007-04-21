$: << File.dirname(__FILE__)+'/../lib'

require 'osm/rexml'

require 'test/unit'
require 'test/unit/assertions'
require 'rexml/document'

# Simplify the !"§!"%!"$§ REXML a bit
module REXML
  class Element
    def [] symbol
      a = self.attribute(symbol.to_s)
      a.nil? ? nil : a.value
    end
    def []= symbol, value; self.add_attribute(symbol.to_s, value)end
    def == other; self.to_s == other.to_s end
    def eql? other; self.to_s.eql? other.to_s end
  end
end


include OSM

class ReXmlOsmPrimitiveTest < Test::Unit::TestCase

  class MyOsmPrimitive < OsmPrimitive
    def self.canonical_name
      "test"
    end
  end

  def test_to_xml
    osm = MyOsmPrimitive.new
    osm[:foo] = "bar"
    e = REXML::Document.new(osm.to_xml).root
    assert_equal 1, e.elements.size
    assert_equal "foo", e.elements[1][:k]
    assert_equal "bar", e.elements[1][:v]
  end
  
end


class ReXmlNodeTest < Test::Unit::TestCase

  def make_node *args
    Node.new(*args).to_rexml
  end

  def test_name
    e = make_node nil
    assert_equal "node", e.name
  end

  def test_id
    e = make_node :id => 23
    assert_equal "23", e[:id]
  end
  
  def test_latlon
    e = make_node :lat => 1.0, :lon => 2.0
    assert_equal "1.0", e[:lat]
    assert_equal "2.0", e[:lon]
  end

  def test_attrs
    tags = {'name' => 'Baker Street', 'oneway' => nil}
    e = make_node :tags => tags
    assert_equal tags.size, e.elements.size
    assert_equal ['tag', 'tag'], e.elements.map {|x| x.name}
    assert_equal tags, Hash[*e.elements.to_a.map{|x| [x[:k], x[:v]] }.flatten]
  end
  
  def test_to_rexml
    e = make_node :id => 23, :lat => 51.1, :lon => 11.2
    e2 = REXML::Element.new("node")
    e2[:id] = '23'
    e2[:lat] = 51.1;
    e2[:lon] = 11.2;
    assert_equal e2, e
  end

end


class ReXmlSegmentTest < Test::Unit::TestCase
  
  def make_segment *args
    REXML::Document.new(Segment.new(*args).to_xml).root
  end
  

  def test_segment_fromto
    e = make_segment :from => 23, :to => 42 #incomplete nodes
    assert_equal "23", e[:from]
    assert_equal "42", e[:to]
  end

end
