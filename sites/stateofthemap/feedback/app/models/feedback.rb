class Feedback < ActiveRecord::Base
  belongs_to :user
 
  #validates_numericality_of :score
  #validates_length_of :comments, :within => 12..500

  def validate
    if comments
      errors.add_to_base "Too many comments" unless comments.length < 500
    end

  end
end
