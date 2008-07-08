require File.dirname(__FILE__) + '/../test_helper'

class OldNodeTest < Test::Unit::TestCase
  fixtures :current_nodes, :users, :current_node_tags, :nodes, :node_tags
  set_fixture_class :current_nodes => :Node
  set_fixture_class :nodes => :OldNode
  set_fixture_class :node_tags => :OldNodeTag
  set_fixture_class :current_node_tags => :NodeTag
    
  def test_node_too_far_north
	  invalid_node_test(:node_too_far_north)
  end
  
  def test_node_north_limit
    valid_node_test(:node_north_limit)
  end
  
  def test_node_too_far_south
    invalid_node_test(:node_too_far_south)
  end
  
  def test_node_south_limit
    valid_node_test(:node_south_limit)
  end
  
  def test_node_too_far_west
    invalid_node_test(:node_too_far_west)
  end
  
  def test_node_west_limit
    valid_node_test(:node_west_limit)
  end
  
  def test_node_too_far_east
    invalid_node_test(:node_too_far_east)
  end
  
  def test_node_east_limit
    valid_node_test(:node_east_limit)
  end
  
  def test_totally_wrong
    invalid_node_test(:node_totally_wrong)
  end
  
  # This helper method will check to make sure that a node is within the world, and
  # has the the same lat, lon and timestamp than what was put into the db by 
  # the fixture
  def valid_node_test(nod)
    node = nodes(nod)
    dbnode = Node.find(node.id)
    assert_equal dbnode.lat, node.latitude.to_f/SCALE
    assert_equal dbnode.lon, node.longitude.to_f/SCALE
    assert_equal dbnode.user_id, node.user_id
    assert_equal dbnode.version, node.version
    assert_equal dbnode.visible, node.visible
    assert_equal dbnode.timestamp, node.timestamp
    #assert_equal node.tile, QuadTile.tile_for_point(nodes(nod).lat, nodes(nod).lon)
    assert_valid node
  end
  
  # This helpermethod will check to make sure that a node is outwith the world, 
  # and has the same lat, lon and timesamp than what was put into the db by the
  # fixture
  def invalid_node_test(nod)
    node = nodes(nod)
    dbnode = Node.find(node.id)
    assert_equal dbnode.lat, node.latitude.to_f/SCALE
    assert_equal dbnode.lon, node.longitude.to_f/SCALE
    assert_equal dbnode.user_id, node.user_id
    assert_equal dbnode.version, node.version
    assert_equal dbnode.visible, node.visible
    assert_equal dbnode.timestamp, node.timestamp
    #assert_equal node.tile, QuadTile.tile_for_point(nodes(nod).lat, nodes(nod).lon)
    assert_equal false, node.valid?
  end
  

end
