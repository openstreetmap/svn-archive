package Geo::OSM::MapFeatures;

use warnings;
use strict;

use Error;
use LWP::UserAgent;
use Storable;
use XML::Simple;

use base qw(Class::Accessor);
__PACKAGE__->mk_accessors(qw(export_url api_url mapfeatures_pagename trace));

use Geo::OSM::MapFeatures::Feature;

=head1 NAME

Geo::OSM::MapFeatures - Parses and represents OpenStreetMap Map Features

=head1 VERSION

Version 0.01

=cut

our $VERSION = '0.04';


=head1 SYNOPSIS

use Geo::OSM::MapFeatures;

my $mf = new Geo::OSM::MapFeatures;
$mf->download();
$mf->parse();

# To print a simple ascii representation:
foreach my $category ( $mf->categories() ){
    print "\n\n===== $category =====\n";
    foreach my $feature ( $mf->features($category) ){
        print "$feature\n";
    }
}

# Or you can choose not to use the string overloading and get the
# individual elements yourself:
foreach my $category ( $mf->categories() ){
    print "\n\n===== $category =====\n";
    foreach my $feature ( $mf->features($category) ){
        print "Key: ".$feature->key()."\n";
        print "Value(s): ". join("\n          ", @{$feature->values()})."\n";
        print "Description: ".$feature->description()."\n\n";
    }
}

=head1 FUNCTIONS

=head2 new (constructor)

Create a new instance of this class.

Returns: new instance of this class.

=cut

sub new {
    my ( $pkg, $params ) = @_;
    my $class = ref $pkg || $pkg;
    my $self = bless( {}, $class);

    $self->export_url("http://wiki.openstreetmap.org/index.php?title=Special:Export/%s");
    $self->api_url("http://wiki.openstreetmap.org/api.php");
    $self->mapfeatures_pagename("Map_Features");

    return $self;
}

=head2 download

Downloads Map Features from wiki.openstreetmap.org.

Throws exceptions if something goes wrong.

There is also a debug_download function that runs download then freezes
the content into "mapfeatures.debug" in the current directory, so you
don't have to wait so much for the wiki while developing.

Returns: undef

=cut

sub download {
    my $self = shift;

    # Setup HTTP useragent
    my $ua = LWP::UserAgent->new;
    $ua->agent("Geo_OSM_MapFeatures/$Geo::OSM::MapFeatures::VERSION");

    # Fetch the main map features page
    my $req = HTTP::Request->new(GET => sprintf($self->export_url(), $self->mapfeatures_pagename()));
    warn "Fetching Map_Features\n" if $self->trace();
    my $res = $ua->request($req);
    if(! $res->is_success) {
        throw Geo::OSM::MapFeatures::Error::Network("Couldn't fetch Map_Features: ".$res->status_line);
    }

    # Parse and fetch the templates containing the actual features
    my $xml = XMLin($res->content);

    while( $xml->{page}->{revision}{text}{content} =~ /{{(.*?)}}/gs ){
        my $template = $1;
        next unless $template =~ /(Map_Features:\S+)/;
        my $page = $1;

        my $req = HTTP::Request->new(GET => sprintf("%s?action=expandtemplates&text={{%s}}&format=xml",$self->api_url, $page));
        warn "Fetching $page via MediaWiki api.php\n" if $self->trace();
        my $res = $ua->request($req);
        if(! $res->is_success) {
            throw Geo::OSM::MapFeatures::Error::Network("Couldn't fetch $page (".$req->uri()."): ".$res->status_line);
        }

        push(@{$self->{featuretemplates}}, $page);
        $self->{content}{$page} = XMLin($res->content);
    }
}

# download and cache so we don't have to constantly wait for the wiki to come around to responding
sub debug_download {
    my $self = shift;

    if( -f 'mapfeatures.debug' ){
        my $data = retrieve('mapfeatures.debug') or die;
        $$self{featuretemplates} = $$data{featuretemplates};
        $$self{content} = $$data{content};
    } else {
        $self->download();
        my $data = {featuretemplates => $$self{featuretemplates}, content => $$self{content}};
        store($data, 'mapfeatures.debug') or die;
    }
}

=head2 parse

Parses map features.

=cut

