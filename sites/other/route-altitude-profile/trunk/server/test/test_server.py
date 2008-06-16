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
    # Test top left corner of SRTM tiles
    self.assertEqual(posFromLatLon(0,0),0) 
    self.assertEqual(posFromLatLon(0,1),1200*1200) 
    self.assertEqual(posFromLatLon(0,2),1200*1200*2) 
    self.assertEqual(posFromLatLon(1,0),-1200*1200*360) 
    self.assertEqual(posFromLatLon(0,-1),-1200*1200) 

    # Test points within a SRTM tile
    # First point to the east of first tile is located at:
    # lat = 0
    # lon = 1 / 1200 = 0.00083333333333333339
    # Its position should be 1
    self.assertEqual(posFromLatLon(0,0.00083333333333333339),1) 
    
    # Fifth point to the east of first tile is located at:
    # lat = 0
    # lon = 5 / 1200 = 0.0041666666666666666
    # Its position should be 5
    self.assertEqual(posFromLatLon(0,0.0041666666666666666),5) 
    
    # First point to the south of first tile is located at:
    # lat = - 1 / 1200 = -0.00083333333333333339
    # lon = 0
    # Its position should be 1200
    self.assertEqual(posFromLatLon(-0.00083333333333333339,0),1200) 
    
    # 3 points to the south and 2 points to the east in first tile is 
    # located at:
    # lat = - 3 / 1200 = -0.0025000000000000001 
    # lon = 2 / 1200 = 0.0016666666666666668
    # Its position should be 3602
    self.assertEqual(posFromLatLon(-0.0025000000000000001,0.0016666666666666668),3602) 

    # Test coordinates in between points of an SRTM tile
    
    # Somewhere between second and third point to the east of first tile.
    # lat = 0
    # lon = 2.8 / 1200 = 0.0023333333333333331 
    # Its position should be rounded to the nearest neighbour, so 3.
    self.assertEqual(posFromLatLon(0,0.0023333333333333331),3) 
    
    # Somewhere between second and third point to the south of first tile.
    # lat = -2.8 / 1200 = -0.0023333333333333331  
    # lon = 0
    # Its position should be rounded to the nearest neighbour, so 3600.
    self.assertEqual(posFromLatLon(-0.0023333333333333331,0),3600) 
    
    # Somewhere between second and third point to the east of first tile,
    # and fourth and fith point to the south.
    # lat = -4.4 / 1200 = -0.003666666666666667
    # lon = 2.1 / 1200 = 0.00175
    # Its position should be rounded to the nearest neighbour, so 4802.
    self.assertEqual(posFromLatLon(-0.003666666666666667,0.00175),4802) 
  
  def testGetAltitude(self):
    # I picked a few random coordinates:
    self.assertEqual(getAltitude(-37,144), 88)
    self.assertEqual(getAltitude(-37.3,144.4), 109)

  def testGoogleChartURL(self):
    # Tests if the server returns the expected Google Chart URL for the
    # example routes.

    # First 4 points of route 2:

    route = [\
      {'id' : 1, 'lat' : -37.794528, 'lon' : 144.989826},
      {'id' : 2, 'lat' : -37.795407, 'lon' : 145.000048},
      {'id' : 3, 'lat' : -37.791279, 'lon' : 145.013591},
      {'id' : 4, 'lat' : -37.791322, 'lon' : 145.029184}\
    ]
    # For the horizontal scale, we need to know the horizontal distance 
    # between the coordinates.
    distances = []
    for i in range(3):
      distances.append(distance.distance(
        (route[i]['lat'],route[i]['lon'] ),
        (route[i+1]['lat'],route[i+1]['lon'] ),
      ).kilometers) 

    # First point will have coordinate 0, last point the sum of all distances
    x_coordinates = [\
      0,
      distances[0],
      distances[0] + distances[1],
      sum(distances)
    ]
    
    # y coordinates are the altitudes:
    # Note: this test will fail if the altitudes are calculated differently.
    # Point 1 : 207
    # Point 2 : 190
    # Point 3 : 160
    # Point 4 : 142

    y_coordinates = [207, 190,160, 142]

    # Create gchart
    # http://code.google.com/apis/chart/#line_charts
    # http://pygooglechart.slowchop.com/
    chart = XYLineChart(325, 200, 
                        x_range=(0,max(x_coordinates)), y_range=(min(y_coordinates),max(y_coordinates)))
    chart.add_data(x_coordinates)
    chart.add_data(y_coordinates)

    chart.set_axis_labels(Axis.BOTTOM, ['0', str(max(x_coordinates))[0:4] + " km"])
    
    chart.set_axis_labels(Axis.LEFT, [str(min(y_coordinates)) + " m", str(max(y_coordinates)) + " m"])
    
    expected_url = chart.get_url() 
    
    # So let's test that:    

    result = altitude_profile_gchart_function(route)
    self.assertEqual(result['gchart_url'], expected_url) 

if __name__ == '__main__':
    unittest.main()
