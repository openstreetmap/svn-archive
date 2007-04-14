#    Copyright (C) 2005 Tommy Persson, tpe@ida.liu.se
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

package curl;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use strict;

my $urlbase = "http://www.openstreetmap.org/api/0.3/";

#BEGIN {
#    my $easy = 1;
#    foreach my $prefix (@INC) {
#	print STDERR "PREFIX:$prefix\n";
#	if (-f "$prefix/WWW/Curl/Easy.pm") {
#	    print STDERR "Easy.pm found: $prefix\n";
#	    $easy = 0;
#	}
#    }
#    if ($easy) {
#	eval {
#	    require WWW::Curl::easy;
#	    import WWW::Curl::easy;
#	    sub neweasy { return WWW::Curl::easy->new(); };
#	}
#    } else {
#	eval {
#	    require WWW::Curl::Easy;
#	    import WWW::Curl::Easy;
#	    sub neweasy { return WWW::Curl::Easy->new(); };
#	}
#    }
#}

##use WWW::Curl::easy;

sub neweasy {
  eval { require "WWW/Curl/Easy.pm"; import WWW::Curl::easy; };
  unless ($@) {
      return WWW::Curl::Easy->new();
  }
  eval { require "WWW/Curl/easy.pm"; import WWW::Curl::easy; };
  unless ($@) {
      return WWW::Curl::easy->new();
  }
}

sub chunk { 
    my ($data,$pointer)=@_; 
    ${$pointer}.=$data; 
##    print STDERR "CHUNK:$data\n";
    return length($data) 
}

sub hchunk { 
    my ($data,$pointer)=@_; 
    ${$pointer}.=$data; 
##    print STDERR "CHUNK:$data\n";
    return length($data) 
}

sub grab_landsat {
    my $west = shift;
    my $south = shift;
    my $east = shift;
    my $north = shift;
    my $width_px = shift;
    my $height_px = shift;

    my $url = 'http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=' .
	"$width_px" . '&height=' . "$height_px" . '&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=' . "$west,$south,$east,$north";

#    my $url = "http://www.ida.liu.se/~tompe/";
    print STDERR "URL:$url\n";

#    my $curl = WWW::Curl::easy->new();
    my $curl = neweasy ();

    my $headers = "";
    my $body = "";
    
    $curl->setopt ($curl->CURLOPT_HEADERFUNCTION, \&hchunk );
    $curl->setopt ($curl->CURLOPT_WRITEFUNCTION, \&chunk );
    $curl->setopt ($curl->CURLOPT_WRITEHEADER, \$headers );
#    $curl->setopt (CURLOPT_WRITEDATA, \$body );
    $curl->setopt ($curl->CURLOPT_FILE, \$body );
    $curl->setopt ($curl->CURLOPT_URL, $url);
##    $curl->setopt (CURLOPT_VERBOSE, 1);
##    $curl->setopt (CURLOPT_HEADER, 0);

    if ($curl->perform() != 0) {
	print "STDERR Failed ::".$curl->errbuf."\n";
    };
##    print STDERR "HEADER:$headers\n";
    if (check_200_OK ($headers)) {
	return $body;
    } else {
	return -1;
    }

}


sub grab_osm {
    my $west = shift;
    my $south = shift;
    my $east = shift;
    my $north = shift;
    my $username = shift;
    my $password = shift;

    my $url = "${urlbase}map?bbox=$west,$south,$east,$north";

    print STDERR "URL:$url\n";
##    print STDERR "$username:$password";

##    my $curl = WWW::Curl::easy->new();
    my $curl = neweasy ();

    my $headers = "";
    my $body = "";
    
    $curl->setopt ($curl->CURLOPT_HEADERFUNCTION, \&hchunk );
    $curl->setopt ($curl->CURLOPT_WRITEFUNCTION, \&chunk );
    $curl->setopt ($curl->CURLOPT_FILE, \$body );
    $curl->setopt ($curl->CURLOPT_URL, $url);
    $curl->setopt ($curl->CURLOPT_HEADER, 0);
    $curl->setopt ($curl->CURLOPT_USERPWD, "$username:$password");

    if ($curl->perform() != 0) {
	print "STDERR Failed ::".$curl->errbuf."\n";
    }
#    print $body;
    return $body;
}


