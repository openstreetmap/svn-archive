<?php

require_once('inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - About</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>What is Freemap?</h1>
<hr/>
<p><em>Freemap</em> is a project to create free and
annotatable maps of the UK countryside, using 
<a href='http://www.openstreetmap.org'>OpenStreetMap</a> data to create the
maps. Freemap maps aim to show not only the official rights of way, but 
all paths with public access, many of which are missing on other maps.</p>

<h2>New tile server!</h2>
<p>Chris Jones of Swansea University Computer Society has very
generously donated a box to host the Freemap tiles on. This should be
up and running very soon... watch this space! </p>
</p>
<h2>Annotatable maps</h2>
<p>Freemap isn't just a map though. It
aims to be an <em>interactive</em> source of information for
UK countryside users, showing not just the maps themselves, but also 
all sorts of other information useful to walkers. For example, want to find
a good pub? Or a good bed and breakfast? Or even an interesting view or 
recommended path? Or, want to <em>avoid</em> a path blocked by fences, bogs
or mad cows? Or even want to get directions in a place where the path is hard
to follow? Freemap can provide all this information in one interactive 
map.</p>
<h2>It needs you though!</h2>
<p>The information on Freemap is provided by you, the user. So know your 
local area and want to share your knowledge with others? Go ahead and annotate
the map with your favourite viewpoint, or warn visitors to your area that one
of your local paths is not easy to follow. Or did you have an occasion when you
got lost, and eventually found the right way? Again, annotate the map and share
the information with others. </p>
<h2>Why Freemap?</h2>
<p>Why use Freemap rather than existing commercial mapping providers? 
Hopefully the above will give you some idea but to sum up:
<ul>
<li><em>Maps are free</em>. You can freely use and copy Freemap maps however
you like without worrying about being on the receiving end of legal action.
The only thing you can't do is to prevent other people doing the same!</li>
<li><em>Maps are up-to-date</em>. Because Freemap maps are derived from
<a href='http://www.openstreetmap.org'>OpenStreetMap</a> data, they are 
up-to-date to within a week. Some commercial mapping providers are 
<em>several years</em> out of date with their countryside mapping!
You too can contribute to the underlying map data yourself;
visit <a href='http://www.openstreetmap.org'>OpenStreetMap</a> for more
details.</li>
<li><em>Maps are interactive</em>. As discussed above, with Freemap it's not
just the map itself; it's a free, interactive and user-contributable source of 
information for countryside users.</li>
<li><em>It's not just the rights of way!</em> Many commercial maps only show
rights of way, or only selected unofficial paths. Many commonly-used unofficial
paths are not shown! Freemap aims to show <em>all</em> paths which people have 
right of access on. Official paths are shown in red, unofficial in purple.</li>
</ul>
<h2>Geograph photos</h2>
<p>At the most highly zoomed-in scales, Freemap overlays photos from the
<a href='http://www.geograph.org.uk'>Geograph</a> project on the map. By 
clicking on the camera icon you can see a photo of that location - a great way
of getting an idea what the walk looks like before you set out!</p>
<h2>For developers</h2>
<p>Freemap's own data, i.e. the map annotations,  is now available through the 
<em>Freemap API</em>,
described <a href='api.php'>here</a>. So if you're creating your own 
walking website, or even creating a standalone application, you can access
Freemap data for use in your own project.</p>
<h2>Finally, who wrote this thing?</h2>
<p>Freemap is developed by Nick Whitelegg (nickw on Freemap). I have 
been involved in OSM since 2005 and started Freemap way back in 2004,
originally as a standalone project. When OSM came to the fore in 2005 I added
my data to the OSM database and have been developing Freemap as a
UK walker orientated OSM site since. I can be contacted by email
<a href='mailto:nick_whitelegg@yahoo.co.uk'>here</a>.</p>
</div>
</body>
</html>
