#!/usr/bin/perl

# Delete.pm
# ---------
#
# Deletes an object.
#
# Part of the "osmtools" suite of programs
# Originally written by Frederik Ramm <frederik@remote.org>; public domain

package Delete;

use strict;
use warnings;

use OsmApi;

our $globalListOfDeletedStuff = {};

# deletes one object
#
# fails if the object is not deleted
#
# parameters: 
#   $what: 'node', 'way', or 'relation'
#   $id: object id
#   $changeset: id of changeset to use for delete operation
# return:
#   success=1 failure=undef

sub delete
{
    my ($what, $id, $changeset) = @_;
    my $recurse = 0;

    my $xml = determine_delete_action($what, $id, $changeset, $recurse, 0);
    return undef unless defined ($xml);

    my $resp = OsmApi::post("changeset/$changeset/upload", "<osmChange version='0.6'>\n<delete>\n$xml</delete></osmChange>");
    if (!$resp->is_success)
    {
        print STDERR "$what $id cannot be deleted: ".$resp->status_line."\n";
        return undef;
    }
    return 1;
}

# the delete workhorse; finds out which XML to upload to the API to
# delete an object.
#
# Parameters:
# see sub delete.
#
# Returns:
# undef on error, else the new XML to send to the API.
# The XML has to 
# be wrapped in <osm>...</osm> or inside a <modify>...</modify>
# in a changeset upload.

sub determine_delete_action
{
    my ($what, $id, $changeset, $recursive, $indent) = @_;

    my $copy=0;
    my $out = "";
    my $members = [];
    my $version;
    my $user;

    my $resp = OsmApi::get("$what/$id");
    if (!$resp->is_success)
    {
        print STDERR " "x$indent;
        print STDERR "$what $id cannot be retrieved: ".$resp->status_line."\n";
        return undef;
    }

    foreach (split(/\n/, $resp->content()))
    { 
        if (/<$what/) 
        { 
            /\sid="([^"]+)"/ or die; 
            die unless $id eq $1; 
            /\sversion="([^"]+)"/ or die; 
            $version = $1;
            /user="([^"]+)/;
            $user=$1;
            $copy = 1;
            $out = $_;
	    $out =~ s/">/"\/>/g;
            $members = [];
        } 
        elsif ($copy) 
        { 
            $copy=0 if (/<\/$what/);
            if (/<nd ref=.(\d+)/)
            {
                push(@$members, { type => "node", id => $1 });
            }
            elsif (/<member.*type=.(way|node|relation).*id=.(\d+)/)
            {
                push(@$members, { type => $1, id => $2 });
            }
        } 
    }; 

    print STDERR " "x$indent;
    print STDERR "$what $id last modified by $user (version $version) - deleting\n",
    $out =~ s/changeset="\d+"/changeset="$changeset"/;
        if ($recursive && scalar(@$members))
        {
            print STDERR " "x$indent;
            print STDERR "recursively deleting members of $what $id\n";
            foreach (@$members)
            {
                if (!defined($globalListOfDeletedStuff->{$_->{type}.$_->{id}}))
                {
                    my $ua = determine_delete_action($_->{type}, $_->{id}, $changeset, 1, $indent + 2);
                    $out = $ua . $out if defined($ua);
                    $globalListOfDeletedStuff->{$_->{type}.$_->{id}} = 1;
                }
            }
        }
        return $out;
}

1;
