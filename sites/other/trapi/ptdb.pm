#!/usr/bin/perl
# Copyright 2008, 2009 Blars Blarson.  
# Distributed under GPL version 2, see GPL-2

use strict;
use warnings;

use Math::Trig;
use File::Path;

use constant MAXZOOM => 14;		# magic number with current ptn scheme
use constant NOPTN => "\0\0\0\0";	# toptn(0,0,0)
use constant MAXLAT => 85.051128;	# deliberatly slightly less than the thoretical value
use constant MINLAT => -(MAXLAT);
use constant CONV => 10000000;		# conversion from lat/lon to int
use constant {NONE => 0, NODE => 1, WAY => 2, RELATION => 3, ROLE => 4};
use constant MEMBER => { 'node' => NODE, 'way' => WAY, 'relation' => RELATION, 'role' => ROLE };
use constant MEMBERTYPE => ( '', 'node', 'way', 'relation', 'role' );
use constant PZ => pack("N", 0);

our $devnull;
our @comtags;
our %tagsversion;

sub ptdbinit($) {
    our ($mode) = @_;
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

# tags are encoded in a variable-length code.
# first byte 0 means no more tags, 1 means string follows.
# >=192 means 2 more bytes, >=128 means 1 more byte
# vals and roles are the same except 0 means string follows.
#
# commontags reads the tags file and creates the hashes for encoding
# and arrays for decoding
sub commontags($) {
    my ($ctn) = @_;
    return unless($ctn);
    print "processing tags.$ctn\n" if (VERBOSE > 30);
    my ($v, $t, $vv, $tn, $vn, $va, $ta, $vva, $tag);
    open TAGS, "<", DBDIR."tags.$ctn" or die "Could not open tags.$ctn: $!";
    $comtags[$ctn] = [
	undef,
	[ {}, [], {}, [], ],
	[ {}, [], {}, [], ],
	[ {}, [], {}, [], ],
	[ {}, [], ],
    ];

    while ($_ = <TAGS>) {
	chomp;
	if (/^\t\t([^\t]*)\t(\d+)$/) {
	    my $val = $1;
	    unless (defined $vv) {
		$vv = {};
		$v->{$tag} = $vv;
		$vva = [];
		$va->[$tn] = $vva;
		$vn = 0;
	    }
	    $vv->{$val} = ++$vn;
	    $vva->[$vn] = $val;
	} elsif (/^\t([^\t]*)\t(\d+)$/) {
	    $tag = $1;
	    $t->{$tag} = ++$tn;
	    $ta->[$tn] = $tag;
	    $vv = undef;
	} elsif (/^(\w+)s$/) {
	    my $m = MEMBER->{$1};
	    die "Malformed line in tags.$ctn: $_" unless($m);
	    ($t, $ta, $v, $va) = @{$comtags[$ctn]->[$m]};
	    $tn = ($m != ROLE);
	} else {
	    die "Malformed line in tags.$ctn: $_";
	}
    }
    close TAGS;
    print "tags.$ctn processed\n" if (VERBOSE > 10);
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

# open file from ptn, and name
#   name is "data", "nodes", "ways", or "relations"
# if file doesn't exist, return /dev/null if read only or create it
# we keep a cache of file handles
sub openptn($$) {
    my ($ptn, $name) = @_;
    my $f;
    my $ptnname = $ptn.$name;
    our ($opened, $hits, $misses, $cachecount, $mode);
    our (%filecache);
    if (defined $filecache{$ptnname}) {
	$hits++;
	$filecache{$ptnname}->[1] = ++$cachecount;
	return $filecache{$ptnname}->[0];
    }
    my ($z, $x, $y) = fromptn($ptn);
    print "opening z$z/$x/$y/$name\n" if (VERBOSE > 34);
    unless (open $f, $mode, "z$z/$x/$y/$name") {
	my $err = $!;
	return $devnull if ($mode eq "<");
	open $f, "+>", "z$z/$x/$y/$name" or $f = undef();
	unless (defined $f) {
	    unless (mkdir "z$z/$x/$y") {
		unless (mkdir "z$z/$x") {
		    mkdir "z$z" or die "Could not mkdir z$z for z$z/$x/$y/$name: $!\nopen err: $err";
		    mkdir "z$z/$x" or die "Could not mkdir z$z/$x for z$z/$x/$y/$name: $!";
		}
		mkdir "z$z/$x/$y" or die "Could not mkdir z$z/$x/$y for z$z/$x/$y/$name: $!";
	    }
	    open $f, "+>",  "z$z/$x/$y/$name"
		or die "Could not open z$z/$x/$y/$name: $!";
	}
	if ($name eq "data") {
	    printvnum($f, TAGSVERSION);
	    $tagsversion{$f} = TAGSVERSION;
	    commontags(TAGSVERSION) if (!defined($comtags[TAGSVERSION]));
	}
    } elsif ($name eq "data") {
	my $tv = getvnum($f);
	unless (defined $tv || $mode eq '<') {
	    $tv = TAGSVERSION;
	    printvnum($f, $tv);
	}
#	elsif ($tv > 1) {
#	    my ($vz, $vx, $vy) = fromptn($ptn);
#	    print "!!! broken tile z$vz $vx,$vy\n";
#	    $tv = 0;
#	}
	$tagsversion{$f} = $tv;
	commontags($tv) if ($tv && !defined($comtags[$tv]));
    }
    # keep a cache of the most recently opened 500 files
    if ($opened++ > MAXOPEN) {
	print "Cache full after $hits hits\n" if (VERBOSE > 10);
	my @toclose =
	    sort {${$filecache{$a}}[1] <=> ${$filecache{$b}}[1]} keys %filecache;
	while ($opened > KEEPOPEN) {
	    my $toclose = shift @toclose;
	    if ($toclose =~ /data$/) {
#		delete $tagsversion{$filecache{$toclose}->[0]};
	    }
	    delete $filecache{$toclose};
	    $opened--;
	}
    }
    $misses++;
    $filecache{$ptnname} = [$f, ++$cachecount];
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
	    delete $tagsversion{$filecache{$ptnname}->[0]}
	        if ($name eq "data");
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
    my $s = "";
    my $c;
    while (defined($c = getc($f))) {
	if ($c eq "\0") {
	    return $s;
	}
	$s .= $c;
    }
    return $s;
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
	if ($type == NODE) {
	    $tiles{nodeptn($id)}++;
	} elsif ($type == WAY) {
	    my $t = wayptn($id);
	    if (exists $wtodo{$t}) {
		${$wtodo{$t}}{$id}++;
	    } else {
		$wtodo{$t} = {$id => 1};
	    }
	} elsif ($type == RELATION) {
	    my $t = relationptn($id);
	    $rtodo{$t} //= {};
	    $rtodo{$t}->{$id}++;
	} else {
	    die "Unknown relation $id type $type";
	}
    }
    # can't use foreach because %rtodo can change
    while (my @rt = keys %rtodo) {
	while (my $t = shift @rt) {
	    my %rthis = %{$rtodo{$t}};
	    delete $rtodo{$t};
	    my $rf = openptn($t, "relations");
	    my $df = openptn($t, "data");
	    seek $rf, 0, 0;
	    while (my ($r, $off) = readrel($rf)) {
		last unless (defined $r);
		next unless ($r && exists $rthis{$r});
	        $rdone{$r}++;
		seek $df, $off, 0;
		my @mm = readmemb($df);
		foreach my $mi (@mm) {
		    my ($type, $n, $role) = @$mi;
		    if ($type == NODE) {
			$tiles{nodeptn($n)}++;
		    } elsif ($type == WAY) {
			my $wp = wayptn($n);
			if (exists $wtodo{$wp}) {
			    ${$wtodo{$wp}}{$n}++;
			} else {
			    $wtodo{$wp} = {$n => 1};
			}
		    } elsif ($type == RELATION) {
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
	my $wf = openptn($t, "ways");
	my $df = openptn($t, "data");
	seek $wf, 0, 0;
	while (my ($w, $off) = readway($wf)) {
	    last unless (defined $w);
	    next unless ($w && $off && exists $wthis{$w});
	    seek $df, $off, 0;
	    my @nodes = readwaynodes($df);
	    foreach my $n (@nodes) {
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
    writecache();
    print "Splitting z$ez $ex,$ey\n" if (VERBOSE > 2);
    my $nd = openptn($ptn, "data");
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
    $nf = openptn($ptn, "nodes");
    seek $nf, 0, 0;
    while (my ($nid, $nlat, $nlon, $noff) = readnode($nf)) {
	last unless(defined $nid);
	next unless($nid);
	my ($nx, $ny) = getTileNumber($nlat/CONV, $nlon/CONV, $nz);
	print "moving node $nid to z$nz $nx,$ny\n" if (VERBOSE > 30);
	my $p = toptn($nz, $nx, $ny);
	unless ($nnf = $nf{$p}) {
	    $nnf = openptn($p, "nodes");
	    seek $nnf, 0, 2;
	    $nf{$p} = $nnf;
	}
	my ($nnoff);
	if ($noff) {
	    unless ($nnd = $nd{$p}) {
		$nnd = openptn($p, "data");
		seek $nnd, 0, 2;
		$nd{$p} = $nnd;
	    }
	    $nnoff = tell($nnd);
	    seek $nd, $noff, 0;
	    my @tv = readtags($nd, NODE);
	    printtags($nnd, \@tv, NODE);
	} else {
	    $nnoff = 0;
	}
	printnode($nnf, $nid, $nlat, $nlon, $nnoff);
	nodeptn($nid,$p);
	$n{$nid} = $p;
    }
    my $wf = openptn($ptn, "ways");
    seek $wf, 0, 0;
    my $w;
    while (my ($wid, $woff) = readway($wf)) {
	last unless (defined $wid);
	next unless($wid);
	print "Way $wid\n" if (VERBOSE > 4);
	if ($woff) {
	    my %w;
	    seek $nd, $woff, 0;
	    my @nodes = readwaynodes($nd);
	    foreach my $nn (@nodes) {
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
			$nnd = openptn($p, "data");
			seek $nnd, 0, 2;
		    }
		    $nnoff = tell $nnd;
		    printwaynodes($nnd, \@nodes);
		    my @tv = readtags($nd, WAY);
		    printtags($nnd, \@tv, WAY);
		    wayptn($wid,$p);
		    $first = 0;
		} else {
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($p);
			print "  also in z$uz $ux,$uy\n";
		    }
		    $nnoff = 0;
		}
		my $nwf;
		unless ($nwf = $wf{$p}) {
		    $nwf = openptn($p, "ways");
		    $wf{$p} = $nwf;
		}
		seek $nwf, 0, 2;
		printway($nwf, $wid, $nnoff);
	    }
	    if ($first) {
		my $p = nodeptn($nodes[0]);
		$p = toptn(0,1,1) if ($p eq NOPTN);
		if (VERBOSE > 4) {
		    my ($uz, $ux, $uy) = fromptn($p);
		    print " moved to z$uz $ux,$uy\n";
		}
		$nnd = openptn($p, "data");
		seek $nnd, 0, 2;
		my $nnoff = tell $nnd;
		printwaynodes($nnd, \@nodes);
		my @tv = readtags($nd, WAY);
		printtags($nnd, \@tv, WAY);
		my $nwf = openptn($p, "ways");
		seek $nwf, 0, 0;
		my $mt;
		while (my ($w, $woff) = readway($nwf)) {
		    last unless(defined $w);
		    $mt //= tell($nwf) unless($w);
		    next unless ($w == $wid);
		    $mt = tell($nwf);
		    last;
		}
		seek $nwf, $mt-8, 0 if ($mt);
		printway($nwf, $wid, $nnoff);
		wayptn($wid,$p);
	    }
	} else {
	    my $wptn = wayptn($wid);
	    my $nwf = openptn($wptn, "ways");
	    seek $nwf, 0, 0;
	    while (my ($ww, $wwoff) = readway($nwf)) {
		last unless (defined $ww);
		next unless ($ww == $wid);
		my $nwd = openptn($wptn, "data");
		seek $nwd, $wwoff, 0;
		my @nodes = readwaynodes($nwd);
		foreach my $nn (@nodes) {
		    if ($n{$nn}) {
			$w{$n{$nn}} = 1;
		    }
		}
		my $nwf;
		foreach my $p (keys %w) {
		    if (VERBOSE > 4) {
			my ($uz, $ux, $uy) = fromptn($p);
			print "  in z$uz $ux,$uy\n";
		    }
		    unless ($nwf = $wf{$p}) {
			$nwf = openptn($p, "ways");
			$wf{$p} = $nwf;
		    }
		    seek $nwf, 0, 2;
		    printway($nwf, $wid, 0);
		}
		last;
	    }
	}
    }
    my $rf = openptn($ptn, "relations");
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
	my ($rid, $roff);
        last unless ((($rid, $roff) = readrel($rf)) && defined($rid));
	$rfp = tell $rf;
	next unless($rid);
	print "relation $rid\n" if (VERBOSE > 4);
	my %tiles = reltiles([[RELATION, $rid]]);
	my (@members, @tv);
	my $first = ($roff != 0);
	if ($first) {
	    seek $nd, $roff, 0;
	    @members = readmemb($nd);
	    @tv = readtags($nd, RELATION);
	}
	foreach my $t (keys %t) {
	    next unless (exists $tiles{$t});
	    my ($nnoff);
	    if ($first) {
		unless ($nnd = $nd{$t}) {
		    $nnd = openptn($t, "data");
		    seek $nnd, 0, 2;
		}
		seek $nnd, 0, 2;
		$nnoff = tell $nnd;
		printmemb($nnd, \@members);
		printtags($nnd, \@tv, RELATION);
		relationptn($rid,$t);
		$first = 0;
	    } else {
		$nnoff = 0;
	    }
	    my $nrf;
	    unless ($nrf = $rf{$t}) {
		$nrf = openptn($t, "relations");
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
	    printrel($nrf, $rid, $nnoff);
	}
	if ($first) {
	    my $t = each %tiles;
	    unless (defined $t) {
		print "missing relation $rid\n" if (VERBOSE > 1);
		next;
	    }
	    $nnd = openptn($t, "data");
	    seek $nnd, 0, 2;
	    my $nnoff = tell $nnd;
	    printmemb($nnd, \@members);
	    printtags($nnd, \@tv, RELATION);
	    my $nrf = openptn($t, "relations");
	    if (VERBOSE > 4) {
		my ($uz, $ux, $uy) = fromptn($t);
		print "  moved to z$uz $ux,$uy\n";
	    }
	    seek $nrf, 0, 0;
	    my ($mt);
	    while (my ($r, $roff) = readrel($nrf)) {
		last unless (defined $r);
		$mt //= tell($nrf) unless ($r);
		next unless ($r == $rid);
		$mt = tell($nrf);
		last;
	    }
	    seek $nrf, $mt-8, 0 if($mt);
	    printrel($nrf, $rid, $nnoff);
	    relationptn($rid,$t);
	}
    }
    flushptn($ptn);
    rmtree("z$ez/$ex/$ey",{});
    return 1;
}

# vnum are variable-length numbers.  1 byte is up to 127, 2 up to 16511,
# 3 up to 2113663, 4 up to 270549119
sub printvnum($$) {
    my ($f, $v) = @_;
    if ($v >= 128) {
	$v -= 128;
	if ($v >= 16384) {
	    $v -= 16384;
	    if ($v >= 2097152) {
		$v -= 2097152;
		print $f pack "C4", (224 + ($v>>24)), (($v>>16) & 0xff),
		    (($v>>8) & 0xff, $v & 0xff);
	    } else {
		print $f pack "C3", (192 + ($v>>16)), (($v>>8) & 0xff),
		    ($v & 0xff);
	    }
	} else {
	    print $f pack "C2", (128 + ($v>>8)), ($v & 0xff);
	}
    } else {
	print $f pack "C", $v;
    }
}

sub getvnum($) {
    my ($f) = @_;
    my $c = getc($f);
    return undef unless(defined $c);
    my $v = unpack "C", $c;
    if ($v >= 128) {
	if ($v >= 192) {
	    if ($v >= 224) {
		$v -= 224;
		$v <<= 8;
		$v += unpack "C", getc($f);
		$v <<= 8;
		$v += unpack "C", getc($f);
		$v <<= 8;
		$v += unpack("C", getc($f)) + 2097152 + 16384 + 128;
		return $v;
	    }
	    $v -= 192;
	    $v <<= 8;
	    $v += unpack "C", getc($f);
	    $v <<= 8;
	    $v += unpack("C", getc($f)) + 16384 + 128;
	    return $v;
	}
	$v -= 128;
	$v <<= 8;
	$v += unpack("C", getc($f)) + 128;
    }
    return $v;
}
    
	

sub printtags($$$) {
    my ($f, $tags, $t) = @_;
    my @tags = @$tags;
    my $tagsver = $tagsversion{$f};
    my $h = $tagsver ? $comtags[$tagsver]->[$t] : undef;
    while (my $tag = shift @tags) {
	my $val = shift @tags;
	print "  k=$tag v=$val tagsver=$tagsver\n" if (VERBOSE > 99);
	if ($tagsver) {
	    my $tn = $h->[0]->{$tag};
	    if ($tn) {
		print "    tn=$tn\n" if (VERBOSE > 99);
		printvnum($f, $tn);
		if (defined $h->[2]->{$tag}) {
		    my $vn = $h->[2]->{$tag}->{$val};
		    if ($vn) {
			printvnum($f, $vn);
		    } else {
			print $f pack("C",0)."$val\0";
		    }
		} else {
		    print $f "$val\0";
		}
	    } else {
		print $f pack("C",1)."$tag\0$val\0";
	    }
	} else {
	    print $f "$tag\0$val\0";
	}
    }
    print $f "\0";
    print "  next off ".tell($f)."\n" if (VERBOSE > 99);
}

sub readtags($$) {
    my ($f, $t) = @_;
    my @tags;
    my $tagsver = $tagsversion{$f};
    if ($tagsver) {
	my $a = $comtags[$tagsver]->[$t];
	while (my $c = getvnum($f)) {
	    if ($c == 1) {
		my $tag = gets($f);
		my $val = gets($f);
		push @tags, $tag, $val;
	    } else {
		push @tags, $a->[1]->[$c];
		if (defined $a->[3]->[$c]) {
		    my $i = getvnum($f);
		    if ($i) {
			push @tags, $a->[3]->[$c]->[$i];
		    } else {
			push @tags, gets($f);
		    }
		} else {
		    push @tags, gets($f);
		}
	    }
	}
    } else {
	my $tag;
	while (defined($tag = gets($f))) {
	    last if ($tag eq "");
	    my $val = gets($f);
	    push @tags, $tag, $val;
	}
    }
    return @tags;
}

sub printmemb($$) {
    my ($f, $memb) = @_;
    my @members = @$memb;
    my $tagsver = $tagsversion{$f};
    my $h = $tagsver ? $comtags[$tagsver]->[ROLE]->[0] : undef;
    while (my $m = shift @members) {
	print $f pack("CN", $m->[0], $m->[1]);
	my $role = $m->[2];
	if ($tagsver) {
	    if (exists $h->{$role}) {
		printvnum($f, $h->{$role});
	    } else {
		print $f pack("C", 0).$role."\0";
	    }
	} else {
	    print $f "$role\0";
	}
    }
    print $f pack "C", NONE;
}

sub readmemb($) {
    my ($f) = @_;
    my @members;
    my ($b, $role);
    my $tagsver = $tagsversion{$f};
    my $a = $tagsver ? $comtags[$tagsver]->[ROLE]->[1] : undef;
    while (defined($b = getc($f))) {
	my ($type) = unpack "C", $b;
	last unless($type);
	last unless(read $f, $b, 4);
	my ($id) = unpack "N", $b;
	my $r = $tagsver ? getvnum($f) : 0;
	if ($r) {
	    $role = $a->[$r];
	} else {
	    $role = gets($f);
	}
	push @members, [$type, $id, $role];
    }
    return @members;
}

sub readnode($) {
    my ($f) = @_;
    my $b;
    read $f, $b, 16 or return undef;
    return unpack "NN!N!N", $b;
}

sub readway($) {
    my ($f) = @_;
    my $b;
    read $f, $b, 8 or return undef;
    return unpack "NN", $b;
}

sub readrel($) {
    my ($f) = @_;
    my $b;
    read $f, $b, 8 or return undef;
    return unpack "NN", $b;
}

sub readwaynodes($) {
    my ($f) = @_;
    my ($b);
    if ($tagsversion{$f}) {
	my $nodes = getvnum($f) // 0;
	print "reading $nodes nodes\n" if (VERBOSE > 99);
	read $f, $b, (4 * $nodes);
	return unpack("N$nodes", $b);
    } else {
	my @nodes;
	while (read $f, $b, 4) {
	    my $n = unpack "N", $b;
	    last unless ($n);
	    push @nodes, $n;
	}
	return @nodes;
    }
}

sub printnode($$$$$) {
    my ($f, $id, $lat, $lon, $off) = @_;
    print $f pack "NN!N!N", $id, $lat, $lon, $off;
}

sub printway($$$) {
    my ($f, $id, $off) = @_;
    print $f pack "NN", $id, $off;
}

sub printrel($$$) {
    my ($f, $id, $off) = @_;
    print $f pack "NN", $id, $off;
}

sub printwaynodes($$) {
    my ($f, $n) = @_;
    if ($tagsversion{$f}) {
	my $nodes = scalar(@$n);
	print "saving $nodes nodes\n" if (VERBOSE > 99);
	printvnum($f, $nodes);
	print $f pack "N$nodes", @$n;
    } else {
	foreach my $node (@$n) {
	    print $f pack "N", $node;
	}
	print $f PZ;
    }
    print " tags at ".tell($f)."\n" if (VERBOSE > 99);
}

# garbagecollect a single tile
sub gcptn($) {
    my ($ptn) = @_;
    flushptn($ptn);
    my ($z, $x, $y) = fromptn($ptn);
    print "Garbagecollect: z$z $x,$y\n" if (VERBOSE > 4);
    my ($df, $ndf, $nf, $nnf, $wf, $nwf, $rf, $nrf, $b);
    if (open $df, "<", "z$z/$x/$y/data") {
	$tagsversion{$df} = getvnum($df);
	open $ndf, ">", "z$z/$x/$y/data.new";
	printvnum($ndf, TAGSVERSION);
	$tagsversion{$ndf} = TAGSVERSION;
	commontags(TAGSVERSION) unless (defined $comtags[TAGSVERSION]);
    } else {
	$df = $devnull;
	$ndf = undef;
    }
    if (open $nf, "<", "z$z/$x/$y/nodes") {
	open $nnf, ">", "z$z/$x/$y/nodes.new";
    } else {
	$nf = $devnull;
	$nnf = undef;
    }
    if (open $wf, "<", "z$z/$x/$y/ways") {
	open $nwf, ">", "z$z/$x/$y/ways.new";
    } else {
	$wf = $devnull;
	$nwf = undef;
    }
    if (open $rf, "<", "z$z/$x/$y/relations") {
	open $nrf, ">", "z$z/$x/$y/relations.new";
    } else {
	$rf = $devnull;
	$nrf = undef;
    }
    my %seen;
    while (my ($n, $lat, $lon, $off) = readnode($nf)) {
	last unless (defined $n);
	next unless ($n);
	if (exists $seen{$n}) {
	    print "Duplicate node $n in tile z$z $x,$y\n";
	    next;
	}
	my $noff = 0;
	if ($off) {
	    seek $df, $off, 0;
	    $noff = tell $ndf;
	    my @tags = readtags($df, NODE);
	    printtags($ndf, \@tags, NODE);
	}
	printnode($nnf, $n, $lat, $lon, $noff);
	$seen{$n} = 1;
	my $oldptn = nodeptn($n);
	if (defined $oldptn && $oldptn ne NOPTN) {
	    if ($oldptn ne $ptn) {
		my ($uz, $ux, $uy) = fromptn($oldptn);
		print "  node $n is actually in tile z$z $x,$y not z$uz $ux,$uy\n";
#		    nodeptn($n, $ptn);
	    }
	} else {
	    print "  node $n is in tile z$z $x,$y not deleted\n";
#		nodeptn($n, $ptn);
	}
    }
    %seen = ();
    while (my ($w, $off) = readway($wf)) {
	last unless (defined $w);
	next unless ($w);
	if (exists $seen{$w}) {
	    print "Duplicate way $w\n";
	    next;
	}
	my $noff = 0;
	if ($off) {
	    seek $df, $off, 0;
	    $noff = tell $ndf;
	    my @nodes = readwaynodes($df);
	    my @tags = readtags($df, WAY);
	    printwaynodes($ndf, \@nodes);
	    printtags($ndf, \@tags, WAY);
	}
	$seen{$w} = 1;
	my $oldptn = wayptn($w);
	if (defined $oldptn) {
	    if ($off && ($ptn ne $oldptn)) {
		my ($ux, $uy, $uz) = fromptn($oldptn);
		print "  way $w is actually in z$z $x,$y not z$uz $ux,$uy\n";
#		    wayptn($w, $ptn);
	    }
	    printway($nwf, $w, $noff);
	} else {
	    if ($off) {
		print "  way $w is actually in z$z $x,$y not deleted\n";
#		    wayptn($w, $ptn);
#		    printway($nwf, $w, $noff);
	    } else {
		print "  way $w is deleted, not in z$z $x,$y\n";
	    }
	}
    }
    %seen = ();
    while (my ($r, $off) = readrel($rf)) {
	last unless (defined $r);
	next unless ($r);
	if (exists $seen{$r}) {
	    print "Duplicate relation $r\n";
	    next;
	}
	my $noff = 0;
	if ($off) {
	    seek $df, $off, 0;
	    $noff = tell $ndf;
	    my @members = readmemb($df);
	    printmemb($ndf, \@members);
	    my @tags = readtags($df, RELATION);
	    printtags($ndf, \@tags, RELATION);
	}
	my $oldptn = relationptn($r);
	if (defined $oldptn && $oldptn ne NOPTN) {
	    if ($off && ($ptn ne $oldptn)) {
		my ($uz, $ux, $uy) = fromptn($oldptn);
		print "  relation $r is actually in z$z $x,$y not z$uz $ux,$uy\n";
#		    relationptn($r, $ptn);
	    } else {
		printrel($nrf, $r, $noff);
	    }
	} else {
	    if ($off && $z != 0) {
		print "  relation $r is actually in z$z $x,$y not deleted\n";
#		    relationptn($r, $ptn);
#		    printrel($nrf, $r, $noff);
	    } else {
		print "  relation $r is deleted, not in z$z $x,$y\n";
	    }
	}
    }
    if (defined $ndf) {
	delete $tagsversion{$df};
	close $df;
	delete $tagsversion{$ndf};
	close $ndf;
	rename "z$z/$x/$y/data.new","z$z/$x/$y/data";
    }
    if (defined $nnf) {
	close $nf;
	close $nnf;
	rename "z$z/$x/$y/nodes.new","z$z/$x/$y/nodes";
    }
    if (defined $nwf) {
	close $wf;
	close $nwf;
	rename "z$z/$x/$y/ways.new","z$z/$x/$y/ways";
    }
    if (defined $nrf) {
	close $rf;
	close $nrf;
	rename "z$z/$x/$y/relations.new","z$z/$x/$y/relations";
    }
}

1;

