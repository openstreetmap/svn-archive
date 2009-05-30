#!/usr/bin/perl -w
use LWP::Simple;
use XML::Parser;

my $id=$ARGV[0];

warn ("id:$id");
my $url="http://api.openstreetmap.org/api/0.6/relation/$id/full";
warn ("url:$url");
my %nlat;
my %nlon;
my $wayid;
my @unsortedways;
my @sortedways;
my $name="";

sub aAnfang
{
    ($wert_des_zeigers,$starttag,%hash) = @_;

    if ($starttag eq "node") {
	my $nid=$hash{"id"};
	$nlat{$nid}=$hash{"lat"};
	$nlon{$nid}=$hash{"lon"};
    } elsif ($starttag eq "way") {
	$wayid=$hash{"id"};
	@{$wnodes{$wayid}}=();
    } elsif ($starttag eq "nd") {
	$ref=$hash{"ref"};
#	print "push $wayid,$ref\n";
	push @{$wnodes{$wayid}},$ref;
    } elsif ($starttag eq "relation") {
	$rid=$hash{"id"};
#	print "relation $rid";
    } elsif ($starttag eq "member") {
	my $ref=$hash{"ref"};
	if ($hash{"type"} eq "way") {
	    if ($rid==$id) {
		push  @unsortedways,$ref;
	    }
	}
    } elsif ($starttag eq "tag") {
	if (defined($rid)) {
	    if (($rid==$id) and  ($hash{"k"} eq "name")) {
		$name=$hash{"v"};
	    }
	}
    }
}
sub aEnde
{
     ($wert_des_zeigers,$endtag) = @_;
     if ($endtag eq "realtion") {
	 $rid=undef;
     }
}
sub aInhalt
{
}

sub printWay
{
    my $id=shift;
    my $direction=shift;
    my $printfirst=shift;
    my $rstr="";
    my $i=0;
    foreach my $node (@{$wnodes{$id}}) {
	my $str="";
	if ((($i==0) and ($direction==0)) or (($i==$#{$id}) and ($direction==1))) {
	    if ($printfirst) {
		$str=sprintf("   %E   %E\n",$nlon{$node},$nlat{$node});
	    }
	    $printfirst=0;
	    $i++;
	} else {
	    $str=sprintf("   %E   %E\n",$nlon{$node},$nlat{$node});
	}
	if ($direction==0) {
	    $rstr.=$str;
	} else {
	    $rstr=$str.$rstr;
	}
    }
    print $rstr;
}

############################ MAIN ####

warn("get");
$content = get($url);
warn("get done");
die "Could get $url" unless defined $content;

#$content = `cat full`;


my $azeiger = new XML::Parser ();

$azeiger->setHandlers (Start => \&aAnfang,End => \&aEnde );
warn("parse");
$azeiger->parse($content);
warn("parse done");
my $firstway=(pop @unsortedways);
#print "way:$firstway\n";
my $firstnode=${$wnodes{$firstway}}[0];
my $lastnode=${$wnodes{$firstway}}[-1];
#print "f,l:$firstnode,$lastnode\n";
push @sortedways, $firstway;
$dire{$firstway}=0;

$name=~s/,(.*)$//;
$name=~s/\s*$//;
$name=~s/^\s*//;

my $polyid=1;

my $condition=0;
if ($#unsortedways >=0) {
    $condition=1;
}

while ($condition==1) {
    my $found=-1;
    my $i=0;
    my $direction=0;
    $condition=0;
    foreach my $way (@unsortedways) {
	if ($way>=0) {
	    $condition=1;
	    warn("way:$way $i");
	    if ($lastnode==${$wnodes{$way}}[0]) {
		$found=$i;
	    } elsif ($lastnode==${$wnodes{$way}}[-1]) {
		$found=$i;
		$direction=1;
	    }
	}
	$i++;

    }
    if ($condition) {
	if (($found==-1) and ($firstnode == $lastnode)) {
	    warn("First==Last");
	    print "$name\n$polyid\n";
	    $polyid++;
	    my $printfirst=1;
	    foreach my $way (@sortedways) {
		my $i=0;
		my $rstr="";
		&printWay($way,$dire{$way},$printfirst);
		$printfirst==0;
	    }
	    print "END\n";
	    @sortedways=();
	    my $firstway=0;
	    my $i=0;
	    foreach my $way (@unsortedways) {
		if ($way>=0) {
		    if ($firstway==0) {
			$firstway=$way;
			$found=$i;
		    }
		}
		$i++;
	    }

	    $unsortedways[$found]=-1;
#print "way:$firstway\n";
	    my $firstnode=${$wnodes{$firstway}}[0];
	    my $lastnode=${$wnodes{$firstway}}[-1];
	    push @sortedways,$firstway;
	    $dire{$firstway}=0;
	    
	} else {
	    ($found!=-1) or die("No Way found for node $lastnode ");
	    
	    my $way=$unsortedways[$found];
	    warn("found $found $way\n");
	    $unsortedways[$found]=-1;
	    push @sortedways,$way;
	
	    if ($direction==0) {
		$lastnode=${$wnodes{$way}}[-1];
	    } else {
		$lastnode=${$wnodes{$way}}[0];
	    }
	    $dire{$way}=$direction;
	

	    # Finde einen Way der zum letzen past
	}
	warn("while $condition");
    }
}

($firstnode == $lastnode ) or die ("gap between node $firstnode and $lastnode");
print "$name\n$polyid\n";
$polyid++;
my $printfirst=1;
foreach my $way (@sortedways) {
    &printWay($way,$dire{$way},$printfirst);
    $printfirst=0;

}

print "END\nEND\n";
