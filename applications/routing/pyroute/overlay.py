import cairo
import os
from base import pyrouteModule
from pyrouteMenu import *
from menuIcons import menuIcons
from overlayArea import overlayArea

class guiOverlay(pyrouteModule):
    def __init__(self, modules):
        pyrouteModule.__init__(self, modules)
        self.modules = modules
        self.icons = menuIcons()
        self.menus = loadMenus('Menus')
        for name,stuff in self.menus.items():
          print "Loaded menu %s" % name

    def fullscreen(self):
        """Asks if the menu is fullscreen -- if it is, then the
        map doesn't need to be drawn underneath"""
        return(self.get('menu'))
    
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
        dx = rect.width / nx
        dy = rect.height / ny
        for i in range(0,nx):
            x1 = rect.x + i * dx
            self.cells[i] = {}
            for j in range(0,ny):
                y1 = rect.y + j * dy
                self.cells[i][j] = overlayArea(cr,x1,y1,dx,dy,self.modules, self.icons)
                self.clickable.append(self.cells[i][j])
        currentMenu = self.get('menu')
        if(currentMenu):
            self.drawMenu(currentMenu)
        else:
            self.cells[0][0].mainMenuButton()
            self.cr.set_line_width(2)
            self.cr.set_dash((2,2,2), 0);
            self.cr.set_source_rgba(0.4,0,0)
            y = 100
            self.cr.move_to(0,y)
            self.cr.line_to(rect.width,y)
            self.cr.stroke()
                    
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
	            self.set('menu',None)
	            return
	        function()
          
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
      
    def menu_list(self, module):
        self.backButton(0,0)
        n = 9
        offset = 0
        selectedFeed = int(self.modules['data'].getOption('selectedFeed',0))
        titlebar = self.rect.copyself(1.0/3.0,0,1,0.25)
        line1, line2 = titlebar.ysplit(0.5)
        back = line1.copyself(0,0,0.25,1)
        next = line1.copyself(0.75,0,1,1)
        back.button("","option:add:selectedFeed:-1","back")
        next.button("","option:add:selectedFeed:1","next")
        self.clickable.append(back)
        self.clickable.append(next)

        line1.copyself(0.25,0,0.75,1).drawText("Feed %d of %d" % (selectedFeed + 1, len(self.modules['plugins'][module].groups)))

        try:
            group = self.modules['plugins'][module].groups[selectedFeed]
        except KeyError:
            return
        except IndexError:
            return
        
        line2.drawText(group.name)
        
        listrect = self.rect.ysplitn(0, 0.25, 0.8, 1, n)
        ownpos = self.modules['position'].get()
        for i in range(0,n):
            textarea, button = listrect[i].xsplit(0.8)
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
                button.button("", "route:%1.5f:%1.5f" % (item.lat, item.lon), "goto")
                self.clickable.append(button)
            except IndexError:
                pass


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

        self.cells[0][1].button("set pos","ownpos:clicked","set_pos")
        self.cells[1][1].button("route to","route:clicked","route_to")
        self.cells[2][1].button("direct to","direct:clicked","direct_to")

        self.cells[0][2].button("waypoint", "waypoint:clicked", "bookmark")
        self.cells[1][2].button("extend route", "extend:route:clicked", "extend_route")
        self.cells[2][2].button("extend direct", "extend:direct:clicked", "extend_direct")

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
