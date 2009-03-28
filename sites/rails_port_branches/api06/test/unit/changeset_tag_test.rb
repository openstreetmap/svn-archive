require File.dirname(__FILE__) + '/../test_helper'

class ChangesetTagTest < Test::Unit::TestCase
  api_fixtures

  def test_changeset_tag_count
    assert_equal 1, ChangesetTag.count
  end
  
  def test_length_key_valid
    key = "k"
    (0..255).each do |i|
      tag = ChangesetTag.new
      tag.id = 1
      tag.k = key*i
      tag.v = "v"
      assert_valid tag
    end
  end
  
  def test_length_value_valid
    val = "v"
    (0..255).each do |i|
      tag = ChangesetTag.new
      tag.id = 1
      tag.k = "k"
      tag.v = val*i
      assert_valid tag
    end
  end
  
  def test_length_key_invalid
    ["k"*256].each do |k|
      tag = ChangesetTag.new
      tag.id = 1
      tag.k = k
      tag.v = "v"
      assert !tag.valid?, "Key #{k} should be too long"
      assert tag.errors.invalid?(:k)
    end
  end
  
  def test_length_value_invalid
    ["v"*256].each do |v|
      tag = ChangesetTag.new
      tag.id = 1
      tag.k = "k"
      tag.v = v
      assert !tag.valid?, "Value #{v} should be too long"
      assert tag.errors.invalid?(:v)
    end
  end
  
  def test_empty_tag_invalid
    tag = ChangesetTag.new
    assert !tag.valid?, "Empty tag should be invalid"
    assert tag.errors.invalid?(:id)
  end
  
  def test_uniqueness
    tag = ChangesetTag.new
    tag.id = changeset_tags(:changeset_1_tag_1).id
    tag.k = changeset_tags(:changeset_1_tag_1).k
    tag.v = changeset_tags(:changeset_1_tag_1).v
    assert tag.new_record?
    assert !tag.valid?
    assert_raise(ActiveRecord::RecordInvalid) {tag.save!}
    assert tag.new_record?
  end
end
