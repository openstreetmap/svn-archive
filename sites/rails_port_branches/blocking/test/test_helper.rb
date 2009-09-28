ENV["RAILS_ENV"] = "test"
require File.expand_path(File.dirname(__FILE__) + "/../config/environment")
require 'test_help'
load 'composite_primary_keys/fixtures.rb'

class ActiveSupport::TestCase
  # Transactional fixtures accelerate your tests by wrapping each test method
  # in a transaction that's rolled back on completion.  This ensures that the
  # test database remains unchanged so your fixtures don't have to be reloaded
  # between every test method.  Fewer database queries means faster tests.
  #
  # Read Mike Clark's excellent walkthrough at
  #   http://clarkware.com/cgi/blosxom/2005/10/24#Rails10FastTesting
  #
  # Every Active Record database supports transactions except MyISAM tables
  # in MySQL.  Turn off transactional fixtures in this case; however, if you
  # don't care one way or the other, switching from MyISAM to InnoDB tables
  # is recommended.
  self.use_transactional_fixtures = false

  # Instantiated fixtures are slow, but give you @david where otherwise you
  # would need people(:david).  If you don't want to migrate your existing
  # test cases which use the @david style and don't mind the speed hit (each
  # instantiated fixtures translates to a database query per test method),
  # then set this back to true.
  self.use_instantiated_fixtures  = false


  # Load standard fixtures needed to test API methods
  def self.api_fixtures
    #print "setting up the api_fixtures"
    fixtures :users, :changesets, :changeset_tags

    fixtures :current_nodes, :nodes
    set_fixture_class :current_nodes => 'Node'
    set_fixture_class :nodes => 'OldNode'

    fixtures  :current_node_tags,:node_tags
    set_fixture_class :current_node_tags => 'NodeTag'
    set_fixture_class :node_tags => 'OldNodeTag'

    fixtures :current_ways
    set_fixture_class :current_ways => 'Way'

    fixtures :current_way_nodes, :current_way_tags
    set_fixture_class :current_way_nodes => 'WayNode'
    set_fixture_class :current_way_tags => 'WayTag'

    fixtures :ways
    set_fixture_class :ways => 'OldWay'

    fixtures :way_nodes, :way_tags
    set_fixture_class :way_nodes => 'OldWayNode'
    set_fixture_class :way_tags => 'OldWayTag'

    fixtures :current_relations
    set_fixture_class :current_relations => 'Relation'

    fixtures :current_relation_members, :current_relation_tags
    set_fixture_class :current_relation_members => 'RelationMember'
    set_fixture_class :current_relation_tags => 'RelationTag'

    fixtures :relations
    set_fixture_class :relations => 'OldRelation'

    fixtures :relation_members, :relation_tags
    set_fixture_class :relation_members => 'OldRelationMember'
    set_fixture_class :relation_tags => 'OldRelationTag'
    
    fixtures :gpx_files, :gps_points, :gpx_file_tags
    set_fixture_class :gpx_files => 'Trace'
    set_fixture_class :gps_points => 'Tracepoint'
    set_fixture_class :gpx_file_tags => 'Tracetag'

    fixtures :client_applications
  end

  ##
  # takes a block which is executed in the context of a different 
  # ActionController instance. this is used so that code can call methods
  # on the node controller whilst testing the old_node controller.
  def with_controller(new_controller)
    controller_save = @controller
    begin
      @controller = new_controller
      yield
    ensure
      @controller = controller_save
    end
  end

  ##
  # for some reason assert_equal a, b fails when the ways are actually
  # equal, so this method manually checks the fields...
  def assert_ways_are_equal(a, b)
    assert_not_nil a, "first way is not allowed to be nil"
    assert_not_nil b, "second way #{a.id} is not allowed to be nil"
    assert_equal a.id, b.id, "way IDs"
    assert_equal a.changeset_id, b.changeset_id, "changeset ID on way #{a.id}"
    assert_equal a.visible, b.visible, "visible on way #{a.id}, #{a.visible.inspect} != #{b.visible.inspect}"
    assert_equal a.version, b.version, "version on way #{a.id}"
    assert_equal a.tags, b.tags, "tags on way #{a.id}"
    assert_equal a.nds, b.nds, "node references on way #{a.id}"
  end

  ##
  # for some reason a==b is false, but there doesn't seem to be any 
  # difference between the nodes, so i'm checking all the attributes 
  # manually and blaming it on ActiveRecord
  def assert_nodes_are_equal(a, b)
    assert_equal a.id, b.id, "node IDs"
    assert_equal a.latitude, b.latitude, "latitude on node #{a.id}"
    assert_equal a.longitude, b.longitude, "longitude on node #{a.id}"
    assert_equal a.changeset_id, b.changeset_id, "changeset ID on node #{a.id}"
    assert_equal a.visible, b.visible, "visible on node #{a.id}"
    assert_equal a.version, b.version, "version on node #{a.id}"
    assert_equal a.tags, b.tags, "tags on node #{a.id}"
  end

  def basic_authorization(user, pass)
    @request.env["HTTP_AUTHORIZATION"] = "Basic %s" % Base64.encode64("#{user}:#{pass}")
  end

  def content(c)
    @request.env["RAW_POST_DATA"] = c.to_s
  end
  
  # Used to check that the error header and the forbidden responses are given
  # when the owner of the changset has their data not marked as public
  def assert_require_public_data(msg = "Shouldn't be able to use API when the user's data is not public")
    assert_response :forbidden, msg
    assert_equal @response.headers['Error'], "You must make your edits public to upload new data", "Wrong error message"
  end
  
  # Not sure this is the best response we could give
  def assert_inactive_user(msg = "an inactive user shouldn't be able to access the API")
    assert_response :unauthorized, msg
    #assert_equal @response.headers['Error'], ""
  end
  
  def assert_no_missing_translations(msg="")
    assert_select "span[class=translation_missing]", false, "Missing translation #{msg}"
  end
  
  # Add more helper methods to be used by all tests here...
end
