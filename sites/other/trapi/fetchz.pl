#!/usr/bin/perl

use strict;
use warnings;

use constant VERSION=0;
use ptdb;

ptdbinit("<");

my ($ptn, $n, $tn, $lat, $lon, $off, $key, $val, $w, $tw, $tr);
my (%pw, %pn, %pr, %tiles);

my ($bbs, $bbw, $bbn, $bbe);

print "Content-Type: text/xml; charset=utf8\n\n";

$_ = $ARGV[0];
if (/map\?bbox\=(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)$/) {
# print "WSEN: $1, $2, $3, $4\n";
    my ($west,$south) = getTileNumber($2,$1,14);
    my ($east,$north) = getTileNumber($4,$3,14);
# print "WSEN: $west, $south, $east, $north\n";
    my ($x, $y);
    for($y=$north; $y <= $south; $y++) {
	for($x=$west; $x <= $east; $x++) {
	    $tiles{etoptn($x,$y)} = 1;
	}
    }
    ($bbs, $bbw, undef, undef) = Project($west, $south, 14);
    (undef, undef, $bbn, $bbe) = Project($east, $north, 14);
} elsif (/node\/(\d+)$/) {
    my $node = $1;
    $ptn = nodeptn($node);
    $pn{$ptn} = {$node => 1};
} elsif (/way\/(\d+)$/) {
    my $way = $1;
    $ptn = wayptn($way);
    $pw{$ptn} = {$way => 1};
} elsif (/relation\/(\d+)$/) {
    my $rel = $1;
    $ptn = relationptn($rel);
    $pr{$ptn} = {$rel => 1};
} else {
    die "Unknown request $_";
}

print "<?xml version='1.0' encoding='UTF-8'?>\n";
print "<osm version=\"0.5\" generator=\"Trapi 0.0\">\n";
if ($bbs) {
  print "<bound box=\"$bbs,$bbw,$bbn,$bbe\" origin=\"http://www.openstreetmap.org/api/0.5\"/>\n";
}

foreach $ptn (keys %tiles) {
    my $nd = openptn($ptn, "<", "data");
    my $wf = openptn($ptn, "<", "ways");
    my $rf = openptn($ptn, "<", "relations");

# first we go through the ways, looking for ones stored remotely or with nodes
# not in the tile
    seek $wf, 0, 0;
    while (read $wf, $w, 8) {
      ($tw, $off) = unpack "NN", $w;
      next unless($tw);
      if ($off == 0) {
	# way stored remotly
        $w = wayptn($tw);
# print "Remote way $tw\n";
	unless (exists $tiles{$w}) {
	    unless (defined $pw{$w}) {
		$pw{$w} = {};
	    }
	    ${$pw{$w}}{$tw} = 1;
        }
      } else {
	seek $nd, $off, 0;
	while(read $nd, $n, 4) {
	  ($tn) = unpack "N", $n;
	  last unless ($tn);
	  $n = nodeptn($tn);
	  unless (exists $tiles{$n}) {
	    # node stored remotly
	    unless (defined $pn{$n}) {
	      $pn{$n} = {};
	    }
	    ${$pn{$n}}{$tn} = 1;
	  }
	}
      }
    }
    seek $rf, 0, 0;
    while (read $rf, $w, 8) {
      ($tr, $off) = unpack "NN", $w;
      next unless($tr);
      if ($off == 0) {
	my $r = relationptn($tr);
	unless (exists $tiles{$r}) {
	  unless (defined $pr{$r}) {
	    $pr{$r} = {};
	  }
	  ${$pr{$r}}{$tr} = 1;
        }
      }
    }
}

# now we go through the remote ways, looking for nodes and ways not in the tile
  foreach my $tp (keys %pw) {
    my $pwf = openptn($tp, "<", "ways");
    my $pd = openptn($tp, "<", "data");
    seek $pwf, 0, 0;
    while (read $pwf, $w, 8) {
      ($tw, $off) = unpack "NN", $w;
      next unless($tw);
      if (exists ${$pw{$tp}}{$tw}) {
	seek $pd, $off, 0;
	while(read $pd, $n, 4) {
	  ($tn) = unpack "N", $n;
	  last unless ($tn);
	  $n = nodeptn($tn);
	  unless (exists $tiles{$n}) {
	  # node stored remotly
	    unless (defined $pn{$n}) {
	      $pn{$n} = {};
	    }
	    ${$pn{$n}}{$tn} = 1;
	  }
	}
      }
    }
  }
    
# print nodes in the tile
foreach $ptn (keys %tiles) {
    my $nf = openptn($ptn, "<", "nodes");
    my $nd = openptn($ptn, "<", "data");

    my ($z, $x, $y) = fromptn($ptn);
    print "<-- nodes from z$z $x $y >\n";
    seek $nf, 0, 0;
    while (read $nf, $n, 16) {
	($tn, $lat, $lon, $off) = unpack "NN!N!N", $n;
	next unless($tn);
	$lat /= 10000000;
	$lon /= 10000000;
	print "<node id=\"$tn\" lat=\"$lat\" lon=\"$lon\" ";
	if ($off == 0) {
	    print "/>\n";
	} else {
	    print ">\n";
	    seek $nd, $off, 0;
	    while ($key = gets $nd) {
                $val = gets $nd;
                print "  <tag k=\"$key\" v=\"$val\"/>\n";
            }
	    print "</node>\n";
	}
    }
}

