package SAXOsmHandler;

#=====================================================================

=head1 NAME

SAXOsmHandler - Osmrender implementation

=head1 SYNOPSIS

 my $handler = SAXOsmHandler->new($node, $way, $relation);
 my $parser  = XML::Parser::PerlSAX->new(Handler => $handler);

=head2 DESCRIPTION

This module contains a SAX handler code that can be used in conjunction
with L<XML::Parser::PerlSAX> to parse OSM XML files.

=cut

#=====================================================================

use strict;
use warnings;


#=====================================================================

=head1 OBJECT CREATION AND DESTRUCTION

=cut

#=====================================================================

#--------------------------------------------------------------------

=head2 new( $node, $way, $relation )

Creates a new L<SAXOsmHandler> instance.

=cut

#--------------------------------------------------------------------
sub new 
{
    my ($type, $node, $way, $relation, $bounds) = @_;
    
    return bless {current  => undef,
                  node     => $node,
                  way      => $way,
                  relation => $relation,
                  bounds   => $bounds }, $type;
}


#=====================================================================

=head1 METHODS

=cut

#=====================================================================

#--------------------------------------------------------------------

=head2 start_element( $element )

=cut

#--------------------------------------------------------------------
sub start_element 
{
    my ($self, $element) = @_;

    if ($element->{Name} eq 'node') 
    {
        undef $self->{current};
        return if defined $element->{'Attributes'}{'action'}
               && $element->{'Attributes'}{'action'} eq 'delete';
               
        my $id = $element->{Attributes}{id};
        
        $self->{node}{$id} =
          $self->{current} = {id        => $id,
                              layer     => 0, 
                              lat       => $element->{'Attributes'}{'lat'}, 
                              lon       => $element->{'Attributes'}{'lon'}, 
                              user      => $element->{'Attributes'}{'user'}, 
                              timestamp => $element->{'Attributes'}{'timestamp'}, 
                              ways      => [],
                              relations => [] };
        bless $self->{current}, 'node';
    }
    elsif ($element->{Name} eq 'way')
    {
        undef $self->{current};
        return if defined $element->{'Attributes'}{'action'}
               && $element->{'Attributes'}{'action'} eq 'delete';
               
        my $id = $element->{'Attributes'}{'id'};
        $self->{way}{$id}  =
          $self->{current} = {id    => $id,
                              layer => 0, 
                              user      => $element->{'Attributes'}{'user'}, 
                              timestamp => $element->{'Attributes'}{'timestamp'}, 
                              nodes => [],
                              relations => [] };

        bless $self->{current}, 'way';
        
    }
    elsif ($element->{Name} eq 'relation')
    {
        undef $self->{current};
        return if defined $element->{'Attributes'}{'action'}
               && $element->{'Attributes'}{'action'} eq 'delete';
        
        my $id = $element->{'Attributes'}{'id'};
        $self->{relation}{$id} =
              $self->{current} = {id        => $id, 
                                  user      => $element->{'Attributes'}{'user'}, 
                                  timestamp => $element->{'Attributes'}{'timestamp'}, 
                                  members   => [],
                                  relations => [] };
              
        bless $self->{current}, 'relation';
    }
    elsif (($element->{Name} eq 'nd') and (ref $self->{current} eq 'way'))
    {
        push(@{$self->{current}{'nodes'}},
             $self->{node}{$element->{'Attributes'}->{'ref'}})
            if defined($self->{node}{$element->{'Attributes'}->{'ref'}});
    }
    elsif (($element->{Name} eq 'member') and (ref $self->{current} eq 'relation'))
    {
        # relation members are temporarily stored as symbolic references (e.g. a
        # string that contains "way:1234") and only later replaced by proper 
        # references.
        
        push(@{$self->{current}{'members'}}, 
            [ $element->{Attributes}{role}, 
              $element->{Attributes}{type}.':'.
              $element->{Attributes}{ref } ]);
    }
    elsif ($element->{Name} eq 'tag')
    {
        # store the tag in the current element's hash table.
        # also extract layer information into a direct hash member for ease of access.
        
        $self->{current}{tags }{ $element->{Attributes}{k} }= $element->{Attributes}{v};
        $self->{current}{layer} = $element->{Attributes}{v}
            if $element->{Attributes}{k} eq "layer";
    }
    elsif ($element->{Name} eq 'bounds')
    {
        my $b = $self->{bounds}; # Just a shortcut
        my $minlat = $element->{Attributes}{minlat};
        my $minlon = $element->{Attributes}{minlon};
        my $maxlat = $element->{Attributes}{maxlat};
        my $maxlon = $element->{Attributes}{maxlon};

        $b->{minlat} = $minlat if !defined($b->{minlat}) || $minlat < $b->{minlat};
        $b->{minlon} = $minlon if !defined($b->{minlon}) || $minlon < $b->{minlon};
        $b->{maxlat} = $maxlat if !defined($b->{maxlat}) || $maxlat > $b->{maxlat};
        $b->{maxlon} = $maxlon if !defined($b->{maxlon}) || $maxlon > $b->{maxlon};
    }
    else
    {
        # ignore for now
    }
}


#--------------------------------------------------------------------

=head2 characters( )

=cut

#--------------------------------------------------------------------
sub characters 
{
    # osm data format has no plain character data
}


#--------------------------------------------------------------------

=head2 end_element( )

=cut

#--------------------------------------------------------------------
sub end_element 
{
    # no
}


#--------------------------------------------------------------------

=head2 start_document( )

=cut

#--------------------------------------------------------------------
sub start_document 
{
}


#--------------------------------------------------------------------

=head2 end_document( )

=cut

#--------------------------------------------------------------------
sub end_document 
{
}


1;
__END__

=head1 SEE

L<XML::Parser::PerlSAX>

=head1 VERSION

$Id: SAXOsmHandler.pm 4 2008-03-11 15:24:12Z thomas $

=cut


