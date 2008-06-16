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

if __name__ == '__main__':
    unittest.main()
