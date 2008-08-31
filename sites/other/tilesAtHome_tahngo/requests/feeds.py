from django.contrib.syndication.feeds import Feed
from tah.requests.models import Request

class LatestRequests(Feed):
    title = "Lateste requests"
    link = "/latest/"
    description = "The latest requests tilesets that are not yet active"

    def items(self):
        return Request.objects.filter(status=0).order_by('-request_time')[:5]

    def item_link(self):
        return "sdfsdf"

    def item_pubdate(self, item):
        return item.clientping_time

class LatestRequestsByCategory(Feed):
    title = "Chicagocrime.org site news"
    link = "/sitenews/"
    description = "Updates on changes and additions to chicagocrime.org."

    def items(self):
        return Request.objects.order_by('-pub_date')[:5]
