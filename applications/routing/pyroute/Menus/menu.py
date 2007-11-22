#!/usr/bin/python
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
  for file in os.listdir('.'):
    result = re.search('(\w+)\.txt', file)
    if(result):
      menuName = result.group(1)
      menus[menuName] = loadMenu(file)
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
  for name, menu in menus.items():
    print "Menu for %s:" % name
    printMenu(menu)
  
  printStrings(menus)
  
  checkIcons(menus,"../icons/bitmap")
  