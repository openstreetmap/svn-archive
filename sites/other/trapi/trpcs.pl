#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

# update trapi database based on gziped osm or osc files.
# takes file names on stdin.
# Updates timestamp and deletes the file if it is an osc file.

use strict;
use warnings;

use constant VERBOSE => 5;		# verbosity
use ptdb;

ptdbinit("+<");

$| = 1;

my $ignoretags = IGNORETAGS;
my ($id, $lat, $lon, $x, $y, $ptn, $off, @tv, $tv);
my ($nodes, $ways, $relations, $splits) = (0, 0, 0, 0);
my $deletemode = 0;
while (my $gz = <>) {
    chomp $gz;
    open OSC, "-|", "zcat", $gz
	or die "Could not zcat $gz";
    
    while ($_ = <OSC>) {
	if (/^\s*\<delete\b/) {
	    print "Delete mode\n" if (VERBOSE > 9);
	    $deletemode = 1;
	} elsif (/^\s*\<\/delete\b/) {
	    print "End Delete mode\n" if (VERBOSE > 9);
	    $deletemode = 0;
	} elsif (/^\s*\<create\b/) {
	    print "Create mode\n" if (VERBOSE > 9);
	    $deletemode = 0;
	} elsif (/^\s*\<modify\b/) {
	    print "Modify mode\n" if (VERBOSE > 9);
	    $deletemode = 0;
	} elsif (/^\s*\<node\s/) {
	    $nodes++;
	    @tv = ();
	    unless (/\/\>\s*$/) {
		while (! /\<\/node\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		    if ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
			my $tag = $1;
			my $val = $2;
			push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		    }
		}
	    }
	    print "Node: $_" if (VERBOSE > 20);
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    if ($deletemode) {
		$ptn = nodeptn($id);
		if ($ptn eq NOPTN) {
		    print "Delete of missing node $id ignored\n"
			if (VERBOSE > 7);
		    next;
		}
		my $nf = openptn($ptn,"+<","nodes");
		seek $nf, 0, 0;
		my ($n);
		while (read $nf, $n, 16) {
		    ($n, $lat, $lon, $off) = unpack "NN!N!N", $n;
		    next unless($n == $id);
		    print "Deleting node $id\n" if (VERBOSE > 9);
		    seek $nf, -16, 1;
		    print $nf pack "NN!N!N", 0, 0, 0, 0;
		    last;
		}
		nodeptn($id,NOPTN);
		next;
	    }
	    ($lat) = /\slat\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	    ($lon) = /\slon\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	    ($x, $y) = getTileNumber($lat, $lon, MAXZOOM);
	    $ptn = etoptn($x, $y);
	    print "id: $id lat: $lat lon: $lon x: $x y:$y\n" if (VERBOSE > 18);
	    my $oldptn = nodeptn($id);
	    my $nf = openptn($ptn, "+<", "nodes");
	    my ($uz, $ux, $uy) = fromptn($ptn);
	    if ($oldptn eq NOPTN) {
		print "Creating new node $id in $uz $ux,$uy\n" if (VERBOSE > 11);
		seek $nf, 0, 0;
		my ($n, $tlat, $tlon, $noff, $mt);
		while (read $nf, $n, 16) {
		    ($n, $tlat, $tlon, $noff) = unpack "NN!N!N", $n;
		    unless ($n) {
			$mt //= tell $nf;
			next;
		    }
		    next unless ($n == $id);
		    $mt = tell $nf;
		    last;
		}
		seek $nf, $mt-16, 0 if ($mt);
		if (tell($nf) >= SPLIT) {
		    if (splitptn($ptn)) {
			$splits++;
			$ptn = etoptn($x, $y);
			$nf = openptn($ptn, "+<", "nodes");
			seek $nf, 0, 2;
		    }
		}
	    } elsif ($oldptn eq $ptn) {
		print "Replacing node $id in tile $uz $ux,$uy\n" if (VERBOSE > 6);
		seek $nf, 0, 0;
		my ($n, $tlat, $tlon);
		while (read $nf, $n, 16) {
		    ($n, $tlat, $tlon, $off) = unpack "NN!N!N", $n;
		    if ($n == $id) {
			seek $nf, -16, 1;
			last;
		    }
		}
	    } else {
		if (VERBOSE > 4) {
		    my ($vz, $vx, $vy) = fromptn($oldptn);
		    print "Moving node $id from $vz $vx,$vy to $uz $ux,$uy\n"
		}
		my $onf = openptn($oldptn, "+<", "nodes");
		seek $onf, 0, 0;
		my ($n, $tn, $tlat, $tlon);
		while (read $onf, $n, 16) {
		    ($tn, $tlat, $tlon, $off) = unpack "NN!N!N", $n;
		    if ($tn == $id) {
			seek $onf, -16, 1;
			print $onf pack "NN!N!N", 0, 0, 0, 0;
			last;
		    }
		}
		seek $nf, 0, 2;
		my $owf = openptn($oldptn, "+<", "ways");
		seek $owf, 0, 0;
		my $odf = openptn($oldptn, "+<", "data");
		my ($w, $woff, %wtc, %ways);
		while (read $owf, $w, 8) {
		    ($w, $woff) = unpack "NN", $w;
		    next if ($w == 0);
		    if ($woff == 0) {
			my $wp = wayptn($w);
			if (exists $wtc{$wp}) {
			    ${$wtc{$wp}}{$w} = 1;
			} else {
			    $wtc{$wp} = {$w => 1};
			}
		    } else {
			seek $odf, $woff, 0;
			while (read $odf, $n, 4) {
			    $n = unpack "N", $n;
			    last unless ($n);
			    next unless ($n == $id);
			    $ways{$w} = 1;
			    last;
			}
		    }
		}
		my ($wp);
		foreach $wp (keys %wtc) {
		    next if ($wp eq $ptn);
		    my %wh = %{$wtc{$wp}};
		    $owf = openptn($wp, "+<", "ways");
		    seek $owf, 0, 0;
		    $odf = openptn($wp, "+<", "data");
		    while (read $owf, $w, 8) {
			($w, $woff) = unpack "NN", $w;
			next unless (exists $wh{$w});
			seek $odf, $woff, 0;
			while (read $odf, $n, 4) {
			    $n = unpack "N", $n;
			    last unless ($n);
			    next unless ($n == $id);
			    $ways{$w} = 1;
			    last;
			}
		    }
		}
		my $nwf = openptn($ptn, "+<", "ways");
		seek $nwf, 0, 0;
		while (read $nwf, $w, 8) {
		    ($w, $woff) = unpack "NN", $w;
		    $ways{$w} = 0 if (exists $ways{$w});
		}
		foreach $w (keys %ways) {
		    if ($ways{$w}) {
			print "  adding way $w to $uz $ux,$uy\n"
			    if (VERBOSE > 4);;
			print $nwf pack "NN", $w, 0;
		    }
		}
		my $orf = openptn($oldptn, "+<", "relations");
		seek $orf, 0, 0;
		my ($r, $roff, %rtc, %rels);
	      rproc:	while (read $orf, $r, 8) {
		  ($r, $roff) = unpack "NN", $r;
		  next if ($r == 0);
		  if ($roff == 0) {
		      my $rp = relationptn($r);
		      if (exists $rtc{$rp}) {
			  ${$rtc{$rp}}{$r} = {} unless (exists ${$rtc{$rp}}{$r});
		      } else {
			  $rtc{$rp} = {$r => {}};
		      }
		  } else {
		      seek $odf, $roff, 0;
		      while (read $odf, $n, 4) {
			  $n = unpack "N", $n;
			  last unless ($n);
			  gets($odf);
			  next unless ($n == $id);
			  $rels{$r} = 1;
			  next rproc;
		      }
		      while (read $odf, $w, 4) {
			  $w = unpack "N", $w;
			  last unless ($w);
			  gets($odf);
			  next unless (exists $ways{$w});
			  $rels{$r} = 1;
			  next rproc;
		      }
		      my ($rr);
		      while (read $odf, $rr, 4) {
			  $rr = unpack "N", $rr;
			  last unless ($rr);
			  gets($odf);
			  if (exists $rels{$rr}) {
			      $rels{$r} = 1;
			      next rproc;
			  }
			  my $rrp = relationptn($rr);
			  if (exists $rtc{$rrp}) {
			      my %x = %{$rtc{$rrp}};
			      if (exists $x{$rr}) {
				  ${$x{$rr}}{$r} = 1;
			      } else {
				  $x{$rr} = {$r => 1};
			      }
			  } else {
			      $rtc{$rrp} = {$rr => {$r => 1}};
			  }
		      }
		  }
	      }
		while (my @rt = keys %rtc) {
		    foreach my $t (@rt) {
			my %x = %{$rtc{$t}};
			delete $rtc{$t};
			my $orf = openptn($t, "+<", "relations");
			seek $orf, 0, 0;
			my $odf = openptn($t, "+<", "data");
		      rrtc:		while (read $orf, $r, 8) {
			  ($r, $roff) = unpack "NN", $r;
			  next if (exists $rels{$r});
			  next unless (exists $x{$r});
			  next unless ($roff);
			  seek $odf, $roff, 1;
			  while (read $odf, $n, 4) {
			      $n = unpack "N", $n;
			      last unless ($n);
			      gets($odf);
			      next unless ($n == $id);
			      $rels{$r} = 1;
			      foreach my $rr (keys %{$x{$r}}) {
				  $rels{$rr} = 1;
			      }
			      next rrtc;
			  }
			  while (read $odf, $w, 4) {
			      $w = unpack "N", $w;
			      last unless ($w);
			      gets($odf);
			      next unless (exists $ways{$w});
			      $rels{$r} = 1;
			      foreach my $rr (keys %{$x{$r}}) {
				  $rels{$rr} = 1;
			      }
			      next rrtc;
			  }
			  my ($rr);
			  while (read $odf, $rr, 4) {
			      $rr = unpack "N", $rr;
			      last unless ($rr);
			      gets($odf);
			      if (exists $rels{$rr}) {
				  $rels{$r} = 1;
				  foreach my $rrr (keys %{$x{$r}}) {
				      $rels{$rrr} = 1;
				  }
				  next rrtc;
			      }
			      my $rrp = relationptn($rr);
			      if (exists $rtc{$rrp}) {
				  my %y = %{$rtc{$rrp}};
				  if (exists $y{$rr}) {
				      ${$y{$rr}}{$r} = 1;
				  } else {
				      $y{$rr} = {$r => 1};
				  }
			      } else {
				  $rtc{$rrp} = {$rr => {$r => 1}};
			      }
			  }
		      }
		    }
		}
		my $nrf = openptn($ptn, "+<", "relations");
		seek $nrf, 0, 0;
		while (read $nrf, $r, 8) {
		    ($r, $roff) = unpack "NN", $r;
		    delete $rels{$r} if (exists $rels{$r});
		}
		foreach $r (keys %rels) {
		    print "  adding relation $r to $uz $ux,$uy\n"
			if (VERBOSE > 4);
		    print $nrf pack "NN", $r, 0;
		}
	    }
	    if (@tv) {
		print "writing node $id tags\n" if (VERBOSE > 22);
		my $nd = openptn($ptn, "+<", "data");
		seek $nd, 0, 2;
		$off = tell $nd;
		if ($off == 0) {
		    print $nd "\0";
		    $off = 1;
		}
		print "tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 24);
		foreach $tv (@tv) {
		    print $nd "$tv\0";
		}
		print $nd "\0";
	    } else {
		$off = 0;
	    }
	    print $nf pack "NN!N!N", $id, int($lat * CONV), int($lon * CONV),
	        $off;
	    nodeptn($id, $ptn) if ($ptn ne $oldptn);
	} elsif (/^\s*\<way\s+/) {
	    $ways++;
	    @tv = ();
	    my @nodes = ();
	    unless (/\/\>\s*$/) {
		while (! /\<\/way\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		    if ($tv =~ /\<nd\s+ref\=\"(\d+)\"/) {
			push @nodes, $1;
		    } elsif ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
			my $tag = $1;
			my $val = $2;
			push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		    }
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Way: $_" if (VERBOSE > 19);
	    if ($deletemode) {
		$ptn = wayptn($id);
		if ($ptn eq NOPTN) {
		    print "Delete of nonexistant way $id ignored\n"
			if (VERBOSE > 3);
		    next;
		}
		print "Deleting way $id\n" if (VERBOSE > 4);
		my $wf = openptn($ptn, "+<", "ways");
		seek $wf, 0, 0;
		my ($w);
		while (read $wf, $w, 8) {
		    ($w, $off) = unpack "NN", $w;
		    next unless ($w == $id);
		    seek $wf, -8, 1;
		    print $wf pack "NN", 0, 0;
		    my $wd = openptn($ptn, "+<", "data");
		    seek $wd, $off, 0;
		    my %ptns = ();
		    my ($n);
		    while (read $wd, $n, 4) {
			$n = unpack "N", $n;
			last unless ($n);
			$ptns{nodeptn($n)}++;
		    }
		    delete $ptns{$ptn};
		    foreach my $p (keys %ptns) {
			$wf = openptn($p, "+<", "ways");
			seek $wf, 0, 0;
			while (read $wf, $w, 8) {
			    ($w, $off) = unpack "NN", $w;
			    next unless ($w == $id);
			    seek $wf, -8, 1;
			    print $wf pack "NN", 0, 0;
			    last;
			}
		    }
		    last;
		}
		wayptn($id, NOPTN);
		next;
	    }
	    $ptn = wayptn($id);
	    unless (@nodes) {
		print "Way $id has no nodes\n" if (VERBOSE > 2);
		print "Way: $_" if (VERBOSE > 3);
		if ($ptn ne NOPTN) {
		    my $wf = openptn($ptn, "+<", "ways");
		    seek $wf, 0, 0;
		    my ($w);
		    while (read $wf, $w, 8) {
			($w, $off) = unpack "NN", $w;
			next unless ($w == $id);
			seek $wf, -8, 1;
			print $wf pack "NN", 0, 0;
			my $wd = openptn($ptn, "+<", "data");
			seek $wd, $off, 0;
			my %ptns = ();
			my ($n);
			while (read $wd, $n, 4) {
			    $n = unpack "N", $n;
			    last unless ($n);
			    $ptns{nodeptn($n)}++;
			}
			delete $ptns{$ptn};
			foreach my $p (keys %ptns) {
			    $wf = openptn($p, "+<", "ways");
			    seek $wf, 0, 0;
			    while (read $wf, $w, 8) {
				($w, $off) = unpack "NN", $w;
				next unless ($w == $id);
				seek $wf, -8, 1;
				print $wf pack "NN", 0, 0;
				last;
			    }
			}
			last;
		    }
		    wayptn($id, NOPTN);
		}
		next;
	    }
	    my $new = $ptn eq NOPTN;
	    my ($wf, $wd, %oldptns, %rtc, %ptns);
	    foreach my $node (@nodes) {
		$ptns{nodeptn($node)}++;
	    }
	    if ($new) {
		$ptn = nodeptn($nodes[0]);
		$ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "New way $id in tile $uz $ux,$uy\n";
		}
		$wf = openptn($ptn, "+<", "ways");
		seek $wf, 0, 0;
		my ($w);
		while (read $wf, $w, 8) {
		    ($w, $off) = unpack "NN", $w;
		    next if ($w);
		    seek $wf, -8, 1;
		    last;
		}
		$wd = openptn($ptn, "+<", "data");
		seek $wd, 0, 2;
		if (tell($wd) == 0) {
		    print $wd "\0";
		}
	    } else {
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Update way $id in tile $uz $ux,$uy\n";
		}
		$wf = openptn($ptn, "+<", "ways");
		seek $wf, 0, 0;
		my ($w, $woff);
		$off = undef;
		while (read $wf, $w, 8) {
		    ($w, $woff) = unpack "NN", $w;
		    if ($w == $id) {
			seek $wf, -8, 1;
			$off = $woff if ($woff);
			last;
		    }
		}
		$wd = openptn($ptn, "+<", "data");
		if (defined $off) {
		    seek $wd, $off, 0;
		    while (read $wd, $w, 4) {
			my $node = unpack "N", $w;
			last unless ($node);
			$oldptns{nodeptn($node)}++;
		    }
		}
		my $rf = openptn($ptn, "+<", "relations");
		seek $rf, 0, 0;
		my ($r, $roff);
		while (read $rf, $r, 8) {
		    ($r, $roff) = unpack "NN", $r;
		    next unless ($r);
		    $rtc{$r} = 1;
		}
		unless ((exists $ptns{$ptn}) || ((exists $ptns{NOPTN}) && ($ptn eq toptn(0,1,1)))) {
		    print $wf pack "NN", 0, 0;
		    $ptn = nodeptn($nodes[0]);
		    $ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($ptn);
			print "  moving to $uz $ux,$uy\n";
		    }
		    $wf = openptn($ptn, "+<", "ways");
		    seek $wf, 0, 0;
		    my $mt;
		    while (read $wf, $w, 8) {
			($w, $off) = unpack "NN", $w;
			if ($w == 0) {
			    $mt //= tell $wf;
			    next;
			}
			next unless ($w == $id);
			$mt = tell $wf;
			last;
		    }
		    seek $wf, $mt-8, 0 if (defined $mt);
		    $wd = openptn($ptn, "+<", "data");
		    $new = 1;
		}
		seek $wd, 0, 2;
	    }
	    $off = tell $wd;
	    print "nodes: ".scalar(@nodes)." tags: ".scalar(@tv)." off: $off\n"
		if (VERBOSE > 20);
	    foreach my $node (@nodes) {
		print $wd pack "N", $node;
	    }
	    print $wd pack "N", 0;
	    foreach $tv (@tv) {
		print $wd "$tv\0";
	    }
	    print $wd "\0";
	    print $wf pack "NN", $id, $off;
	    wayptn($id, $ptn) if($new);
	    my %rt;
	    foreach my $p (keys %ptns) {
		if ($p ne $ptn && ! defined($oldptns{$p})) {
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($p);
			print "  adding to z$uz $ux,$uy\n";
		    }
		    my $pwf = openptn($p, "+<", "ways");
		    seek $pwf, 0, 0;
		    my ($mt, $w);
		    while (read $pwf, $w, 8) {
			($w, $off) = unpack "NN", $w;
			unless ($w) {
			    $mt //= tell $pwf;
			    next;
			}
			next unless ($w == $id);
			$mt = tell $pwf;
			last;
		    }
		    seek $pwf, $mt-8, 0 if ($mt);
		    print $pwf pack "NN", $id, 0;
		    my $prf = openptn($p, "+<", "relations");
		    seek $prf, 0, 0;
		    my ($r, $roff, $f);
		    while (read $prf, $r, 8) {
			($r, $roff) = unpack "NN", $r;
			next unless ($r);
			if (exists $rtc{$r}) {
			    $rtc{$r} = 0;
			}
		    }
		    foreach my $r (keys %rtc) {
			if ($rtc{$r}) {
			    unless (exists $rt{$r}) {
				$rt{$r} = {reltiles([], [], [[$r]])};
			    }
			    if (exists ${$rt{$r}}{$p}) {
				seek $prf, 0, 2;
				print $prf pack "NN", $r, 0;
			    }
			} else {
			    $rtc{$r} = 1;
			}
		    }
		}
	    }
	    foreach my $p (keys %oldptns) {
		if ($p ne $ptn && ! defined($ptns{$p})) {
		    my ($uz, $ux, $uy) = fromptn($p);
		    print "  removing from z$uz $ux,$uy\n";
		    my $pwf = openptn($p, "+<", "ways");
		    seek $pwf, 0, 0;
		    my ($w);
		    while (read $pwf, $w, 8) {
			($w, $off) = unpack "NN", $w;
			next unless ($w eq $id);
			seek $pwf, -8, 1;
			print $pwf pack "NN", 0, 0;
			last;
		    }
		}
	    }
	    
	} elsif (/^\s*\<relation\s+/) {
	    $relations++;
	    @tv = ();
	    my @nodes = ();
	    my @ways = ();
	    my @relations = ();
	    unless (/\/\>\s*$/) {
		while (! /\<\/relation\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		    if ($tv =~ /\<member\s+type\=\"(\w+)\"\s+ref\=\"(\d+)\"(?:\s+role\=\"([^\"]*)\")?/) {
			if ($1 eq "node") {
			    push @nodes, [$2, $3];
			} elsif ($1 eq "way") {
			    push @ways, [$2, $3];
			} elsif ($1 eq "relation") {
			    push @relations, [$2, $3];
			} else {
			    print "Unknown relation member type \"$1\" ignored.\n";
			}
		    } elsif ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
			my $tag = $1;
			my $val = $2;
			push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		    }
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Relation: $_" if (VERBOSE > 18);
	    $ptn = relationptn($id);
	    if ($deletemode) {
		if ($ptn ne NOPTN) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Delete relation $id from z$uz $ux,$uy\n"
			if (VERBOSE > 4);
		    my %tiles = reltiles([],[],[[$id]]);
		    while (my $t = each %tiles) {
			my $rf = openptn($t, "+<", "relations");
			seek $rf, 0, 0;
			my $r;
			while (read $rf, $r, 8) {
			    ($r, $off) = unpack "NN", $r;
			    next unless($r == $id);
			    seek $rf, -8, 1;
			    print $rf pack "NN", 0, 0;
			    last;
			}
		    }
		    relationptn($id,NOPTN);
		} else {
		    print "Delete of nonexistant relation $id ignored\n"
			if (VERBOSE > 2);
		}
		next;
	    }
	    my (%oldtiles, %rtc, $rf);
	    my %tiles = reltiles(\@nodes, \@ways, \@relations);
	    if ($ptn eq NOPTN) {
		$ptn = each %tiles;
		unless($ptn) {
		    print "Relation $id has no members\n" if (VERBOSE > 1);
		    print "Relation: $_" if (VERBOSE > 3);
		    next;
		}
		$ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "New relation $id in z$uz $ux,$uy\n";
		}
		$rf = openptn($ptn, "+<", "relations");
		seek $rf, 0, 2;
	    } else {
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Modify relation $id in z$uz $ux,$uy\n";
		}
		%oldtiles = reltiles([],[],[[$id]]);
		$oldtiles{$ptn}++;
		$rf = openptn($ptn, "+<", "relations");
		my ($r, $rp);
		seek $rf, 0, 0;
		while (read $rf, $r, 8) {
		    ($r, $off) = unpack "NN", $r;
		    unless ($r) {
			$rp = tell $rf unless ($rp);
			next;
		    }
		    if ($r == $id) {
			$rp = tell $rf;
		    } else {
			$rtc{$r} = 1;
		    }
		}
		seek $rf, $rp-8, 0 if ($rp);
		unless ($tiles{$ptn}) {
		    print $rf pack "NN", 0, 0;
		    $ptn = (each %tiles) // NOPTN;
		    $ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($ptn);
			print "  moving to z$uz $ux,$uy\n";
		    }
		    $rf = openptn($ptn, "+<", "relations");
		    seek $rf, 0, 0;
		    $rp = undef;
		    while (read $rf, $r, 8) {
			($r, $off) = unpack "NN", $r;
			unless ($r) {
			    $rp = tell $rf unless ($rp);
			    next;
			}
			next unless ($r == $id);
			$rp = tell $rf;
			last;
		    }
		    seek $rf, $rp-8, 0 if ($rp);
		}
	    }
	    my $rd = openptn($ptn, "+<", "data");
	    seek $rd, 0, 2;
	    $off = tell $rd;
	    if ($off == 0) {
		print $rd "\0";
		$off = 1;
	    }
	    print "nodes: ".scalar(@nodes)." ways: ".scalar(@ways)." relations: ".scalar(@relations)." tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 19);
	    foreach my $node (@nodes) {
		print $rd (pack "N", ${$node}[0]).${$node}[1]."\0";
	    }
	    print $rd pack "N", 0;
	    foreach my $way (@ways) {
		print $rd (pack "N", ${$way}[0]).${$way}[1]."\0";
	    }
	    print $rd pack "N", 0;
	    foreach my $rel (@relations) {
		print $rd (pack "N", ${$rel}[0]).${$rel}[1]."\0";
	    }
	    print $rd pack "N", 0;
	    foreach $tv (@tv) {
		print $rd "$tv\0";
	    }
	    print $rd "\0";
	    print $rf pack "NN", $id, $off;
	    relationptn($id, $ptn);
	    my %rt;
	    while (my $p = each %tiles) {
		next if ($p eq $ptn);
		next if (exists $oldtiles{$p});
		if (VERBOSE > 4) {
		    my ($vz, $vx, $vy) = fromptn($p);
		    print "  also in z$vz $vx,$vy\n";
		}
		my $prf = openptn($p, "+<", "relations");
		seek $prf, 0, 0;
		my ($r, $roff, $f, $mt);
		while (read $prf, $r, 8) {
		    ($r, $roff) = unpack "NN", $r;
		    unless ($r) {
			$mt //= tell $prf;
			next;
		    }
		    next unless (exists $rtc{$r});
		    $rtc{$r} = 0;
		}
		seek $prf, $mt - 8, 0 if (defined $mt);
		print $prf pack "NN", $id, 0;
		foreach $r (keys %rtc) {
		    if ($rtc{$r}) {
			unless (exists $rt{$r}) {
			    $rt{$r} = {reltiles([],[],[[$r]])};
			}
			if (exists ${$rt{$r}}{$p}) {
			    seek $prf, 0, 0;
			    my ($rr, $rroff, $mt);
			    while (read $prf, $rr, 8) {
				($rr, $rroff) = unpack "NN", $rr;
				unless ($rr) {
				    $mt //= tell $prf;
				    next;
				}
				next unless ($rr == $r);
				$mt = tell $prf;
				last;
			    }
			    seek $prf, $mt-8, 0 if ($mt);
			    print $prf pack "NN", $r, 0;
			}
		    } else {
			$rtc{$r} = 1;
		    }
		}
	    }
	    while (my $p = each %oldtiles) {
		next if ($p eq $ptn);
		next if (exists $tiles{$p});
		my ($vz, $vx, $vy) = fromptn($p);
		print "  remove from z$vz $vx,$vy\n" if (VERBOSE > 4);
		my $prf = openptn($p, "+<", "relations");
		seek $prf, 0, 0;
		my ($r, $roff);
		while (read $prf, $r, 8) {
		    ($r, $roff) = unpack "NN", $r;
		    next unless ($r == $id);
		    seek $prf, -8, 1;
		    print $prf pack "NN", 0, 0;
		    last;
		}
	    }
	}
    }
    
    writecache();
    if ($gz =~ /(?:^|\/)\d+\-(\d+)\.osc\.gz$/) {
	open STAMP, ">", "timestamp" or die "Could not open timestamp";
	print STAMP "$1\n";
	close STAMP;
	unlink $gz;
    }
    print "Processed $gz\n" if (VERBOSE > 0);
    print "Nodes: $nodes Ways: $ways Relations: $relations Splits: $splits\n"
	if (VERBOSE > 1);
    cachestat() if (VERBOSE > 2);
    
}
