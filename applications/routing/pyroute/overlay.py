import cairo
import os
from base import pyrouteModule
from pyrouteMenu import *
from menuIcons import menuIcons
from overlayArea import overlayArea
from colorsys import hsv_to_rgb

class guiOverlay(pyrouteModule):
    def __init__(self, modules):
        pyrouteModule.__init__(self, modules)
        self.modules = modules
        self.icons = menuIcons()
        self.menus = loadMenus('Menus')
        self.dragbar = None
        self.dragpos = 0
        for name,stuff in self.menus.items():
          print "Loaded menu %s" % name

    def fullscreen(self):
        """Asks if the menu is fullscreen -- if it is, then the
        map doesn't need to be drawn underneath"""
        return(self.get('menu'))
    
    def handleDrag(self,dx,dy,startX,startY):
      if(self.dragbar):
        if(self.dragbar.contains(startX,startY)):
          scale = -20.0 / float(self.rect.h)
          self.dragpos = self.dragpos + dy * scale
          if(self.dragpos < 0):
            self.dragpos = 0
          #print "Dragging %1.2f (by %1.2f * %1.2f)" % (self.dragpos, dy,scale)
          return(True)
      return(False)
    
    def handleClick(self,x,y):
        """return 1 if click was handled"""
        for cell in self.clickable:
            if(cell.contains(x,y)):
                if(cell.handleClick(x,y)):
                    return(1)
        if(self.fullscreen()):
            return(1)
        return(0)
    def draw(self, cr, rect):
        self.cr = cr
        self.rect = overlayArea(cr,rect.x,rect.y,rect.width,rect.height,self.modules,self.icons)
        nx = 3
        ny = 4
        self.clickable = []
        self.cells = {}
        rotate = (rect.width > rect.height)
        if(not rotate):
          dx = rect.width / nx
          dy = rect.height / ny
          for i in range(0,nx):
              x1 = rect.x + i * dx
              self.cells[i] = {}
              for j in range(0,ny):
                  y1 = rect.y + j * dy
                  self.cells[i][j] = overlayArea(cr,x1,y1,dx,dy,self.modules, self.icons)
                  self.clickable.append(self.cells[i][j])
        else:
          dy = rect.height / nx
          dx = rect.width / ny
          for i in range(0,nx):
              y1 = rect.y + i * dy
              self.cells[i] = {}
              for j in range(0,ny):
                  x1 = rect.x + j * dx
                  self.cells[i][j] = overlayArea(cr,x1,y1,dx,dy,self.modules, self.icons)
                  self.clickable.append(self.cells[i][j])
          

          
        currentMenu = self.get('menu')
        if(currentMenu):
            self.drawMenu(currentMenu)
        else:
          self.mapOverlay()

    def mapOverlay(self):
      if(1):
        self.cells[0][0].button("Menu",None,"hint")
      else:
	      # Tickmark to show where the active button is
	      self.cr.set_line_width(2)
	      self.cr.set_dash((10,5), 0);
	      self.cr.set_source_rgba(0.4,0,0)
	      button = self.cells[0][0]
	      self.cr.move_to(button.xc(0.5),button.y2)
	      self.cr.line_to(button.x2,button.y2)
	      self.cr.line_to(button.x2,button.yc(0.5))
	      self.cr.stroke()
    
      # Make the buttons clickable
      self.cells[0][0].setEvent("menu:main")

    def drawMenu(self, menu):
      try:
        self.genericMenu(self.menus[menu])
        return
      except KeyError:
        menuName = 'menu_%s' % menu
        try:
          function = getattr(self, menuName)
        except AttributeError:
          print "Error: %s not defined" % menuName
          self.set('menu','')
          return
        function()

    def sketchOverlay(self):
      colourMenu = self.cells[2][3]
      r = float(self.get('sketch_r',0))
      g = float(self.get('sketch_g',0))
      b = float(self.get('sketch_b',0))
      colourMenu.fill(r,g,b)
      colourMenu.setEvent("menu:sketch_colour")
      # TODO: all clickable places to global array/module
          
    def menu_gps(self):
      self.backButton(0,0)
      
      selectLine = self.cells[0][1].copyAndExtendTo(self.cells[2][1])
      selectLine.icon("3h")
      mode = self.get("PositionMode")
      self.cells[0][1].button( \
        "GPSd",
        "option:set:PositionMode:gpsd",
        mode == 'gpsd' and 'selected' or 'unselected')
      self.cells[1][1].button( \
        "Manual",
        "option:set:PositionMode:manual",
        mode == 'manual' and 'selected' or 'unselected')
      self.cells[2][1].button( \
        "pos.txt",
        "option:set:PositionMode:txt",
        mode == 'txt' and 'selected' or 'unselected')
      
    def menu_download(self):
      self.backButton(0,0)

      centreLine = self.cells[1][0].copyAndExtendTo(self.cells[2][0])
      centreLine.icon("2h")
      
      self.cells[1][0].button( \
        "Around me",
        "option:set:DownloadCentre:pos",
        self.get('DownloadCentre') == 'pos' and 'selected' or 'unselected')
      self.cells[2][0].button( \
        "Destination",
        "option:set:DownloadRange:dest",
        self.get('DownloadCentre') == 'dest' and 'selected' or 'unselected')
        
      dataTypeLine = self.cells[0][1].copyAndExtendTo(self.cells[2][1])
      dataTypeLine.icon("3h")
      
      self.cells[0][1].button( \
        "Data",
        "option:toggle:DownloadData",
        self.get('DownloadData') and 'checked' or 'unchecked')
      self.cells[1][1].button( \
        "Maps",
        "option:toggle:DownloadMaps",
        self.get('DownloadMaps') and 'checked' or 'unchecked')
      self.cells[2][1].button( \
        "POI",
        "option:toggle:DownloadPOI",
        self.get('DownloadPOI') and 'checked' or 'unchecked')

      distanceLine = self.cells[0][2].copyAndExtendTo(self.cells[2][2])
      distanceLine.icon("3h")
      
      self.cells[0][2].button( \
        "20km",
        "option:set:DownloadRange:20",
        self.get('DownloadRange') == '20' and 'selected' or 'unselected')
      self.cells[1][2].button( \
        "100km",
        "option:set:DownloadRange:100",
        self.get('DownloadRange') == '100' and 'selected' or 'unselected')
      self.cells[2][2].button( \
        "500km",
        "option:set:DownloadRange:500",
        self.get('DownloadRange') == '500' and 'selected' or 'unselected')
      
      detailLine = self.cells[0][3].copyAndExtendTo(self.cells[1][3])
      detailLine.icon("2h")
      
      self.cells[0][3].button( \
        "This zoom",
        "option:set:DownloadDetail:selected",
        self.get('DownloadDetail') == 'selected' and 'selected' or 'unselected')
      self.cells[1][3].button( \
        "All zoom",
        "option:set:DownloadDetail:all",
        self.get('DownloadDetail') == 'all' and 'selected' or 'unselected')
      
      self.cells[2][3].button("Go","download:","download")


    def menu_sketch_colour(self):
      self.backButton(0,0)
      self.colourMenu(1,0, 0,1,0, 'sketch')
      self.colourMenu(2,0, 0,0,1, 'sketch')
      self.colourMenu(0,1, 1,1,0, 'sketch')
      self.colourMenu(1,1, 0,1,1, 'sketch')
      self.colourMenu(2,1, 1,0,1, 'sketch')
      self.colourMenu(0,2, 1,0,0, 'sketch')
      self.colourMenu(1,2, 0,0,0, 'sketch')
      self.colourMenu(2,2, 1,1,1, 'sketch')
      
    def colourMenu(self,x,y,r,g,b,use):
      self.cells[x][y].fill(r,g,b)
      self.cells[x][y].setEvent("+set_colour:%s:%1.2f:%1.2f:%1.2f" % (use,r,g,b))
      
    
    def backButton(self,i,j):
        self.cells[i][j].button("","menu:","up")
    def genericMenu(self, menu):
      for y in range(4):
        for x in range(3):
          item = menu["%d,%d"%(x,y)]
          if item['name'] == 'Up':
            self.backButton(x,y)
          else:
            self.cells[x][y].button(item['name'],item['action'],item['icon'])

    def menu_feeds(self):
      self.menu_list("rss")
    def menu_geonames(self):
      self.menu_list("geonames")
    def menu_waypoints(self):
      self.menu_list("waypoints")
      
    def menu_list(self, module):
      self.backButton(0,0)
      n = 9
      offset = int(self.dragpos)
      selectedFeed = int(self.get('selectedFeed',0))
      titlebar = self.rect.copyself(1.0/3.0,0,1,0.25)
      line1, line2 = titlebar.ysplit(0.5)
      back = line1.copyself(0,0,0.25,1)
      next = line1.copyself(0.75,0,1,1)
      back.button("","option:add:selectedFeed:-1","back")
      next.button("","option:add:selectedFeed:1","next")
      self.clickable.append(back)
      self.clickable.append(next)


      self.dragbar = self.rect.copyself(0.0,0.25,0.88,1.0)

      try:
        group = self.modules['poi'][module].groups[selectedFeed]
      except KeyError:
        line2.drawText("\"%s\" not loaded"%module)
        return
      except IndexError:
        line2.drawText("No such set #%d"%selectedFeed)
        return
      
      line1.copyself(0.28,0,0.73,1).drawText("Set %d of %d" % (selectedFeed + 1, len(self.modules['poi'][module].groups)))
      
      line2.drawText(group.name)
      
      listrect = self.rect.ysplitn(0, 0.25, 1.0, 1, n)
      ownpos = self.get('ownpos')
      for i in range(0,n):
        textarea, button = listrect[i].xsplit(0.88)
        color, textarea = textarea.xsplit(0.025)
        
        # Pattern for the left hand side to show how far down the list
        # we are - model it as a colour, where red is the top, and purple
        # is bottom
        h = float(i + offset) / float(len(group.items))
        v = (((i + offset) % 2) == 0) and 1.0 or 0.95
        r,g,b = hsv_to_rgb(h, 1, v)
        color.fill(r,g,b)
        
        if(i > 0):
          self.cr.set_line_width(0.5)
          self.cr.set_dash((2,2,2), 0);
          self.cr.set_source_rgb(0,0,0)
          self.cr.move_to(textarea.x1,textarea.y1)
          self.cr.line_to(textarea.x2,textarea.y1)
          self.cr.stroke()
        try:
          item = group.items[i + offset]
          textarea.drawTextSomewhere(item.formatText(), 0.1,0.1,0.9,0.5)
          textarea.drawTextSomewhere(item.formatPos(ownpos), 0.1,0.6,0.9,0.9)
          button.button("", "+route:%1.5f:%1.5f" % (item.lat, item.lon), "goto")
          self.clickable.append(button)
        except IndexError:
          pass

    def menu_main(self):
        self.backButton(0,0)
        self.cells[1][0].button("Zoom in","zoom:in",None)
        self.cells[2][0].button("Zoom out","zoom:out",None)

        self.cells[0][1].button("View","menu:view","view")
        self.cells[1][1].button("GPS","menu:gps","gps")
        self.cells[2][1].button("Download","menu:download","download")

        self.cells[0][2].button("Data","menu:data","data")
        self.cells[1][2].button("","",None)
        self.cells[2][2].icon("generic")        
        self.cells[2][2].button("Sketch mode","+option:toggle:sketch",self.get("sketch") and "checked" or "unchecked")

        self.cells[0][3].icon("generic")
        self.cells[0][3].button("Centre me","option:toggle:centred",self.get("centred") and "checked" or "unchecked")
        #self.cells[1][3].button("Options","menu:options",None)
        self.cells[1][3].button(None,None,None)
        self.cells[2][3].button("Mode","menu:mode","mode")

    def menu_click(self):
        self.backButton(0,0)
        
        latlonRect = self.rect.copyself(0.33,0,1,0.25)
        latRect = latlonRect.copyself(0,0,1,0.5)
        lonRect = latlonRect.copyself(0,0.5,1,1)
        
        lat,lon = self.get('clicked')
        
        NS = lat > 0 and 'N' or 'S'
        EW = lon > 0 and 'E' or 'W'
        
        latRect.drawTextSomewhere('%1.4f %s' % (abs(lat), NS), 0.05, 0.05, 0.7, 0.95)
        lonRect.drawTextSomewhere('%1.4f %s' % (abs(lon), EW), 0.3, 0.05, 0.95, 0.95)
        #def drawTextSomewhere(self,text,px1,py1,px2,py2):
        #innerBox = self.copyself(px1,py1,px2,py2)
        #innerBox.drawText(text)
    
        #self.cells[1][0].button("(lat)",None,None)
        #self.cells[2][0].button("(lon)",None,None)

        self.cells[0][1].button("set pos","+ownpos:clicked","set_pos")
        self.cells[1][1].button("route to","+route:clicked","route_to")
        self.cells[2][1].button("direct to","+direct:clicked","direct_to")

        self.cells[0][2].button("waypoint", "+add_waypoint:clicked", "bookmarks")
        self.cells[1][2].button("extend route", "+extend:route:clicked", "extend_route")
        self.cells[2][2].button("extend direct", "+extend:direct:clicked", "extend_direct")

        self.cells[0][3].button("",None,None)
        self.cells[1][3].button("",None,None)
        self.cells[2][3].button("",None,None)

    def menu_options(self):
        view,scroll = self.rect.xsplit(0.8)
        view.fill(1,0,0)
        scroll.fill(0,1,0)
        self.backButton(0,0)


if __name__ == "__main__":
    a = guiOverlay(None,None)
    print dir(a)
