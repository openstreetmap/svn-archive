ENV["RAILS_ENV"] = "test"
require File.expand_path(File.dirname(__FILE__) + "/../config/environment")
require 'test_help'

class Test::Unit::TestCase
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
    fixtures :users

    fixtures :current_nodes, :nodes
    set_fixture_class :current_nodes => :Node
    set_fixture_class :nodes => :OldNode

    fixtures :current_segments, :segments
    set_fixture_class :current_segments => :Segment
    set_fixture_class :segments => :OldSegment

    fixtures :current_ways, :current_way_segments, :current_way_tags
    set_fixture_class :current_ways => :Way
    set_fixture_class :current_way_segments => :WaySegment
    set_fixture_class :current_way_tags => :WayTag

    fixtures :ways, :way_segments, :way_tags
    set_fixture_class :ways => :OldWay
    set_fixture_class :way_segments => :OldWaySegment
    set_fixture_class :way_tags => :OldWayTag
  end

  # Add more helper methods to be used by all tests here...
end
