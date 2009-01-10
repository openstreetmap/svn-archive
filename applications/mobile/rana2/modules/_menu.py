#!/usr/bin/python
#----------------------------------------------------------------------
# Utility functions for creating menus
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
import gtk

def page_button(self, text, page):
  return(create_button(self, text, self.getFn('change_page'), page))

def create_button(self, text, function=None, params=None, template=None):
  if(template):
    btn = gtk.Button(text, template)
  else:
    btn = gtk.Button(text)
  if(function):
    btn.connect("clicked", function, params)
  return(btn)

def parent_button(self, parent_menu):
  btn = gtk.Button("Cancel", gtk.STOCK_CANCEL)
  btn.connect("clicked", self.getFn('change_page'), parent_menu)
  return(btn)

def search_menu_item(self, text, icon, variable):
  btn = gtk.Button(text, icon)
  btn.connect("clicked", self.update_search, (variable, text))
  return(btn)

def search_menu(self, parent, variable):
  print "Creating search menu"
  letter_sets = ("abc", "def","ghi","jkl","mno","pqrs","tuv","wxyz")
  (dx,dy) = (3,5)
  table = gtk.Table(rows=dy, columns=dx, homogeneous=True)

  (x,y) = (1,2)
  for letters in letter_sets:
    
    table.attach(search_menu_item(self, letters, None, variable),
      x, x+1, 
      y, y+1, 
      gtk.FILL|gtk.EXPAND, 
      gtk.FILL|gtk.EXPAND)
    x += 1
    if(x >= dx):
      x = 0
      y += 1
      
  table.attach(search_menu_item(self, "OK", gtk.STOCK_OK, variable),
    2, 3, 
    0, 1, 
    gtk.FILL|gtk.EXPAND, 
    gtk.FILL|gtk.EXPAND)
  table.attach(search_menu_item(self, "Clear", gtk.STOCK_CLEAR, variable),
    0, 1, 
    2, 3, 
    gtk.FILL|gtk.EXPAND, 
    gtk.FILL|gtk.EXPAND)
  table.attach(search_menu_item(self, "Up", gtk.STOCK_GO_UP, variable),
    0, 1, 
    1, 2, 
    gtk.FILL|gtk.EXPAND, 
    gtk.FILL|gtk.EXPAND)
  table.attach(search_menu_item(self, "Down", gtk.STOCK_GO_DOWN, variable),
    1, 2, 
    1, 2, 
    gtk.FILL|gtk.EXPAND, 
    gtk.FILL|gtk.EXPAND)
    
  table.attach(parent_button(self, parent),
    2, 3, 
    1, 2, 
    gtk.FILL|gtk.EXPAND, 
    gtk.FILL|gtk.EXPAND) 

  self.search_result_widget = gtk.Label("")
  self.search_result_widget.set_justify(gtk.JUSTIFY_LEFT)
  
  self.search_result_index = 0
  table.attach(self.search_result_widget, 0,2, 0,1, gtk.FILL|gtk.EXPAND, gtk.FILL|gtk.EXPAND) 
  
  self.update_search(None,(variable,"Clear"))
  
  return(table)

def create_menu(self, parent, buttons):
  """Layout a set of buttons in a fashion suitable for 
  a menu (the definition of which may depend on
  screen shape, touchscreen size, or user preferences)"""
  
  (x,y) = (0,0)
  (dx,dy) = self.d['_menu_grid']
  
  perPage = (dx * dy) - 2
  pages = 1 + (len(buttons) - 2) / perPage
  #print "Need %d pages (%d / %d)" % (pages, len(buttons), perPage)

  table = gtk.Table(rows=dy, columns=dx, homogeneous=True)
  
  # Add special 1st button, always goes to parent
  buttons.insert(0, parent_button(self, parent))
  
  for btn in buttons:
    table.attach(btn,
      x, x+1, 
      y, y+1, 
      gtk.FILL|gtk.EXPAND, 
      gtk.FILL|gtk.EXPAND)
    x += 1
    if(x >= dx):
      x = 0
      y += 1
      if(y >= dy):
        self.log("menu too big", 3) # todo: split into pages
        break
  return(table)

