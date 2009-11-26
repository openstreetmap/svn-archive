<?php
/*
Code to get co-ordinates from map taken from http://maposmatic.org/ and
copyright (c) 2009 Étienne Loks <etienne.loks_AT_peacefrogsDOTnet>
Other code copyright (c) Russ Phillips <russ AT phillipsuk DOT org>

This file is part of OSM Error.

OSM Error is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// Configuration file

// Base API URL to get map via bounding box. Do not include query string
$osm_api_base = "http://api.openstreetmap.org/api/0.6/map";

// Initial bounding box
$left = -1.2288;
$bottom = 53.4151;
$right = -1.188;
$top = 53.4346;

// Debug?
$DEBUG = False;
$LOG_FILE = "/dev/null";
?>
