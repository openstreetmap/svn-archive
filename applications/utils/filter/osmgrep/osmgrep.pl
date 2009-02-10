#!/usr/bin/perl

# simple script that helps you find certain objects in .osm files.
#
# called like this:
#
# perl oscgrep.pl -t type regex file file...
#
# where
#   "type" is either way, node, or relation;
#   "regex" is any regex
#   "file" is an .osc file
#
# lists all elements from the .osm file that match the given type, and
# regex (regex is applied against full XML content of the object). If -t or
# regex are omitted, dumps all types/all content; if file is omitted,
# reads from stdin.
#
# Written by Frederik Ramm <frederik@remote.org>, public domain.

use Getopt::Std;
my $options = {};
getopts("a:t:", $options);
my $grep = shift(@ARGV);

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
    return if (defined($grep) && $content !~ /$grep/s);
    print "$content\n";
}
