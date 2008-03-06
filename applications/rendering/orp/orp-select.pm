#!/usr/bin/perl

# OR/P - Osmarender in Perl
# -------------------------
#
# Selection Module
#
# (See orp.pl for details.)
#
# This module contains the implementation for the various styles of
# object selection supported in <rule> elements.

sub select_elements_without_tags
{
    my ($oldsel, $e) = @_;
    my $newsel = Set::Object->new();
    foreach ($oldsel->members())
    {
        next if defined($e) and ref($_) != $e;
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
        next if defined($e) and ref($_) != $e;
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
        next if defined($e) and ref($_) ne $e;
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
        next if (defined($e) and ref($_) != $e);
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
        next if defined($e) and ref($_) != $e;
        foreach my $key(@keys_wanted)
        {
            next outer if (defined($_->{"tags"}->{$key}));
        }
        $newsel->insert($_);
    }

    return $newsel;
}

# e=way or node, s not supptd, v must not contain ~
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
            ($e eq 'way') ? @{$index_way_tags->{$key}||[]} : 
            ($e eq 'node') ? @{$index_node_tags->{$key}||[]} : 
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

# e=node, s=way, v must not contain ~
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

sub select_elements_with_given_tag_key_and_value_slow
{
    my ($oldsel, $e, $k, $v, $s) = @_;
    my @values_wanted = split('\|', $v);
    my $newsel = Set::Object->new();
    my @keys_wanted = split('\|', $k);

outer:
    foreach ($oldsel->members())
    {   
        next if defined($e) and ref($_) != $e; 
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
                        ($value eq $keyval and defined($keyval)))
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

1;
