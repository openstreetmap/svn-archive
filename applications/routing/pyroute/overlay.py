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
        
    def menu_main(self):
        self.backButton(0,0)
        self.cells[1][0].button("Mark",None,"map_pin")
        self.cells[2][0].button("Draw",None,"sketch")
        
        self.cells[0][1].button("View","menu:view","viewMenu")
        self.cells[1][1].button("GPS","menu:gps","gps")
        self.cells[2][1].button("Download","menu:download","download")

        self.cells[0][2].button()
        self.cells[1][2].button()
        self.cells[2][2].button()
        
        self.cells[0][3].button("Centre","option:toggle:centred","centre")
        self.cells[1][3].button("Options","menu:options","options")
        self.cells[2][3].button("Mode", "menu:mode","transport")

    def menu_mode(self):
        self.backButton(0,0)
        self.cells[1][0].button("Cycle","mode:cycle","bike")
        self.cells[2][0].button("Walk","mode:foot","hike")
        
        self.cells[0][1].button("MTB","mode:cycle","mtb")
        self.cells[1][1].button("Car","mode:car","car")
        self.cells[2][1].button("Hike","mode:foot","hike")

        self.cells[0][2].button("Fast cycle","mode:cycle","fastbike")

        self.cells[0][3].button("HGV","mode:hgv","hgv")
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

    def menu_search_eat(self):
        self.backButton(0,0)
        self.cells[1][0].button("Pub food", "search:amenity=pub;food=yes", None)
        self.cells[2][0].button("Restaurant", "search:amenity=restaurant", None)
        
        self.cells[0][1].button("Cafe", "search:amenity=cafe",None)
        self.cells[1][1].button("Fast food","search:amenity=fast_food",None)
        self.cells[2][1].button("Takeaway",None,None)
    
    def menu_search_sleep(self):
        self.backButton(0,0)
        self.cells[1][0].button("Hotel", "search:tourism=hotel",None)
        self.cells[2][0].button("Hostel", "search:tourism=hostel",None)
        
    def menu_search_repair(self):
        self.backButton(0,0)
        self.cells[1][0].button("Bike shop","search:amenity=bike_shop",None)
        self.cells[2][0].button("Garage","search:amenity=garage",None)
        
    def menu_search_buy(self):
        self.backButton(0,0)
        self.cells[1][0].button("Supermarket","search:amenity=supermarket",None)
        self.cells[2][0].button("Mall",None,None)
        
        self.cells[0][1].button("High street",None,None)
        self.cells[1][1].button("Dep't Store",None,None)
        self.cells[2][1].button("Outdoor","search:shop=outdoor",None)
        
        self.cells[0][2].button("DIY","search:tourism=diy",None)
        self.cells[1][2].button("",None,None)
        self.cells[2][2].button("",None,None)
    
    def menu_search_help(self):
        self.backButton(0,0)
        self.cells[1][0].button("Police Stn", "search:amenity=police", None)
        self.cells[2][0].button("Fire Stn", "search:amenity=fire", None)
        
        self.cells[0][1].button("Hospital", "search:amenity=hospital",None)
        self.cells[1][1].button("Ranger", "search:amenity=ranger_station", None)
        self.cells[2][1].button("Pharmacy", "search:amenity=pharmacy", None)
        
    def menu_search_park(self):
        self.backButton(0,0)
        self.cells[1][0].button("Car park", "search:amenity=parking", None)
        self.cells[2][0].button("Free car park","search:amenity=parking;cost=free",None)
        
        self.cells[0][1].button("Bike park", "search:amenity=cycle_parking", None)
        self.cells[1][1].button("Lay-by", "search:amenity=layby", None)
        
    def menu_search_hire(self):
        self.backButton(0,0)
        self.cells[1][0].button("Car hire", "search:amenity=car_hire", None)
        self.cells[2][0].button("Bike hire", "search:amenity=bike_hire",None)
        
        self.cells[0][1].button("Ski hire","search:amenity=ski_hire",None)

    def menu_search(self):
        self.backButton(0,0)
        self.cells[1][0].button("Eat","menu:search_eat",None)
        self.cells[2][0].button("Sleep","menu:search_sleep",None)

        self.cells[0][1].button("Fuel","menu:search_fuel",None)
        self.cells[1][1].button("Repair","menu:search_repair",None)
        self.cells[2][1].button("Buy","menu:search_buy",None)

        self.cells[0][2].button("Help","menu:search_help",None)
        self.cells[1][2].button("Park","menu:search_park",None)
        self.cells[2][2].button("Hire","menu:search_hire",None)
        
        self.cells[0][3].button("",None,None)

    def menu_view(self):
        self.backButton(0,0)
        self.cells[1][0].button("People",None,"people")
        self.cells[2][0].button("Wiki","menu:geonames","wiki")

        self.cells[0][1].button("Business","menu:search","business")
        self.cells[1][1].button("RSS","menu:feeds","rss")
        self.cells[2][1].button("Bookmarks",None,"bookmark")

        self.cells[0][2].button("Routes",None,"route")
        self.cells[1][2].button("Waypoints",None,"waypoints")
        self.cells[2][2].button("Drawings",None,"sketch")
        
        self.cells[0][3].button("Events",None,"events")
    
    def menu_download(self):
        self.backButton(0,0)
        self.cells[1][0].button("",None,None)
        self.cells[2][0].button("",None,None)

        self.cells[0][1].button("",None,None)
        self.cells[1][1].button("",None,None)
        self.cells[2][1].button("Route data","download:osm:0.2","download")

        self.cells[0][2].button("",None,None)
        self.cells[1][2].button("",None,None)
        self.cells[2][2].button("",None,None)
        
        self.cells[0][3].button("",None,None)
        self.cells[1][3].button("",None,None)
        self.cells[2][3].button("",None,None)

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
