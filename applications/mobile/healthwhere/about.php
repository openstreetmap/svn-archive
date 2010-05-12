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
<a href = "index.php">Healthwhere</a> <?=VERSION ?> uses data from <a href = "http://www.openstreetmap.org/">OpenStreetMap</a> (<a href = "http://wiki.openstreetmap.org/wiki/License">licence details</a>) to find local pharmacies and hospitals.
</p>

<p>
Postcode data from <a href = "http://www.ordnancesurvey.co.uk/oswebsite/opendata/">OS OpenData</a>.
</p>

<p>
Healthwhere is designed to work well on very small screens such as on a mobile phone, but should also work on a standard monitor. It is hosted on <a href = "http://www.mappage.org">mappage.org</a>, which also has other mapping-related projects.
</p>

<p>
The <a href = "/download/healthwhere.tar.gz">source code</a> is copyright &copy; 2009-2010 Russell Phillips, and released under the <a href = "http://www.opensource.org/licenses/gpl-2.0.php">GNU GPL v2</a>.
</p>

<?php
require ("inc_foot.php");
?>
