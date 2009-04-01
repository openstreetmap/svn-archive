# OR/P - Osmarender in Perl
# -------------------------
#
# Selection Module
#
# (See orp.pl for details.)
#
# This module contains the implementation for the various styles of
# object selection supported in <rule> elements.

use strict;
use warnings;

our $index_way_tags;
our $index_node_tags;
our $debug;

# for collision avoidance / proximity filter
my $used_boxes = {};

sub select_elements_without_tags
{
    my ($oldsel, $e) = @_;
    my $newsel = Set::Object->new();
    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        $newsel->insert($_) unless defined($_->{"tags"});
    }
    return $newsel;
}


sub select_elements_with_any_tag
{
    my ($oldsel, $e) = @_;
    my $newsel = Set::Object->new();

    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        $newsel->insert($_) if defined($_->{"tags"});
    }
    return $newsel;
}

sub select_elements_with_given_tag_value
{
    my ($oldsel, $e, $v);
    my $newsel = Set::Object->new();
    my $seek = {};
    $seek->{$_} = 1 foreach(split('\|', $v));
outer:
    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        foreach my $value(values(%{$_->{"tags"}}))
        {
            if (defined($seek->{$value}))
            {
                $newsel->insert($_);
                next outer;
            }
        }
    }
    return $newsel;
}

sub select_elements_with_given_tag_key
{
    my ($oldsel, $e, $k) = @_;
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);

outer:
    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        foreach my $key(@keys_wanted)
        {
            if (defined($_->{"tags"}->{$key}))
            {
                $newsel->insert($_);
                next outer;
            }
        }
    }

    return $newsel;
}

sub select_elements_without_given_tag_key
{
    my ($oldsel, $e, $k) = @_;
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);


outer:
    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        foreach my $key(@keys_wanted)
        {
            next outer if (defined($_->{"tags"}->{$key}));
        }
        $newsel->insert($_);
    }

    return $newsel;
}

# this is a speedy method to find objects that have one of a list of 
# specified values for one or more tag. instead of looking through all 
# objects, it uses the index_way_tags/index_node_tags structure to quickly 
# identify objects that have the tag at all, and then only checks for
# the correct value being present.
#
# it supports "node" or "way" for the "e" attribute, does not support
# the "s" attribute, and the non-value ("~") is not allowed as part of 
# the "v" attribute.
#
sub select_elements_with_given_tag_key_and_value_fast
{
    my ($oldsel, $e, $k, $v) = @_;
    my @values_wanted = split('\|', $v);
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);

    foreach my $key(split('\|', $k))
    {
        # retrieve list of objects with this key from index.
        my @objects = 
            (defined $e && $e eq 'way') ? @{$index_way_tags->{$key}||[]} :
            (defined $e && $e eq 'node') ? @{$index_node_tags->{$key}||[]} :
            (@{$index_way_tags->{$key}||[]}, @{$index_node_tags->{$key}||[]});

        debug(sprintf('%d objects retrieved from index for e="%s" k="%s"', 
            scalar(@objects), $e, $k)) if ($debug->{"indexes"});

        # process only those from oldsel that have this key.
outer:
        foreach (@objects)
        {   
            next unless ($oldsel->contains($_));
            foreach my $value(@values_wanted)
            {   
                if ($_->{"tags"}->{$key} eq $value)
                {   
                    $newsel->insert($_);
                    next outer;
                }   
            }   
        } 
    }
    return $newsel;
}

# this is a fast, indexed lookup in the same manner as the above
# select_elements_with_given_tag_key_and_value_fast, but geared towards
# the situation where nodes are selected based on the tags of ways they
# are member of.
#
# it supports only "node" for the "e" attribute and only "way" for the
# "s" attribute, and the non-value ("~") is not allowed as part of 
# the "v" attribute.
#
sub select_nodes_with_given_tag_key_and_value_for_way_fast
{
    my ($oldsel, $k, $v) = @_;
    my @values_wanted = split('\|', $v);
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);

    foreach my $key(split('\|', $k))
    {
        # process only those from oldsel that have this key.
outer:
        foreach my $way(@{$index_way_tags->{$key}||[]})
        {   
            foreach my $value(@values_wanted)
            {   
                if ($way->{"tags"}->{$key} eq $value)
                {   
                    foreach (@{$way->{'nodes'}})
                    {
                        next unless ($oldsel->contains($_));
                        $newsel->insert($_);
                    }   
                }   
            }
        } 
    }
    return $newsel;
}

