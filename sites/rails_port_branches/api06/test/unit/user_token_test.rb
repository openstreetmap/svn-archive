require File.dirname(__FILE__) + '/../test_helper'

class UserTokenTest < Test::Unit::TestCase
  api_fixtures
  fixtures :user_tokens

  def test_user_token_count
    assert_equal 0, UserToken.count
  end
  
end
