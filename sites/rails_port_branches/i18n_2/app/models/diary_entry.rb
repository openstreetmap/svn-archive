class DiaryEntry < ActiveRecord::Base
  belongs_to :user
  belongs_to :language, :foreign_key => 'language'
  
  has_many :diary_comments, :include => :user,
                            :conditions => ["users.visible = ?", true],
                            :order => "diary_comments.id"

  validates_presence_of :title, :body
  validates_length_of :title, :within => 1..255
  #validates_length_of :language, :within => 2..5, :allow_nil => false
  validates_numericality_of :latitude, :allow_nil => true,
                            :greater_than_or_equal_to => -90, :less_than_or_equal_to => 90
  validates_numericality_of :longitude, :allow_nil => true,
                            :greater_than_or_equal_to => -180, :less_than_or_equal_to => 180
  validates_associated :user
end
