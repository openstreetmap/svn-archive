require File.dirname(__FILE__) + '/../test_helper'

class DiaryEntryTest < Test::Unit::TestCase
  api_fixtures
  fixtures :diary_entries, :languages
  
  def test_diary_entry_count
    assert_equal 2, DiaryEntry.count
  end
  
  def test_diary_entry_validations
    diary_entry_valid({})
    diary_entry_valid({:title => ''}, false)
    diary_entry_valid({:title => 'a'*255})
    diary_entry_valid({:title => 'a'*256}, false)
    diary_entry_valid({:body => ''}, false)
    diary_entry_valid({:latitude => 90})
    diary_entry_valid({:latitude => 90.00001}, false)
    diary_entry_valid({:latitude => -90})
    diary_entry_valid({:latitude => -90.00001}, false)
    diary_entry_valid({:longitude => 180})
    diary_entry_valid({:longitude => 180.00001}, false)
    diary_entry_valid({:longitude => -180})
    diary_entry_valid({:longitude => -180.00001}, false)
  end
  
  def diary_entry_valid(attrs, result = true)
    entry = diary_entries(:normal_user_entry_1).clone
    entry.attributes = attrs
    assert_equal result, entry.valid?, "Expected #{attrs.inspect} to be #{result}"
  end
  
end
