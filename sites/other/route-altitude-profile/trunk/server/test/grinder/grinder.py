# import altitudeprofile_pb2   # Can't get this to work somehow.

from net.grinder.script.Grinder import grinder

import random
import string

from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
from net.grinder.common import GrinderException

tests = {
  "gchart_url_get_1km_10pnt" : Test(1, "Google Chart URL, GET 1 km, 10 points"),
  "gchart_url_get_1km_100pnt" : Test(2, "Google Chart URL, GET 1 km, 100 points"),
  "gchart_url_get_10km_10pnt" : Test(3, "Google Chart URL, GET 10 km, 10 points"),
  "gchart_url_get_10km_100pnt" : Test(4, "Google Chart URL, GET 10 km, 100 points"),
  "gchart_url_get_100km_10pnt" : Test(5, "Google Chart URL, GET 100 km, 10 points"),
  "gchart_url_get_100km_100pnt" : Test(6, "Google Chart URL, GET 100 km, 100 points")
}

log = grinder.logger.output
out = grinder.logger.TERMINAL

# Server Properties
SERVER     = "http://altitude-pg.sprovoost.nl"
URI        = "/profile/gchart_url"

class TestRunner:
  def __call__(self):
    for count in range(100):
      for idx in range(len(tests)):
        testId = random.choice(tests.keys())
        log("Runing %s " % testId, out)

        # generate random route from 49.5, 8.5
        # it will have 10, 100 or 500 points
        # length will be approximately 1 km
        # 1 km ~ 0.01 degrees latitude
        # 1 km ~ 0.02 degrees longitude in Germany
        # Too keep things simple, we will pick a direction (north, south, east or west) and then travel 100 meters in that direction.

        if "10pnt" in testId:
          n = 10
        elif "100pnt" in testId:
          n = 100
        elif "500pnt" in testId:
          n = 500
        
        if "1km" in testId:
          d = 1
        elif "10km" in testId:
          d = 10
        elif "100km" in testId:
          d = 100

        startlat = str(50.0 + (random.random() - 0.5) * 2 * 4)
        startlon = str(18.0 + (random.random() - 0.5) * 2 * 12)
 
        latString = "lats=" + startlat
        lonString = "lons=" + startlon
        origin = [49.5, 8.5]
    
        directions = {"east" : [0,0.002 * d], "west" : [0,-0.002 * d], "north" : [0.001 * d, 0], "south" : [-0.001 * d,0]}

        for i in range(n):
          direction = random.choice(directions.values())
          destination = [0,0]
          destination[0] = origin[0] + direction[0]
          destination[1] = origin[1] + direction[1]
        
          latString += "," + str(destination[0])
          lonString += "," + str(destination[1])

          origin = destination

        getString = "?" + latString + "&" + lonString
        

        requestString = "%s%s%s" % (SERVER, URI, getString)

        grinder.statistics.delayReports = 1
        request = tests[testId].wrap(HTTPRequest())

        log("Sending request %s " % requestString, out)
        result = request.GET(requestString)

        expected_result = "http://chart.apis.google.com/chart?cht=lxy&chs=325x200"

        if not expected_result in result.getText():
            grinder.statistics.forLastTest.setSuccess(0)
            writeToFile(result.getText(), testId)
            log("Error in result")

  def writeToFile(text, testId):
      filename = grinder.getFilenameFactory().createFilename(
          testId + "-page", "-%d.html" % grinder.runNumber)

      file = open(filename, "w")
      print >> file, text
      file.close()

