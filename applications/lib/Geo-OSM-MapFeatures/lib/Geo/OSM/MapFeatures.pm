package Geo::OSM::MapFeatures;

use warnings;
use strict;

use utf8;

use Data::Dumper;
use Error;
use HTML::TableExtract qw(tree);
use LWP::UserAgent;
use URI::Escape qw(uri_escape);
use Storable;
use XML::Simple;

use base qw(Class::Accessor);
__PACKAGE__->mk_accessors(qw(api_url mapfeatures_pagename trace));

use Geo::OSM::MapFeatures::Feature;

=head1 NAME

Geo::OSM::MapFeatures - Parses and represents OpenStreetMap Map Features

=head1 VERSION

Version 0.10

=cut

our $VERSION = '0.10';


=head1 SYNOPSIS

 use Geo::OSM::MapFeatures;
 
 my $mf = new Geo::OSM::MapFeatures;
 $mf->download();
 $mf->parse();
 
 # To print a simple ascii representation:
 foreach my $category ( sort( $mf->categories() ) ){
     print "\n\n===== $category =====\n";
     foreach my $feature ( $mf->features($category) ){
         print "$feature\n";
     }
 }
 
 # Or you can choose not to use the string overloading and get the
 # individual elements yourself:
 foreach my $category ( sort( $mf->categories() ) ){
     print "\n\n===== $category =====\n";
     foreach my $feature ( $mf->features($category) ){
         print "Key: ".$feature->key()."\n";
         print "Value(s): ". join("\n          ", @{$feature->values()})."\n";
         print "Description: ".$feature->description()."\n\n";
     }
 }

=head1 FUNCTIONS

=head2 new (constructor)

Create a new instance of this class. Pass parameters as a hashref.

Parameters:

=over 8

=item page

What page to fetch. Defaults to "Map_Features".

Translated pages work if the table header names are recognized, the module
contains a mapping table with translated names in over a dozen languages.

=back

Returns: new instance of this class.

=cut

sub new {
    my ( $pkg, $params ) = @_;
    my $class = ref $pkg || $pkg;
    my $self = bless( {}, $class);

    if( $$params{page} ){
	    $self->mapfeatures_pagename($$params{page});
    } else {
	    $self->mapfeatures_pagename("Map_Features");
    }

    $self->api_url("http://wiki.openstreetmap.org/api.php");

    my %tableheader_translations = (
	    'En' => {
		    key => 'key',
		    value => 'value',
		    element => 'element',
		    comment => 'comment',
	    },
	    De => {
		    key => 'Schlüssel',
		    value => 'Wert',
		    element => 'Element',
		    comment => 'Kommentar',
	    },
	    ES => {
		    key => 'Clave',
		    value => 'Valor',
		    element => 'Elemento',
		    comment => 'Comentario',
	    },
	    FR => {
		    key => 'Clé',
		    value => 'Valeur',
		    element => 'Élément',
		    comment => 'Commentaire',
	    },
	    IT => {
		    key => 'Chiave',
		    value => 'Valore',
		    element => 'Elemento',
		    comment => 'Spiegazione',
	    },
	    Ja => {
		    key => 'キー',
		    value => '値',
		    element => '要素',
		    comment => '説明',
	    },
	    Hu => {
		    key => 'Kulcs',
		    value => 'Érték',
		    element => 'Alapelem',
		    comment => 'Magyarázat',
	    },
	    Pt => {
		    key => 'Chave',
		    value => 'Valor',
		    element => 'Element',
		    comment => 'Comentários',
	    },
	    Ro => {
		    key => 'Cheie',
		    value => 'Valoare',
		    element => 'Element',
		    comment => 'Descriere',
	    },
	    RU => {
		    key => 'Ключ',
		    value => 'Значение',
		    element => 'Элементы',
		    comment => 'Описание',
	    },
	    SK => {
		    key => 'Klúč',
		    value => 'Hodnota',
		    element => 'Element',
		    comment => 'Komentár',
	    },
	    Sv => {
		    key => 'Nyckelord',
		    value => 'Värde',
		    element => 'Element',
		    comment => 'Kommentar',
	    },
	    Tr => {
		    key => 'Anahtar',
		    value => 'Değer',
		    element => 'Öğe',
		    comment => 'Açıklama',
	    },
	    Lt => {
		    key => 'Kategorija',
		    value => 'Kodas',
		    element => 'Įvedimo būdai',
		    comment => 'Aprašymas',
	    },
	    Uk => {
		    key => 'Ключ',
		    value => 'Значення',
		    element => 'Елемент',
		    comment => 'Пояснення',
	    },
	    Traditional_Chinese => {
		    key => '類別',
		    value => '值',
		    element => '元素',
		    comment => '說明',
	    },
    );

    # Build and compile regexes with all translations
    foreach my $string ( qw(key value element comment) ){
	    my @translations = ();
	    foreach my $language ( values(%tableheader_translations) ){
		    push(@translations, $$language{$string});
	    }

	    my $regex_string = join('|', @translations);
	    $self->{tableheader_translation_regexes}{$string} = qr/$regex_string/i;
    }

    return $self;
}

=head2 download

Downloads Map Features from wiki.openstreetmap.org.

Throws exceptions if something goes wrong.

Returns: undef

=cut

