
class pyrouteModule:
  def __init__(self, modules):
    self.m  = modules
  def reportModuleConnectivity(self):
    for name, data in self.m.items():
      print "* %s" % name
    
