##################################################################
## EntitiesV3.pm - Wraps entities used by OSM                   ##
## By Martijn van Oosterhout <kleptog@svana.org>                ##
##                                                              ##
## Package that wraps the entities used by OSM into Perl        ##
## object, so they can be easily manipulated by various         ##
## packages.                                                    ##
##                                                              ##
## Licence: LGPL                                                ##
##################################################################

use XML::Writer;
use strict;

############################################################################
## Top level Entity type, parent of all types, includes stuff relating to ##
## tags and IDs which are shared by all entity types                      ##
############################################################################
package Geo::OSM::Entity;
use POSIX qw(strftime);

use Carp;

sub _new
{
  bless {}, shift;
}

sub _get_writer
{
  my($self,$res) = @_;
  return new XML::Writer(OUTPUT => $res, NEWLINES => 0, ENCODING => 'utf-8');
}

sub add_tag
{
  my($self, $k,$v) = @_;
  push @{$self->{tags}}, $k, $v;
}

sub set_tags
{
  my($self,$tags) = @_;
  if( ref($tags) eq "HASH" )
  { $self->{tags} = [%$tags] }
  elsif( ref($tags) eq "ARRAY" )
  { $self->{tags} = [@$tags] }
  else
  { croak "set_tags must be HASH or ARRAY" }
}

sub tag_xml
{
  my ($self,$writer) = @_;
  my @a = @{$self->{tags}};
  
  my $str = "";
  
  while( my($k,$v) = splice @a, 0, 2 )
  {
    $writer->emptyTag( "tag", 'k' => $k, 'v' => $v );
  }
}

sub set_id
{
  my($self,$id) = @_;
  $self->{id} = $id;
}  

sub id
{
  my $self = shift;
  return $self->{id};
}

sub set_timestamp
{
  my($self,$time) = @_;
  if( defined $time )
  { $self->{timestamp} = $time }
  else
  { $self->{timestamp} = strftime "%Y-%m-%dT%H:%M:%S+00:00", gmtime(time) }
}

sub timestamp
{
  my $self = shift;
  return $self->{timestamp};
}

sub full_xml
{
  my $self = shift;
  return qq(<?xml version="0.1"?>\n<osm version="0.3">\n).$self->xml()."</osm>\n";
}

package Geo::OSM::Way;
our @ISA = qw(Geo::OSM::Entity);

sub new
{
  my($class, $attr, $tags, $segs) = @_;
  
  my $obj = bless $class->SUPER::_new(), $class;
  
  $obj->set_tags($tags);
  $obj->set_segs($segs);
  $obj->set_id($attr->{id} );
  $obj->set_timestamp( $attr->{timestamp} );
  
  return $obj;
}

sub type { return "way" }

sub set_segs
{
  my($self,$segs) = @_;
  $self->{segs} = $segs;
}

sub segs
{
  my $self = shift;
  return [@{$self->{segs}}];  # Return a copy
}

sub xml
{
  my $self = shift;
  my $str = "";
  my $writer = $self->_get_writer(\$str);

  $writer->startTag( "way", id => $self->id, timestamp => $self->timestamp );
  $self->tag_xml( $writer );
  for my $seg (@{$self->segs})
  {
    $writer->emptyTag( "seg", id => $seg );
  }
  $writer->endTag( "way" );
  $writer->end;
  return $str;
}

package Geo::OSM::Segment;
our @ISA = qw(Geo::OSM::Entity);

sub new
{
  my($class, $attr, $tags) = @_;
  
  my $obj = bless $class->SUPER::_new(), $class;
  
  $obj->set_tags($tags);
  $obj->set_id($attr->{id} );
  $obj->set_timestamp( $attr->{timestamp} );
  $obj->{from} = $attr->{from};
  $obj->{to} = $attr->{to};
  
  return $obj;
}

sub type { return "segment" }

sub set_fromto
{
  my($self,$from,$to) = @_;
  $self->{from} = $from;
  $self->{to} = $to;
}

sub from
{
  shift->{from};
}
sub to
{
  shift->{to};
}

sub xml
{
  my $self = shift;
  my $str = "";
  my $writer = $self->_get_writer(\$str);

  $writer->startTag( "segment", id => $self->id, from => $self->from, to => $self->to, timestamp => $self->timestamp );
  $self->tag_xml( $writer );
  $writer->endTag( "segment" );
  $writer->end;
  return $str;
}

package Geo::OSM::Node;
our @ISA = qw(Geo::OSM::Entity);

sub new
{
  my($class, $attr, $tags) = @_;
  
  my $obj = bless $class->SUPER::_new(), $class;
  
  $obj->set_tags($tags);
  $obj->set_id($attr->{id} );
  $obj->set_timestamp( $attr->{timestamp} );
  $obj->{lon} = $attr->{lon};
  $obj->{lat} = $attr->{lat};
  
  return $obj;
}

sub type { return "node" }

sub set_latlon
{
  my($self,$lat,$lon) = @_;
  $self->{lat} = $lat;
  $self->{lon} = $lon;
}

sub lat
{
  shift->{lat};
}
sub lon
{
  shift->{lon};
}

sub xml
{
  my $self = shift;
  my $str = "";
  my $writer = $self->_get_writer(\$str);
  
  $writer->startTag( "node", id => $self->id, lat => $self->lat, lon => $self->lon, timestamp => $self->timestamp );
  $self->tag_xml( $writer );
  $writer->endTag( "node" );
  $writer->end;
  return $str;
}



1;
