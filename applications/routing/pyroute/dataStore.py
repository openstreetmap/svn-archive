from base import pyrouteModule

class DataStore(pyrouteModule):
    def __init__(self, globals, modules):
        pyrouteModule.__init__(self,modules)
        self.options = {}
        self.state = {}
        self.globals = globals # TODO: remove
        
    def handleEvent(self,event):
        action,params = event.split(':',1)
        print "Handling event %s" % event
        if(action == 'menu'):
            self.state['menu'] = params
        elif(action == 'mode'):
            self.state['mode'] = params
            self.closeAllMenus()
        elif(action == 'option'):
            method, name = params.split(':',1)
            if(method == 'toggle'):
                self.options[name] = not self.getOption(name)
            elif(method == 'add'):
                name,add = name.split(':')
                self.options[name] = self.getOption(name,0) + float(add)
                print "adding %f to %s, new = %s" % (float(add),name,str(self.getOption(name)))
        elif(action == 'route'):
          if(not self.globals.ownpos['valid']):
            print "Can't route, don't know start position"
            return
          if(params == 'clicked'):
            lat,lon = self.getState('clicked')
          else:
            lat, lon = [float(ll) for ll in params.split(':',1)]
          transport = self.globals.modules['data'].getState('mode')
          self.globals.modules['route'].setStartLL(self.globals.ownpos['lat'],self.globals.ownpos['lon'], transport)
          self.globals.modules['route'].setEndLL(lat,lon,transport)
          self.globals.modules['route'].setMode('route')          
          self.globals.modules['route'].update()
          self.globals.forceRedraw()
          self.closeAllMenus()
        elif(action == 'ownpos'):
          lat,lon = self.getState('clicked')
          self.globals.ownpos = {'lat':lat,'lon':lon,'valid':True}
          print "Set ownpos to %f,%f" % (lat,lon)
          self.closeAllMenus()
          self.globals.handleUpdatedPosition()
        elif(action == 'direct'):
          transport = self.globals.modules['data'].getState('mode')
          self.globals.modules['route'].setStartLL(self.globals.ownpos['lat'],self.globals.ownpos['lon'], transport)
          if(params == 'clicked'):
            lat,lon = self.getState('clicked')
          else:
            lat, lon = [float(ll) for ll in params.split(':',1)]
          self.globals.modules['route'].setEndLL(lat,lon,transport)
          self.globals.modules['route'].setMode('direct')
          self.globals.modules['route'].update()
          self.globals.forceRedraw()
          self.closeAllMenus()
        elif(action == 'download'):
          self.globals.modules['osmdata'].download( \
            self.globals.ownpos['lat'],
            self.globals.ownpos['lon'],
            0.05)
        
        
        
    def closeAllMenus(self):
        self.setState('menu',None)
        
    def getState(self,name):
        return(self.state.get(name, None))
    def setState(self,name,value):
        self.state[name] = value
    def getOption(self,name,default=None):
        return(self.options.get(name,default))
    def setOption(self,name,value):
        self.options[name] = value
