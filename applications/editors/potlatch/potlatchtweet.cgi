#!/usr/bin/perl
use strict;
use Net::Twitter;
use CGI;
use URI;

my $query = CGI->new;
my $res = URI->new;

my $success = 0;
my $errcode = 0;
my $errmsg = '';
my $errerr = '';
my $submitted = 0;

if ($query->param('tweet') and
    $query->param('lat') and
    $query->param('long') and
    $query->param('twitter_id') and
    $query->param('twitter_pwd') and
    $query->param('clientver')) {
    $submitted = 1;

    my $app = 'Potlatch';
    my $ver = $query->param('clientver');
    my $ua = "$app/$ver";

    my $twat = Net::Twitter->new(
        traits => ['API::REST'],
        username => $query->param('twitter_id'),
        password => $query->param('twitter_pwd'),
        useragent  => $ua,
        clientname => $app,
        clientver  => $ver,
        clienturl  => 'http://openstreetap.org/edit',

        # identi.ca or twitter?
        ($query->param('identica')
         ? (identica => 1)
         : ()),

        # identica takes this as-is, twitter says "from web" because
        # it isn't registering source paramaters anymore, we'd have to
        # use OAuth to play nice with it.
        source => $app,
    );

    eval {
        $twat->update({
            status => $query->param('tweet'),
            lat => $query->param('lat'),
            long => $query->param('long')
        });
    };
    if (my $err = $@) {
        $errcode = $err->code;
        $errmsg  = $err->message;
        $errerr  = $err->error;
    } else {
        $success = 1;
    }
}

$res->query_form(
    success => $success,
    errcode => $errcode,
    errmsg  => $errmsg,
    errerr  => $errerr,
    submit  => $submitted,
);

my $q = $res->as_string;
$q =~ s/^\?//;

	print <<EOF;
Content-Type: application/x-www-form-urlencoded

&$q&
EOF

