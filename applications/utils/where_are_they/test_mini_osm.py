#!/usr/bin/python
# Tests for mini osm (mini_osm.py)
#
# Nick Burch - 30/07/2006

import unittest
from mini_osm import mini_osm_pgsql, mini_osm

class TestMiniOSM(unittest.TestCase):
	def testNew(self):
		"""Test new"""
#		miniosm = mini_osm("pgsql")
	def testNewPG(self):
		"""Test new"""
		miniosm = mini_osm_pgsql()
	def testNewRand(self):
		"""Test new"""
		try:
			miniosm = mini_osm("madeup")
			self.fail("Didn't break")
		except:
			pass

	def testConnect(self):
		"""Test connecting"""
#		miniosm = mini_osm("pgsql")
#		miniosm.connect()
	def testConnectPG(self):
		"""Test connecting"""
		miniosm = mini_osm_pgsql()
		miniosm.connect()

	def testGetNodesWithTagNameAndTypeSQL(self):
		"""Test getting nodes by name + other tag"""
		miniosm = mini_osm_pgsql()
		miniosm.connect()

		exp_i = {'lat': 51.728700000000003, 'id': 7211096, 'long': -1.2380899999999999, 'tags': [('place', 'village'), ('name', 'Iffley')]}
		exp_b = {'lat': 51.381300000000003, 'id': 1947201, 'long': -2.3593199999999999, 'tags': [('place', 'city'), ('name', 'Bath')]}

		# Iffley is a village
		iffley = miniosm.getNodesWithTagNameAndType("Iffley","place",["village","town"])
		self.assertEquals(len(iffley.keys()), 1)
		self.assertEquals(iffley[iffley.keys()[0]], exp_i)

		# Iffley isn't a town or a city
		iffley = miniosm.getNodesWithTagNameAndType("Iffley","place",["city","town"])
		self.assertEquals(len(iffley.keys()), 0)

		# Bath is a city
		bath = miniosm.getNodesWithTagNameAndType("Bath","place",["city","town"])
		self.assertEquals(len(bath.keys()), 1)
		self.assertEquals(bath[bath.keys()[0]], exp_b)

	def testNodesInArea(self):
		"""Test nodes in the area"""
		miniosm = mini_osm_pgsql()
		miniosm.connect()
		nodes = miniosm.getNodesInArea(51.716243333, -1.238533333, 100)

		exp_a = {'lat': 51.715600000000002, 'id': 583488, 'long': -1.23712, 'tags': []}
		exp_b = {'lat': 51.716799999999999, 'id': 583485, 'long': -1.23793, 'tags': []}
		exp_c = {'lat': 51.716299999999997, 'id': 583486, 'long': -1.23733, 'tags': []}
		self.assertEquals(len(nodes.keys()), 3)
		self.assertEquals(nodes[nodes.keys()[0]], exp_a)
		self.assertEquals(nodes[nodes.keys()[1]], exp_b)
		self.assertEquals(nodes[nodes.keys()[2]], exp_c)

	def testNodesInAreaWithTag(self):
		"""Test nodes in the area with tags"""
		miniosm = mini_osm_pgsql()
		miniosm.connect()

		nodes = miniosm.getNodesInAreaWithTag(51.716243333, -1.238533333, 1750, "place")
		exp = [ 
				[('place', 'village'), ('name', 'Iffley')],
				[('place', 'village'), ('name', 'Rose Hill')]
		]
		self.assertEquals(nodes[nodes.keys()[0]]["tags"], exp[0])
		self.assertEquals(nodes[nodes.keys()[1]]["tags"], exp[1])

		nodes = miniosm.getNodesInAreaWithTag(51.716243333, -1.238533333, 1750, "amenity","petrol_station")
		self.assertEquals(len(nodes.keys()), 1)


if __name__ == '__main__':
    unittest.main()
