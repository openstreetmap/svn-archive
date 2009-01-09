#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

use strict;
use warnings;

use Math::Trig;
use File::Path;

use constant MAXZOOM => 14;		# magic number with current ptn scheme
use constant NOPTN => "\0\0\0\0";	# toptn(0,0,0)
use constant MAXLAT => 85.051128;	# deliberatly slightly less than the thoretical value
use constant MINLAT => -(MAXLAT);
use constant CONV => 10000000;		# conversion from lat/lon to int
use constant MEMBER => { 'node' => 1, 'way' => 2, 'relation' => 3 };
use constant MEMBERTYPE => ( '', 'node', 'way', 'relation' );

our $devnull;
sub ptdbinit($) {
    my ($mode) = @_;
    open NODES, $mode, DBDIR."nodes.db"
	or die "Could not open nodes.db: $!";
    open WAYS, $mode, DBDIR."ways.db"
	or die "Could not open ways.db: $!";
    open RELATIONS, $mode, DBDIR."relations.db"
	or die "Could not open relations.db: $!";
    open ZOOMS, $mode, DBDIR."zooms.db"
	or die "Could not open zooms.db: $!";
    open $devnull, "<", "/dev/null"
	or die "Could not open /dev/null: $!";
}

# pack 4-bit z, 14-bit x and y into 4-byte ptn (packed tile number)
sub toptn($$$) {
    my ($z, $x, $y) = @_;
    return pack "b32", sprintf "%0.4b%0.14b%0.14b", $z, $x, $y;
}

# given MAXZOOM xy, return ptn
sub etoptn($$) {
    my ($x, $y) = @_;
    # ZOOMS is stored for MAXZOOM-1
    seek ZOOMS, (($x>>1)<<(MAXZOOM-1)) | ($y>>1), 0;
    my $z;
    if (read ZOOMS, $z, 1) {
	$z = unpack "C", $z;
    }
    $z = MINZOOM unless($z);
    return toptn($z, $x >> (MAXZOOM-$z), $y >> (MAXZOOM-$z));
}

# unpack 4-bit z, 14-bit x and y from 4-byte ptn (packed tile number)
sub fromptn($) {
    if (unpack("b32", $_[0]) =~ /^(.{4,4})(.{14,14})(.{14,14})$/) {
	return (oct "0b".$1, oct "0b".$2, oct "0b".$3);
    }
    return 0, 0, 0;
}

# lat, lon, zoom to xy
# based on wiki.openstreetmap.org/index.php/Slippy_map_tilenames
sub getTileNumber($$$) {
    my ($lat,$lon,$z) = @_;
    # use closest tile near poles
    $lat = MAXLAT if ($lat > MAXLAT);
    $lat = MINLAT if ($lat < MINLAT);
    my $xtile = int( ($lon+180)/360 * (1<<$z) ) ;
    my $ytile = int( (1 - log(tan($lat*pi/180) + sec($lat*pi/180))/pi)/2 * (1<<$z) ) ;
    return(($xtile, $ytile));
}

# open file from ptn, mode, and name
#   name is "data", "nodes", "ways", or "relations"
# if file doesn't exist, return /dev/null if read only or create it
# we keep a cache of file handles
sub openptn($$$) {
    my ($ptn,$mode,$name) = @_;
    my $f;
    my $ptnname = $ptn.$name;
    our ($opened, $hits, $misses);
    our (%filecache);
    if (defined $filecache{$ptnname}) {
	my @c = @{$filecache{$ptnname}};
	if ($c[1] eq $mode) {
	    $hits++;
            ${$filecache{$ptnname}}[2] = time();
   	    return $c[0];
        } else {
	    print "changing mode from $c[1] to $mode\n" if (VERBOSE > 10);
	    delete $filecache{$ptnname};
	    $opened--;
	}
    }
    my ($z, $x, $y) = fromptn($ptn);
    unless (open $f, $mode, "z$z/$x/$y/$name") {
	my $err = $!;
	return $devnull if ($mode eq "<");
	my $m = $mode;
	if ($m eq "+<") {
	    $m = "+>";
	    open $f, $m, "z$z/$x/$y/$name" or $f = undef();
	} else {
	    $f = undef();
	}
	unless (defined $f) {
	    unless (mkdir "z$z/$x/$y") {
		unless (mkdir "z$z/$x") {
		    mkdir "z$z" or die "Could not mkdir z$z for z$z/$x/$y/$name: $!\nopen err: $err";
		    mkdir "z$z/$x" or die "Could not mkdir z$z/$x for z$z/$x/$y/$name: $!";
		}
		mkdir "z$z/$x/$y" or die "Could not mkdir z$z/$x/$y for z$z/$x/$y/$name: $!";
	    }
	    open $f, $m,  "z$z/$x/$y/$name"
		or die "Could not open z$z/$x/$y/$name: $!";
	}
    }
    # keep a cache of the most recently opened 500 files
    if ($opened++ > MAXOPEN) {
	print "Cache full after $hits hits\n" if (VERBOSE > 10);
	my @toclose =
	    sort {${$filecache{$a}}[2] <=> ${$filecache{$b}}[2]} keys %filecache;
	while ($opened > KEEPOPEN) {
	    my $toclose = shift @toclose;
	    delete $filecache{$toclose};
	    $opened--;
	}
    }
    $misses++;
    $filecache{$ptnname} = [$f, $mode, time()];
    return $f;
}

