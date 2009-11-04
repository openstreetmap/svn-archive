#!/usr/bin/python
#-----------------------------------------------------------------------------
# Loads touchscreen menus from textfiles on disk
#
# Usage: 
#   (library code for pyroute GUI, not for direct use)
#
# Format:
#   * One menu per textfile  (menuname.txt)
#   * One line per 'column'
#   * Line containing "-----" starts a new row 
#      (the first line should also contain one of these)
#   * 4 rows of 3 columns. all must be defined, even if blank
#   * Item is "name" or "name|event"
#     * name should be unique amongst all menus
#     * name is currently displayed as-is (TODO: translation table)
#     * "Up" is a special name that closes the menu
#        * Typically the top/left item is always 'Up'
#     * event is a message to send when the menu item is pressed
#     * lines can be blank to represent no menu item in that position
#-----------------------------------------------------------------------------
# Copyright 2007, Oliver White
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
#-----------------------------------------------------------------------------
import os
import re

def loadMenu(filename):
  menu = {}
  try:
    file = open(filename, "r")
    x = 0
    y = -1
    for line in file:
      line = line.rstrip()
      if(line == '-----'):
        x = 0
        y = y + 1
        continue
      try:
        name,action = line.split('|', 2)
      except ValueError:
        name,action = (line,None)
      #print "%d,%d: %s" % (x,y,action)
      icon = name.lower()
      icon = re.sub('\s+', '_', icon)
      menu["%d,%d"%(x,y)] = {'name':name,'action':action,'icon':icon}
      x = x + 1
      
    file.close()
  except IOError:
    print "Can't open file %s" % filename
  return menu

def loadMenus(directory):
  menus = {}
  for file in os.listdir(directory):
    result = re.search('(\w+)\.txt', file)
    if(result):
      menuName = result.group(1)
      menus[menuName] = loadMenu("%s/%s" % (directory,file))
  return(menus)

def printMenu(menu):
  print "-" * 63
  for y in range(4):
    for x in range(3):
      item = menu["%d,%d"%(x,y)]
      print expand(item['name'], 20),
      print "|",
    print ""
    for x in range(3):
      item = menu["%d,%d"%(x,y)]
      print expand(item['action'], 20),
      print "|",
    print ""
    for x in range(3):
      item = menu["%d,%d"%(x,y)]
      print expand(item['icon'], 20),
      print "|",
    print "\n" + "-" * 63

def printStrings(menus):
  print "\n\nMenu item names:"
  names = {}
  # Look for a list of menu names
  for menuName, menu in menus.items():
    for xy,item in menu.items():
      name = item['name']
      if name:
        names[name] = 1
  # Print sorted list of unique names
  names = names.keys()
  names.sort()
  for name in names:
    print name
    
def checkIcons(menus, directory):
  print "\n\nMissing icons:"
  count = 0
  for menuName, menu in menus.items():
    for xy,item in menu.items():
      if(item['icon']):
        icon = "%s/%s.png" % (directory, item['icon'])
        if(not os.path.exists(icon)):
          print icon
          count = count + 1
  print "%d missing icons" % count

def expand(text,size):
  if(text == None):
    return(" " * size)
  need = size - len(text)
  n1 = int(need/2)
  n2 = need-n1
  return(" " * n1 + text + " " * n2)


if(__name__ == "__main__"):
  menus = loadMenus('.')
  
  import sys
  try:
    if(sys.argv[1] == 'list'):
      for name, menu in menus.items():
        print "Menu for %s:" % name
        printMenu(menu)
    elif(sys.argv[1] == 'strings'):
      printStrings(menus)
    elif(sys.argv[1] == 'icons'):
      checkIcons(menus,"../icons/bitmap")
    else:
      print "Loaded %d menus" % len(menus)
  except IndexError:
    print "Usage: %s [mode]" % sys.argv[0]
    print "  * 'list' - lists the contents of all menus"
    print "  * 'strings' - exports names, ready for translating"
    print "  * 'icons' - checks that all the icons exist"
    