sub get {
    my $suffixdata = shift;
    my $username = shift;
    my $password = shift;

    my $url = "$urlbase$suffixdata";

    print STDERR "URL:$url\n";
##    print STDERR "$username:$password";

#    my $curl = WWW::Curl::easy->new();
    my $curl = neweasy ();

    my $header = "";
    my $body = "";
    
    $curl->setopt ($curl->CURLOPT_HEADERFUNCTION, \&hchunk );
    $curl->setopt ($curl->CURLOPT_WRITEFUNCTION, \&chunk );
    $curl->setopt ($curl->CURLOPT_FILE, \$body );
    $curl->setopt ($curl->CURLOPT_URL, $url);
    $curl->setopt ($curl->CURLOPT_HEADER, 0);
    $curl->setopt ($curl->CURLOPT_USERPWD, "$username:$password");
    $curl->setopt ($curl->CURLOPT_WRITEHEADER, \$header);
##    $curl->setopt (CURLOPT_VERBOSE, 1);

    if ($curl->perform() != 0) {
	print "STDERR Failed ::".$curl->errbuf."\n";
    }
#    print $body;

    chomp $body;
    if (check_200_OK ($header)) {
	return $body;
    } else {
	return -1;
    }
    return $body;
}

sub delete {
    my $suffixdata = shift;
    my $username = shift;
    my $password = shift;

    my $url = "$urlbase$suffixdata";

    print STDERR "URL:$url\n";
##    print STDERR "$username:$password";

##    my $curl = WWW::Curl::easy->new();
    my $curl = neweasy ();

    my $header = "";
    my $body = "";
    
    $curl->setopt ($curl->CURLOPT_HEADERFUNCTION, \&hchunk );
    $curl->setopt ($curl->CURLOPT_WRITEFUNCTION, \&chunk );
    $curl->setopt ($curl->CURLOPT_FILE, \$body );
    $curl->setopt ($curl->CURLOPT_URL, $url);
    $curl->setopt ($curl->CURLOPT_HEADER, 0);
    $curl->setopt ($curl->CURLOPT_USERPWD, "$username:$password");
    $curl->setopt ($curl->CURLOPT_WRITEHEADER, \$header);
    $curl->setopt ($curl->CURLOPT_CUSTOMREQUEST, "DELETE");
##    $curl->setopt (CURLOPT_VERBOSE, 1);

    if ($curl->perform() != 0) {
	print "STDERR Failed ::".$curl->errbuf."\n";
    }
#    print $body;

    chomp $body;
    return (check_200_OK ($header));
}


sub read_callback {
    my ($maxlength,$pointer)=@_;
##    print "MAXLENGTH: $maxlength\n";
##    print "POINTER: $$pointer\n";
    my $data = $$pointer;
    $$pointer = "";
    return $data;
}

sub put_data {
    my $suffixdata = shift;
    my $data = shift;
    my $username = shift;
    my $password = shift;

    my $length = length ($data);

    my $body = "";
    my $header = "";

    my $url = "$urlbase$suffixdata";

    print STDERR "URL:$url\n";
##    print STDERR "$username:$password";

##    my $curl = WWW::Curl::easy->new();
    my $curl = neweasy ();

    $curl->setopt ($curl->CURLOPT_READFUNCTION, \&read_callback);
    $curl->setopt ($curl->CURLOPT_UPLOAD, 1);
    $curl->setopt ($curl->CURLOPT_PUT, 1);
    $curl->setopt ($curl->CURLOPT_URL, $url);
    $curl->setopt ($curl->CURLOPT_USERPWD, "$username:$password");
#    $curl->setopt (CURLOPT_READDATA, \$data );
    $curl->setopt ($curl->CURLOPT_INFILE, \$data );
    $curl->setopt ($curl->CURLOPT_INFILESIZE, length ($data) );

    $curl->setopt ($curl->CURLOPT_HEADERFUNCTION, \&hchunk );
    $curl->setopt ($curl->CURLOPT_HEADER, 0);
    $curl->setopt ($curl->CURLOPT_WRITEFUNCTION, \&chunk );
    $curl->setopt ($curl->CURLOPT_FILE, \$body );
    $curl->setopt ($curl->CURLOPT_WRITEHEADER, \$header);

###    $curl->setopt (CURLOPT_VERBOSE, 1);


    if ($curl->perform() != 0) {
	print "STDERR Failed ::".$curl->errbuf."\n";
    }
#    print $body;
##    print $header;
    chomp $body;
    if (check_200_OK ($header)) {
	return $body;
    } else {
	return -1;
    }
}

sub check_200_OK {
    my $s = shift;
    if ($s =~ /200 OK/) {
#	print STDERR "200 OK\n";
	return 1;
    }
    print "HEADER: $s\n";
    return 0;
}

return 1;
