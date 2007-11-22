from base import pyrouteModule

class DataStore(pyrouteModule):
    def __init__(self, globals, modules):
        pyrouteModule.__init__(self,modules)
        self.options = {}
        self.globals = globals # TODO: remove
        
    def handleEvent(self,event):
        action,params = event.split(':',1)
        print "Handling event %s" % event
        
        if(action == 'menu'):
            self.set('menu', params)
        elif(action == 'mode'):
            self.set('mode', params)
            self.closeAllMenus()
            
        elif(action == 'option'):
            method, name = params.split(':',1)
            if(method == 'toggle'):
                self.set(name, not self.get(name))
            elif(method == 'add'):
                name,add = name.split(':')
                self.set(name, self.get(name,0) + float(add))
        elif(action == 'route'):
          ownpos = self.get('ownpos')
          
          if(not ownpos['valid']):
            print "Can't route, don't know start position"
            return
          if(params == 'clicked'):
            lat,lon = self.get('clicked')
          else:
            lat, lon = [float(ll) for ll in params.split(':',1)]
            
          transport = self.get('mode')
          
          router = self.m['route']
          router.setStartLL(ownpos['lat'], ownpos['lon'], transport)
          router.setEndLL(lat,lon,transport)
          router.setMode('route')
          router.update()
          self.globals.forceRedraw()
          self.closeAllMenus()
        elif(action == 'ownpos'):
          lat,lon = self.get('clicked')
          self.set('ownpos', {'lat':lat,'lon':lon,'valid':True})
          print "Set ownpos to %f,%f" % (lat,lon)
          self.closeAllMenus()
          self.globals.handleUpdatedPosition()
        elif(action == 'direct'):
          start = self.get('ownpos')
          transport = self.get('mode')
          self.globals.modules['route'].setStartLL(start['lat'], start['lon'], transport)
          if(params == 'clicked'):
            lat,lon = self.get('clicked')
          else:
            lat, lon = [float(ll) for ll in params.split(':',1)]
          self.globals.modules['route'].setEndLL(lat,lon,transport)
          self.globals.modules['route'].setMode('direct')
          self.globals.modules['route'].update()
          self.globals.forceRedraw()
          self.closeAllMenus()
        elif(action == 'download'):
          centre = self.get('ownpos')
          if(not centre['valid']):
            print "Need to set your own position before downloading"
            return
          sizeToDownload = 0.1
          self.globals.modules['osmdata'].download( \
            centre['lat'],
            centre['lon'],
            sizeToDownload)
    
    def closeAllMenus(self):
        self.set('menu',None)
    def getData(self,name,default=None):
        return(self.options.get(name,default))
    def setData(self,name,value):
        self.options[name] = value
