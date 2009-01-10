#!/usr/bin/python
#----------------------------------------------------------------------
# GUI framework, for displaying pages created by the modules/ directory
#----------------------------------------------------------------------
# Copyright 2007-2009, Oliver White
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
import pygtk
pygtk.require('2.0')
import gtk
import sys
import os
import gobject

def updateObject(obj):
  obj.update()
  gobject.timeout_add(1200, updateObject, obj)
  return(False)

class GuiBase:
  """Modular GUI for the Rana mobile map program"""
  def __init__(self, initialPage=None):
    # Create the window
    self.window = gtk.Window()
    self.window.set_title('Rana')
    self.window.connect('delete-event', gtk.main_quit)
    self.window.resize(480,640) # to simulate openmoko
    self.window.move(gtk.gdk.screen_width() - 500, 50)
    
    self.widget = None # Widget filling the screen
    self.visible_module = None # Module responsible for self.widget
    self.update_function = None
    
    self.m = {} # List of modules
    
     # Program data
     # keys starting with underscores are not for saving to disk
    self.d = {
      '_pages':{
        'menu':'global'},
      '_functions':{
        'change_page':self.change_page},
      '_menu_grid':(3,4),
      }

    # Load all the program functionality, which is in modules
    self.load_modules('modules')
    
    # Select the initial page
    if(initialPage):
      self.change_page(None, initialPage)
    else:
      self.change_page(None, 'title')

    # Regular updates
    gobject.timeout_add(1000, updateObject, self)
    
    # Run the GUI
    gtk.main()

  def load_modules(self, module_path):
    """Load all modules from the specified directory"""
    sys.path.append(module_path)
    for f in os.listdir(module_path):
      if(f[0:1] != '_' and f[-3:] == '.py'):
        name = f[0:-3]
        filename = "%s\\%s" % ( module_path, f[0:-3]) # with directory name, but without .py extension
        try:
          a = __import__(name)
        except ImportError:
          continue
        self.m[name] = getattr(a, name)(self.m, self.d, name)
        #print " * %s: %s" % (name, self.m[name].__doc__)
    for k,m in self.m.items():
      m.startup()
  
  def update(self):
    if(self.update_function):
      self.update_function()

  def setWidget(self, newMainWidget):
    if(self.widget):
      self.window.remove(self.widget)
    self.widget = newMainWidget
    self.window.add(newMainWidget)
    self.window.show_all()

  def change_page(self, widget, page):
    self.d['history:-1'] = self.d.get('page', None)
    self.d['page'] = page
    which_module = self.d['_pages'].get(page, None)
    if(not which_module):
      print "No module registered for %s" % page
    elif(not self.m.has_key(which_module)):
      print "Module %s not loaded" % which_module
    else:
      module = self.m[which_module]
      function = getattr(module, "page_"+page)
      widget = function()
      self.setWidget(widget)
      self.visible_module = module
      try:
        self.update_function = getattr(module, "update")
      except AttributeError:
        self.update_function = None

if __name__ == "__main__":
  if(len(sys.argv) > 1):
    initialPage = sys.argv[1]
  else:
    initialPage = None
    
  program = GuiBase(initialPage)

 