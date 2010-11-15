#!/usr/bin/perl -w

use LWP::UserAgent;


############################################################################

package MiniStringIO;

sub new
{
	my $class= shift;
	my $self= bless { string => shift, pointer => 0 }, $class;

	return $self;	
}

sub read
{
	my $self= shift;
	my $len= shift;
	
	my $r= substr($self->{string}, $self->{pointer}, $len);
	$self->{pointer}+= $len;
	
	return $r;
}

############################################################################


package main;



sub encodevalue
{
	my ($n, $type)= @_;
	my $a;
	
	if (ref($n) eq "HASH")
	{
		# fixme: missing
	}
	elsif (ref($n) eq "ARRAY")
	{
		$a= chr(10) . encodelong(scalar(@$n));
		$a.= encodevalue($_, $type) foreach(@$n);
		return $a;
	}
	if (!defined($type)) # not defined, so we have to find out
	{
		$type= "String";
		$type= "Float" if ($n=~/^([+-]?)(?=\d|\.\d)\d*(\.\d*)?([Ee]([+-]?\d+))?$/); # taken from http://perldoc.perl.org/perlfaq4.html
	}
	if ($type eq "String")
	{
		return chr(2) . encodestring($n);
	}
	elsif ($type eq "Float")
	{
		return chr(0) . encodedouble($n);
	}
	elsif ($type eq "NilClass")
	{
		return chr(5);
	}
}


sub encodestring
{
	my $n= shift;
	
	return chr(int(length($n)/256)) . chr(length($n)%256) . $n;
}

sub encodedouble
{
	return reverse(pack("d", shift));
}


sub encodelong
{
	return pack("N", shift);
}

####################################

sub getbyte
{
	my $st= shift;
	my $c= unpack("C", $st->read(1) );
	return $c;
}

sub getint
{
	my $st= shift;
	my $i= unpack("n", $st->read(2) );
	return $i;
}

sub getlong
{
	my $st= shift;
	my $l= unpack("N", $st->read(4) );
	return $l;
}

sub getstring
{
	my $st= shift;
	
	my $len= unpack("n", $st->read(2) );
	my $str= $st->read($len);
	return $str;
}

sub getdouble
{
	my $st= shift;
	my $s= $st->read(8);
	my $d= unpack("d", reverse($s));
	return $d;
}

sub getarray
{
	my $st= shift;
	my $len= unpack("N", $st->read(4) );
	my @a;
	for(my $i=0; $i<$len; $i++)
	{
		push(@a, getvalue($st));
	}
	return @a; # v1
#	return \@a; # v2
}

sub getvalue
{
	my $st= shift;
	my $type= unpack("C", $st->read(1) );
	if    ($type== 0) { return getdouble($st); }
	elsif ($type== 1) { return getbyte($st); }
	elsif ($type== 2) { return getstring($st); }
	elsif ($type== 3) { die "not yet implemented"; return 1; } # fixme: not yet implemented
	elsif ($type== 5) { return 0; }
	elsif ($type== 6) { return 0; }
	elsif ($type== 8) { die "not yet implemented"; return 1; } # fixme: not yet implemented
	elsif ($type==10) { return getarray($st); }
}




############################################################################

my $amf;

my $url= "http://www.openstreetmap.org/api/0.6/amf/read";

my @bbox= (-1.4916, 51.86895, -1.47949, 51.88447); # west, south, eeast, north


$amf = chr(0) . chr(0);						# FP8
$amf.= chr(0) . chr(0);						# no headers
$amf.= chr(0) . chr(1);						# one body

$amf.= encodestring("whichways_deleted");	# message
$amf.= encodestring("1", "String");			# unique ID for this message
$amf.= encodelong(0);						# size of body in bytes, Potlatch ignores this
$amf.= encodevalue(\@bbox);					# argument



my $ua = LWP::UserAgent->new;
$ua->agent("amf_finddeleted.pl");

my $req = HTTP::Request->new(POST => $url);
$req->content_type("application/x-www-form-urlencoded");
$req->content($amf);


# Pass request to the user agent and get a response back
my $res = $ua->post($url, "Content-Type" => "application/x-www-form-urlencoded", Content => $amf);

# Check the outcome of the response
if ($res->is_success) 
{
	my $r= $res->content;

	my ($junk)= ($r=~ /^(.{6})/gsc);
	
	my $sio= MiniStringIO->new($r);
	$sio->read(6);

	my $s1= getstring($sio);
	my $s2= getstring($sio);
	my $lo= getlong($sio);
	#printf("%s %s %ld\n", $s1, $s2, $lo); # dbg

	my @ways= getvalue($sio);
	printf("%s\n", join(",", @ways) );
}
else 
{
	print $res->status_line, "\n";
}

