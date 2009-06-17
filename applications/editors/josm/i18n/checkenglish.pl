#! /usr/bin/perl -w

# Show diffs in en_GB.
# Find errors in original texts which have silently been fixed by native
# english speakers instead of informing the authors.

open FILE,"<","po/en_GB.po" or die;
my $last = "";
my $type = "";
my $msgid;
my $msgid_pl;
my $msgstr;
my $msgstr_pl;
my $fuzzy;
while(<FILE>)
{
  chomp;
  if($_ =~ /^#/ || !$_)
  {
    check();
    $fuzzy = ($_ =~ /fuzzy/);
    next;
  }
  if($_ =~ /^"(.*)"$/) {$last .= $1;}
  elsif($_ =~ /^(msg.+) "(.*)"$/)
  {
    check();
    $last=$2;$type =$1;
  }
  else
  {
    die "Strange line";
  }
}

sub fixstr($)
{
  my $msgid = shift;
  $msgid =~ s/(colo)(r)/$1u$2/ig;
  $msgid =~ s/(gr)a(y)/$1e$2/ig;
  $msgid =~ s/([^\w]+cent|met)(e)(r)/$1$3$2/ig;
  $msgid =~ s/^(cent|met)(e)(r)/$1$3$2/ig;
  $msgid =~ s/(minimi|maximi|vectori|anonymi|Orthogonali|synchroni|Initiali|customi)z/$1s/ig;
  $msgid =~ s/(licen)s/$1c/ig;
  $msgid =~ s/dialog/dialogue/ig;
  $msgid =~ s/(spel)led/$1t/ig;
  $msgid =~ s/_/ /ig;
  return $msgid;
}

sub check
{
    if($type eq "msgid") {$msgid = $last;$msgid_pl="";}
    elsif($type eq "msgid_plural") {$msgid_pl = $last;}
    elsif($type eq "msgstr[0]") {$msgstr = $last;}
    else
    {
      if($type eq "msgstr") {$msgstr = $last;}
      elsif($type eq "msgstr[1]") {$msgstr_pl = $last;}
      if((!$fuzzy) && $msgstr && $msgid)
      {
        $msgid = fixstr($msgid) if($msgstr ne $msgid);
        if($msgstr ne $msgid) { print "  $msgid\n=>$msgstr\n"; }
        if($msgid_pl && $msgstr_pl ne $msgid_pl) { print "  $msgid_pl\np>$msgstr_pl\n"; }
      }
      $msgid = "";
      $msgstr = "";
      $msgid_pl = "";
      $msgstr_pl = "";
      $type = "";
    }
}