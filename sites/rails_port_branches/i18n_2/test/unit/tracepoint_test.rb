require File.dirname(__FILE__) + '/../test_helper'

class TracepointTest < Test::Unit::TestCase
  api_fixtures
  
  def test_tracepoint_count
    assert_equal 1, Tracepoint.count
  end
  
end