# print openptn cache statistics
sub cachestat {
    our ($hits, $misses);
    my $c = $hits/($hits + $misses) * 100;
    print "Hits: $hits Misses: $misses  Cache: $c\%\n";
}

# get rid of cache associated with ptn
sub flushptn($) {
    my ($ptn) = @_;
    our (%filecache, $opened);
    foreach my $name ("data", "nodes", "ways", "relations") {
	my $ptnname = $ptn.$name;
	if (exists $filecache{$ptnname}) {
	    delete $filecache{$ptnname};
	    $opened--;
	}
    }
}

# force the file buffers to be written to disk
sub writecache() {
    our (%filecache);
    my $select = select;
    while (my ($k, $f) = each %filecache) {
	select ${$f}[0];
	$| = 1;
	$| = 0;
    }
    select NODES;
    $| = 1;
    $| = 0;
    select WAYS;
    $| = 1;
    $| = 0;
    select RELATIONS;
    $| = 1;
    $| = 0;
    select ZOOMS;
    $| = 1;
    $| = 0;
    select $select;
}

# close all files
sub closeall {
    our (%filecache, $opened);
    while (my $ptn = each %filecache) {
	delete $filecache{$ptn};
    }
    $opened = 0;
    close NODES;
    close WAYS;
    close RELATIONS;
    close ZOOMS;
}

# get a null-terminated string from a file
sub gets($) {
    my ($f) = @_;
    my ($c, $s);
    $s = "";
    while (read $f, $c, 1) {
	if ($c eq "\0") {
	    return $s;
	}
	$s .= $c;
    }
    return undef();
}

# return or set ptn of a node
sub nodeptn {
    my ($node, $ptn) = @_;
    seek NODES, $node * 4, 0;
    if (defined $ptn) {
	print NODES $ptn;
    } else {
	$ptn = NOPTN unless(read NODES, $ptn, 4);
    }
    return $ptn;
}

# return or set ptn of a way
sub wayptn {
    my ($way, $ptn) = @_;
    seek WAYS, $way * 4, 0;
    if (defined $ptn) {
	print WAYS $ptn;
    } else {
	$ptn = NOPTN unless(read WAYS, $ptn, 4);
    }
    return $ptn;
}

# return or set ptn of a relation
sub relationptn {
    my ($relation, $ptn) = @_;
    seek RELATIONS, $relation * 4, 0;
    if (defined $ptn) {
	print RELATIONS $ptn;
    } else {
	$ptn = NOPTN unless(read RELATIONS, $ptn, 4);
    }
    return $ptn;
}


# get lat and lon of the corners of a tile
# based on wiki.openstreetmap.org/index.php/Slippy_map_tilenames
sub Project {
    my ($X,$Y, $Zoom) = @_;
    my $Unit = 1 / (1 << $Zoom);
    my $relY1 = $Y * $Unit;
    my $relY2 = $relY1 + $Unit;
    
    # note: $LimitY = ProjectF(degrees(atan(sinh(pi)))) = log(sinh(pi)+cosh(pi)) = pi
    # note: degrees(atan(sinh(pi))) = 85.051128..
    #my $LimitY = ProjectF(85.0511);
    
    # so stay simple and more accurate
    my $LimitY = pi;
    my $RangeY = 2 * $LimitY;
    $relY1 = $LimitY - $RangeY * $relY1;
    $relY2 = $LimitY - $RangeY * $relY2;
    my $Lat1 = ProjectMercToLat($relY1);
    my $Lat2 = ProjectMercToLat($relY2);
    $Unit = 360 / (1 << $Zoom);
    my $Long1 = -180 + $X * $Unit;
    return(($Lat2, $Long1, $Lat1, $Long1 + $Unit)); # S,W,N,E
}
sub ProjectMercToLat($){
    my $MercY = shift();
    return( 180/pi* atan(sinh($MercY)));
}
sub ProjectF
{
    my $Lat = shift;
    $Lat = deg2rad($Lat);
    my $Y = log(tan($Lat) + (1/cos($Lat)));
    return($Y);
}