sub parse {
    my $self = shift;

    throw Geo::OSM::MapFeatures::Error("No content, is it downloaded?")
        unless scalar(@{$self->{featuretemplates}}) and $self->{content};

    foreach my $featuretemplate ( @{$self->{featuretemplates}} ){
        my($table) = $self->{content}{$featuretemplate}{expandtemplates} =~ /\{\|(.*)\|\}/s
            or throw Geo::OSM::MapFeatures::Error::Parse("Could not extract table on $featuretemplate");

        my @rows = split(/^\|-/m, $table);
        foreach my $row (@rows){
            my @columns = split("\n", $row);

            # Ignore lines not starting with "| "
            @columns = grep {/^\| /} @columns;

            # Skip lines that doesn't have 6 columns
            next unless $#columns == 5;

            my $key = $columns[0];
            my $value = $columns[1];
            my $elementtypes = $columns[2];
            my $description = $columns[3];

            $key = $self->_clean_wikitable_cell($key);
            $value = $self->_clean_wikitable_cell($value);
            $description = $self->_clean_wikitable_cell($description);

            # Get all types for the feature
            my @elementtypes;
            push(@elementtypes, 'node') if $elementtypes =~ /node/;
            push(@elementtypes, 'way') if $elementtypes =~ /way/;
            push(@elementtypes, 'area') if $elementtypes =~ /area/;

            my $feature = new Geo::OSM::MapFeatures::Feature($key, $value, \@elementtypes, $description);
            push(@{$self->{features}{$featuretemplate}}, $feature);
        }
    }
}

sub _clean_wikitable_cell {
    my $self = shift;
    my $content = shift;

    $content =~ s/^\| //;

    # Strip leading and trailing whitespace
    $content =~ s/^\s*//;
    $content =~ s/\s*$//;

    # Clean away wikilinks
    $content =~ s/\[\[(?:.*?\|\s*)?(.*?)\]\]/$1/g;

    # Clean away hyperlinks
    $content =~ s/\[\S+:\/\/(?:[^\]]+\s+([^\]]+)|[^\]]+\/([^\]]+)(?:\.\w{2,5})?\s*)\]/$1 ? $1 : $2/ge;

    # Clean away other markup
    $content =~ s/<[^>]+>//g;

    return $content;
}

=head2 categories

Returns a list of feature categories.

=cut

sub categories {
    my $self = shift;
    return keys( %{ $self->{features} } );
}

=head2 features

Returns a list of features.

If given an argument it as taken as a category, and only features
in that category will be returned.

=cut

sub features {
    my $self = shift;
    my $category = shift;

    if( defined($category) ){
        return @{ $self->{features}{$category} };
    } else {
        my @result = ();
        foreach my $category ( $self->categories() ){
            push(@result, $self->features($category));
        }
        return @result;
    }
}

=head1 Exception classes

=head2 Geo::OSM::MapFeatures::Error

Base exception class for errors thrown by this module

=cut

package Geo::OSM::MapFeatures::Error;
use base qw(Error);

sub new {
    my $self  = shift;
    my $text  = "" . shift;
    my $params = shift;

    local $Error::Depth = $Error::Depth + 1;

    $self->SUPER::new(-text => $text, %$params);
}

sub stringify {
    my $self = shift;
    my $text = $self->SUPER::stringify;
    $text .= sprintf(" at %s line %d.\n", $self->file, $self->line)
        unless($text =~ /\n$/s);
    $text;
}

=head2 Geo::OSM::MapFeatures::Error::Network

Network error

=cut

package Geo::OSM::MapFeatures::Error::Network;
use base qw(Geo::OSM::MapFeatures::Error);

=head2 Geo::OSM::MapFeatures::Error::Parse

Go find out who broke map feature this time...

=cut

package Geo::OSM::MapFeatures::Error::Parse;
use base qw(Geo::OSM::MapFeatures::Error);

=head1 AUTHOR

Knut Arne Bjørndal, C<< <bob at cakebox.net> >>

=head1 BUGS

Please report any bugs or feature requests to C<bug-geo-osm-mapfeatures at rt.cpan.org>, or through
the web interface at L<http://rt.cpan.org/NoAuth/ReportBug.html?Queue=Geo-OSM-MapFeatures>.  I will be notified, and then you'll
automatically be notified of progress on your bug as I make changes.




=head1 SUPPORT

You can find documentation for this module with the perldoc command.

    perldoc Geo::OSM::MapFeatures


You can also look for information at:

=over 4

=item * RT: CPAN's request tracker

L<http://rt.cpan.org/NoAuth/Bugs.html?Dist=Geo-OSM-MapFeatures>

=item * AnnoCPAN: Annotated CPAN documentation

L<http://annocpan.org/dist/Geo-OSM-MapFeatures>

=item * CPAN Ratings

L<http://cpanratings.perl.org/d/Geo-OSM-MapFeatures>

=item * Search CPAN

L<http://search.cpan.org/dist/Geo-OSM-MapFeatures>

=back


=head1 ACKNOWLEDGEMENTS


=head1 COPYRIGHT & LICENSE

Copyright 2008 Knut Arne Bjørndal, all rights reserved.

This program is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.


=cut

1; # End of Geo::OSM::MapFeatures
