#!/usr/bin/perl

	use LWP::UserAgent;
	use CGI;
	
	$query=new CGI;
	$success=0;
	if ($query->param('tweet') and
		$query->param('lat') and
		$query->param('long') and
		$query->param('twitter_id') and
		$query->param('twitter_pwd')) {
		$browser = LWP::UserAgent->new;
		$browser->credentials('twitter.com:80', 'Twitter API', $query->param('twitter_id'), $query->param('twitter_pwd'));
		$response=$browser->get("http://twitter.com/account/verify_credentials.json");	# $response->code should be 200
		if ($response->code==200) {
			$response=$browser->post('http://twitter.com/statuses/update.json', 
				X-Twitter-Client => 'Potlatch',
				Content => { status => $query->param('tweet'),
							 lat    => $query->param('lat'),
							 long   => $query->param('long') }
			);
			if ($response->code==200) { $success=1; }
		}
	}
	print <<EOF;
Content-Type: application/x-www-form-urlencoded

&success=$success&
EOF

