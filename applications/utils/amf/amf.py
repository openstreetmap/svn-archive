from pyamf.remoting.client import RemotingService, remoting
remoting.CONTENT_TYPE = "application/x-amf; charset=utf-8"

gateway = RemotingService("http://www.openstreetmap.org/api/0.5/amf/read")
whichways_deleted = gateway.getService('whichways_deleted')
deleted = whichways_deleted(-1.4916, 51.88447, -1.46949, 51.86895)
print "Deleted ways: %s" % deleted[0]
for d in deleted[0]:
    gateway = RemotingService("http://www.openstreetmap.org/api/0.5/amf/read")
    getway = gateway.getService("getway_old")
    print getway(d, -1)
