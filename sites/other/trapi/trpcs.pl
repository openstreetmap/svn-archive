#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

# update trapi database based on gziped osm or osc files.
# takes file names on stdin.
# Updates timestamp and deletes the file if it is an osc file.

use strict;
use warnings;

use constant VERBOSE => 5;		# verbosity
use trapi;

chdir TRAPIDIR or die "could not chdir ".TRAPIDIR.": $!";

ptdbinit("+<");

$| = 1;

our (%togc, $cachecount);
# garbage collect
sub garbagecollect() {
    our (%filecache, %whengc, $devnull);
    my @togc = sort {($whengc{$a} // 0) <=> ($whengc{$b} // 0) ||
	 ($togc{$a} // 0) <=> ($togc{$b} // 0)} keys %togc;
    my $todo = GCCOUNT;
    while (my $ptn = shift @togc) {
	# avoid tiles being used
	next if (exists $filecache{$ptn."data"});
	gcptn($ptn);
	$whengc{$ptn} = $cachecount;
	delete $togc{$ptn};
	last unless (--$todo);
    }
    print "Tiles left to garbagecollect: ".scalar(@togc)."\n"
	if (VERBOSE > 3 && scalar(@togc));
}

my $ignoretags = IGNORETAGS;
my ($id, $lat, $lon, $x, $y, $ptn, $off, @tv, $tv);
my ($nodes, $ways, $relations, $splits) = (0, 0, 0, 0);
my $deletemode = 0;
while (my $gz = <>) {
    chomp $gz;
    open OSC, "-|", "zcat", $gz
	or die "Could not zcat $gz";
    
    # add/modify
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
	} elsif ($deletemode) {
	    next;
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
			push @tv, $tag, $val unless (IGNORETAGS && $tag =~ /$ignoretags/o);
		    }
		}
	    }
	    print "Node: $_" if (VERBOSE > 20);
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    ($lat) = /\slat\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	    ($lon) = /\slon\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	    ($x, $y) = getTileNumber($lat, $lon, MAXZOOM);
	    $ptn = etoptn($x, $y);
	    print "id: $id lat: $lat lon: $lon x: $x y:$y\n" if (VERBOSE > 18);
	    my $oldptn = nodeptn($id);
	    my $nf = openptn($ptn, "nodes");
	    my ($uz, $ux, $uy) = fromptn($ptn);
	    if ($oldptn eq NOPTN) {
		print "Creating new node $id in $uz $ux,$uy\n" if (VERBOSE > 11);
		seek $nf, 0, 0;
		my ($mt);
		while (my ($n, $tlat, $tlon, $noff) = readnode($nf)) {
		    last unless (defined $n);
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
			delete $togc{$ptn};
			our %whengc;
			delete $whengc{$ptn};
			$ptn = etoptn($x, $y);
			$nf = openptn($ptn, "nodes");
			seek $nf, 0, 2;
		    }
		}
	    } elsif ($oldptn eq $ptn) {
		print "Replacing node $id in tile $uz $ux,$uy\n" if (VERBOSE > 6);
		seek $nf, 0, 0;
		while (my ($n, $tlat, $tlon, $off) = readnode($nf)) {
		    last unless (defined $n);
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
		my $onf = openptn($oldptn, "nodes");
		seek $onf, 0, 0;
		while (my ($tn, $tlat, $tlon, $toff) = readnode($onf)) {
		    last unless (defined $tn);
		    if ($tn == $id) {
			seek $onf, -16, 1;
			printnode($onf, 0, 0, 0, 0);
			$togc{$oldptn} = $cachecount if ($toff);
			last;
		    }
		}
		seek $nf, 0, 2;
		my $owf = openptn($oldptn, "ways");
		seek $owf, 0, 0;
		my $odf = openptn($oldptn, "data");
		my (%wtc, %ways);
		while (my ($w, $woff) = readway($owf)) {
		    last unless (defined $w);
		    next if ($w == 0);
		    if ($woff == 0) {
			my $wp = wayptn($w);
			$wtc{$wp} //= {};
			$wtc{$wp}->{$w} = 1;
		    } else {
			seek $odf, $woff, 0;
			my @nodes = readwaynodes($odf);
			foreach my $n (@nodes) {
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
		    $owf = openptn($wp, "ways");
		    seek $owf, 0, 0;
		    $odf = openptn($wp, "data");
		    while (my ($w, $woff) = readway($owf)) {
			last unless (defined $w);
			next unless (exists $wh{$w});
			seek $odf, $woff, 0;
			my @nodes = readwaynodes($odf);
			foreach my $n (@nodes) {
			    next unless ($n == $id);
			    $ways{$w} = 1;
			    last;
			}
		    }
		}
		my $nwf = openptn($ptn, "ways");
		seek $nwf, 0, 0;
		while (my ($w, $woff) = readway($nwf)) {
		    last unless (defined $w);
		    next unless($w);
		    $ways{$w} = 0 if (exists $ways{$w});
		}
		our %waycache;
		foreach my $w (keys %ways) {
		    if ($ways{$w}) {
			delete $waycache{$w};
			print "  adding way $w to $uz $ux,$uy\n"
			    if (VERBOSE > 4);;
			printway($nwf, $w, 0);
		    }
		}
		my $orf = openptn($oldptn, "relations");
		$odf = openptn($oldptn, "data");
		seek $orf, 0, 0;
		my (%rtc, %rels);
	      rproc:	while (my ($r, $roff) = readrel($orf)) {
		  last unless (defined $r);
		  next unless ($r);
		  if ($roff == 0) {
		      my $rp = relationptn($r);
		      $rtc{$rp} //= {};
		      $rtc{$rp}->{$r} //= {};
		  } else {
		      seek $odf, $roff, 0;
		      my @members = readmemb($odf);
		      foreach my $m (@members) {
			  my ($type, $mid, $role) = @$m;
			  if ($type == NODE) {
			      next unless ($mid == $id);
			      $rels{$r} = 1;
			      next rproc;
			  } elsif ($type == WAY) {
			      next unless (exists $ways{$mid});
			      $rels{$r} = 1;
			      next rproc;
			  } elsif ($type == RELATION) {
			      if (exists $rels{$mid}) {
				  $rels{$r} = 1;
				  next rproc;
			      }
			      my $rrp = relationptn($mid);
			      $rtc{$rrp} //= {};
			      $rtc{$rrp}->{$mid} //= {};
			      $rtc{$rrp}->{$mid}->{$r} = 1;
			  } else {
			      die "Unknown relation $r type $type";
			  }
		      }
		  }
	      }
		my %rseen;
		while (my @rt = keys %rtc) {
		    foreach my $t (@rt) {
			my %x = %{$rtc{$t}};
			delete $rtc{$t};
			my $orf = openptn($t, "relations");
			seek $orf, 0, 0;
			my $odf = openptn($t, "data");
		      rrtc: while (my ($r, $roff) = readrel($orf)) {
			  last unless (defined $r);
			  next unless ($r && $roff);
			  next if (exists $rels{$r});
			  next unless (exists $x{$r});
			  $rseen{$r} = 1;
			  seek $odf, $roff, 0;
			  my @members = readmemb($odf);
			  foreach my $m (@members) {
			      my ($type, $mid, $role) = @$m;
			      if ($type == NODE) {
				  next unless ($mid == $id);
				  $rels{$r} = 1;
				  foreach my $rr (keys %{$x{$r}}) {
				      $rels{$rr} = 1;
				  }
				  next rrtc;
			      } elsif ($type == WAY) {
				  next unless (exists $ways{$mid});
				  $rels{$r} = 1;
				  foreach my $rr (keys %{$x{$r}}) {
				      $rels{$rr} = 1;
				  }
				  next rrtc;
			      } elsif ($type == RELATION) {
				  if (exists $rels{$mid}) {
				      $rels{$r} = 1;
				      foreach my $rrr (keys %{$x{$r}}) {
					  $rels{$rrr} = 1;
				      }
				      next rrtc;
				  }
				  if ($rseen{$mid}) {
				      print "  seen relation $mid before\n"
					  if (VERBOSE > 99);
				      next;
				  }
				  my $rrp = relationptn($mid);
				  $rtc{$rrp} //= {};
				  $rtc{$rrp}->{$mid} //= {};
				  $rtc{$rrp}->{$mid}->{$r} = 1;
			      } else {
				  die "unknown relation $r type $type";
			      }
			  }
		      }
		    }
		}
		my $nrf = openptn($ptn, "relations");
		seek $nrf, 0, 0;
		while (my ($r, $roff) = readrel($nrf)) {
		    last unless (defined $r);
		    next unless ($r);
		    delete $rels{$r} if (exists $rels{$r});
		}
		foreach my $r (keys %rels) {
		    print "  adding relation $r to z$uz $ux,$uy\n"
			if (VERBOSE > 4);
		    printrel($nrf, $r, 0);
		}
	    }
	    if (@tv) {
		print "writing node $id tags\n" if (VERBOSE > 22);
		my $nd = openptn($ptn, "data");
		$togc{$ptn} = $cachecount;
		seek $nd, 0, 2;
		$off = tell $nd;
		print "tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 24);
		printtags($nd, \@tv, NODE);
	    } else {
		$off = 0;
	    }
	    printnode($nf, $id, int($lat * CONV), int($lon * CONV), $off);
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
			push @tv, $tag, $val unless (IGNORETAGS && $tag =~ /$ignoretags/o);
		    }
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Way: $_" if (VERBOSE > 19);
	    $ptn = wayptn($id);
	    our %waycache;
	    delete $waycache{$id};
	    unless (@nodes) {
		print "Way $id has no nodes\n" if (VERBOSE > 2);
		print "Way: $_" if (VERBOSE > 3);
		if ($ptn ne NOPTN) {
		    my $wf = openptn($ptn, "ways");
		    seek $wf, 0, 0;
		    while (my ($w, $off) = readway($wf)) {
			last unless (defined $w);
			next unless ($w == $id);
			seek $wf, -8, 1;
			printway($wf, 0, 0);
			my $wd = openptn($ptn, "data");
			$togc{$ptn} = $cachecount;
			seek $wd, $off, 0;
			my %ptns = ();
			my @nodes = readwaynodes($wd);
			foreach my $n (@nodes) {
			    $ptns{nodeptn($n)}++;
			}
			delete $ptns{$ptn};
			foreach my $p (keys %ptns) {
			    $wf = openptn($p, "ways");
			    seek $wf, 0, 0;
			    while (my ($w, $off) = readway($wf)) {
				last unless (defined $w);
				next unless ($w == $id);
				seek $wf, -8, 1;
				printway($wf, 0, 0);
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
		$wf = openptn($ptn, "ways");
		seek $wf, 0, 0;
		while (my ($w, $off) = readway($wf)) {
		    last unless (defined $w);
		    next if ($w);
		    seek $wf, -8, 1;
		    last;
		}
		$wd = openptn($ptn, "data");
		seek $wd, 0, 2;
	    } else {
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Update way $id in tile $uz $ux,$uy\n";
		}
		$wf = openptn($ptn, "ways");
		seek $wf, 0, 0;
		$off = undef;
		while (my ($w, $woff) = readway($wf)) {
		    last unless (defined $w);
		    if ($w == $id) {
			seek $wf, -8, 1;
			$off = $woff if ($woff);
			last;
		    }
		}
		$wd = openptn($ptn, "data");
		$togc{$ptn} = $cachecount;
		if (defined $off) {
		    seek $wd, $off, 0;
		    my @n = readwaynodes($wd);
		    foreach my $node (@n) {
			$oldptns{nodeptn($node)}++;
		    }
		}
		my $rf = openptn($ptn, "relations");
		seek $rf, 0, 0;
		while (my ($r, $roff) = readrel($rf)) {
		    last unless (defined $r);
		    next unless ($r);
		    $rtc{$r} = 1;
		}
		unless ((exists $ptns{$ptn}) || ((exists $ptns{NOPTN}) && ($ptn eq toptn(0,1,1)))) {
		    printway($wf, 0, 0);
		    $ptn = nodeptn($nodes[0]);
		    $ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($ptn);
			print "  moving to z$uz $ux,$uy\n";
		    }
		    $wf = openptn($ptn, "ways");
		    seek $wf, 0, 0;
		    my $mt;
		    while (my ($w, $off) = readway($wf)) {
			last unless (defined $w);
			if ($w == 0) {
			    $mt //= tell $wf;
			    next;
			}
			next unless ($w == $id);
			$mt = tell $wf;
			last;
		    }
		    seek $wf, $mt-8, 0 if (defined $mt);
		    $wd = openptn($ptn, "data");
		    $togc{$ptn} = $cachecount;
		    $new = 1;
		}
		seek $wd, 0, 2;
	    }
	    $off = tell $wd;
	    print "nodes: ".scalar(@nodes)." tags: ".scalar(@tv)." off: $off\n"
		if (VERBOSE > 20);
	    printwaynodes($wd, \@nodes);
	    printtags($wd, \@tv, WAY);
	    printway($wf, $id, $off);
	    wayptn($id, $ptn) if($new);
	    my %rt;
	    foreach my $p (keys %ptns) {
		if ($p ne $ptn && ! defined($oldptns{$p})) {
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($p);
			print "  adding to z$uz $ux,$uy\n";
		    }
		    my $pwf = openptn($p, "ways");
		    seek $pwf, 0, 0;
		    my ($mt);
		    while (my ($w, $off) = readway($pwf)) {
			last unless (defined $w);
			unless ($w) {
			    $mt //= tell $pwf;
			    next;
			}
			next unless ($w == $id);
			$mt = tell $pwf;
			last;
		    }
		    seek $pwf, $mt-8, 0 if ($mt);
		    printway($pwf, $id, 0);
		    my $prf = openptn($p, "relations");
		    seek $prf, 0, 0;
		    my ($f);
		    while (my ($r, $roff) = readrel($prf)) {
			last unless (defined $r);
			next unless ($r);
			if (exists $rtc{$r}) {
			    $rtc{$r} = 0;
			}
		    }
		    foreach my $r (keys %rtc) {
			if ($rtc{$r}) {
			    unless (exists $rt{$r}) {
				$rt{$r} = {reltiles([[3, $r]])};
			    }
			    if (exists $rt{$r}->{$p}) {
				seek $prf, 0, 2;
				printrel($prf, $r, 0);
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
		    my $pwf = openptn($p, "ways");
		    seek $pwf, 0, 0;
		    while (my ($w, $woff) = readway($pwf)) {
			last unless (defined $w);
			next unless ($w eq $id);
			seek $pwf, -8, 1;
			printway($pwf, 0, 0);
			last;
		    }
		}
	    }
	    
	} elsif (/^\s*\<relation\s+/) {
	    $relations++;
	    @tv = ();
	    my @members = ();
	    unless (/\/\>\s*$/) {
		while (! /\<\/relation\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		    if ($tv =~ /\<member\s+type\=\"(\w+)\"\s+ref\=\"(\d+)\"(?:\s+role\=\"([^\"]*)\")?/) {
			push @members, [MEMBER->{$1}, $2, $3];
		    } elsif ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
			my $tag = $1;
			my $val = $2;
			push @tv, $tag, $val unless (IGNORETAGS && $tag =~ /$ignoretags/o);
		    }
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Relation: $_" if (VERBOSE > 18);
	    $ptn = relationptn($id);
	    my (%oldtiles, %rtc, $rf);
	    my %tiles = reltiles(\@members);
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
		$rf = openptn($ptn, "relations");
		seek $rf, 0, 2;
	    } else {
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Modify relation $id in z$uz $ux,$uy\n";
		}
		%oldtiles = reltiles([[3, $id]]);
		$oldtiles{$ptn}++;
		$rf = openptn($ptn, "relations");
		my ($rp);
		seek $rf, 0, 0;
		while (my ($r, $off) = readrel($rf)) {
		    last unless (defined $r);
		    unless ($r) {
			$rp //= tell $rf;
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
		    printrel($rf, 0, 0);
		    $ptn = (each %tiles) // NOPTN;
		    $ptn = toptn(0,1,1) if ($ptn eq NOPTN);
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($ptn);
			print "  moving to z$uz $ux,$uy\n";
		    }
		    $rf = openptn($ptn, "relations");
		    seek $rf, 0, 0;
		    $rp = undef;
		    while (my ($r, $off) = readrel($rf)) {
			last unless (defined $r);
			unless ($r) {
			    $rp //= tell $rf;
			    next;
			}
			next unless ($r == $id);
			$rp = tell $rf;
			last;
		    }
		    seek $rf, $rp-8, 0 if ($rp);
		}
	    }
	    my $rd = openptn($ptn, "data");
	    $togc{$ptn} = $cachecount;
	    seek $rd, 0, 2;
	    $off = tell $rd;
	    print "members: ".scalar(@members)." tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 19);
	    printmemb($rd, \@members);
	    printtags($rd, \@tv, RELATION);
	    printrel($rf, $id, $off);
	    relationptn($id, $ptn);
	    my %rt;
	    while (my $p = each %tiles) {
		next if ($p eq $ptn);
		next if (exists $oldtiles{$p});
		if (VERBOSE > 4) {
		    my ($vz, $vx, $vy) = fromptn($p);
		    print "  also in z$vz $vx,$vy\n";
		}
		my $prf = openptn($p, "relations");
		seek $prf, 0, 0;
		my ($f, $mt);
		while (my ($r, $roff) = readrel($prf)) {
		    last unless (defined $r);
		    unless ($r) {
			$mt //= tell $prf;
			next;
		    }
		    if ($r == $id) {
			$mt = tell $prf;
			last;
		    }
		    next unless (exists $rtc{$r});
		    $rtc{$r} = 0;
		}
		seek $prf, $mt - 8, 0 if (defined $mt);
		printrel($prf, $id, 0);
		foreach my $r (keys %rtc) {
		    if ($rtc{$r}) {
			unless (exists $rt{$r}) {
			    $rt{$r} = {reltiles([[3, $r]])};
			}
			if (exists $rt{$r}->{$p}) {
			    seek $prf, 0, 0;
			    my ($mt);
			    while (my ($rr, $rroff) = readrel($prf)) {
				last unless (defined $rr);
				unless ($rr) {
				    $mt //= tell $prf;
				    next;
				}
				next unless ($rr == $r);
				$mt = tell $prf;
				last;
			    }
			    seek $prf, $mt-8, 0 if ($mt);
			    printrel($prf, $r, 0);
			}
		    } else {
			$rtc{$r} = 1;
		    }
		}
	    }
	    while (my $p = each %oldtiles) {
		next if ($p eq $ptn);
		next if (exists $tiles{$p});
		if (VERBOSE > 4) {
		    my ($vz, $vx, $vy) = fromptn($p);
		    print "  remove from z$vz $vx,$vy\n";
		}
		my $prf = openptn($p, "relations");
		seek $prf, 0, 0;
		while (my ($r, $roff) = readrel($prf)) {
		    last unless (defined $r);
		    next unless ($r == $id);
		    seek $prf, -8, 1;
		    printrel($prf, 0, 0);
		    last;
		}
	    }
	}
    }
    close OSC;
    open OSC, "-|", "zcat", $gz
	or die "Could not zcat $gz";
    
    # delete relations
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
	} elsif (!$deletemode) {
	    next;
	} elsif (/^\s*\<relation\s+/) {
	    $relations++;
	    unless (/\/\>\s*$/) {
		while (! /\<\/relation\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Relation: $_" if (VERBOSE > 18);
	    $ptn = relationptn($id);
	    if ($ptn ne NOPTN) {
		$togc{$ptn} = $cachecount;
		my ($uz, $ux, $uy) = fromptn($ptn);
		print "Delete relation $id from z$uz $ux,$uy\n"
		    if (VERBOSE > 4);
		my %tiles = reltiles([[3, $id]]);
		$tiles{$ptn}++;
		$tiles{toptn(0,1,1)}++ if ($tiles{NOPTN});
		while (my $t = each %tiles) {
		    my $rf = openptn($t, "relations");
		    seek $rf, 0, 0;
		    while (my ($r, $off) = readrel($rf)) {
			last unless (defined $r);
			next unless($r == $id);
			seek $rf, -8, 1;
			printrel($rf, 0, 0);
			last;
		    }
		}
		relationptn($id, NOPTN);
	    } else {
		print "Delete of nonexistant relation $id ignored\n"
		    if (VERBOSE > 2);
	    }
	}
    }
    close OSC;
    open OSC, "-|", "zcat", $gz
	or die "Could not zcat $gz";
    # delete ways
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
	} elsif (!$deletemode) {
	    next;
	} elsif (/^\s*\<way\s+/) {
	    $ways++;
	    unless (/\/\>\s*$/) {
		while (! /\<\/way\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		}
	    }
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    print "Way: $_" if (VERBOSE > 19);
	    our %waycache;
	    delete $waycache{$id};
	    $ptn = wayptn($id);
	    if ($ptn eq NOPTN) {
		print "Delete of nonexistant way $id ignored\n"
		    if (VERBOSE > 3);
		next;
	    }
	    if (VERBOSE > 4) {
		my ($uz, $ux, $uy) = fromptn($ptn);
		print "Deleting way $id from z$uz $ux,$uy\n";
	    }
	    my $wf = openptn($ptn, "ways");
	    seek $wf, 0, 0;
	    while (my ($w, $off) = readway($wf)) {
		last unless (defined $w);
		next unless ($w == $id);
		seek $wf, -8, 1;
		printway($wf, 0, 0);
		my $wd = openptn($ptn, "data");
		$togc{$ptn} = $cachecount;
		seek $wd, $off, 0;
		my %ptns = ();
		my @nodes = readwaynodes($wd);
		foreach my $n (@nodes) {
		    $ptns{nodeptn($n)}++;
		}
		delete $ptns{$ptn};
		foreach my $p (keys %ptns) {
		    $wf = openptn($p, "ways");
		    seek $wf, 0, 0;
		    while (my ($w, $off) = readway($wf)) {
			last unless (defined $w);
			next unless ($w == $id);
			seek $wf, -8, 1;
			printway($wf, 0, 0);
			last;
		    }
		}
		last;
	    }
	    wayptn($id, NOPTN);
	}
    }
    close OSC;
    open OSC, "-|", "zcat", $gz
	or die "Could not zcat $gz";
    # delete nodes
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
	} elsif (!$deletemode) {
	    next;
	} elsif (/^\s*\<node\s/) {
	    $nodes++;
	    unless (/\/\>\s*$/) {
		while (! /\<\/node\>/s) {
		    $tv = <OSC>;
		    $_ .= $tv;
		}
	    }
	    print "Node: $_" if (VERBOSE > 20);
	    ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	    $ptn = nodeptn($id);
	    if ($ptn eq NOPTN) {
		print "Delete of missing node $id ignored\n"
		    if (VERBOSE > 7);
		next;
	    }
	    my $nf = openptn($ptn, "nodes");
	    seek $nf, 0, 0;
	    while (my ($n, $lat, $lon, $off) = readnode($nf)) {
		last unless (defined $n);
		next unless($n == $id);
		if (VERBOSE > 9) {
		    my ($uz, $ux, $uy) = fromptn($ptn);
		    print "Deleting node $id from z$uz $ux,$uy\n";
		}
		seek $nf, -16, 1;
		printnode($nf, 0, 0, 0, 0);
		$togc{$ptn} = $cachecount if ($off);
		last;
	    }
	    nodeptn($id, NOPTN);
	}
    }
    close OSC;
    
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
    garbagecollect() if (GCCOUNT);
}