# find the tiles this relation is in
sub reltiles($) {
    my @members = @{shift @_};
    my (%tiles, %wtodo, %rdone, %rtodo);
    foreach my $m (@members) {
	my ($type, $id, $role) = @$m;
	if ($type == 1) {
	    $tiles{nodeptn($id)}++;
	} elsif ($type == 2) {
	    my $t = wayptn($id);
	    if (exists $wtodo{$t}) {
		${$wtodo{$t}}{$id}++;
	    } else {
		$wtodo{$t} = {$id => 1};
	    }
	} elsif ($type == 3) {
	    my $t = relationptn($id);
	    if (exists $rtodo{$t}) {
		${$rtodo{$t}}{$id}++;
	    } else {
		$rtodo{$t} = {$id => 1};
	    }
	} else {
	    die "Unknown relation $id type $type";
	}
    }
    while (my @rt = keys %rtodo) {
	while (my $t = shift @rt) {
	    my %rthis = %{$rtodo{$t}};
	    delete $rtodo{$t};
	    my $rf = openptn($t, "+<", "relations");
	    my $df = openptn($t, "+<", "data");
	    seek $rf, 0, 0;
	    my ($r, $off);
	    while (read $rf, $r, 8) {
		($r, $off) = unpack "NN", $r;
		next unless ($r && exists $rthis{$r});
	        $rdone{$r}++;
		seek $df, $off, 0;
		my ($type, $n);
		while (read $df, $n, 5) {
		    ($type, $n) = unpack "CN", $n;
		    last unless ($type);
		    gets($df);
		    if ($type == 1) {
			$tiles{nodeptn($n)}++;
		    } elsif ($type == 2) {
			my $wp = wayptn($n);
			if (exists $wtodo{$wp}) {
			    ${$wtodo{$wp}}{$n}++;
			} else {
			    $wtodo{$wp} = {$n => 1};
			}
		    } elsif ($type == 3) {
			next if(exists $rdone{$n});
			my $rrp = relationptn($n);
			if (exists $rtodo{$rrp}) {
			    ${$rtodo{$rrp}}{$n}++;
			} else {
			    $rtodo{$rrp} = {$n => 1};
			}
		    } else {
			die "Unknown relation $r type $type";
		    }
	        }
            }
        }
    }
    foreach my $t (keys %wtodo) {
        $tiles{$t}++;
        my %wthis = %{$wtodo{$t}};
	my $wf = openptn($t, "+<", "ways");
	my $df = openptn($t, "+<", "data");
	seek $wf, 0, 0;
	my ($w, $off, $n);
	while (read $wf, $w, 8) {
	    ($w, $off) = unpack "NN", $w;
	    next unless (exists $wthis{$w});
	    seek $df, $off, 0;
	    while (read $df, $n, 4) {
	        $n = unpack "N", $n;
		last unless($n);
		$tiles{nodeptn($n)}++;
	    }
	}
    }
    return %tiles;
}

