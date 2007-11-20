import cairo
import os

class menuIcons:
    def __init__(self):
        self.images = {}
        self.cantLoad = []
    def load(self,name):
        filename = "icons/bitmap/%s.png" % name
        if(not os.path.exists(filename)):
            return(0)
        self.images[name] = cairo.ImageSurface.create_from_png(filename)
        if(self.images[name]):
            return(1)
        return(0)
    def draw(self,cr,name,x,y,w,h):
        if not name in self.images.keys():
            if(name in self.cantLoad):
                return
            if(not self.load(name)):
                self.cantLoad.append(name)
                return
        imagesize = 120.0
        cr.save()
        cr.translate(x,y)
        cr.scale(w / imagesize, h / imagesize)
        cr.set_source_surface(self.images[name],0,0)
        cr.paint()
        cr.restore()

class overlayArea:
    def __init__(self,cr,x,y,dx,dy,modules,iconSet):
        self.cr = cr
        self.x1 = x;
        self.y1 = y;
        self.x2 = x + dx;
        self.y2 = y + dy;
        self.w = dx
        self.h = dy
        self.cx = x + 0.5 * dx
        self.cy = y + 0.5 * dy
        self.event = None
        self.modules = modules
        self.iconSet = iconSet
    def fill(self,r,g,b,outline=0):
        self.cr.set_source_rgb(r,g,b)
        self.cr.rectangle(self.x1,self.y1,self.w,self.h)
        if(outline):
            self.cr.stroke()
        else:
            self.cr.fill()
    def mainMenuButton(self):
        self.cr.set_line_width(2)
        self.cr.set_dash((2,2,2), 0);
        self.cr.set_source_rgba(0.4,0,0)
        self.cr.arc(self.cx,self.cy, 0.5*self.w, 0, 2*3.14)
        self.cr.stroke()
        self.setEvent("menu:main")
    def drawTextSomewhere(self,text,px1,py1,px2,py2):
        innerBox = self.copyself(px1,py1,px2,py2)
        innerBox.drawText(text)
    def drawText(self,text):
        self.cr.select_font_face('Verdana', cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
        test_fontsize = 60
        self.cr.set_font_size(test_fontsize)
        xbearing, ybearing, textwidth, textheight, xadvance, yadvance = self.cr.text_extents(text)
        # Resize the font to fit
        ratiox = textwidth / self.w
        ratioy = textheight / self.h
        ratio = max(ratiox,ratioy)
        
        self.cr.set_font_size(test_fontsize / ratio)
        textwidth = textwidth / ratio
        textheight = textheight / ratio

        marginx = 0.5 * (self.w - textwidth)
        marginy = 0.5 * (self.h - textheight)
        # Text
        self.cr.move_to(self.x1 + marginx, self.y2 - marginy)
        self.cr.set_source_rgba(0, 0, 0, 0.5)
        self.cr.show_text(text)
        
    def button(self,text=None,event=None,icon=None):
        # Rectangle
        if(not icon):
          if(text):
            self.iconSet.draw(self.cr,"generic",self.x1,self.y1,self.w,self.h)
          else:
            self.iconSet.draw(self.cr,"blank",self.x1,self.y1,self.w,self.h)
        if(icon):
            self.iconSet.draw(self.cr,icon,self.x1,self.y1,self.w,self.h)
        if(text):
            self.drawTextSomewhere(text,0.2,0.6,0.8,0.8)
        if(event):
            self.setEvent(event)
    def setEvent(self,text):
        self.event = text
    def xc(self,p):
        return(self.x1 + p * self.w)
    def yc(self,p):
        return(self.y1 + p * self.h)
    def copyself(self,px1,py1,px2,py2):
        x1 = self.xc(px1)
        y1 = self.yc(py1)
        return(overlayArea( \
            self.cr,
            x1,
            y1,
            self.xc(px2) - x1,
            self.yc(py2) - y1,
            self.modules,
            self.iconSet))
    def xsplit(self,p):
        a = self.copyself(0,0,p,1)
        b = self.copyself(p,0,1,1)
        return(a,b)
    def ysplit(self,p):
        a = self.copyself(0,0,1,p)
        b = self.copyself(0,p,1,1)
        return(a,b)
    def xsplitn(self,px1,py1,px2,py2,n):
        dpx = (px2 - px1) / n
        cells = []
        for i in range(0,n-1):
            px = px1 + i * dpx
            cells.append(self.copyself(px,py1,px + dpx,py2))
        return(cells)
    def ysplitn(self,px1,py1,px2,py2,n):
        dpy = (py2 - py1) / n
        cells = []
        for i in range(0,n):
            py = py1 + i * dpy
            cells.append(self.copyself(px1,py,px2,py+dpy))
        return(cells)
            
    def contains(self,x,y):
        if(x > self.x1 and x < self.x2 and y > self.y1 and y < self.y2):
            return(1)
    def handleClick(self,x,y):
        if(self.event):
            self.modules['data'].handleEvent(self.event)
            return(1)
        return(0)
    
class guiOverlay:
    def __init__(self, modules):
        self.modules = modules
        self.icons = menuIcons()

    def fullscreen(self):
        """Asks if the menu is fullscreen -- if it is, then the
        map doesn't need to be drawn underneath"""
        return(self.modules['data'].getState('menu'))
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
        currentMenu = self.modules['data'].getState('menu')
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
            self.modules['data'].setState('menu','')
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
        self.cells[2][1].button("Download",None,"download")

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

    def menu_click(self):
        self.backButton(0,0)
        self.cells[1][0].button("(lat)",None,None)
        self.cells[2][0].button("(lon)",None,None)

        self.cells[0][1].button("set pos","ownpos:clicked",None)
        self.cells[1][1].button("route to","route:clicked",None)
        self.cells[2][1].button("direct to","direct:clicked",None)

        self.cells[0][2].button("waypoint","waypoint:clicked",None)
        self.cells[1][2].button("extend route","extend:route:clicked",None)
        self.cells[2][2].button("extend direct","extend:direct:clicked",None)

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
