class Statistic < ActiveRecord::Base

  validates_presence_of :locale
  validates_presence_of :language

end
