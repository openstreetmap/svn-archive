<?php

require_once('inc.php');
session_start();

?>
<html>
<head>
<title>Freemap - Contribute!</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<body> 

<?php write_sidebar(); ?>

<div id='main' class='content'>
<h1>Freemap User Guide: How do I ....?</h1>
<hr/>
<h2>....annotate the map?</h2>
<p>First thing you can do is to add <em>annotations</em> to the map,
to let others know about interesting
views, path blockages (such as muddy paths, barbed wire, or mad cows)
or directions at points where the path is
hard to follow. To do this, simply select &quot;Annotate&quot; mode, 
and click at the point you wish to annotate. You will then be prompted to
select the type of annotation (hazard, directions or interesting place) and
asked to provide a description.</p>
<h2>....write comments on points of interest?</h2>
<p>On Freemap you can also write comments on points of interest, such as pubs,
hill summits, or villages, or (in a wiki-like fashion) update comments that
others have written.  There are two ways to do this:
<ul>
<li>Firstly, you can just click on the appropriate
point of interest, for example, a pub symbol on the map. A box will come up 
showing existing comments on the feature (if any exist already) with a link 
allowing you to write new comments.</li>
<li>Secondly, if you select the &quot;Nearby points of interest&quot; link,
all points of interest within about 5km of your current position will be
displayed. Selecting one of these will take you to a simple wiki-like page
on the feature, where you can view or edit existing comments, or add your own.
</li>
</ul>
<p><strong>Note</strong> if spam becomes a problem, it may 
become a requirement to be logged in to add or update comments.</p>
</body>
</html>
