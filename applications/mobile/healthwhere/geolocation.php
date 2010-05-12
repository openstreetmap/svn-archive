<?php
/*
Healthwhere, a web service to find local pharmacies and hospitals
Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

require ("inc_head.php");
require_once ("inc_head_html.php");
?>

<p>
On supported browsers, Healthwhere uses <a href = "http://en.wikipedia.org/wiki/Geolocation">geolocation</a> technology to automatically populate the latitude &amp; longitude boxes with your location. Note that accuracy varies greatly, so you may wish to enter a postcode or latitude &amp; longitude manually, if you know them.
</p>

<p>
Your position is determined by your browser, and methods vary. If you are using a device that has a GPS receiver, this will probably be used to determine your position, with a good level of accuracy. See your browser's documentation for more information.
</p>

<?php
require ("inc_foot.php");
?>
