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

package osmsegment;

use FindBin qw($RealBin);
use lib "$RealBin/../perl";

use osmbase;

use strict;

use vars qw (@ISA  $AUTOLOAD);
@ISA = qw (osmbase);


# class: path, unsurfaced, minor, estate, street, secondary, primary or 
# motorway
#foot: has to be no, unofficial or yes
# horse: same as foot
# bike: same as foot
# car: no or yes


sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    SUPER::new $class (
		       FROM => 0,
		       TO => 0,
		       @_
		       );
}

sub set_from {
    my $self = shift;
    my $val = shift;
    $self->{FROM} = $val;
}

sub get_from {
    my $self = shift;
    return $self->{FROM};
}

sub set_to {
    my $self = shift;
    my $val = shift;
    $self->{TO} = $val;
}

sub get_to {
    my $self = shift;
    return $self->{TO};
}

sub get_osmuid {
    my $self = shift;
    my $res = $self->get_uid ();
    $res =~ s/s//;
    return $res;
}

sub get_osmfrom {
    my $self = shift;
    my $res = $self->get_from ();
    $res =~ s/n//;
    return $res;
}

sub get_osmto {
    my $self = shift;
    my $res = $self->get_to ();
    $res =~ s/n//;
    return $res;
}



sub print {
    my $self = shift;
    my $from = $self->get_from ();
    my $to = $self->get_to ();
    my $uid = $self->get_uid ();
    my $tags = $self->get_tags ();
    print "OSMSEGMENT: $from $to $uid --- ";
    foreach my $k (keys %{$tags}) {
	print "$k - $tags->{$k}; ";
    }
    print "\n";
}

sub update_osm_segment {
    my $self = shift;
    my $username = shift;
    my $password = shift;

    my $from = $self->get_osmfrom ();
    my $to = $self->get_osmto ();
    my $tags = $self->get_tags ();

    my $uid = $self->get_osmuid ();
    print STDERR "Update segment: $uid $from $to\n";
    if ($uid) {
	return osmutil::update_segment ($uid, $from, $to, $tags, $username,
					$password);
    }
}


return 1;
