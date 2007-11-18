class DataStore:
    def __init__(self, globals):
        self.options = {}
        self.state = {}
        self.globals = globals
        
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
					lat, lon = [float(ll) for ll in params.split(':',1)]
					transport = self.globals.modules['data'].getState('mode')
					self.globals.modules['route'].setStartLL(self.globals.ownpos[0],self.globals.ownpos[1], transport)
					self.globals.modules['route'].setEndLL(lat,lon,transport)
					self.globals.modules['route'].update()
					self.globals.forceRedraw()
					self.closeAllMenus()

            
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