# split a tile into 4 of next zoom level
sub splitptn($) {
    my ($ptn) = @_;
    my ($ez, $ex, $ey) = fromptn($ptn);
    return(0) if ($ez >= MAXZOOM);
    print "Splitting $ez $ex,$ey\n" if (VERBOSE > 2);
    my $nd = openptn($ptn, "+<", "data");
    my $nz = $ez + 1;
    my $fz = 1 << (MAXZOOM - $nz);
    my $bx = $ex << (MAXZOOM - $nz);
    my $bxx = $bx + $fz;
    my $by = $ey << (MAXZOOM - $nz);
    for (my $xx = $bx; $xx < $bxx; $xx++) {
	seek ZOOMS, ($xx << (MAXZOOM-1)) | $by, 0;
	print ZOOMS pack("C", $nz) x $fz;
    }
    my ($nf, $nnf, %nf, $nnd, %nd, %n, %wf, %w, $n);
    $nf = openptn($ptn, "+<", "nodes");
    seek $nf, 0, 0;
    while (read $nf, $n, 16) {
	my ($nid, $nlat, $nlon, $noff) = unpack "NN!N!N", $n;
	next unless($nid);
	my ($nx, $ny) = getTileNumber($nlat/CONV, $nlon/CONV, $nz);
#	print "moving node $nid to z$nz $nx,$ny\n";
	my $p = toptn($nz, $nx, $ny);
	unless ($nnf = $nf{$p}) {
	    $nnf = openptn($p, "+<", "nodes");
	    seek $nnf, 0, 2;
	    $nf{$p} = $nnf;
	}
	my ($nnoff);
	if ($noff) {
	    unless ($nnd = $nd{$p}) {
		$nnd = openptn($p, "+<", "data");
		seek $nnd, 0, 2;
		$nd{$p} = $nnd;
		unless (tell($nnd)) {
		    print $nnd "\0";
		}
	    }
	    $nnoff = tell($nnd);
	    seek $nd, $noff, 0;
	    my $s;
	    while (defined($s = gets($nd)) && ($s ne "")) {
		print $nnd $s."\0";
	    }
	    print $nnd "\0";
	} else {
	    $nnoff = 0;
	}
	print $nnf pack "NN!N!N", $nid, $nlat, $nlon, $nnoff;
	nodeptn($nid,$p);
	$n{$nid} = $p;
    }
    my $wf = openptn($ptn,"+<","ways");
    seek $wf, 0, 0;
    my $w;
    while (read $wf, $w, 8) {
	my ($wid, $woff) = unpack "NN", $w;
	next unless($wid);
	print "Way $wid\n" if (VERBOSE > 4);
	if ($woff) {
	    my %w;
	    my @nodes;
	    seek $nd, $woff, 0;
	    my $n;
	    while (read $nd, $n, 4) {
		my $nn = unpack "N", $n;
		last unless ($nn);
		push @nodes, $nn;
		if ($n{$nn}) {
		    $w{$n{$nn}} = 1;
		}
	    }
	    my $first = 1;
	    foreach my $p (keys %w) {
		my $nnoff;
		if ($first) {
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($p);
			print " moved to z$uz $ux,$uy\n";
		    }
		    unless ($nnd = $nd{$p}) {
			$nnd = openptn($p, "+<", "data");
			seek $nnd, 0, 2;
			print $nnd "\0" unless (tell($nnd));
		    }
		    $nnoff = tell $nnd;
		    foreach my $nn (@nodes) {
			print $nnd pack "N", $nn;
		    }
		    print $nnd pack "N", 0;
		    my $s;
		    while (defined($s = gets($nd)) && ($s ne "")) {
			print $nnd $s."\0";
		    }
		    print $nnd "\0";
		    wayptn($wid,$p);
		    $first = 0;
		} else {
		    my ($uz, $ux, $uy) = fromptn($p);
		    print "  also in z$uz $ux,$uy\n" if (VERBOSE > 4);
		    $nnoff = 0;
		}
		my $nwf;
		unless ($nwf = $wf{$p}) {
		    $nwf = openptn($p, "+<", "ways");
		    $wf{$p} = $nwf;
		}
		seek $nwf, 0, 2;
		print $nwf pack "NN", $wid, $nnoff;
	    }
	    if ($first) {
		my $p = nodeptn($nodes[0]);
		$p = toptn(0,1,1) if ($p eq NOPTN);
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($p);
		    print " moved to z$uz $ux,$uy\n";
		}
		$nnd = openptn($p, "+<", "data");
		seek $nnd, 0, 2;
		my $nnoff = tell $nnd;
		unless ($nnoff) {
		    print $nnd "\0";
		    $nnoff = 1;
		}
		foreach my $nn (@nodes) {
		    print $nnd pack "N", $nn;
		}
		print $nnd pack "N", 0;
		my $s;
		while (defined($s = gets($nd)) && ($s ne "")) {
		    print $nnd $s."\0";
		}
		print $nnd "\0";
		my $nwf = openptn($p, "+<", "ways");
		seek $nwf, 0, 0;
		while (read $nwf, $w, 8) {
		    ($w, $woff) = unpack "NN", $w;
		    next unless ($w);
		    next unless ($w == $wid);
		    seek $nwf, -8, 1;
		    last;
		}
		print $nwf pack "NN", $wid, $nnoff;
		wayptn($wid,$p);
	    }
	} else {
	    my $wptn = wayptn($wid);
	    my $nwf = openptn($wptn, "+<", "ways");
	    seek $nwf, 0, 0;
	    while (read $nwf, $w, 8) {
		my ($ww, $wwoff) = unpack "NN", $w;
		next unless ($ww == $wid);
		my $nwd = openptn($wptn, "+<", "data");
		seek $nwd, $wwoff, 0;
		while (read $nwd, $w, 4) {
		    my $nn = unpack "N", $w;
		    last unless ($nn);
		    if ($n{$nn}) {
			$w{$n{$nn}} = 1;
		    }
		}
		my $nwf;
		foreach my $p (keys %w) {
		    my ($uz, $ux, $uy) = fromptn($p);
		    print "  in z$uz $ux,$uy\n" if (VERBOSE > 4);
		    unless ($nwf = $wf{$p}) {
			$nwf = openptn($p, "+<", "ways");
			$wf{$p} = $nwf;
		    }
		    seek $nwf, 0, 2;
		    print $nwf pack "NN", $wid, 0;
		}
		last;
	    }
	}
    }
    my $rf = openptn($ptn, "+<", "relations");
    my $rfp = 0;
    my $nx = $ex << 1;
    my $ny = $ey << 1;
    my (%t, %rf);
    $t{toptn($nz,$nx,$ny)} = 1;
    $t{toptn($nz,$nx+1,$ny)} = 1;
    $t{toptn($nz,$nx,$ny+1)} = 1;
    $t{toptn($nz,$nx+1,$ny+1)} = 1;
    for(;;) {
	seek $rf, $rfp, 0;	# reltiles may have moved the file pointer
        last unless (read $rf, $w, 8);
	$rfp = tell $rf;
	my ($rid, $roff) = unpack "NN", $w;
	next unless($rid);
	print "relation $rid\n" if (VERBOSE > 4);
	my %tiles = reltiles([[3,$rid]]);
	my (@members, @tv);
	my $first = ($roff != 0);
	if ($first) {
	    seek $nd, $roff, 0;
	    while (read $nd, $w, 5) {
		my ($type, $nn) = unpack "CN", $w;
		last unless($type);
		push @members, [$type, $nn, gets($nd)];
	    }
	    seek $nd, -4, 1;
	    my $s;
	    while (defined ($s = gets($nd)) && ($s ne "")) {
		push @tv, $s, gets($nd);
	    }
	}
	foreach my $t (keys %t) {
	    next unless (exists $tiles{$t});
	    my ($nnoff);
	    if ($first) {
		unless ($nnd = $nd{$t}) {
		    $nnd = openptn($t, "+<", "data");
		    seek $nnd, 0, 2;
		    print $nnd "\0" unless (tell($nnd));
		}
		seek $nnd, 0, 2;
		$nnoff = tell $nnd;
		foreach my $m (@members) {
		    my @m= @$m;
		    print $nnd pack("CN",$m[0],$m[1]).$m[2]."\0";
		}
		print $nnd pack "C", 0;
		foreach my $s (@tv) {
		    print $nnd $s."\0";
		}
		print $nnd "\0";
		relationptn($rid,$t);
		$first = 0;
	    } else {
		$nnoff = 0;
	    }
	    my $nrf;
	    unless ($nrf = $rf{$t}) {
		$nrf = openptn($t, "+<", "relations");
		$rf{$t} = $nrf;
	    }
	    if (VERBOSE > 4) {
		my ($uz, $ux, $uy) = fromptn($t);
		if ($nnoff) {
		    print "  moved to z$uz $ux,$uy\n";
		} else {
		    print "  in z$uz $ux,$uy\n";
		}
	    }
	    seek $nrf, 0, 2;
	    print $nrf pack "NN", $rid, $nnoff;
	}
	if ($first) {
	    my $t = each %tiles;
	    unless (defined $t) {
		print "missing relation $rid\n" if (VERBOSE > 1);
		next;
	    }
	    $nnd = openptn($t, "+<", "data");
	    seek $nnd, 0, 2;
	    my $nnoff = tell $nnd;
	    unless ($nnoff) {
		print $nnd "\0";
		$nnoff = 1;
	    }
	    foreach my $m (@members) {
		my @m = @$m;
		print $nnd pack("CN",$m[0],$m[1]).$m[2]."\0";
	    }
	    print $nnd pack "C", 0;
	    foreach my $s (@tv) {
		print $nnd $s."\0";
	    }
	    print $nnd "\0";
	    relationptn($rid,$t);
	    my $nrf = openptn($t, "+<", "relations");
	    if (VERBOSE > 4) {
		my ($uz, $ux, $uy) = fromptn($t);
		print "  moved to z$uz $ux,$uy\n";
	    }
	    seek $nrf, 0, 0;
	    my ($r, $roff, $mt);
	    while (read $nrf, $r, 8) {
		($r, $roff) = unpack "NN", $r;
		$mt //= tell($nrf) unless ($r);
		next unless ($r == $rid);
		$mt = tell($nrf);
		last;
	    }
	    seek $nrf, $mt-8, 0 if($mt);
	    print $nrf pack "NN", $rid, $nnoff;
	}
    }
    flushptn($ptn);
    rmtree("z$ez/$ex/$ey",{});
    return 1;
}

1;

