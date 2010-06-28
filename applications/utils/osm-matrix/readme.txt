#####################################################################################################
# OSM-MATRIX - create a gpx-grid for show the grid of osm-matrix by monty							#
#                                                                                                   #
# Copyright (C) 2010 Jan Tappenbeck, osm(at)tappenbeck.net                                          #
# components in use of garry68 (gpx-file-componetents), components for tile-calculation 			#
# (http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames)											#
#                                                                                                   #
# This program is free software; you can redistribute it and/or modify it under the terms of the    #
# GNU General Public License as published by the Free Software Foundation; either version 3 of      #
# the License, or (at your option) any later version.                                               #
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;         #
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.         #
# See the GNU General Public License for more details.                                              #
# You should have received a copy of the GNU General Public License along with this program;        #
# if not, see <http://www.gnu.org/licenses/>.                                                       #	
#                                                                                                   #
#  DEVELOP and TESTED in WINDOWS VISTA 64bit / ActivePerl											#
#                                                                                                   #
#####################################################################################################

# known problems
# when the define area crosses the data-border -> define two area !!!
#
#        gpx-grid for osm-matrix
#
#  PARAMETERS:
#        osm  [s] 	inputfile
#        name [s]   prefix for the
#
#        min/max geographic coordinates
#        w [f]   	west-limit
#        n [f]   	north-limit
#        e [f]   	east-limit
#        s [f]   	south-limit
#
#        z [i]   	tile-zoom - default 15
#
# EXAMPLE: osm-matrix.pl --name=haiti --w=-74.5 --n=20.25 --e=-71.24 --s=17.6
