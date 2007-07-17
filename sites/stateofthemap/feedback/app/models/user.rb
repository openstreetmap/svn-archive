class User < ActiveRecord::Base
  
  has_many :talks
  has_many :feedbacks
  
  validates_uniqueness_of :email

CHARS = 'abcdefghijklmnopqrtuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  def self.make_token(length=32)
    rndchars = ''

    length.times do
      rndchars += CHARS[(rand * CHARS.length).to_i].chr
    end

    return rndchars
  end


end
