require File.dirname(__FILE__) + '/../test_helper'

class ChangesetTest < ActiveSupport::TestCase
  api_fixtures
  
  def test_changeset_count
    assert_equal 7, Changeset.count
  end
  
end
