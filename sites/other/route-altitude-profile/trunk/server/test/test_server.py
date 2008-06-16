import sys
import os
sys.path += [os.path.abspath('.')]
from altitude import *
import unittest

class TestAltitudeServer(unittest.TestCase):
  def testTableAltitudeExists(self):
    tables = db.get_tables()

    self.assert_('public.altitude' in tables)

  def testPosFromLatLon(self):
    # Test bottom left corner of SRTM tiles
    # Note that the file name of an srtm tile corresponds to the coordinate
    # of its bottom left element. However, the files start at top left. 
    # The top left element (row 1, col 1) has index 0, the left most element
    # (col 1) of the second row has index 1200, etc. 
    # The bottom left element (row 1200, col 1) of a tile therefore has index
    # 1199 * 1200 = 1 438 800
    
    self.assertEqual(posFromLatLon(0,0),0 + 1199*1200) 
    self.assertEqual(posFromLatLon(0,1),1200*1200 + 1199*1200) 
    self.assertEqual(posFromLatLon(0,2),1200*1200*2 + 1199*1200) 
    self.assertEqual(posFromLatLon(1,0),1200*1200*360 + 1199*1200) 
    self.assertEqual(posFromLatLon(0,-1),-1200*1200 + 1199*1200) 
    
    # The coordinate of the top left element of a tile has the same
    # longitude as the tile bottom left element and its latitude is 
    # 1199/1200 degrees north of it:
    self.assertEqual(posFromLatLon(1199./1200,0),0) 
    self.assertEqual(posFromLatLon(1199./1200,1),1200*1200) 
    self.assertEqual(posFromLatLon(1 + 1199./1200,0),1200*1200*360) 

    # Test points within a SRTM tile
    # First point to the east (top row) of first tile is located at:
    # lat = 1199./1200
    # lon = 1 / 1200 = 0.00083333333333333339
    # Its position should be 1
    self.assertEqual(posFromLatLon(1199./1200,0.00083333333333333339),1) 
    
    # Fifth point to the east of first tile (top row) is located at:
    # lat = 1199./1200
    # lon = 5 / 1200 = 0.0041666666666666666
    # Its position should be 5
    self.assertEqual(posFromLatLon(1199./1200,0.0041666666666666666),5) 
    
    # First point to the south (second row) of first tile is located at:
    # lat = 1198. / 1200 
    # lon = 0
    # Its position should be 1200
    self.assertEqual(posFromLatLon(1198./ 1200,0),1200) 
    
    # 3 points to the south and 2 points to the east in first tile is 
    # located at:
    # lat = 1196 / 1200
    # lon = 2 / 1200 = 0.0016666666666666668
    # Its position should be 3602
    self.assertEqual(posFromLatLon(1196. / 1200,0.0016666666666666668),3602) 

    # Test coordinates in between points of an SRTM tile
    
    # Somewhere between second and third point to the east of first tile.
    # (top row)
    # lat = 1199. / 1200
    # lon = 2.8 / 1200 = 0.0023333333333333331 
    # Its position should be rounded to the nearest neighbour, so 3.
    self.assertEqual(posFromLatLon(1199. / 1200,0.0023333333333333331),3) 
    
    # Somewhere between second and third point to the south of first tile.
    # lat = (1199 -2.8) / 1200
    # lon = 0
    # Its position should be rounded to the nearest neighbour, so 3600.
    self.assertEqual(posFromLatLon((1199 - 2.8) / 1200,0),3600) 
    
    # Somewhere between second and third point to the east of first tile,
    # and fourth and fith point to the south.
    # lat = (1199-4.4) / 1200
    # lon = 2.1 / 1200 = 0.00175
    # Its position should be rounded to the nearest neighbour, so 4802.
    self.assertEqual(posFromLatLon((1199-4.4) / 1200,0.00175),4802) 
  
  def testGetAltitude(self):
    # I picked a few random coordinates:
    self.assertEqual(getAltitude(-36,144), 88)
    self.assertEqual(getAltitude(-37.3,144.4), 519)

  def testInterpolation(self):
    print "\nTest interpolation"
    # We'll use route 1 as an example

    route = [\
      {'id' : 1, 'lat' : -37.817460, 'lon' : 144.967450},
      {'id' : 2, 'lat' : -37.806643, 'lon' : 144.962394}
    ]

    # It's 1.28 kilometers long and only consists of two points.
    
    # We'll add one extra point to it.
    # Its coordinate should be:
    # lat = (-37.806643 + - 37.817460) / 2 = -37.812051499999995
    # lon = (144.967450 + 144.962394) / 2 = 144.964922

    pair = route[:] # mind in Python there is a *big* difference between
                    # a = b and a = b[:]. 

    expected_result = [\
      {'id' : 1, 'lat' : -37.817460, 'lon' : 144.967450},
      {'id' : 2, 'lat' : -37.812051499999995, 'lon' : 144.964922},
      {'id' : 3, 'lat' : -37.806643, 'lon' : 144.962394}
    ]
      
    self.assertEqual(addPointsToPair(pair, 1), expected_result) 
    
    # Return route to its original state:
    route = [\
      {'id' : 1, 'lat' : -37.817460, 'lon' : 144.967450},
      {'id' : 2, 'lat' : -37.806643, 'lon' : 144.962394}
    ]

    # The minimum resolution is the length of the route dived by 100.
    # In this case 12.8 meters.
  
    origin = [route[0]['lat'], route[0]['lon']]
    destination = route[1]['lat'], route[1]['lon']
 
    expected_route_length =  distance.distance(origin, destination).kilometers

    route_length = getRouteLength(route)

    self.assertEqual(route_length, expected_route_length)

    min_res = route_length / 100.   

    # If two points are more than than 1.5 times this minimum resolution
    # apart, they will be supplemented by extra points.
    
    # In this case, the distance between two points is the same as the
    # length of the route.
    
    distance_between_points = route_length
    
    # The number of extra points is given by floor(distance / min_res) - 1,
    # which should be 99

    pair = route[:] 

    number_of_extra_points = getNumberOfExtraPoints(pair, min_res)

    #floor(distance_between_points / min_res) - 1

    self.assertEqual(number_of_extra_points, 99)
  
    # Now we add these extra points    
    interpolateRoute(route, 100)

    # It should now have 101 points
    self.assertEqual(len(route), 101)

    # The 'id' elements should have been renumbered:
    for i in range(101):
      self.assertEqual(route[i]['id'], i + 1)

    # We calculated the coordinates of the middle points above:
    self.assertEqual(route[50]['lat'], -37.812051499999995)
    self.assertEqual(route[50]['lon'], 144.964922)

  def testGoogleChartURL(self):
    print "\nTest Google Chart URL"
    # Tests if the server returns a Google Chart URL for an
    # example routes. We will not check if it is correct.

    # First 4 points of route 2:

    route = [\
      {'id' : 1, 'lat' : -37.794528, 'lon' : 144.989826},
      {'id' : 2, 'lat' : -37.795407, 'lon' : 145.000048},
      {'id' : 3, 'lat' : -37.791279, 'lon' : 145.013591},
      {'id' : 4, 'lat' : -37.791322, 'lon' : 145.029184}\
    ]

    self.assert_(altitude_profile_gchart_function(route))

if __name__ == '__main__':
    unittest.main()
