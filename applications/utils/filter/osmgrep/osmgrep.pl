#!/usr/bin/perl

# simple script that helps you find certain objects in .osm files.
#
# called like this:
#
# perl oscgrep.pl [-i] [-l] [-t type] [-r regex] file file...
#
# where
#   "type" is either way, node, or relation;
#   "regex" is any regex
#   "file" is an .osm file
#
# lists all elements from the .osm file that match the given type and
# regex (regex is applied against full XML content of the object). If -t or
# -r are omitted, dumps all types/all content; if file is omitted,
# reads from stdin.
#
# -i causes the regex to be evaluated case-insensitively.
# -l causes the output to have one line per OSM element only.
#
# Written by Frederik Ramm <frederik@remote.org>, public domain.

use Getopt::Std;
my $options = {};
getopts("ilr:t:", $options);
my $grep = $options->{r};

while(<>)
{
    if (/<(node|way|relation).*?(\/)?>/)
    {
        if ($2 eq "/")
        {
            out($1, $_);
        }
        else
        {
            $seek = $1;
            $buffer = $_;
        }
    }
    elsif (/<\/$seek>/)
    {
        out($seek, $buffer . $_);
    }
    else
    {
        $buffer .= $_;
    }
}

sub out
{
    my ($type, $content) = @_;
    return if (defined($options->{"t"}) && $options->{"t"} ne $type);
    if (defined($grep))
    {
        if ($options->{i})
        {
            return if (defined($grep) && $content !~ /$grep/si);
        }
        else
        {
            return if (defined($grep) && $content !~ /$grep/s);
        }
    }
    $content =~ tr/\n/ / if ($options->{l});
    print "$content\n";
}
