import sys
import os
sys.path += [os.path.abspath('.')]
import server
from server import route1, route2, route3, route4
from numpy import ones
import unittest


class TestAltitudeServerInteraction(unittest.TestCase):
  def testRoutes(self):
    for route in [route1, route2, route3, route4]:
      result = server.server.altitude_profile(route)

      for i in range(len(route)):
        self.assertEqual(result[i]['lat'],route[i]['lat'])
        self.assertEqual(result[i]['lon'],route[i]['lon'])
        # We'll just trust the produced altitude; that tested elsewhere.
        # Just make sure that an altitude is returned.
        self.assert_(result[i]['alt'])

if __name__ == '__main__':
    unittest.main()
