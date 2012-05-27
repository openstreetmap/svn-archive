#!/usr/bin/perl -w
# -CDSL would be better than explicit encoding settings

use strict;
use utf8;

my ($user, $pwd);

# Three ways to handle login data:
  # Enter data directly in these two lines (Be careful witn svn checkin later!)
  # create a file "launchpad.pl_credits" containing the two lines with proper values
  # leave credits empty and enter them on runtime
$user = '';
$pwd = '';

# list of supported languages
my %lang = map {$_ => 1} (
"bg", "cs", "da", "de", "el", "en_AU", "en_GB",
"es", "et", "eu", "fi", "fr", "gl", "hu", "id",
"it", "ja", "nb", "nl", "pl", "pt", "pt_BR", "ru", "sk",
"sv", "tr", "uk", "zh_CN", "zh_TW"
);

my $revision = '$Revision$';
$revision =~ s/^.*?(\d+).*$/$1/;
my $agent = "JOSM_Launchpad/1.$revision";

my $count = 0;#11;
my $cleanall = 0;#1;
my $upload = 0;#1;

if($#ARGV != 0)
{
    warn "No argument given (try Launchpad download URL, \"bzr\", \"bzronly\", \"upload\" or \"download\").";
    system "ant";
    makeupload();
}
elsif($ARGV[0] eq "bzr" || $ARGV[0] eq "bzronly")
{
    mkdir "build";
    die "Could not change into new data dir." if !chdir "build";
    system "bzr export -v josm_trans lp:~openstreetmap/josm/josm_trans";
    chdir "..";
    copypo("build/josm_trans/josm");
    system "rm -rv build/josm_trans";
    if($ARGV[0] ne "bzronly")
    {
      system "ant";
      makeupload();
    }
}
elsif($ARGV[0] eq "upload")
{
    potupload();
}
elsif($ARGV[0] eq "download")
{
    podownload();
}
elsif($ARGV[0] eq "stats")
{
    getstats();
}
else
{
    mkdir "build";
    mkdir "build/josm_trans";
    die "Could not change into new data dir." if !chdir "build/josm_trans";
    system "wget $ARGV[0]";
    system "tar -xf laun*";
    chdir "../..";
    copypo("build/josm_trans");
    system "rm -rv build/josm_trans";
    system "ant";
    makeupload();
}

sub makeupload
{
    if($upload)
    {
        my $outdate = `date -u +"%Y-%m-%dT%H_%M_%S"`;
        chomp $outdate;
        mkdir "build/josm";
        system "cp po/*.po po/josm.pot build/josm";
        chdir "build";
        if(!$count)
        {
          system "tar -cjf ../launchpad_upload_josm_$outdate.tar.bz2 josm";
        }
        else
        {
          my @files = sort glob("josm/*.po");
          my $num = 1;
          while($#files >= 0)
          {
            my @f = splice(@files, 0, $count);
            system "tar -cjf ../launchpad_upload_josm_${outdate}_$num.tar.bz2 josm/josm.pot ".join(" ",@f);
            ++$num;
          }
        }
        system "rm -rv josm";
        chdir "..";
    }
}

sub copypo
{
    my ($path) = @_;
    foreach my $name (split("\n", `find $path -name "*.po"`))
    {
        $name =~ /([a-zA-Z_]+)\.po/;
        if($lang{$1})
        {
            system "cp -v $name po/$1.po";
        }
        elsif($cleanall)
        {
            local $/; undef $/;
            open FILE,"<",$name or die;
            my $x = <FILE>;
            close FILE;
            $x =~ s/\n\n.*$/\n/s;
            open FILE,">","po/$1.po" or die;
            print FILE $x;
            close FILE;
        }
    }
}

sub dologin
{
    require WWW::Mechanize;

    my $mech = WWW::Mechanize->new("agent" => $agent);

      #$mech->add_handler("request_send" => sub {
      #  my($request, $ua, $h) = @_;
      #  printf "FORM: %s\n", $request->content();
      #  return undef;
      #});
    $mech->get("https://translations.launchpad.net/josm/trunk/+login");
      #print $mech->status() ." - ". $mech->uri()."\n"; 
    $mech->submit_form(form_number => 1);
    getcredits();
      #print $mech->status() ." - ". $mech->uri()."\n"; 
    $mech->submit_form(with_fields => {"email" => $user, "password" => $pwd});
      #$mech->dump_headers();
      #print $mech->status() ." - ". $mech->uri()."\n"; 
      #print $mech->content();
    my $form = $mech->form_name("decideform");
    die "Could not login.\n" if !$form;
    my %par = ("yes" => "");  # We need to add "yes" or it does not work
    foreach my $p ($form->param)
    {
        $par{$p} = $form->value($p);
    }
    $mech->post($form->action, \%par);
      #$mech->dump_headers(); 
      #print $mech->content();
      #print $mech->status() ." - ". $mech->uri()."\n"; 
      #$mech->dump_forms();
    return $mech;
}

sub potupload
{
    my $mech = dologin();
    $mech->get("https://translations.launchpad.net/josm/trunk/+translations-upload");
    chdir("po");
    $mech->submit_form(with_fields => {"file" => "josm.pot"});
    chdir("..");
}

sub podownload
{
    my $mech = dologin();
    $mech->get("https://translations.launchpad.net/josm/trunk/+export");
    $mech->submit_form(with_fields => {"format" => "PO"});
    if(!($mech->content() =~ /receive an email shortly/))
    {
      warn "Error requesting file\n";
    }
}

sub getcredits
{
    if(!$user || !$pwd)
    {
        require Term::ReadKey;
        local undef $/;
        if(open FILE, "launchpad.pl_credits")
        {
            eval <FILE>;
            close FILE;
        }

        if(!$user)
        {
            Term::ReadKey::ReadMode(4); # Turn off controls keys
            printf("Enter username: ");
            for(;;)
            {
                my $c = getc();
                print $c;
                last if $c eq "\n";
                $user .= $c;
            }
            Term::ReadKey::ReadMode(0);
        }

        if(!$pwd)
        {
            Term::ReadKey::ReadMode(4); # Turn off controls keys
            printf("Enter password: ");
            for(;;)
            {
                my $c = getc();
                last if $c eq "\n";
                print "*";
                $pwd .= $c;
            }
            print "\n";
            Term::ReadKey::ReadMode(0);
        }
    }
}

sub doget
{
  my ($mech, $page, $arg) = @_;
  for(my $i = 1; $i <= 5; $i++)
  {
    $mech->timeout(30);
    eval
    {
      $mech->get($page);
    };
    my $code = $mech->status();
    print "Try $i: ($code) $@" if $@;
    return $mech if !$@;
    sleep(30+5*$i);
    last if $arg && $arg eq "no503" and $code == 503;
    $mech = WWW::Mechanize->new("agent" => $agent);
  }
  return $mech;
}

sub getstats
{
  my %results;
  require WWW::Mechanize;
  require Data::Dumper;
  require URI::Escape;
  my $mech = WWW::Mechanize->new("agent" => $agent);

  if(open DFILE,"<:utf8","launchpadtrans.data")
  {
    local $/;
    $/ = undef;
    my $val = <DFILE>;
    eval $val;
    close DFILE;
  }

  binmode STDOUT, ":utf8";

  open FILE,">:utf8","launchpadtrans.txt" or die "Could not open output file.";

  for my $lang (sort keys %lang)
  {
    doget($mech, "https://translations.launchpad.net/josm/trunk/+pots/josm/$lang/");
    sleep(1);
    my $cont = $mech->content();
    while($cont =~ /<a href="https?:\/\/launchpad.net\/~(.*?)" class="sprite person(-inactive)?">(.*?)<\/a>/g)
    {
      my ($code, $inactive, $name) = ($1, $2, $3);
      if(exists($results{$code}{$lang}))
      {
        printf "%-5s - %-30s - Found - %s\n", $lang,$code,$name;
        next;
      }
      my $urlcode = URI::Escape::uri_escape($code);
      $mech = doget($mech, "https://translations.launchpad.net/josm/trunk/+pots/josm/$lang/+filter?person=$urlcode", "no503");
      sleep(1);
      my $cont = $mech->content() || "";
      my ($count) = $cont =~ /of[\r\n\t ]+?(\d+)[\r\n\t ]+?result/;
      if($count && $mech->status == 200)
      {
        $results{$code}{NAME} = $name;
        $results{$code}{$lang} = $count;
        $results{$code}{TOTAL} += $count;
        if(open DFILE,">:utf8","launchpadtrans.data")
        {
          print DFILE Data::Dumper->Dump([\%results],['*results']);
          close DFILE;
        }
        printf "%-5s - %-30s - %5d - %s\n", $lang,$code,$count,$name;
        
      }
      else
      {
        printf "%-5s - %-30s - Skip  - %s\n", $lang,$code,$name;
      }
    }
  }
  for my $code (sort {$results{$b}{TOTAL} <=> $results{$a}{TOTAL}} keys %results)
  {
    print FILE "$results{$code}{NAME}:$results{$code}{TOTAL}";
    printf "%5d - %-50s",$results{$code}{TOTAL}, $results{$code}{NAME};
    for my $lang (sort keys %{$results{$code}})
    {
      next if $lang eq "NAME" or $lang eq "TOTAL";
      print FILE ";$lang=$results{$code}{$lang}";
      printf " - %-5s=%5d",$lang, $results{$code}{$lang};
    }
    print FILE "\n";
    print "\n";
  }
}