sub download {
    my $self = shift;

    # Setup HTTP useragent
    my $ua = LWP::UserAgent->new;
    $ua->agent("Geo_OSM_MapFeatures/$Geo::OSM::MapFeatures::VERSION");

    # Fetch MW parser output of page
    my $req = HTTP::Request->new(GET => sprintf("%s?action=parse&prop=text&format=xml&page=%s", $self->api_url, $self->mapfeatures_pagename));

    warn "Fetching ".$req->uri."\n" if $self->trace();
    my $res = $ua->request($req);

    if( ! $res->is_success ){
        throw Geo::OSM::MapFeatures::Error::Network(sprintf("Couldn't fetch %s: %s", $req->uri, $res->status_line));
    }

    $self->{content} = XMLin($res->content);
}

=pod

=head2 debug_download

Download and cache in "mapfeatures.debug" in the current directory, to avoid
downloading the page again and again when developing.

For example do something like the following:

 unless( $ENV{MAPFEATURESDEBUG} ){
 	$mf->download();
 } else {
 	$mf->debug_download();
 }

=cut

sub debug_download {
    my $self = shift;

    if( -f 'mapfeatures.debug' ){
        my $data = retrieve('mapfeatures.debug') or die;
        $$self{content} = $$data{content};
    } else {
        $self->download();
        my $data = {content => $$self{content}};
        store($data, 'mapfeatures.debug') or die;
    }
}

=head2 parse

Parses map features.

=cut

sub parse {
    my $self = shift;

    throw Geo::OSM::MapFeatures::Error("No content, is it downloaded?")
        unless $self->{content};

    throw Geo::OSM::MapFeatures::Error("Couldn't find <parse><text> element, something wrong with api.php?")
    	unless $self->{content}{parse}{text};

    my %data;

    my $tableextractor = HTML::TableExtract->new(
	    # Get header translation regexes with a hash slice
	    headers => [ @{$self->{tableheader_translation_regexes}}{qw(key value element comment)} ],
    );
    $tableextractor->parse($$self{content}{parse}{text});

    if( $tableextractor->tables == 0 ){
	    throw Geo::OSM::MapFeatures::Error::Parse("Did not find any tables");
    }

    #DEBUG: $tableextractor->tree->dump;

    # Examine all matching tables
    foreach my $table ($tableextractor->tables) {

	    # Find headings before the table but at the same level.
	    # Loop through in reverse and find the first of each
	    # heading level upwards
	    my @headings = ();
	    my $lowestheading = 10;
	    foreach my $heading_elem ( reverse( grep { $_->tag() =~ /^h(?:\d)$/ } $table->tree->left ) ){
		    my( $headinglevel ) = $heading_elem->tag() =~ /^h(\d)$/;

		    # Only store the first for a particular level
		    next if defined($headings[$headinglevel]);

		    # Don't store a small heading if we already saw something
		    # larger. For example if we first saw h2 then h3 the h3
		    # belongs to the previous h2, not this one.
		    next if $#headings && $headinglevel > $lowestheading;
		    $lowestheading = $headinglevel;

		    $headings[$headinglevel] = $heading_elem->as_trimmed_text;
	    }
	    @headings = grep { defined } @headings;
	    my $have_added_in_table_heading;

	    foreach my $row ($table->rows) {

		    # If the first column is spanned it's probably a heading
		    # dividing the table in parts.
		    # Make sure to push exactly the last one of these onto
		    # the list of headings
		    if( $$row[0]->attr('colspan') ){
			    pop(@headings) if $have_added_in_table_heading;
			    push(@headings, $$row[0]->as_trimmed_text);
			    $have_added_in_table_heading++;

			    next;
		    }

		    my $key = $$row[0];
		    my $value = $$row[1];
		    my $element = $$row[2];
		    my $description = $$row[3];

		    $key = $key->as_trimmed_text;

		    # Elements are given by images with filenames Mf_(node|way|area).png.
		    # This regex intentionally matches more, to make sure the module can detect that wikifiddlers have "invented" another element type or something
		    my @elementtypes =  map { $_->attr('src') =~ /Mf_(\w+)\./ } $element->find('img');

		    # Find values and split, either by <li> elements or by various text separators
		    my @values;
		    if( $value->find('li') ){
			    @values = map { $_->as_trimmed_text } $value->find('li');
		    } else {
			    # Split on "/" (except for 24/7), "or" and "|"
			    @values = split( m{\s*(?:(?<!24)/(?!7)|\bor\b|\|)\s*}, $value->as_trimmed_text );
		    }

		    $description = $description->as_trimmed_text;

		    #DEBUG: print "Row: k='$key' v='".join("','",@values)."' e='".join("','",@elementtypes)."' c='$description'\n";

		    my $feature = new Geo::OSM::MapFeatures::Feature($key, \@values, \@elementtypes, $description);

		    #FIXME: There should be a real hierarchy, not just a category made by concatenating headings
		    my $headingstring = join(' / ', @headings);
		    push(@{$self->{features}{$headingstring}}, $feature);
	    }
    }
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
our @ISA = qw(Geo::OSM::MapFeatures::Error);

=head2 Geo::OSM::MapFeatures::Error::Parse

Go find out who broke map feature this time...

=cut

package Geo::OSM::MapFeatures::Error::Parse;
our @ISA = qw(Geo::OSM::MapFeatures::Error);

=head1 AUTHOR

Knut Arne Bjørndal, C<< <bob at cakebox.net> >>

=head1 BUGS

Categories are currently made by concatenating headings above a feature. This should probably be a proper hierarchy instead.

The table header translation table should probably be easier to patch from programs calling the module. Or maybe even downloaded from the wiki or something.

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

Copyright 2008-2009 Knut Arne Bjørndal, all rights reserved.

This program is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.


=cut

1; # End of Geo::OSM::MapFeatures
