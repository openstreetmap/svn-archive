import geometry

class dataItem:
    def __init__(self,lat,lon):
        self.lat = lat
        self.lon = lon
        self.title = 'Untitled at %1.3f, %1.3f' % (lat,lon)
    def formatText(self):
        return(self.title)
    def formatPos(self, ownPos = None):
        if(ownPos):
            return("%1.2fkm at %03.1f" % \
                   (geometry.distance(ownPos,(self.lat,self.lon)),
                    geometry.bearing(ownPos,(self.lat,self.lon))))
        else:
            return("%f,%f" % (self.lat,self.lon))

class dataGroup:
    def __init__(self,name):
        self.items = []
        self.name = name

class dataSource:
    def __init__(self):
        self.groups = []
    def callbacks(self, modules):
        self.modules = modules
