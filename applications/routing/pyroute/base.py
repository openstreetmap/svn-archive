
class pyrouteModule:
  def __init__(self, modules):
    self.m  = modules
  def reportModuleConnectivity(self):
    for name, data in self.m.items():
      print "* %s" % name
  def get(self, name, default=None):
    return(self.m['data'].getData(name, default))
  def set(self, name, value):
    return(self.m['data'].setData(name, value))
  def action(self, message):
    self.m['data'].handleEvent(message)
  def ownPos(self):
    return(self.m['position'].get())