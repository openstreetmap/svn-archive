#!/usr/bin/perl

use CGI qw(escapeHTML);
use URI::Escape;
use DBI;
use strict;


my $cgi = CGI->new();
my $values = 'label,user,minlat,minlon,maxlat,maxlon,projection,bgimage,width,height,pixel,color,fromdate,todate,frequency,delay,loopflag';


print <<EOF;
Content-type: text/html

<html><head><title>OSM History</title></head><body>
EOF

my $bg="#cccccc";

if ((!defined($cgi->param("label")) && !defined($cgi->param("show")) && !defined($cgi->param("retry"))) || defined($cgi->param("form")))
{
print <<EOF;
<h1>OSM History</h1>

<strong>Displaying Historic OpenStreetMap Coverage</strong>
<p>
The history service creates animated GIF images that depict how OSM coverage of an area
has changed over time. It does so by painting a dot on a map for each node, using old planet 
files for past data. (It doesn't draw lines for segments and ways, which means that zoomed-in
inner city views look a bit thin sometimes.) You can control some aspects of image generation,
like the background image to be used or what time range you want depicted.
</p>
<p>
Computation is too time-consuming to offer this as a live service, but you can request images 
to be created and then view or download them once they are ready (the list below should give
an indication how many requests are waiting and also how long other requests took to complete).
</p>
<p>
You can browse past results below. If you request a new image, it will show up in the list
after some time. This service is mainly intended for people to generate images, then 
download and use them e.g. in presentations. I will probably delete old images from this 
server from time to time. 
</p>

<form method="post">
<table>
<tr valign="top">
<td bgcolor="$bg" colspan="3">
<b>Fill out this form to request an animated GIF image to be rendered:</b><br />&nbsp;
</td>
</tr>

<tr valign="top">
<td bgcolor="$bg">Identification</td>
<td>&nbsp;</td>
<td><table cellspacing="0" cellpadding="0">
    <tr><td>Your Name:&nbsp;</td><td>
EOF
    print $cgi->textfield("user", "", 21, 20);
    print "</td></tr><tr><td>Image Name:&nbsp;</td><td>";
    print $cgi->textfield("label", "", 51, 50);

print <<EOF;
    </td></tr></table>
    <font size="-1"><em>This is just so that you (and others) can recognize the image later. You can 
    put any user name, it is not checked against a list.</em></font>
</td></tr>

<tr valign="top">
<td bgcolor="$bg">Bounding Box</td>
<td>&nbsp;</td>
<td><table border="0" cellpadding="0" cellspacing="0">
<tr>
EOF
    print $cgi->td("min. lat &nbsp;").$cgi->td($cgi->textfield("minlat", "", 10, 11));
    print $cgi->td("min. lon &nbsp;").$cgi->td($cgi->textfield("minlon", "", 10, 11));
    print "</tr><tr>";
    print $cgi->td("max. lat &nbsp;").$cgi->td($cgi->textfield("maxlat", "", 10, 11));
    print $cgi->td("max. lon &nbsp;").$cgi->td($cgi->textfield("maxlon", "", 10, 11));
print <<EOF;
    </table>
</td></tr>

<tr valign="top">
<td bgcolor="$bg">Projection</td>
<td>&nbsp;</td>
<td>
EOF

print $cgi->popup_menu(
    "projection", 
    [1, 2], 
    1, 
    { 1=> "lat/lon (EPSG:4326)", 2=>"Mercator" }
);

print <<EOF;
    <br />
    <font size="-1"><em>(using Mercator for large areas doesn't work well with background images)</em></font>
</td></tr>

<tr valign="top">
<td bgcolor="$bg">Background</td>
<td>&nbsp;</td>
<td>
EOF

print $cgi->popup_menu(
    "bgimage", 
    [0, 1, 2], 
    2, 
    { 0 => "empty", 1 => "Metacarta vmap0", 2 => "Landsat" }
);

print <<EOF;
</td></tr>

<tr valign="top">
<td bgcolor="$bg">GIF size</td>
<td>&nbsp;</td>
<td>width: 
EOF

print $cgi->textfield("width", "640", 6, 5);
print " OR height: ";
print $cgi->textfield("height", "", 6, 5);

print <<EOF;
    <br />
    <font size="-1"><em>(other dimension results from bounding box and projection data)</em></font>
</td></tr>

<tr valign="top">
<td bgcolor="$bg">OSM Nodes</td>
<td>&nbsp;</td>
<td>
EOF
print $cgi->popup_menu(
    "pixel", 
    [0, 1, 2, 3], 
    1, 
    { 0 => "drawn as pixels", 1 => "drawn as 2x2 rectangles", 2 => "drawn as 3x3 rectangles", 3 => "drawn as 5x5 circles" }
);

print $cgi->popup_menu(
    "color", 
    [0, 1, 2, 3], 
    0, 
    { 0 => "in red", 1 => "in yellow", 2 => "in white", 3 => "in blue" }
);

print <<EOF;
</td></tr>

<tr valign="top">
<td bgcolor="$bg">Date Range</td>
<td>&nbsp;</td>
<td>from 
EOF

print $cgi->popup_menu(
    "fromdate", 
    [0, 60701, 70101, 70701],
    0, 
    { 0 => "earliest", 60701=> "2006-07", 70101=>"2007-01",70701=>"2007-07" }
);

print " to ";

print $cgi->popup_menu(
    "todate", 
    [999999, 60630, 61231, 70630],
    0, 
    { 999999 => "latest", 60630=>"2006-06", 61231=>"2006-12",70630=>"2007-06" }
);
print <<EOF;
    <br />
    <font size="-1"><em>(actual resolution depends on available historic data)</em></font>
</td></tr>

<tr valign="top">
<td bgcolor="$bg">Frame Rate</td>
<td>&nbsp;</td>
<td>
EOF

print $cgi->popup_menu(
    "frequency", 
    [0, 10, 14, 30, 61, 91, 9999],
    30, 
    { 0 => "one frame for every planet file available", 10 => "one frame per 10 days", 
    14 => "one frame per fortnight", 30 => "one frame per month", 61 => "one frame for every two months",
    91 => "one frame per quarter", 9999 => "only one frame altogehter" }
);

print " displayed at ";

print $cgi->popup_menu(
    "delay", 
    [50, 100, 200, 500], 
    100, 
    { 50 => "2 frames per second", 100 => "1 frame per second", 
    200 => "2 seconds per frame", 500 => "5 seconds per frame" }
);

print $cgi->checkbox(
    "loopflag",
    0,
    "1",
    "loop"
);

print <<EOF;
</td></tr>

<tr valign="top">
<td bgcolor="$bg" colspan="3" align="right">
<input type="button" name="Clear" value="Clear" onClick="document.location.href='.'" /> &nbsp; &nbsp; &nbsp; 
<input type="submit" value="Submit" name="Submit" />
</td>
</tr>
</table>
</form>

<h2>Request Queue and log</h2>
EOF

    if (my $lastShown = showRequests(undef, 10))
    {
        printf "<p><a href=\"?show=%d\">next page (older requests)</a>", $lastShown-1;
    }
}
elsif (defined($cgi->param("show")))
{
    print "<h2>Request Queue and log</h2>";
    if (my $lastShown = showRequests($cgi->param("show"), 25))
    {
        printf "<p><a href=\"?show=%d\">next page (older requests)</a>", $lastShown-1;
    }
    print "<p><a href=\"".$cgi->url()."\">Return</a>";
}

else
{
    my $dsn="DBI:mysql:host=localhost:database=osmhistory";
    my $dbh = DBI->connect($dsn);
    if (!defined($dbh))
    { 
        print "Database error - cannot connect: ".escapeHTML($DBI::errstr)."</body></html>"; 
        exit; 
    }

    my $query;
    if (defined($cgi->param("retry")))
    {
        $query=sprintf("update jobs set status='waiting' where id=%d", $cgi->param("retry"));
        $cgi->delete("retry");
    }
    else
    {
        my $q;

        foreach my $v (split(/,/, $values))
        {
            my $p = $cgi->param($v);
            $q .= defined($p) ? $dbh->quote($p) : "null";
            $q .= ",";
        }

        $query = "insert into jobs ($values,date_entered,status) values ($q".time().",'waiting')";
    }

    if ($dbh->do($query))
    {
        print "Your query has been inserted into the queue.";
        print "<p><a href=\"".$cgi->url(-query=>1)."&form=1\">Return</a>";
    }
    else
    {
        print "<font color='red'><b>Could not insert query:</b></font><p>";
        print escapeHTML($dbh->errstr);
        print "<p><a href=\"".$cgi->url(-query=>1)."&form=1\">Return</a>";
    }
}

print <<EOF;
<hr>
<em>Scripts written in Perl by Frederik Ramm &lt;frederik\@remote.org&gt;, Public Domain, source available in OSM SVN (applications/rendering/history).<br>
All images use OpenStreetMap data which is licensed under <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA 2.0</a>.</em>
</body></html>
EOF

sub mytime
{
     my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(shift);
     return sprintf "%04d-%02d-%02d %02d:%02d", 
        $year+1900,$mon+1,$mday,$hour,$min;
}

sub showRequests
{
    my ($starting, $howmany) = @_;

    my $dsn="DBI:mysql:host=localhost:database=osmhistory";
    my $dbh = DBI->connect($dsn);
    if (!defined($dbh))
    { 
        print "Database error - cannot connect: ".escapeHTML($DBI::errstr)."</body></html>"; 
        exit; 
    }
    my $query = "select * from jobs ";
    if (defined($starting))
    {
        $query.="where id<=$starting ";
    }
    $howmany++;
    my $last_shown;
    $query .= "order by id desc limit $howmany";
    my $sth = $dbh->prepare($query);
    $sth->execute();
    print "<table>";
    while(my $row=$sth->fetchrow_hashref())
    {
        $howmany--;
        last if ($howmany == 0);
        $last_shown = $row->{"id"};
        if ($row->{"status"} eq "finished")
        {
            print "<tr valign=top><td><a href='images/$row->{id}.gif'><img border=0 src='thumbnails/$row->{id}.gif'></a></td>";
            print "<td><b>".escapeHTML($row->{"label"})."</b><br>";
            printf "requested by: ".escapeHTML($row->{"user"}).", id: ".$row->{"id"}."<br>";
            printf "Image size: %d kB (%d by %d pixels, %d animation frames)<br>", 
                $row->{"filesize"}/1024, 
                $row->{"width"}, 
                $row->{"height"},
                $row->{"num_frames"};
            printf "requested %s, processed %s (took %d seconds)<br>", 
                mytime($row->{"date_entered"}), 
                mytime($row->{"date_finished"}), 
                $row->{"date_finished"}-$row->{"date_started"};
        }
        else
        {
            print "<tr valign=top><td>(no thumbnail)</td>";
            print "<td><b>".escapeHTML($row->{"label"})."</b><br>";
            printf "requested by: ".escapeHTML($row->{"user"}).", id: ".$row->{"id"}."<br>";
            printf "Status: ".$row->{"status"};
            print "<br>".$row->{"errmsg"} if ($row->{"errmsg"} ne "");
            printf "<br>requested %s<br>", mytime($row->{"date_entered"});
        }
        printf "bbox: %.4f,%.4f,%.4f,%.4f, date range: ",
            $row->{"minlat"},$row->{"minlon"},$row->{"maxlat"},$row->{"maxlon"};
        if ($row->{"fromdate"} == 0)
        {
            print "earliest to ";
        }
        else
        {
            printf "20%02d-%02d-%02d to ",
            $row->{"fromdate"}/10000,
            $row->{"fromdate"}%10000/100,
            $row->{"fromdate"}%100;
        }
        if ($row->{"todate"} == 999999)
        {
            print "latest";
        }
        else
        {
            printf "20%02d-%02d-%02d",
            $row->{"todate"}/10000,
            $row->{"todate"}%10000/100,
            $row->{"todate"}%100;
        }
        print "<br><a href=\"?";
        foreach my $v (split(/,/, $values))
        {
            next if ($v eq "height");
            print $v."=".uri_escape($row->{$v})."&";
        }
        print "form=1\">new request based on these parameters</a>";

        if ($row->{"status"} eq "failed")
        {
            printf "&nbsp;/&nbsp;<a href=\"?retry=%d\">re-try this request</a>", $row->{"id"};
        }
        print "</td></tr>\n";
    }
    print "</table>";
    return ($howmany==0) ? $last_shown : undef;
}
