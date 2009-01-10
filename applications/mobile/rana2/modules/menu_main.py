#!/usr/bin/python
#----------------------------------------------------------------------
# Main menu for Rana
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

class menu_main(RanaModule):
  """Displays the title screen"""
  def __init__(self, m, d, name):
    RanaModule.__init__(self,m,d,name)
  def startup(self):
    self.registerPage('menu')
    self.registerPage('modes')

  def page_modes(self):
    # All buttons are just labelled with the title-case version of their mode name
    buttons = [page_button(self, page, page.lower()) for page in ("Map", "Compass", "Speedo", "Postcodes","GPS")]
    return(create_menu(self, self.d['history:-1'], buttons))

  def page_menu(self):
    buttons = [
      page_button(self, "Mode", "modes"),
      page_button(self, "Add POI", "add_poi")
      ]
    return(create_menu(self, 'map', buttons))

