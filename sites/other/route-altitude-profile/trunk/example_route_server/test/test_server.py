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
      expected_result = []
      for point in route:
        point['alt'] = 1
        expected_result.append(point)

      result = server.server.altitude_profile(route)
      self.assertEqual(result,expected_result)

if __name__ == '__main__':
    unittest.main()
