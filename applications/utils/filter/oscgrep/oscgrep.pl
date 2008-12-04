#!/usr/bin/perl

# simple script that helps you find certain objects in .osc files.
#
# called like this:
#
# perl oscgrep.pl -a action -t type regex file file...
#
# where
#   "action" is either create, modify, or delete;
#   "type" is either way, node, or relation;
#   "regex" is any regex
#   "file" is an .osc file
#
# lists all elements from the .osc file that match the given action, type, and
# regex (regex is applied against full XML content of the object). If -a, -t or
# regex are omitted, dumps all actions/all types/all content; if file is omitted,
# reads from stdin.
#
# Written by Frederik Ramm <frederik@remote.org>, public domain.

use Getopt::Std;
my $options = {};
getopts("a:t:", $options);
my $grep = shift(@ARGV);

while(<>)
{
    if (/<(modify|create|delete)/)
    {
        $action = $1;
    }
    elsif (/<(node|way|relation).*?(\/)?>/)
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
    return if (defined($options->{"a"}) && $options->{"a"} ne $action);
    return if (defined($options->{"t"}) && $options->{"t"} ne $type);
    return if (defined($grep) && $content !~ /$grep/);
    print "<$action>\n$content</$action>\n";
}
