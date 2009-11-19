#!/usr/bin/perl
use strict;
use Net::Twitter;
use CGI;

my $query = CGI->new;
my $success = 0;
my $errcode = 0;
my $params = 0;

if ($query->param('tweet') and
    $query->param('lat') and
    $query->param('long') and
    $query->param('twitter_id') and
    $query->param('twitter_pwd') and
    $query->param('clientver')) {
    $params = 1;
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
    } else {
        $success = 1;
    }
}
	print <<EOF;
Content-Type: application/x-www-form-urlencoded

&success=$success&errcode=$errcode&params=$params&
EOF

