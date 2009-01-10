#!/usr/bin/python
#----------------------------------------------------------------------
# Add a point-of-interest (POI) to the map
#----------------------------------------------------------------------
# Copyright 2008-2009, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#----------------------------------------------------------------------
from _base import *
from _menu import *
import gtk
import re

CITY_LIST = (
  "Aberdeen", 
  "Armagh", 
  "Bangor", 
  "Bath", 
  "Belfast", 
  "Birmingham", 
  "Bradford", 
  "Brighton", 
  "Bristol", 
  "Cambridge", 
  "Canterbury", 
  "Cardiff", 
  "Carlisle", 
  "Chester", 
  "Chichester", 
  "London", 
  "Coventry", 
  "Derby", 
  "Dundee", 
  "Durham", 
  "Edinburgh", 
  "Ely", 
  "Exeter", 
  "Glasgow", 
  "Gloucester", 
  "Hereford", 
  "Inverness", 
  "Kingston upon Hull", 
  "Lancaster", 
  "Leeds", 
  "Leicester", 
  "Lichfield", 
  "Lincoln", 
  "Lisburn", 
  "Liverpool", 
  "Londonderry", 
  "Manchester", 
  "Newcastle", 
  "Newport", 
  "Newry", 
  "Norwich", 
  "Nottingham", 
  "Oxford", 
  "Peterborough", 
  "Plymouth", 
  "Portsmouth", 
  "Preston", 
  "Ripon", 
  "Salford", 
  "Salisbury", 
  "Sheffield", 
  "Southampton", 
  "St Albans", 
  "St Davids", 
  "Stirling", 
  "Stoke on Trent", 
  "Sunderland", 
  "Swansea", 
  "Truro", 
  "Wakefield", 
  "Wells", 
  "Westminster", 
  "Winchester", 
  "Wolverhampton", 
  "Worcester", 
  "York", )

class add_poi(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
    self.dialog_input = {'city':''}
    
  def startup(self):
    self.registerPage('add_poi')
    
  def update_search(self, widget, params):
    (name,value) = params
    print "updating %s" % self.name
    if(value == "OK"):
      print "Selected %s" % self.selected_result
    elif(value == "Clear"):
      self.dialog_input[name] = ''
      self.search_result_index = 0
    elif(value == "Up"):
      self.search_result_index -= 1
      if(self.search_result_index < 0):
        self.search_result_index = 0
    elif(value == "Down"):
      self.search_result_index += 1
    elif(value == "Cancel"):
      print "Cancelled"
    else:
      self.dialog_input[name] += "[%s]"%value

    reg_exp = re.compile(self.dialog_input[name], re.IGNORECASE)
    options = []
    skip = max(self.search_result_index-3, 0)
    count = 0
    search_anywhere = self.d.get("text_search_anywhere", False) # whether to allow searching for text in the middle of string, e.g. "Dav" match "St Davids"
    for candidate in CITY_LIST:
      if(search_anywhere):
        match = reg_exp.search(candidate)
      else:
        match = reg_exp.match(candidate)
      if(match):
        if(skip == 0):
          if(count == self.search_result_index):
            self.selected_result = candidate
            candidate = "*** "+ candidate + " ***"
          options.append(candidate)
          if(len(options) > 6):
            break
        else:
          skip -= 1
        count += 1
    #self.search_result_widget.set_text("\n".join(options))
    
  def page_add_poi(self):
    return(search_menu(self, 'menu', "city"))
