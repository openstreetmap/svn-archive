<html>
<head>
<title>OpenStreetMap tiles@home</title>
<link rel="stylesheet" href="../../styles.css">
</head>
<body>
<div class="all">
<h1 class="title"><a href="../../"><img src="../../Gfx/tah.png" alt="tiles@home" width="600" height="109"></a></h1>
<p class="title">Rendering request queues</p>
<hr>

<?php
  /* Graph of tiles@home render requests
  ** OJW 2007
  ** License: GNU GPL v2 or at your option any later version
  */
  include("../../lib/requests.inc");
?>

<p>The <a href="Recent/">latest 30 requests in each queue</a> are available (which used to be displayed on this page)</p>

<h2>Today</h2>
<p><img src="http://munin.openstreetmap.org/openstreetmap/dev.openstreetmap-tah_processed-day.png"></p>

<h2>This week</h2>
<p><img src="http://munin.openstreetmap.org/openstreetmap/dev.openstreetmap-tah_processed-week.png"></p>

<p>Munin has a <a href="http://munin.openstreetmap.org/openstreetmap/dev.openstreetmap.html#T@H">page for these graphs</a>, and for <a href="http://munin.openstreetmap.org/openstreetmap/dev.openstreetmap.html">the dev server</a> in general</p>
  
<h2>Notes</h2>
  
<ul>
<li><b>Pending</b> requests are created when you ask for an area to be rerendered. (see the <a href="http://wiki.openstreetmap.org/index.php/Tiles%40home/APIs">API</a> if you want to know how to make those requests)</li>

<li><b>Active</b> requests have been taken by tiles@home clients to render</li>

<li><b>Done</b> means the result has been uploaded, and the request is effectively closed.</li>
</ul>

<h3>So what does that mean?</h3>

<ul>

<li><b>Green (or blue) goes up</b> - something's been requested</li>

<li><b>Green (or blue) to red</b> - tiles@home client has taken a request</li>

<li><b>Red to Pink</b> - a successful rendering</li>

<li><b>Turquoise</b> - Long-term moving average of fulfilled requests per hour</li>

</ul>

<h3>Request timeouts</h3>

<p>After 1.5 hours in the <b>active</b> queue, requests are moved to the <b>new</b> queue.  This is when someone said that they'd render something but failed to upload the result. Basically the request will be requeued at the very start of the queue.</p>

<p>Timeouts occur 2 &times; after 1.5 hours, then 4 times after 6 hours. If they haven't been finished by then, requests are deleted.</p>

<h3>How many tiles are being uploaded</h3>
<p></p>The 'done' queue represents the number of <i>tilesets</i> being uploaded per the last hour.</p>
	
</div>
</body>
</html>
