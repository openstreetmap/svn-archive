from django.contrib.syndication.feeds import Feed
from tah.requests.models import Request

class LatestRequests(Feed):
    title = "Latest requests"
    link = "/latest/"
    description = "The latest requests tilesets that are not yet active"


    def get_object(self, bits):
        #return the requests priority
        if len(bits): return bits[0]
        else: return None

    def items(self, priority):
        if priority != None:
            return Request.objects.filter(status=0, priority=priority).order_by('-request_time')[:10]
        else:
            return Request.objects.filter(status=0).order_by('-request_time')[:50]

    def item_link(self):
        return "sdfsdf"

    def item_pubdate(self, item):
        return item.clientping_time

class OldestRequestsByPriority(Feed):
    title = "Oldest requests"
    link = "/oldest/"
    description = "The oldest unhandled requests by category."

    def get_object(self, bits):
        #return the requests priority
        if len(bits): return bits[0]
        else: return None

    def items(self, priority):
        if priority != None:
            return Request.objects.filter(status=0, priority=priority).order_by('request_time')[:50]
        else:
            return Request.objects.filter(status=0).order_by('request_time')[:50]

    def item_link(self):
        return "sdfsdf"

    def item_pubdate(self, item):
        return item.clientping_time