# print the nodes used by ways

    foreach my $tp (keys %pn) {
      my ($tz, $tx, $ty) = fromptn($tp);
      print "<-- some nodes from z$tz $tx $ty >\n";
      my $pnf = openptn($tp, "<", "nodes");
      my $pd = openptn($tp, "<", "data");
      seek $pnf, 0, 0;
      while(read $pnf, $n, 16) {
	($tn, $lat, $lon, $off) = unpack "NN!N!N", $n;
	next unless($tn);
	if (exists ${$pn{$tp}}{$tn}) {
	  $lat /= 10000000;
	  $lon /= 10000000;
	  print "<node id=\"$tn\" lat=\"$lat\" lon=\"$lon\" ";
	  if ($off == 0) {
	    print "/>\n";
	  } else {
	    print ">\n";
	    seek $pd, $off, 0;
	    while ($key = gets $pd) {
	      $val = gets $pd;
	      print "  <tag k=\"$key\" v=\"$val\"/>\n";
	    }
	    print "</node>\n";
	  }
	}
      }
    }

# print ways
foreach $ptn (keys %tiles) {
    my $nd = openptn($ptn, "<", "data");
    my $wf = openptn($ptn, "<", "ways");

    my ($z, $x, $y) = fromptn($ptn);
    print "<-- ways from z$z $x $y >\n";
    seek $wf, 0, 0;
    while(read $wf, $w, 8) {
      ($tw, $off) = unpack "NN", $w;
      next unless ($tw);
      next unless ($off);
      print "<way id=\"$tw\">\n";
      seek $nd, $off, 0;
      while (read $nd, $w, 4) {
	($tn) = unpack "N", $w;
	last if($tn == 0);
	print "  <nd ref=\"$tn\"/>\n";
      }
      while ($key = gets $nd) {
	$val = gets $nd;
	print "  <tag k=\"$key\" v=\"$val\"/>\n";
      }
      print "</way>\n";
    }
  }

    foreach my $tp (keys %pw) {
      my ($tz, $tx, $ty) = fromptn($tp);
      print "<-- some ways from z$tz $tx $ty >\n";
      my $pwf = openptn($tp, "<", "ways");
      my $pd = openptn($tp, "<", "data");
      seek $pwf, 0, 0;
      while (read $pwf, $w, 8) {
	($tw, $off) = unpack "NN", $w;
	next unless($tw);
	if ($off && exists ${$pw{$tp}}{$tw}) {
	  print "<way id=\"$tw\">\n";
	  seek $pd, $off, 0;
	  while(read $pd, $n, 4) {
	    ($tn) = unpack "N", $n;
	    last unless ($tn);
	    print "  <nd ref=\"$tn\"/>\n";
	  }
	  while ($key = gets $pd) {
	    $val = gets $pd;
	    print "  <tag k=\"$key\" v=\"$val\"/>\n";
	  }
	  print "</way>\n";
	}
      }
    }

# print relations

foreach $ptn (keys %tiles) {
  my $nd = openptn($ptn, "<", "data");
  my $rf = openptn($ptn, "<", "relations");
  
  my ($z, $x, $y) = fromptn($ptn);
  print "<-- relations from z$z $x $y>\n";
  seek $rf, 0, 0;
  while (read $rf, $w, 8) {
    ($tr, $off) = unpack "NN", $w;
    next unless ($tr);
    next unless ($off);
    print "<relation id=\"$tr\">\n";
    seek $nd, $off, 0;
    while (read $nd, $w, 4) {
      ($tn) = unpack "N", $w;
      last unless ($tn);
      my $role = gets($nd);
      print "  <member type=\"node\" ref=\"$tn\" role=\"$role\"/>\n";
    }
    while (read $nd, $w, 4) {
      ($tw) = unpack "N", $w;
      last unless ($tw);
      my $role = gets($nd);
      print "  <member type=\"way\" ref=\"$tw\" role=\"$role\"/>\n";
    }
    while (read $nd, $w, 4) {
      ($tw) = unpack "N", $w;
      last unless ($tw);
      my $role = gets($nd);
      print "  <member type=\"relation\" ref=\"$tw\" role=\"$role\"/>\n";
    }
      while ($key = gets $nd) {
	$val = gets $nd;
	print "  <tag k=\"$key\" v=\"$val\"/>\n";
      }
    print "</relation>\n";
  }
}

foreach my $tp (keys %pr) {
  my ($tz, $tx, $ty) = fromptn($tp);
  print "<-- some relations from z$tz $tx $ty >\n";
  my $prf = openptn($tp, "<", "relations");
  my $pd = openptn($tp, "<", "data");
  seek $prf, 0, 0;
  while (read $prf, $w, 8) {
    ($tr, $off) = unpack "NN", $w;
    next unless($tr);
    if ($off && exists ${$pr{$tp}}{$tr}) {
      print "<relation id=\"$tr\">\n";
      seek $pd, $off, 0;
      while (read $pd, $w, 4) {
        ($tn) = unpack "N", $w;
        last unless ($tn);
        my $role = gets($pd);
        print "  <member type=\"node\" ref=\"$tn\" role=\"$role\"/>\n";
      }
      while (read $pd, $w, 4) {
        ($tw) = unpack "N", $w;
        last unless ($tw);
        my $role = gets($pd);
        print "  <member type=\"way\" ref=\"$tw\" role=\"$role\"/>\n";
      }
      while (read $pd, $w, 4) {
        ($tw) = unpack "N", $w;
        last unless ($tw);
        my $role = gets($pd);
        print "  <member type=\"relation\" ref=\"$tw\" role=\"$role\"/>\n";
      }
      while ($key = gets $pd) {
	$val = gets $pd;
	print "  <tag k=\"$key\" v=\"$val\"/>\n";
      }
      print "</relation>\n";
    }
  }
}

print "</osm>\n";