# this is the equivalent to a "table scan" in an SQL database; it simply
# tuns throught all the elements in the current selection and checks whether
# they need to be copied into the new selection. it supports (more or less)
# everything but is also the slowest method.
#
sub select_elements_with_given_tag_key_and_value_slow
{
    my ($oldsel, $e, $k, $v, $s) = @_;
    my @values_wanted = split('\|', $v);
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);

outer:
    foreach ($oldsel->members())
    {   
        next if defined($e) and ref($_) ne $e and not ($e eq 'way' and ref($_) eq 'multipolygon');
        # determine whether we're comparing against the tags of the object
        # itself or the tags selected with the "s" attribute.
        my $tagsets;
        if ($s eq "way")
        {   
            $tagsets = []; 
            foreach my $way(@{$_->{"ways"}})
            {   
                push(@$tagsets, $way->{"tags"});
            }   
        }   
        else
        {   
            $tagsets = [ $_->{"tags"} ];
        }   

        foreach my $key(@keys_wanted)
        {   
            foreach my $value(@values_wanted)
            {   
                foreach my $tagset(@$tagsets)
                {   
                    my $keyval = $tagset->{$k};
                    if (($value eq '~' and !defined($keyval)) or
                        (defined($keyval) and ($value eq $keyval)))
                    {   
                        $newsel->insert($_);
                        next outer;
                    }   
                }   
            }   
        }   
    } 
    
    return $newsel;
}

# this implements a very simple proximity selection. it works only for nodes and
# draws an imaginary box around the node. then it checks all "used" boxes in the
# same proximity class and unselects the object if a collision is detected. 
#
# otherwise, the object remains selected and its box is stored.
#
# there are many FIXMEs: 
# 1. the box is computed based on lat/lon so will have different sizes on the map
#    at different latitudes.
# 2. any object that is selected is considered to have "used" its box. this mechanism
#    only works when the proximity filter is on the last selection rule (which then
#    only contains drawing code). If subsequent rules further reduce the object set,
#    then the boxes are "used" nonetheless.
# 3. the order in which the objects are processed is more or less random (as the 
#    storage is backed by a perl hash). it will be identical for identical input
#    data, but as soon as input data varies a bit, the order might change completely.

sub select_proximity
{
    my ($oldsel,$hp, $vp, $pc) = @_;
    my $newsel = Set::Object->new();
    $pc = "default" if ($pc eq "");
    foreach ($oldsel->members())
    {
        # proximity stuff currently only works for nodes; copy others
        if (ref($_) ne "node")
        {
            $newsel->insert($_);
            next;
        }
        
        my $bottom = $_->{'lat'} - $hp;
        my $left = $_->{'lon'} - $vp;
        my $top = $_->{'lat'} + $hp;
        my $right = $_->{'lon'} + $vp;
        my $intersect = 0;

        foreach my $ub(@{$used_boxes->{$pc}})
        {
            if ((($ub->[0] > $bottom && $ub->[0] < $top) || ($ub->[2] > $bottom && $ub->[2] < $top) || ($ub->[0] <= $bottom && $ub->[2] >= $top)) &&
               (($ub->[1] > $left && $ub->[1] < $right) || ($ub->[3] > $left && $ub->[3] < $right) || ($ub->[1] <= $left && $ub->[3] >= $right)))
            {
                # intersection detected; skip this object.
                $intersect = 1;
                # debug("object skipped due to collision in class '$pc'");
                last;
            }
        }
        next if ($intersect);
        $newsel->insert($_);
        #debug("object added in class '$pc'");
        push(@{$used_boxes->{$pc}}, [ $bottom, $left, $top, $right ]);
    }
    delete $used_boxes->{$pc} if ($pc eq "default");
    return $newsel;
}

# this implements a minimum size selection. it selects all objects whose bounding
# box circumference exceeds the specified number.
#
# formula taken from osmarender.xsl, where it states
# <!--
#    cirfer = T + (N * [1.05 - ([t - 5] / 90)])
#    T Latitude difference N Longitude difference t absolute Latitude 
#    The formula interpolates a cosine function with +10% error at the poles/equator and -10% error in the north Italy.
# -->
#
# TODO: optionally replace with proper area computation? store computed area?

sub select_minsize
{
    my ($oldsel,$minsize) = @_;
    my $newsel = Set::Object->new();

    foreach my $element ($oldsel->members())
    {
        # minsize stuff currently only works for ways
        if (ref($element) ne "way")
        {
            next;
        }

        my ($minlat, $minlon, $maxlat, $maxlon);
        foreach my $node (@{$element->{"nodes"}})
        {
            $minlat = $node->{"lat"} if (!defined($minlat) or $node->{"lat"}<$minlat);
            $minlon = $node->{"lon"} if (!defined($minlon) or $node->{"lon"}<$minlon);
            $maxlat = $node->{"lat"} if (!defined($maxlat) or $node->{"lat"}>$maxlat);
            $maxlon = $node->{"lon"} if (!defined($maxlon) or $node->{"lon"}>$maxlon);
        }

        #FIXME: what is this for? next unless defined($minlat);

        my $cirfer = ($maxlat-$minlat) + (($maxlon-$minlon) * (1.05-(($maxlat-5) / 90)));
        $newsel->insert($element) if ($cirfer > $minsize);

        debug(sprintf('select_minsize: way=%s size=%f %s minsize %f', 
            $element->{id}, $cirfer, $cirfer > $minsize ? 'larger than' : 'smaller than', $minsize)) if ($debug->{"selectors"});
    }

    return $newsel;
}

# notConnectedSameTag filter
# it selects objects which are not connected to at least one other object with the same value of $tag

sub select_not_connected_same_tag
{
    my ($oldsel, $tag) = @_;
    my $newsel = Set::Object->new();

    OUTER:
    foreach my $element ($oldsel->members())
    {
         # only ways are supported for now
         if (ref $element ne 'way')
         {
             $newsel->insert($element);
             next;
         }

         my $value = $element->{'tags'}->{$tag};
         next unless defined($value);

         foreach my $node (@{$element->{'nodes'}})
         {
             foreach my $way (@{$node->{'ways'}})
             {
                 my $otherValue = $way->{'tags'}->{$tag};
                 next unless defined($otherValue);
                 next if $way->{'id'} eq $element->{'id'};
                 # skip element if other element with the same value is connected
                 next OUTER if ($otherValue eq $value);
             }
         }

         $newsel->insert($element);
    }

    return $newsel;
}

# Filter for closed or non-closed ways.
# This can be used to style something different if it is drawn like an area or as a linear feature

sub select_closed
{
    my ($selection, $closed) = @_;

    foreach my $element ($selection->members())
    {
        unless ( ref($element) eq 'way')
        {
            $selection->remove($element);
            next;
        }

        # Test for closed-ness
        # * It's not an area if it is tagged area=no
        # * An area must have more than two nodes
        # * The first and last node must be the same
        if ( (!defined($element->{'tags'}->{'area'}) || $element->{'tags'}->{'area'} ne 'no') &&
            scalar( @{ $element->{'nodes'} } ) > 2 &&
            $element->{'nodes'}->[0] == $element->{'nodes'}->[-1])
        {
            $selection->remove($element) if $closed eq 'no';
        }
        else
        {
            $selection->remove($element) if $closed eq 'yes';
        }
    }
}

1;
